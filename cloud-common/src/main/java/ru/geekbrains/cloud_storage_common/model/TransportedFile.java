package ru.geekbrains.cloud_storage_common.model;

public class TransportedFile extends AbstractMessage {

    private String fileName;
    private long fileSize;
    private byte [] data;

    public TransportedFile(String fileName, byte[] bytes, long fileSize) {
        super();
        this.fileName = fileName;
        this.data = bytes;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public byte[] getData() {
        return data;
    }

}
