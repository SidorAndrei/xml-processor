package org.example;

import org.example.controllers.FileScanner;
import org.example.models.Orders;
import org.example.models.SupplierProducts;
import org.example.service.SerializationService;
import org.example.service.XmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Logger LOGGER = LoggerFactory.getLogger(Main.class);
        LOGGER.debug("Start creating serialization services");
        SerializationService<Orders> ordersSerializationService = new SerializationService<>();
        LOGGER.debug("Orders Serialization Service created");
        SerializationService<SupplierProducts> productsSerializationService = new SerializationService<>();
        LOGGER.debug("Products Serialization Service created");

        LOGGER.debug("Start creating XML Creator");
        XmlWriter xmlWriter = new XmlWriter();
        LOGGER.debug("XML Creator created");

        LOGGER.debug("Start creating File Scanner");
        FileScanner scanner = new FileScanner(ordersSerializationService, productsSerializationService, xmlWriter);
        LOGGER.debug("File Scanner created");

        LOGGER.debug("Start scanning");
        try {
            scanner.watch();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        LOGGER.debug("Application has been stopped");
    }
}
