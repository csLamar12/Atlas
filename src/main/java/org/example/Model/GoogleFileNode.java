package org.example.Model;

import com.google.api.services.drive.model.File;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class GoogleFileNode {
    private ImageIcon img;
    private String name, type = null, lastModified;
    private long size;
    private SimpleDateFormat dateFormat;
    private boolean isFile;
    private Map<String, Object> metadata = new HashMap<>();
    private List<FileNode> children = new ArrayList<>();
    private File file;


    public GoogleFileNode(File file) {
        this.file = file;
        this.name = file.getName();
        this.isFile = !"application/vnd.google-apps.folder".equals(file.getMimeType());
        this.size = file.getSize() == null ? 0 : file.getSize();
        this.type = getType();
        this.lastModified = getLastModified();
    }

    // TODO - Add Summary
    public void setMetadata() {
        this.metadata.put("Name", this.name);
        this.metadata.put("Type", this.type);
        this.metadata.put("Size", this.convertSize());
        this.metadata.put("LastModified", this.lastModified);
    }

    public String convertSize() {
        if (this.size >= 1024 && this.size < Math.pow(1024, 2)) {
            return (this.size / 1024) + " KBs";
        } else if (this.size >= Math.pow(1024, 2) && this.size < Math.pow(1024, 3)) {
            return (this.size / (1024 * 1024)) + " MBs";
        } else if (this.size >= Math.pow(1024, 3)) {
            return (this.size / (1024 * 1024 * 1024)) + " GBs";
        }
        return this.size + " Bytes";
    }

    public String getLastModified() {
        setLastModified();
        return this.lastModified;
    }

    public void setLastModified() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.lastModified = file.getModifiedTime() != null
                ? this.dateFormat.format(file.getModifiedTime().getValue())
                : "Unknown";
    }

    public String getType() {
        if (type == null) detectFileType();
        return type;
    }

    public void detectFileType() {
        if (!this.isFile) {
            img = new ImageIcon("src/Resources/folder.png");
            this.type = "Folder";
        } else {
            img = new ImageIcon("src/Resources/file.png");
            this.type = MIME_TYPE_MAP.getOrDefault(file.getMimeType(), "Unknown File Type");
        }
    }

    public List<FileNode> getChildren() {
        return children;
    }

    public String getAbsolutePath() {
        return file.getId(); // Google Drive files use ID instead of absolute paths
    }

    public boolean isFile() {
        return isFile;
    }

    public String getId(){
        return file.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
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

    public void setImg(ImageIcon img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return this.name + " " + this.type + " " + convertSize() + " " + this.lastModified;
    }

    // Map of MIME types to human-readable names
    public final Map<String, String> MIME_TYPE_MAP = new HashMap<>() {{
        // Document types
        put("application/pdf", "PDF Document");
        put("application/msword", "Word Document");
        put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Word Document");
        put("application/vnd.ms-excel", "Excel Spreadsheet");
        put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel Spreadsheet");
        put("application/vnd.ms-powerpoint", "PowerPoint Presentation");
        put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "PowerPoint Presentation");
        put("text/plain", "Text File");
        put("text/csv", "CSV File");
        put("application/rtf", "Rich Text Document");
        put("application/vnd.google-apps.document", "Google Docs Document");
        put("application/vnd.google-apps.spreadsheet", "Google Sheets Document");
        put("application/vnd.google-apps.presentation", "Google Slides Presentation");
        put("application/vnd.google-apps.folder", "Folder");

        // Image types
        put("image/jpeg", "JPEG Image");
        put("image/png", "PNG Image");
        put("image/gif", "GIF Image");
        put("image/bmp", "Bitmap Image");
        put("image/webp", "WebP Image");
        put("image/svg+xml", "SVG Image");
        put("image/tiff", "TIFF Image");
        put("image/x-icon", "Icon File");

        // Video types
        put("video/mp4", "MP4 Video");
        put("video/x-msvideo", "AVI Video");
        put("video/mpeg", "MPEG Video");
        put("video/quicktime", "QuickTime Video");
        put("video/x-ms-wmv", "WMV Video");
        put("video/webm", "WebM Video");
        put("video/3gpp", "3GPP Video");
        put("video/x-flv", "Flash Video");

        // Audio types
        put("audio/mpeg", "MP3 Audio");
        put("audio/wav", "WAV Audio");
        put("audio/ogg", "OGG Audio");
        put("audio/x-ms-wma", "WMA Audio");
        put("audio/aac", "AAC Audio");
        put("audio/flac", "FLAC Audio");

        // Compressed archive types
        put("application/zip", "ZIP Archive");
        put("application/x-rar-compressed", "RAR Archive");
        put("application/x-7z-compressed", "7-Zip Archive");
        put("application/x-tar", "TAR Archive");
        put("application/gzip", "GZIP Archive");

        // Code and script files
        put("text/html", "HTML Document");
        put("text/css", "CSS Stylesheet");
        put("application/javascript", "JavaScript File");
        put("application/json", "JSON File");
        put("application/xml", "XML File");
        put("application/java-archive", "Java Archive (JAR)");
        put("application/x-python-code", "Python Script");
        put("application/x-sh", "Shell Script");

        // Font files
        put("font/woff", "Web Open Font Format (WOFF)");
        put("font/woff2", "Web Open Font Format 2 (WOFF2)");
        put("application/x-font-ttf", "TrueType Font (TTF)");
        put("application/x-font-otf", "OpenType Font (OTF)");

        // Miscellaneous types
        put("application/octet-stream", "Binary File");
        put("application/vnd.android.package-archive", "Android APK");
        put("application/x-msdownload", "Executable File (EXE)");
        put("application/vnd.google-apps.drawing", "Google Drawing");
        put("application/vnd.google-apps.script", "Google Apps Script");
        put("application/vnd.google-apps.site", "Google Sites Document");
    }};
}
