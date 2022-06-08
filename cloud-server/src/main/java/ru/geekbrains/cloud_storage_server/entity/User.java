package ru.geekbrains.cloud_storage_server.entity;

import java.io.File;

public class User {
    private String login;
    private String password;
    private String nickname;
    private File folderPath;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public File getFolderPath() {
        return folderPath;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setFolderPath(File folderPath) {
        this.folderPath = folderPath;
    }
}
