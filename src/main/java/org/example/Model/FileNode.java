package org.example.Model;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileNode {
    private ImageIcon img;
    private String name, type = null, lastModified;
    private long size;
    private SimpleDateFormat dateFormat;
    private boolean isFile;
    private Map<String, Object> metadata = new HashMap<>();
    private List<FileNode> children = new ArrayList<>();
    private File file;
    private final String GoogleDriveFileID;
    private com.google.api.services.drive.model.File gdFile;
    private GoogleFileNode googleFileNode = null;


    public FileNode(File file) {
        this.file = file;
        this.name = file.getName();
        this.isFile = file.isFile();
        this.size = file.length();
        this.type = getType();
        this.lastModified = getLastModified();
        this.GoogleDriveFileID = null;
        this.gdFile = null;
    }

    public FileNode(GoogleFileNode googleFileNode) {
        this.googleFileNode = googleFileNode;
        this.gdFile = googleFileNode.getFile();
        this.name = googleFileNode.getName();
        this.size = googleFileNode.getSize();
        this.lastModified = googleFileNode.getLastModified();
        this.isFile = googleFileNode.isFile();
        this.type = googleFileNode.getType();
        if (!isRootDirectory()) {
            if (googleFileNode.isFile())
                this.img = new ImageIcon("src/main/java/org/example/Resources/file.png");
            else
                this.img = new ImageIcon("src/main/java/org/example/Resources/folder.png");
        } else {
            this.img = new ImageIcon("src/main/java/org/example/Resources/googleDrive.png");
        }

        this.GoogleDriveFileID = googleFileNode.getId();
    }

    public String getGoogleDriveFileID(){
        return GoogleDriveFileID;
    }

    public boolean isGoogleFileNode(){
        return googleFileNode != null;
    }

    public boolean isRootDirectory(){
        if (!this.isFile) {
            if (this.isGoogleFileNode())
                return this.gdFile.getParents() == null || this.gdFile.getParents().isEmpty();
            else
                return this.file.getAbsolutePath().equals("/");
        }
        return false;
    }

    // Todo - Add Summary
    public void setMetadata(){
        this.metadata.put("Name", this.name);
        this.metadata.put("Type", this.type);
        this.metadata.put("Size", this.convertSize());
        this.metadata.put("LastModified", this.lastModified);
    }

    public String convertSize(){
        if (getGoogleDriveFileID() == null)
            this.size = this.file.length();
        else
            this.size = googleFileNode.getSize();

        if (this.size >= 1024 && this.size < Math.pow(1024, 2)) {
            this.size = this.size / 1024;
            return size + "KBs";
        } else if (this.size >= Math.pow(1024, 2) && this.size < Math.pow(1024, 3)) {
            this.size = (this.size / 1024) / 1024;
            return size + "MBs";
        } else if (this.size >= Math.pow(1024, 3)) {
            this.size = ((this.size / 1024) / 1024)/1024;
            return size + "GBs";
        }
        return size + "Bytes";
    }

    public String getLastModified(){
        setLastModified();
        return this.lastModified;
    }
    public void setLastModified(){
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (getGoogleDriveFileID() != null)
            this.lastModified = gdFile.getModifiedTime() != null
                    ? this.dateFormat.format(gdFile.getModifiedTime().getValue())
                    : "Unknown";
        else
            this.lastModified = this.dateFormat.format(this.file.lastModified());
    }

    public String getType() {
        if (type == null) detectFileType();

        return type;
    }

    public void detectFileType(){
        // Todo - use AI to read the MIME File Type and provide String of exact type
        if (!this.isFile){
            img = new ImageIcon("src/main/java/org/example/Resources/folder.png");
            this.type = "Folder";
            return;
        }
        img = new ImageIcon("src/main/java/org/example/Resources/file.png");
        try{

            if (GoogleDriveFileID != null){
                this.type = googleFileNode.MIME_TYPE_MAP.getOrDefault(gdFile.getMimeType(), "Unknown File Type");
            } else {
                FileSignatureReader fsr = new FileSignatureReader(this.file);
                this.type = fsr.readFileSignature();
            }
        } catch (IOException e){
            e.printStackTrace();
//            this.type = "File";
        }
    }

    public List<FileNode> getChildren() {
        if (!isGoogleFileNode()) {
            File[] files = file.listFiles();
            children.clear();
            if (files != null) {
                for (File f : files) {
                    if (f.isHidden())
                        continue;
                    children.add(new FileNode(f));
                }
            }
        }
        return children;
    }

    public String getAbsolutePath(){
        if (GoogleDriveFileID == null)
            return file.getAbsolutePath();
        else
            return gdFile.getId();
    }

    public boolean isFile() {
        return isFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public void setChildren(List<FileNode> children) {
        this.children = children;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ImageIcon getImg() {
        return img;
    }

    @Override
    public String toString() {
        return this.name + " " + this.type + " " + convertSize() + " " + this.lastModified;
    }
}
