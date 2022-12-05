package org.example.configuration;

import lombok.Getter;

import java.util.ResourceBundle;

@Getter
public class Configuration {
    private static Configuration instance = null;
    private final String outputDirectory;
    private final String inputDirectory;
    private final String inputFileNamePrefix;
    private final String inputFileExtension;

    private Configuration() {
        ResourceBundle resource = ResourceBundle.getBundle("scan");
        outputDirectory = resource.getString("output_directory");
        inputDirectory = resource.getString("input_directory");
        inputFileNamePrefix = resource.getString("input_file_name_prefix");
        inputFileExtension = resource.getString("file_extension");
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }
}
