package org.example.Controller;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.client.http.HttpResponse;

import java.awt.Desktop;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoogleDriveFileHandler {

    private Drive service;
    private ExecutorService executorService;

    public GoogleDriveFileHandler(Drive service) {
        this.service = service;
        this.executorService = Executors.newSingleThreadExecutor();  // Using a single thread executor
    }

    // Method to start the download, open, and delete task on a separate thread
    public void downloadOpenAndDeleteFileInBackground(String fileId, String fileName) {
        executorService.submit(() -> {
            try {
                downloadOpenAndDeleteFile(fileId, fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // Method to download, open, and delete the file after closing
    private void downloadOpenAndDeleteFile(String fileId, String fileName) throws IOException {
        // Create temp directory if it does not exist
        java.io.File tempDir = new java.io.File("temp");
        if (!tempDir.exists()) {
            tempDir.mkdir();  // Create the temp directory
        }

        String tempFilePath = "temp/" + fileName;  // Temporary location to store the file

        // Download the file to a temporary location
        downloadFile(fileId, tempFilePath);

        java.io.File localFile = new java.io.File(tempFilePath);

        // Open the file using the default system application
        openFile(localFile);

        // Wait for the file to be closed before deleting
        waitForFileToClose(localFile);

        // Delete the file after it is closed
        if (localFile.exists()) {
            boolean deleted = localFile.delete();
            if (deleted) {
                System.out.println("File deleted successfully: " + tempFilePath);
            } else {
                System.out.println("Failed to delete the file: " + tempFilePath);
            }
        }
    }

    // Method to download the file from Google Drive
    private void downloadFile(String fileId, String localPath) throws IOException {
        // Get the file metadata to determine if it can be directly downloaded or needs to be exported
        File file = service.files().get(fileId).setFields("id, name, mimeType").execute();
        String mimeType = file.getMimeType();

        // If it's a Google Docs, Sheets, or Slides file, export it
        if (mimeType.equals("application/vnd.google-apps.document") ||
                mimeType.equals("application/vnd.google-apps.spreadsheet") ||
                mimeType.equals("application/vnd.google-apps.presentation")) {

            // Export the file to a desired format (e.g., PDF)
            exportGoogleFile(fileId, mimeType, localPath);
        } else {
            // Download the file as binary content
            downloadBinaryFile(fileId, localPath);
        }
    }

    // Method to export Google Docs/Sheets/Slides to a specific format (e.g., PDF)
    private void exportGoogleFile(String fileId, String mimeType, String localPath) throws IOException {
        String exportMimeType = "application/pdf";  // You can choose other formats like DOCX, XLSX, PPTX

        HttpResponse response = service.files().export(fileId, exportMimeType).executeMedia();

        // Save the exported content to a file (e.g., PDF)
        saveFile(response.getContent(), localPath);  // Save the content as a PDF or the selected format
    }

    // Method to download binary files (non-Google Docs/Sheets/Slides files)
    private void downloadBinaryFile(String fileId, String localPath) throws IOException {
        HttpResponse response = service.files().get(fileId).executeMedia();

        // Save the file content to a local file
        saveFile(response.getContent(), localPath);
    }

    // Method to save the downloaded content to a file
    private void saveFile(java.io.InputStream inputStream, String filePath) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, length);
        }
        fileOutputStream.close();
    }

    // Method to open the file using the system's default viewer
    private void openFile(java.io.File file) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(file);  // Open the file with the default system viewer
        } else {
            System.out.println("Desktop is not supported on this platform.");
        }
    }

    // Method to wait for the file to be closed before continuing
    private void waitForFileToClose(java.io.File file) {
        try {
            // A simple mechanism to wait for the file to be closed (polling the file's lock status)
            while (file.exists()) {
                // Here you could implement a more sophisticated check if required
                Thread.sleep(1000);  // Wait for 1 second before checking again
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Public method to download a file and return the temporary path
    public String downloadFileToTempDirectory(String fileId, String fileName) throws IOException {
        // Create temp directory if it does not exist
        java.io.File tempDir = new java.io.File("temp");
        if (!tempDir.exists()) {
            tempDir.mkdir();  // Create the temp directory
        }

        // Temporary location to store the file
        String tempFilePath = "temp/" + fileName;

        // Download the file
        downloadFile(fileId, tempFilePath);

        // Return the temporary file path
        return tempFilePath;
    }


    // Optional: Shutdown executor service when no longer needed
    public void shutdown() {
        executorService.shutdown();
    }
}
