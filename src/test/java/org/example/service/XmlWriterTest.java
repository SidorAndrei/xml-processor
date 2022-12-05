package org.example.service;

import org.example.configuration.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlWriterTest {
    private static Configuration configuration;
    private static XmlWriter xmlWriter;
    private static Path filePath;

    private static void deleteOutputDirectory(Path outputDirPath) throws IOException {
        if (Files.exists(outputDirPath)) {
            File directory = new File(configuration.getOutputDirectory());
            String[] list = directory.list();
            if (list != null && list.length != 0) {
                for (String s : list) {
                    Files.delete(Path.of(configuration.getOutputDirectory(), s));
                }
            }
            Files.delete(outputDirPath);
        }
    }

    @BeforeAll
    public static void setup() {
        configuration = Configuration.getInstance();
    }

    @BeforeEach
    public void initialize() {
        xmlWriter = new XmlWriter();
        filePath = null;
    }

    @AfterEach
    public void deleteFile() throws IOException {
        if (filePath != null && Files.exists(filePath))
            Files.delete(filePath);
        deleteOutputDirectory(Path.of(configuration.getOutputDirectory()));
    }

    @Test
    public void createXmlWriterInstanceOutputDirectoryIsCreated() throws IOException {
        deleteOutputDirectory(Path.of(configuration.getOutputDirectory()));
        xmlWriter = new XmlWriter();
        assertTrue(Files.exists(Path.of(configuration.getOutputDirectory())));
    }

    @Test
    public void createXmlFilePassingANameCreatesTheXmlFileInOutputFolderAndReturnsAbsolutePath() throws IOException {
        filePath = Path.of(xmlWriter.createXmlFile("test1"));
        assertTrue(Files.exists(filePath));
        filePath = null;
    }

    @Test
    public void createXmlFileAlreadyExistentNameDoesNotThrowExceptionAndFileExists() throws IOException {
        String fileName = "test2";

        xmlWriter.createXmlFile(fileName);
        filePath = Path.of(xmlWriter.createXmlFile(fileName));

        assertTrue(Files.exists(filePath));
    }

    @Test
    public void createXmlFileOutputDirectoryDoesNotExistThrowsIOException() throws IOException {
        Path outputDirPath = Path.of(configuration.getOutputDirectory());
        deleteOutputDirectory(outputDirPath);

        String fileName = "test3";

        assertThrows(IOException.class, () -> xmlWriter.createXmlFile(fileName));
    }


    @Test
    public void writeXmlFileXmlStringFileContainsOnlyXmlString() throws IOException {
        String xmlString = """
                <?xml version="1.0" encoding="UTF-8"?>
                <products>
                    <product>
                        <description>Apple iPad 2 with Wi-Fi 16GB - iOS 5 - Black</description>
                        <gtin>00885909464517</gtin>
                        <price currency="USD">399.0</price>
                        <orderid>2343</orderid>
                    </product>
                    <product>
                        <description>Apple MacBook Air A 11.6" Mac OS X v10.7 Lion MacBook</description>
                        <gtin>00885909464043</gtin>
                        <price currency="USD">1149.0</price>
                        <orderid>2344</orderid>
                    </product>
                </products>
                """;
        String fileName = "test4";
        String filePath = String.format("%s/%s", configuration.getOutputDirectory(), fileName);
        xmlWriter.createXmlFile(fileName);
        xmlWriter.writeXmlFile(filePath, xmlString);
        String xmlText = Files.readString(Path.of(filePath));
        assertEquals(xmlString, xmlText);
    }

    @Test
    public void writeXmlFileRewriteExistentFileFileContainsOnlyXmlString() throws IOException {
        String xmlString = """
                <?xml version="1.0" encoding="UTF-8"?>
                <products>
                    <product>
                        <description>Apple iPad 2 with Wi-Fi 16GB - iOS 5 - Black</description>
                        <gtin>00885909464517</gtin>
                        <price currency="USD">399.0</price>
                        <orderid>2343</orderid>
                    </product>
                    <product>
                        <description>Apple MacBook Air A 11.6" Mac OS X v10.7 Lion MacBook</description>
                        <gtin>00885909464043</gtin>
                        <price currency="USD">1149.0</price>
                        <orderid>2344</orderid>
                    </product>
                </products>
                """;
        String xmlStringReplaced = xmlString.replaceAll(" ", "");
        String fileName = "test4";
        String filePath = String.format("%s/%s", configuration.getOutputDirectory(), fileName);
        xmlWriter.createXmlFile(fileName);
        xmlWriter.writeXmlFile(filePath, xmlStringReplaced);
        xmlWriter.writeXmlFile(filePath, xmlString);
        String xmlText = Files.readString(Path.of(filePath));
        assertEquals(xmlString, xmlText);
    }

    @Test
    public void prettifyXmlFile_prettifyFile_fileContainsPrettifiedXmlString() throws IOException, TransformerException, SAXException {
        String prettifiedXmlString = """
                <?xml version="1.0" encoding="UTF-8"?>
                <products>
                    <product>
                        <description>Apple iPad 2 with Wi-Fi 16GB - iOS 5 - Black</description>
                        <gtin>00885909464517</gtin>
                        <price currency="USD">399.0</price>
                        <orderid>2343</orderid>
                    </product>
                    <product>
                        <description>Apple MacBook Air A 11.6" Mac OS X v10.7 Lion MacBook</description>
                        <gtin>00885909464043</gtin>
                        <price currency="USD">1149.0</price>
                        <orderid>2344</orderid>
                    </product>
                </products>
                """.replaceAll("\n", "\r\n");
        String xmlStringReplaced = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <products><product><description>Apple iPad 2 with Wi-Fi 16GB - iOS 5 - Black</description><gtin>00885909464517</gtin><price currency=\"USD\">399.0</price>" +
                "<orderid>2343</orderid>" +
                "</product>" +
                "<product>" +
                "<description>Apple MacBook Air A 11.6\" Mac OS X v10.7 Lion MacBook</description>" +
                "<gtin>00885909464043</gtin>" +
                "<price currency=\"USD\">1149.0</price>" +
                "<orderid>2344</orderid>" +
                "</product>" +
                "</products>";
        String fileName = "test5";
        String filePath = String.format("%s/%s", configuration.getOutputDirectory(), fileName);
        xmlWriter.createXmlFile(fileName);
        xmlWriter.writeXmlFile(filePath, xmlStringReplaced);
        xmlWriter.prettifyXmlFile(filePath);
        String xmlText = Files.readString(Path.of(filePath));
        assertEquals(prettifiedXmlString, xmlText);
    }

}
