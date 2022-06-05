package ru.geekbrains.cloud_storage_common.model;

public class ServiceCommand extends AbstractMessage {
    private String command;
    private String [] data;

    public ServiceCommand(String command, String[] data) {
        this.command = command;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public String[] getData() {
        return data;
    }
}
