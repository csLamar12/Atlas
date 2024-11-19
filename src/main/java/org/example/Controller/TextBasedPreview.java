package org.example.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TextBasedPreview {
    private String filePath;

    public TextBasedPreview(String filePath) {
        this.filePath = filePath;
    }

    public String showPreview() {
        try {
            // Read the content of the text file
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            return content;
        } catch (IOException e) {
            System.err.println("Error reading the text file: " + e.getMessage());
        }
        return "File not readable or is empty :(";
    }
}
