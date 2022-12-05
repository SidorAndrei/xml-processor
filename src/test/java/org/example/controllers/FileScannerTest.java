package org.example.controllers;

import org.example.configuration.Configuration;
import org.example.service.SerializationService;
import org.example.service.XmlWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileScannerTest {
    private static Configuration configuration;
    private static FileScanner fileScanner;


    @BeforeAll
    public static void setup() {
        configuration = Configuration.getInstance();
    }

    private static void deleteInputDirectory() throws IOException {
        if (Files.exists(Path.of(configuration.getInputDirectory()))) {
            File directory = new File(configuration.getInputDirectory());
            String[] list = directory.list();
            if (list != null && list.length != 0) {
                for (String s : list) {
                    Files.delete(Path.of(configuration.getInputDirectory(), s));
                }
            }
            Files.delete(Path.of(configuration.getInputDirectory()));
        }
    }

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

    @BeforeEach
    public void init() throws IOException {
        deleteOutputDirectory(Path.of(configuration.getOutputDirectory()));
        fileScanner = new FileScanner(new SerializationService<>(), new SerializationService<>(), new XmlWriter());
    }

    @Test
    public void initializeFileScanner_createsInputFolder() throws IOException {
        deleteInputDirectory();
        new FileScanner(new SerializationService<>(), new SerializationService<>(), new XmlWriter());
        assertTrue(Files.exists(Path.of(configuration.getInputDirectory())));
    }

    @Test
    public void startScan_inputFolderNotExists_throwsIOException() throws IOException {
        deleteInputDirectory();
        assertThrows(IOException.class, fileScanner::watch);
    }

    @Test
    public void verifyFileName_nameDoesNotContainNumber_returnsFalse() {
        String fileName = String.format("%s%s", configuration.getInputFileNamePrefix(), configuration.getInputFileExtension());
        assertFalse(fileScanner.validateFileNamePattern(fileName));
    }

    @Test
    public void verifyFileName_nameContainsLetterInsteadOfNumber_returnsFalse() {
        String fileName = String.format("%saa%s", configuration.getInputFileNamePrefix(), configuration.getInputFileExtension());
        assertFalse(fileScanner.validateFileNamePattern(fileName));
    }

    @Test
    public void verifyFileName_nameContainsFilePrefix_returnsFalse() {
        String fileName = String.format("12%s", configuration.getInputFileExtension());
        assertFalse(fileScanner.validateFileNamePattern(fileName));
    }

    @Test
    public void verifyFileName_nameContainsFileExtension_returnsFalse() {
        String fileName = String.format("%s12", configuration.getInputFileNamePrefix());
        assertFalse(fileScanner.validateFileNamePattern(fileName));
    }

    @Test
    public void verifyFileName_nameContainsNumber_returnsTrue() {
        String fileName = String.format("%s12%s", configuration.getInputFileNamePrefix(), configuration.getInputFileExtension());
        assertTrue(fileScanner.validateFileNamePattern(fileName));
    }

    @Test
    public void handleEvent_xmlFile_createsOutputFiles() {
        File directory = new File(configuration.getOutputDirectory());
        int length = directory.list() == null ? 0 : directory.list().length;
        fileScanner.handleEvent("orders_test.xml", "test");
        assertNotEquals(length, directory.list().length);
    }

    @Test
    public void handleEvent_WrongXmlFile_createsOutputFiles() throws IOException {
        File directory = new File(configuration.getOutputDirectory());
        deleteOutputDirectory(Path.of(configuration.getOutputDirectory()));
        int length = directory.list() == null ? 0 : directory.list().length;
        fileScanner.handleEvent("orders_test_CannotResolveClassException.xml", "test");
        assertEquals(length, directory.length());
    }

    @Test
    public void handleEvent_InvalidXmlFile_createsOutputFiles() throws IOException {
        File directory = new File(configuration.getOutputDirectory());
        deleteOutputDirectory(Path.of(configuration.getOutputDirectory()));
        directory.createNewFile();
        int length = directory.list() == null ? 0 : directory.list().length;
        fileScanner.handleEvent("orders_test_UnknownFieldException.xml", "test");
        assertEquals(length, directory.length());
    }

}
