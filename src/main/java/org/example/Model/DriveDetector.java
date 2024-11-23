package org.example.Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DriveDetector {

    private List<FileNode> fileNodes = new ArrayList<>();

    public DriveDetector() {
        // Get the list of file roots (like / or C:\)
        File[] files = File.listRoots();
        for (File file : files) {
            // Get the name of the volume associated with the root
            String volumeName = getVolumeName(file);
            FileNode fileNode = new FileNode(file);
            fileNode.setName(volumeName);
            fileNodes.add(fileNode);
        }


    }

    public List<FileNode> getVolumes(){
        return fileNodes;
    }
    private String getVolumeName(File file) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return getVolumeNameForMac(file);
        } else if (os.contains("win")) {
            return getVolumeNameForWindows(file);
        }
        return null;  // Return null if no method is defined for the OS
    }

    public String getVolumeNameForMac(File file) {
        try {
            // For macOS, use the diskutil command to get volume names
            ProcessBuilder builder = new ProcessBuilder("diskutil", "info", file.getAbsolutePath());
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Look for the line containing "Volume Name"
                if (line.contains("Volume Name")) {
                    // Extract and return the volume name
                    return line.split(":")[1].trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;  // Return null if no volume name is found
    }

    private String getVolumeNameForWindows(File file) {
        try {
            // For Windows, use the wmic command to get volume labels
            ProcessBuilder builder = new ProcessBuilder("wmic", "logicaldisk", "get", "caption,volumeName");
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Each line contains the drive letter and volume name
                if (line.trim().startsWith(file.getAbsolutePath().substring(0, 2))) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 1) {
                        return parts[1];  // Return the volume name
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;  // Return null if no volume name is found
    }
}
