package org.example.controllers;

import org.personal.configuration.Configuration;
import org.personal.controllers.FileScanner;
import org.personal.service.SerializationService;
import org.personal.service.XmlWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileScannerTest {
    private static Configuration configuration;
    private static FileScanner fileScanner;


    @BeforeAll
    public static void setup(){
        configuration = Configuration.getInstance();
    }

    @BeforeEach
    public void init() throws IOException {
        delete_output_directory(Path.of(configuration.getOUTPUT_DIRECTORY()));
        fileScanner = new FileScanner(new SerializationService<>(), new SerializationService<>(), new XmlWriter());
    }


    private static void deleteInputDirectory() throws IOException {
        if(Files.exists(Path.of(configuration.getINPUT_DIRECTORY()))){
            File directory = new File(configuration.getINPUT_DIRECTORY());
            String[] list = directory.list();
            if(list != null && list.length != 0) {
                for (String s : list) {
                    Files.delete(Path.of(configuration.getINPUT_DIRECTORY(),s));
                }
            }
            Files.delete(Path.of(configuration.getINPUT_DIRECTORY()));
        }
    }

    private static void delete_output_directory(Path outputDirPath) throws IOException {
        if(Files.exists(outputDirPath)){
            File directory = new File(configuration.getOUTPUT_DIRECTORY());
            String[] list = directory.list();
            if(list != null && list.length != 0) {
                for (String s : list) {
                    Files.delete(Path.of(configuration.getOUTPUT_DIRECTORY(),s));
                }
            }
            Files.delete(outputDirPath);
        }
    }

    @Test
    public void initializeFileScanner_createsInputFolder() throws IOException {
        deleteInputDirectory();
        new FileScanner(new SerializationService<>(),new SerializationService<>(), new XmlWriter());
        assertTrue(Files.exists(Path.of(configuration.getINPUT_DIRECTORY())));
    }

    @Test
    public void startScan_inputFolderNotExists_throwsIOException() throws IOException {
        deleteInputDirectory();
        assertThrows(IOException.class, fileScanner::startScan);
    }

    @Test
    public void verifyFileName_nameDoesNotContainNumber_returnsFalse(){
        String fileName = String.format("%s%s", configuration.getINPUT_FILE_NAME_PREFIX(), configuration.getINPUT_FILE_EXTENSION());
        assertFalse(fileScanner.verifyFileName(fileName));
    }

    @Test
    public void verifyFileName_nameContainsLetterInsteadOfNumber_returnsFalse(){
        String fileName = String.format("%saa%s", configuration.getINPUT_FILE_NAME_PREFIX(), configuration.getINPUT_FILE_EXTENSION());
        assertFalse(fileScanner.verifyFileName(fileName));
    }

    @Test
    public void verifyFileName_nameContainsFilePrefix_returnsFalse(){
        String fileName = String.format("12%s", configuration.getINPUT_FILE_EXTENSION());
        assertFalse(fileScanner.verifyFileName(fileName));
    }

    @Test
    public void verifyFileName_nameContainsFileExtension_returnsFalse(){
        String fileName = String.format("%s12", configuration.getINPUT_FILE_NAME_PREFIX());
        assertFalse(fileScanner.verifyFileName(fileName));
    }

    @Test
    public void verifyFileName_nameContainsNumber_returnsTrue(){
        String fileName = String.format("%s12%s", configuration.getINPUT_FILE_NAME_PREFIX(), configuration.getINPUT_FILE_EXTENSION());
        assertTrue(fileScanner.verifyFileName(fileName));
    }

    @Test
    public void handleEvent_xmlFile_createsOutputFiles() {
        File directory = new File(configuration.getOUTPUT_DIRECTORY());
        int length = directory.list() == null ? 0 : directory.list().length;
        fileScanner.handleEvent("orders_test.xml","test");
        assertNotEquals(length, directory.list().length);
    }

    @Test
    public void handleEvent_WrongXmlFile_createsOutputFiles() throws IOException {
        File directory = new File(configuration.getOUTPUT_DIRECTORY());
        delete_output_directory(Path.of(configuration.getOUTPUT_DIRECTORY()));
        int length = directory.list() == null ? 0 : directory.list().length;
        fileScanner.handleEvent("orders_test_CannotResolveClassException.xml", "test");
        assertEquals(length, directory.length());
    }

    @Test
    public void handleEvent_InvalidXmlFile_createsOutputFiles() throws IOException {
        File directory = new File(configuration.getOUTPUT_DIRECTORY());
        delete_output_directory(Path.of(configuration.getOUTPUT_DIRECTORY()));
        directory.createNewFile();
        int length = directory.list() == null ? 0 : directory.list().length;
        fileScanner.handleEvent("orders_test_UnknownFieldException.xml","test");
        assertEquals(length, directory.length());
    }

}
