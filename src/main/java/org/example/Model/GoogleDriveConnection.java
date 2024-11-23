package org.example.Model;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class GoogleDriveConnection {
    private static final String APPLICATION_NAME = "Google Drive Java App";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(
            DriveScopes.DRIVE
    );
    private Map<String, String> fileIdToPathMap = new HashMap<>();
    private Map<String, FileNode> fileNodeCache = new HashMap<>();
    private static final String CREDENTIALS_FILE_PATH = "src/main/java/org/example/Resources/credentials.json";

    private Drive service;


    // Handles the authentication process and initializes the Drive service
    public void login() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credentials = getCredentials(HTTP_TRANSPORT);
        service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public boolean isLoggedIn(){
        return service != null;
    }

    public Drive getService() {
        return service;
    }

    // Retrieves the root directory as a GoogleFileNode
    public GoogleFileNode getRootDirectory() throws IOException {
        File rootMetadata = service.files().get("root").setFields("id, name, mimeType, size, modifiedTime").execute();
        return new GoogleFileNode(rootMetadata, this);
    }

    // Lists children of a directory as GoogleFileNode objects
    public List<GoogleFileNode> listChildren(String folderId) throws IOException {
        String query = String.format("'%s' in parents", folderId);
        FileList result = service.files().list()
                .setQ(query)
                .setFields("files(id, name, mimeType, size, modifiedTime)")
                .execute();

        List<GoogleFileNode> children = new ArrayList<>();
        for (File file : result.getFiles()) {
            children.add(new GoogleFileNode(file, this));
        }
        return children;
    }

    // Private helper method to get credentials
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets
        FileInputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public void clearTokens() {
        java.io.File tokenDir = new java.io.File(TOKENS_DIRECTORY_PATH);
        if (tokenDir.exists() && tokenDir.isDirectory()) {
            for (java.io.File file : tokenDir.listFiles()) {
                if (file.isFile()) {
                    file.delete(); // Delete the file
                } else {
                    deleteDirectoryRecursively(file); // Recursively delete subdirectories
                }
            }
            System.out.println("Token directory cleared.");
        } else {
            System.out.println("Token directory does not exist or is not a directory.");
        }
    }

    public boolean tokensDirectoryExists() {
        java.io.File tokenDir = new java.io.File(TOKENS_DIRECTORY_PATH);
        return tokenDir.exists() && tokenDir.isDirectory();
    }


    // Helper method to delete directories recursively
    private void deleteDirectoryRecursively(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            for (java.io.File file : dir.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                } else {
                    deleteDirectoryRecursively(file); // Recursively delete subdirectories
                }
            }
            dir.delete();
        }
    }

    // Method to convert GoogleFileNode to FileNode
    public FileNode convertToFileNode(GoogleFileNode googleFileNode) {

        if (fileNodeCache.containsKey(googleFileNode.getId())) {
            return fileNodeCache.get(googleFileNode.getId());
        }

        FileNode fileNode = new FileNode(googleFileNode);

        // If the GoogleFileNode is a folder, we can fetch its children
        if (!googleFileNode.isFile()) {
            try {
                List<GoogleFileNode> children = listChildren(googleFileNode.getId());
                List<FileNode> fileNodes = new ArrayList<>();
                for (GoogleFileNode child : children) {
                    fileNodes.add(convertToFileNode(child));  // Convert each child to FileNode
                }
                fileNode.setChildren(fileNodes);  // Set the children of the folder
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fileNodeCache.put(googleFileNode.getId(), fileNode);
        return fileNode;
    }

    // Method to fetch a file/folder from Google Drive by its ID
    public GoogleFileNode getFileFromDrive(String fileId) throws IOException {
        File fileMetadata = service.files().get(fileId).setFields("id, name, mimeType, size, modifiedTime").execute();
        return new GoogleFileNode(fileMetadata, this);
    }

    public Map<String, String> getFileIdToPathMap() {
        return fileIdToPathMap;
    }

    public String getLoggedInUserName() throws IOException {
        About about = service.about().get().setFields("user").execute();
        return about.getUser().getDisplayName();
    }

    public void AddFileToMap(String fileId, String filePath){
        fileIdToPathMap.put(fileId, filePath);
    }

    public String GetFileNameFromMap(String fileId){
        return fileIdToPathMap.get(fileId);
    }
    public String GetFileIdFromMap(String filePath){
        for (Map.Entry<String, String> entry : fileIdToPathMap.entrySet()) {
            if (entry.getValue().equals(filePath)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
