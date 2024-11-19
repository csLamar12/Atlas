package org.example;

import org.example.Controller.AtlasWindowController;
import org.example.View.AtlasWindow;


public class Main {
    public static void main(String[] args) {
        new AtlasWindowController(new AtlasWindow(), "Volumes");
    }
}