package org.example.controllers;

import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import org.example.configuration.Configuration;
import org.example.models.Orders;
import org.example.models.SupplierProducts;
import org.example.service.SerializationService;
import org.example.service.XmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileScanner {
    private final SerializationService<Orders> ordersSerializationService;
    private final SerializationService<SupplierProducts> productsSerializationService;
    private final XmlWriter xmlWriter;
    private final Logger LOGGER;
    private final Configuration configuration;

    public FileScanner(SerializationService<Orders> ordersSerializationService, SerializationService<SupplierProducts> productsSerializationService, XmlWriter xmlWriter) {
        this.ordersSerializationService = ordersSerializationService;
        this.productsSerializationService = productsSerializationService;
        this.xmlWriter = xmlWriter;
        this.LOGGER = LoggerFactory.getLogger(FileScanner.class);
        configuration = Configuration.getInstance();
        createInputFolderIfNotExists();
    }

    public void watch() throws IOException, InterruptedException {
        LOGGER.debug("Initializing WatchService");
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            Map<WatchKey, Path> keyMap = new HashMap<>();
            LOGGER.debug("WatchService initialized ");

            Path path = Paths.get(configuration.getInputDirectory());
            keyMap.put(path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE), path);
            WatchKey watchKey;
            LOGGER.info("Start scanning...");
            do {
                watchKey = watcher.take();
                Path eventDir = keyMap.get(watchKey);
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    Path eventPath = (Path) event.context();
                    LOGGER.info(String.format("File %s has been found!", eventPath));

                    if (validateFileNamePattern(String.valueOf(eventPath.getFileName())))
                        handleEvent(String.valueOf(eventPath), String.valueOf(eventDir));
                }
            } while (watchKey.reset());
        } catch (IOException e) {
            throw e;
        } catch (InterruptedException e) {
            LOGGER.info("Scan has been stopped");
            LOGGER.error(String.format("Scan has been interrupted - %s", e.getMessage()));
            throw e;
        }
    }

    private void createInputFolderIfNotExists() {
        Path path = Paths.get(configuration.getInputDirectory());
        LOGGER.debug("Checking for input folder");
        if (Files.notExists(path)) {
            try {
                LOGGER.debug("Creating input folder");
                Files.createDirectories(path);
            } catch (IOException e) {
                LOGGER.error("Input folder could not be created");
                throw new RuntimeException(e);
            }
        }
    }

    public boolean validateFileNamePattern(String fileName) {
        boolean result;
        LOGGER.info("Verifying file name");
        if (!(fileName.startsWith(configuration.getInputFileNamePrefix()) && fileName.endsWith(configuration.getInputFileExtension()))) {
            LOGGER.info("File name does not meet the requirements");
            result = false;
        } else {
            result = getFileNameNumber(fileName) != null;
        }
        return result;
    }

    private Integer getFileNameNumber(String fileName) {
        try {
            return Integer.parseInt(
                    fileName
                            .split(configuration.getInputFileNamePrefix())[1]
                            .split(configuration.getInputFileExtension())[0]
            );
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            LOGGER.warn(String.format("File %s does not contain the number", fileName));
            return null;
        }
    }

    public void handleEvent(String fileName, String eventDir) {
        Integer fileNumber = getFileNameNumber(fileName);
        File inputFile = new File(String.format("%s/%s", eventDir, fileName));

        try {
            Orders orders = ordersSerializationService.deserialize(inputFile);
            List<SupplierProducts> suppliersProducts = getSuppliersProducts(orders);
            createOutputFiles(fileNumber, suppliersProducts);
        } catch (AbstractReflectionConverter.UnknownFieldException | CannotResolveClassException e) {
            LOGGER.error(String.format("Error occurred during deserialization - %s", e.getMessage()));
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                LOGGER.error(String.valueOf(stackTraceElement));
            }
        }
    }

    private void createOutputFiles(Integer fileNumber, List<SupplierProducts> suppliersProducts) {
        LOGGER.info("Start creating suppliers output files");
        suppliersProducts.forEach(supplierProducts -> {
            String serialized = productsSerializationService.serialize(supplierProducts);
            String supplierFileName = String.format("%s%s", supplierProducts.getSupplier(), fileNumber);
            try {
                String xmlFilePath = xmlWriter.createXmlFile(supplierFileName);
                xmlWriter.writeXmlFile(xmlFilePath, serialized);
                xmlWriter.prettifyXmlFile(xmlFilePath);
            } catch (IOException | SAXException | TransformerException e) {
                LOGGER.error(String.format("An error occurred - %s", e.getMessage()));
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    LOGGER.error(String.valueOf(stackTraceElement));
                }
            }
        });
    }

    private List<SupplierProducts> getSuppliersProducts(Orders orders) {
        HashMap<String, SupplierProducts> supplierProductsHashMap = new HashMap<>();
        LOGGER.info("Start products filtering");
        orders.getOrders().forEach(order -> {
            LOGGER.info(String.format("Start order %s", order.getID()));
            order.getProducts().forEach(product -> {
                product.setOrderID(order.getID());
                if (!supplierProductsHashMap.containsKey(product.getSupplier())) {
                    supplierProductsHashMap.put(product.getSupplier(), new SupplierProducts(product.getSupplier()));
                }
                supplierProductsHashMap.get(product.getSupplier()).addProduct(product);
                product.setSupplier(null);
            });
        });
        LOGGER.info("Products are filtered by suppliers");
        return new ArrayList<>(supplierProductsHashMap.values());
    }

}
