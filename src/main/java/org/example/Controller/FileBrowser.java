package org.example.Controller;

import org.example.Model.FileNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FileBrowser {

    public List<FileNode> listFilesOf(FileNode root) {
        List<FileNode> files = new ArrayList<>();

        Queue<FileNode> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            // set working directory
            FileNode currentFile = queue.poll();

            files.addAll(currentFile.getChildren());

        }
        return files;
    }
}
