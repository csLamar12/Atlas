package org.example.Controller;

import org.example.Model.*;
import org.example.View.AtlasWindow;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;


public class AtlasWindowController {
    private AtlasWindow atlasWindow;
    private NavigationHistory navigationHistory = new NavigationHistory();
    private String currentDirectory;
    private DriveDetector driveDetector;
    private List<FileNode> volumes;
    private GoogleDriveConnection driveConnection;

    public AtlasWindowController(AtlasWindow atlasWindow, String currentDirectory) {
        this.atlasWindow = atlasWindow;
        this.currentDirectory = currentDirectory;
//        navigationHistory.visitDirectory(currentDirectory);
        showWorkingDirectory();
        bindButtonEvents();
        driveDetector = new DriveDetector();
        volumes = driveDetector.getVolumes();
        new Thread(()-> {
            showExistingGoogleDrive();
        }).start();
        atlasWindow.expandFolder(volumes);
    }

    public void addGoogleDrive(){
        try{
            if (driveConnection != null) {
                for (FileNode fileNode : volumes) {
                    if (fileNode.isGoogleFileNode()){
                        volumes.remove(fileNode);
                        System.out.println("volume removed");
                        break;
                    }
                }
            }
            driveConnection = new GoogleDriveConnection();
            driveConnection.clearTokens();
            driveConnection.login();
            if (driveConnection.isLoggedIn())
                atlasWindow.showMessage("Welcome " + driveConnection.getLoggedInUserName() + "!");
            else {
                atlasWindow.showMessage("Login failed!");
                return;
            }
            GoogleFileNode root = driveConnection.getRootDirectory();
            FileNode googleDriveNode = driveConnection.convertToFileNode(root);
            volumes.add(googleDriveNode);
            if (currentDirectory.equals("Volumes"))
                atlasWindow.expandFolder(volumes);
        } catch (GeneralSecurityException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public void showExistingGoogleDrive(){
        try{
            driveConnection = new GoogleDriveConnection();
            if (!driveConnection.tokensDirectoryExists()){
                return;
            }
            driveConnection.login();
            if (driveConnection.isLoggedIn())
                atlasWindow.showMessage("Welcome back " + driveConnection.getLoggedInUserName() + "!");
            else {
                atlasWindow.showMessage("Login failed!");
                return;
            }
            atlasWindow.showNotification("Google drive is being added in the background.");
            GoogleFileNode root = driveConnection.getRootDirectory();
            FileNode googleDriveNode = driveConnection.convertToFileNode(root);
            volumes.add(googleDriveNode);
            if (currentDirectory.equals("Volumes"))
                atlasWindow.expandFolder(volumes);
        } catch (GeneralSecurityException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public void bindButtonEvents(){
        atlasWindow.getFileTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int viewRow = atlasWindow.getFileTable().getSelectedRow();
                if(viewRow != -1) {
                    // Convert view row index to model row index
                    int modelRow = atlasWindow.getFileTable().convertRowIndexToModel(viewRow);
                    FileNode selectedFileNode = atlasWindow.getFileNodeAt(modelRow);
                    onSelect(selectedFileNode);
                    if (e.getClickCount() == 2) {
                        onRowDoubleClicked(selectedFileNode);
                    }
                }
            }
        });
        atlasWindow.getBackBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileNode fileNode = navigationHistory.goBack(currentDirectory);
                if(fileNode == null) {
                    atlasWindow.expandFolder(volumes);
                    currentDirectory = "Volumes";
                    showWorkingDirectory();
                    return;
                }
                googleFileNodeNavigation(fileNode);
            }
        });
        atlasWindow.getForwardBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileNode fileNode = navigationHistory.goForward(currentDirectory);
                if(fileNode == null)
                    return;
                googleFileNodeNavigation(fileNode);
            }
        });

        atlasWindow.getAddAccountbtn().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addGoogleDrive();
            }
        });

//        atlasWindow.getSearchBar().addFocusListener(new FocusAdapter() {
//            @Override
//            public void focusGained(FocusEvent e) {
//                atlasWindow.getSearchBar().setText("");
//                atlasWindow.getPopupMenu().setVisible(true);
//                atlasWindow.getPopupMenu().show(atlasWindow.getSearchBar(), 0, atlasWindow.getSearchBar().getHeight());
//            }
//
//            @Override
//            public void focusLost(FocusEvent e) {
//
////                atlasWindow.getPopupMenu().setVisible(false);
//                if (atlasWindow.getSearchBar().getText().isEmpty()) {
//                    atlasWindow.getSearchBar().setText("Search\t\t\t               🔍");
//                }
//            }
//        });

//        atlasWindow.getSearchBar().addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyTyped(KeyEvent e) {
//                String query = atlasWindow.getSearchBar().getText().toLowerCase();
//
//            }
//        });
    }

    private void googleFileNodeNavigation(FileNode fileNode) {
        if (fileNode.isGoogleFileNode()){
            String id = driveConnection.GetFileIdFromMap(fileNode.getTempPath());
            try {
                fileNode = driveConnection.convertToFileNode(driveConnection.getFileFromDrive(id));
                atlasWindow.expandFolder(fileNode.getChildren());
                currentDirectory = getGoogleDrivePath(fileNode);
                return;
            }catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        currentDirectory = fileNode.getAbsolutePath();
        atlasWindow.expandFolder(fileNode.getChildren());
    }

    public void showWorkingDirectory(){
        atlasWindow.setWorkingDir(currentDirectory);
    }

    public void onSelect(FileNode selectedFileNode){
        if (!selectedFileNode.isFile())
            return;
        if (atlasWindow.getVideoPreview() != null && atlasWindow.getVideoPreview().isVideoPlaying())
            atlasWindow.getVideoPreview().stopVideo();
        if (atlasWindow.getVideoPreview() != null)
            atlasWindow.getVideoPreview().stopVideo();
        atlasWindow.revalidate();
        switch(selectedFileNode.getType()){
            case "MP4":
                // works for some MP4 files
                atlasWindow.showPreviewPane();
                atlasWindow.addVideoPreview(selectedFileNode.getAbsolutePath());
                break;
            case "C/C++ Source File (.c, .cpp)":
            case "C/C++ Source File":
            case "Java Source File (.java)":
            case "HTML File (.html)":
            case "XML File (.xml)":
            case "Text File":
                atlasWindow.showPreviewPane();
                atlasWindow.addTextBasedPreview(selectedFileNode.getFile());
                break;
            case "JPEG File (.jpg, .jpeg)":
            case "GIF File (.gif)":
            case "PNG File (.png)":
            case "TIFF File (.tif, .tiff)":
            case "Bitmap File (.bmp)":
                atlasWindow.showPreviewPane();
                atlasWindow.addImageBasedPreview(selectedFileNode.getAbsolutePath());
                break;
            case "PDF":
                // DOESN'T WORK YET!
//                atlasWindow.addPDFBasedPreview(selectedFileNode.getAbsolutePath());
                break;

        }
    }

    public String getGoogleDrivePath(FileNode fileNode){
        try {
            return fileNode.getGoogleFileNode().getReadablePath(driveConnection.getService());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void onRowDoubleClicked(FileNode selectedFileNode) {
        try {
            if (selectedFileNode.isFile() && selectedFileNode.getGoogleDriveFileID() != null) {
                GoogleDriveFileHandler gDFileHandler = new GoogleDriveFileHandler(driveConnection.getService());
                gDFileHandler.downloadOpenAndDeleteFileInBackground(selectedFileNode.getGoogleDriveFileID(), selectedFileNode.getName());
            }else if (!selectedFileNode.isFile() && selectedFileNode.getGoogleDriveFileID() != null) {
                navigationHistory.visitDirectory(currentDirectory);
                currentDirectory = getGoogleDrivePath(selectedFileNode);
                atlasWindow.expandFolder(selectedFileNode.getChildren());
                showWorkingDirectory();
            }else if (!selectedFileNode.isFile()) {
                navigationHistory.visitDirectory(currentDirectory);
                currentDirectory = selectedFileNode.getAbsolutePath();
                atlasWindow.expandFolder(selectedFileNode.getChildren());
                showWorkingDirectory();
            } else {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(new File(selectedFileNode.getAbsolutePath()));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
