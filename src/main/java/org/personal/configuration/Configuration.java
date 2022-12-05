package org.personal.configuration;

import lombok.Getter;

import java.util.ResourceBundle;

@Getter
public class Configuration {
    private static Configuration instance = null;
    private final String OUTPUT_DIRECTORY;
    private final String INPUT_DIRECTORY;
    private final String INPUT_FILE_NAME_PREFIX;
    private final String INPUT_FILE_EXTENSION;

    private Configuration(){
        ResourceBundle resource = ResourceBundle.getBundle("scan");
        OUTPUT_DIRECTORY = resource.getString("output_directory");
        INPUT_DIRECTORY = resource.getString("input_directory");
        INPUT_FILE_NAME_PREFIX = resource.getString("input_file_name_prefix");
        INPUT_FILE_EXTENSION = resource.getString("file_extension");
    }

    public static Configuration getInstance() {
        if(instance == null) instance = new Configuration();
        return instance;
    }
}
