package org.example.Controller;

import org.example.Model.*;
import org.example.View.AtlasWindow;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
        navigationHistory.visitDirectory(currentDirectory);
        showWorkingDirectory();
        bindButtonEvents();
        driveDetector = new DriveDetector();
        volumes = driveDetector.getVolumes();
        atlasWindow.expandFolder(volumes);
    }

    public void addGoogleDrive(){
        try{
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
                currentDirectory = fileNode.getAbsolutePath();
                atlasWindow.expandFolder(fileNode.getChildren());
                showWorkingDirectory();
            }
        });
        atlasWindow.getForwardBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileNode fileNode = navigationHistory.goForward(currentDirectory);
                if(fileNode == null)
                    return;
                currentDirectory = fileNode.getAbsolutePath();
                atlasWindow.expandFolder(fileNode.getChildren());
                showWorkingDirectory();
            }
        });

        atlasWindow.getAddAccountbtn().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addGoogleDrive();
            }
        });
    }

    public void showWorkingDirectory(){
        atlasWindow.setWorkingDir(currentDirectory);
    }

    public void onSelect(FileNode selectedFileNode){
        atlasWindow.showPreviewPane();
        if (atlasWindow.getVideoPreview() != null && atlasWindow.getVideoPreview().isVideoPlaying())
            atlasWindow.getVideoPreview().stopVideo();
        if (atlasWindow.getVideoPreview() != null)
            atlasWindow.getVideoPreview().stopVideo();
        atlasWindow.revalidate();
        switch(selectedFileNode.getType()){
            case "MP4":
                // works for some MP4 files
                atlasWindow.addVideoPreview(selectedFileNode.getAbsolutePath());
                break;
            case "C/C++ Source File (.c, .cpp)":
            case "C/C++ Source File":
            case "Java Source File (.java)":
            case "HTML File (.html)":
            case "XML File (.xml)":
            case "Text File":
                atlasWindow.addTextBasedPreview(selectedFileNode.getFile());
                break;
            case "JPEG File (.jpg, .jpeg)":
            case "GIF File (.gif)":
            case "PNG File (.png)":
            case "TIFF File (.tif, .tiff)":
            case "Bitmap File (.bmp)":
                atlasWindow.addImageBasedPreview(selectedFileNode.getAbsolutePath());
                break;
            case "PDF":
                // DOESN'T WORK YET!
//                atlasWindow.addPDFBasedPreview(selectedFileNode.getAbsolutePath());
                break;

        }
    }

    public void onRowDoubleClicked(FileNode selectedFileNode) {
        try {
            if (selectedFileNode.isFile() && selectedFileNode.getGoogleDriveFileID() != null) {
                driveConnection.getFileFromDrive(selectedFileNode.getGoogleDriveFileID());
                GoogleDriveFileHandler gDFileHandler = new GoogleDriveFileHandler(driveConnection.getService());
                gDFileHandler.downloadOpenAndDeleteFileInBackground(selectedFileNode.getGoogleDriveFileID(), selectedFileNode.getName());
            } else if (!selectedFileNode.isFile()) {
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
