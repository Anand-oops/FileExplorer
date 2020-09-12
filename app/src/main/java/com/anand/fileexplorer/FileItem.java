package com.anand.fileexplorer;

public class FileItem {

    private String name;
    private boolean isFolder;
    private String filePath;

    public FileItem(String name, boolean isFolder, String filePath) {
        this.name = name;
        this.isFolder = isFolder;
        this.filePath = filePath;
    }

    public String getName() {
        return name;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public String getFilePath() {
        return filePath;
    }
}