package org.personal.controllers;

import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import org.personal.configuration.Configuration;
import org.personal.interfaces.Scanner;
import org.personal.models.Orders;
import org.personal.models.SupplierProducts;
import org.personal.service.SerializationService;
import org.personal.service.XmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileScanner implements Scanner {
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

    @Override
    public void startScan() throws IOException, InterruptedException {
        LOGGER.debug("Initializing WatchService");
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            Map<WatchKey, Path> keyMap = new HashMap<>();
            LOGGER.debug("WatchService initialized ");

            Path path = Paths.get(configuration.getINPUT_DIRECTORY());
            keyMap.put(path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE), path);
            WatchKey watchKey;
            LOGGER.info("Start scanning...");
            do {
                watchKey = watcher.take();
                Path eventDir = keyMap.get(watchKey);
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    Path eventPath = (Path) event.context();
                    LOGGER.info(String.format("File %s has been found!", eventPath));

                    if (verifyFileName(String.valueOf(eventPath.getFileName())))
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
        Path path = Paths.get(configuration.getINPUT_DIRECTORY());
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

    @Override
    public boolean verifyFileName(String fileName) {
        boolean result;
        LOGGER.info("Verifying file name");
        if (!(fileName.startsWith(configuration.getINPUT_FILE_NAME_PREFIX()) && fileName.endsWith(configuration.getINPUT_FILE_EXTENSION()))) {
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
                            .split(configuration.getINPUT_FILE_NAME_PREFIX())[1]
                            .split(configuration.getINPUT_FILE_EXTENSION())[0]
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
        LOGGER.info("Start products sorting");
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
        LOGGER.info("Products are sorted by suppliers");
        return new ArrayList<>(supplierProductsHashMap.values());
    }

}
