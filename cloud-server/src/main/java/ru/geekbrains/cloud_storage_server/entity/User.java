package ru.geekbrains.cloud_storage_server.entity;

import java.io.File;
import java.util.Objects;

public class User {
    private String login;
    private String password;
    private String nickname;
    private String folderPath;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getFolderPath() {
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

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if( obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        User user = (User) obj;
        return login.equals(user.getLogin()) && password.equals(user.getPassword()) &&
                (folderPath.toString()).equals(user.getFolderPath().toString());
    }

}
