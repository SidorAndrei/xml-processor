package org.personal.service;

import org.personal.configuration.Configuration;
import org.personal.interfaces.XmlFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class XmlWriter implements XmlFileWriter {
    private final Transformer transformer;
    private final DocumentBuilder documentBuilder;
    private final Logger LOGGER;
    private final Configuration configuration;


    public XmlWriter() {
        LOGGER = LoggerFactory.getLogger(XmlWriter.class);
        try{

            LOGGER.debug("Trying to create Transformer using TransformerFactory");
            TransformerFactory transformerFactory = TransformerFactory.newDefaultInstance();
            transformer = transformerFactory.newTransformer();
            LOGGER.debug("Transformer has been created");

            LOGGER.debug("Transformer configuration started");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            LOGGER.debug("Transformer has been configured!");

            LOGGER.debug("DocumentBuilderFactory creating started");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            LOGGER.debug("DocumentBuilderFactory created");

            LOGGER.debug("DocumentBuilder creating started");
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            LOGGER.debug("DocumentBuilder created");

        } catch (TransformerConfigurationException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        configuration = Configuration.getInstance();

        createOutputFolderIfNotExists();
    }

    private void createOutputFolderIfNotExists() {
        Path outputDir = Path.of(configuration.getOUTPUT_DIRECTORY());
        if(Files.notExists(outputDir)) {
            try {
                LOGGER.debug("Creating output folder");
                Files.createDirectories(outputDir);
            } catch (IOException e) {
                LOGGER.error(String.format("Error while creating output folder - %s",e.getMessage()));
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String createXmlFile(String name) throws IOException {
        LOGGER.debug("Creating a file");
        File xml = new File(String.format("%s/%s.xml",configuration.getOUTPUT_DIRECTORY(),name));
        try {

            if (xml.createNewFile()) {
                LOGGER.debug(String.format("File %s has been created", xml.getName()));
            }
            else {
                LOGGER.debug(String.format("File %s already exists", xml.getName()));
            }
        } catch (IOException e){
            LOGGER.error(String.format("Error when creating the file %s - %s", xml.getName(), e.getMessage()));
            throw e;
        }
        LOGGER.info(String.format("File %s has been created", xml.getAbsolutePath()));
        return xml.getAbsolutePath();
    }

    @Override
    public void writeXmlFile(String path, String xmlString) throws IOException {
        LOGGER.debug("Creating FileWriter");
        try(FileWriter xmlWriter = new FileWriter(path)){
            LOGGER.debug("Write in file");
            xmlWriter.write(xmlString);
            LOGGER.debug("File written with success");
        } catch (IOException e){
            LOGGER.error(String.format("file could not be written! - %s",e.getMessage()));
            throw e;
        }
    }

    @Override
    public void prettifyXmlFile(String path) throws SAXException, IOException, TransformerException {
        try {
            LOGGER.debug("Creating document");
            Document document = documentBuilder.parse(new File(path));
            document.setXmlStandalone(true);

            LOGGER.debug("Creating source");
            Source source = new DOMSource(document);
            LOGGER.debug("Creating File writer");
            FileWriter fileWriter = new FileWriter(path);
            LOGGER.debug("Transforming");
            transformer.transform(source, new StreamResult(fileWriter));
            LOGGER.info(String.format("File %s has been transformed!", path));
            fileWriter.close();
        } catch (SAXException e) {
            LOGGER.error(String.format("Error occurred while parsing the file - %s",e.getMessage()));
            throw e;
        } catch (IOException e) {
            LOGGER.error(String.format("File error occurred - %s", e.getMessage()));
            throw e;
        } catch (TransformerException e) {
            LOGGER.error(String.format("Error occurred while transforming the file - %s",e.getMessage()));
            throw e;
        }

    }
}
