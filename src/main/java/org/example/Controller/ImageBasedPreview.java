package org.example.Controller;

import javax.swing.*;
import java.awt.*;

public class ImageBasedPreview {
    private String filePath;

    public ImageBasedPreview(String filePath) {
        this.filePath = filePath;
    }

    public Image showPreview() {
        // Load the image
        ImageIcon imageIcon = new ImageIcon(filePath); // Load the image
        // Scale the image
        return imageIcon.getImage().getScaledInstance(220, 250, Image.SCALE_SMOOTH);
    }
}
