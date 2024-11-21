package org.example.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileSignatureReader {
    private File file;
    private Map<byte[], String> fileSignatures = new HashMap<>();

    public FileSignatureReader(File file) {
        this.file = file;
        initFileSignatures();
    }

    public void initFileSignatures() {
        // Image files
        fileSignatures.put(new byte[]{-1, -40, -1, -32}, "JPEG File (.jpg, .jpeg)");
        fileSignatures.put(new byte[]{71, 73, 70, 56, 55, 97}, "GIF File (.gif)");
        fileSignatures.put(new byte[]{71, 73, 70, 56, 57, 97}, "GIF File (.gif)");
        fileSignatures.put(new byte[]{-119, 80, 78, 71, 13, 10, 26, 10}, "PNG File (.png)");
        fileSignatures.put(new byte[]{73, 73, 42, 0}, "TIFF File (.tif, .tiff)");
        fileSignatures.put(new byte[]{77, 77, 0, 42}, "TIFF File (.tif, .tiff)");
        fileSignatures.put(new byte[]{66, 77}, "Bitmap File (.bmp)");

        // Video files
        fileSignatures.put(new byte[]{0, 0, 1, -80}, "MPEG");
        fileSignatures.put(new byte[]{0, 0, 1, -79}, "MPEG");
        fileSignatures.put(new byte[]{0, 0, 0, 24, 102, 116, 121, 112}, "MP4");
        fileSignatures.put(new byte[]{0, 0, 0, 20, 102, 116, 121, 112}, "MOV");
        fileSignatures.put(new byte[]{26, 69, -33, -93}, "MKV");
        fileSignatures.put(new byte[]{82, 73, 70, 70}, "AVI");
        fileSignatures.put(new byte[]{70, 76, 86, 1}, "FLV");

        // Audio files
        fileSignatures.put(new byte[]{73, 68, 51}, "MP3");
        fileSignatures.put(new byte[]{79, 103, 103, 83}, "OGG");
        fileSignatures.put(new byte[]{82, 73, 70, 70}, "WAV");

        // Document files
        fileSignatures.put(new byte[]{37, 80, 68, 70, 45}, "PDF");
        fileSignatures.put(new byte[]{80, 75, 3, 4}, "ZIP Archive (.zip, .docx, .xlsx, .pptx)");
        fileSignatures.put(new byte[]{80, 75, 7, 8}, "ZIP Archive (empty) (.zip)");
        fileSignatures.put(new byte[]{-48, -49, 17, -32, -95, -79, 26, -31}, "Microsoft Office Document (.doc, .xls, .ppt)");

        // Executable files
        fileSignatures.put(new byte[]{77, 90}, "Windows Executable File (.exe)");
        fileSignatures.put(new byte[]{-119, 69, 76, 70}, "Linux Executable File (.elf)");

        // Archive files
        fileSignatures.put(new byte[]{82, 97, 114, 33}, "RAR Archive (.rar)");
        fileSignatures.put(new byte[]{55, 122, -68, -81, 39, 28}, "7-Zip Archive (.7z)");

        // Web files
        fileSignatures.put(new byte[]{60, 33, 68, 79, 67, 84, 89, 80, 69, 32, 104, 116, 109, 108}, "HTML File (.html)");
        fileSignatures.put(new byte[]{60, 63, 120, 109, 108, 32, 118, 101}, "XML File (.xml)");

        // Programming source files
        fileSignatures.put(new byte[]{35, 105, 110, 99, 108, 117, 100, 101}, "C/C++ Source File (.c, .cpp)");
        fileSignatures.put(new byte[]{47, 42, 32, 74, 97, 118, 97, 32}, "Java Source File (.java)");

        // Specialized files
        fileSignatures.put(new byte[]{66, 76, 69, 78, 68, 69, 82, 45}, "Blender File (.blend)");

        // Plain text and generic files
        fileSignatures.put(new byte[]{-17, -69, -65}, "UTF-8 Text File (.txt)");
        fileSignatures.put(new byte[]{-2, -1}, "UTF-16 BE Text File (.txt)");
        fileSignatures.put(new byte[]{-1, -2}, "UTF-16 LE Text File (.txt)");
        fileSignatures.put(new byte[]{-84, -19, 0, 5, 116, 0, 5, 49}, "Java Serialized Object (.ser)");
    }

    public String readFileSignature() throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] fileHeader = new byte[8];
        fis.read(fileHeader);

        for (Map.Entry<byte[], String> entry : fileSignatures.entrySet()) {
            byte[] signature = entry.getKey();
            if (fileHeader.length >= signature.length && startsWith(fileHeader, signature)) {
                return entry.getValue();
            }
        }
        if (file.getName().endsWith(".c") || file.getName().endsWith(".cpp"))
            return "C/C++ Source File";
        else if (isPlainText(file))
            return "Text File";

            fis.close();
        return "File";
    }

    private static boolean isPlainText(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead;
            byte[] buffer = new byte[1024];

            while ((bytesRead = fis.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    byte b = buffer[i];
                    // Check for non-printable, non-ASCII characters
                    if (b < 0x20 && b != 0x09 && b != 0x0A && b != 0x0D) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean startsWith(byte[] fileHeader, byte[] signature) {
        if (fileHeader.length >= signature.length) {
            for (int i = 0; i < signature.length; i++) {
                if (signature[i] != fileHeader[i]) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < fileHeader.length; i++) {
                if (fileHeader[i] != signature[i]) {
                    return false;
                }
            }
        }

        return true;
    }
}
