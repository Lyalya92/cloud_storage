package ru.geekbrains.cloud_storage_common.model;

import java.util.List;

public class ServerResponse extends AbstractMessage {

    List<String> response;

    public ServerResponse() {

    }

    public ServerResponse(List<String> response) {
        super();
        this.response = response;
    }

    public List<String> getResponse() {
        return response;
    }

    public void setResponse(List<String> response) {
        this.response = response;
    }
}
