package com.mailserver.model;

public class Attachment {

    private int id;
    private int emailId;
    private String filename;
    private int fileSize;
    private String contentType;
    private byte[] fileData;

    public Attachment() {}

    public Attachment(String filename, int fileSize,
                      String contentType, byte[] fileData) {
        this.filename = filename;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.fileData = fileData;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmailId() { return emailId; }
    public void setEmailId(int emailId) { this.emailId = emailId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public int getFileSize() { return fileSize; }
    public void setFileSize(int fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public String getReadableSize() {
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024));
    }
}