package ru.geekbrains.cloud_storage_server.authorization;

import ru.geekbrains.cloud_storage_server.entity.User;

import java.util.ArrayList;

public interface AuthService {
    void start();
    void stop();

    User authorizeUserByLoginAndPassword (String login, String password);
    User createNewUser(String login, String password);
    void changePassword(String login, String oldPass, String newPass);
    void deleteUser(String login, String password);
    String changeNickname(String login, String newNickname);

}
