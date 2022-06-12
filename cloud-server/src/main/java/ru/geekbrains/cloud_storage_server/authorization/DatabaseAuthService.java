package ru.geekbrains.cloud_storage_server.authorization;

import ru.geekbrains.cloud_storage_server.database.DatabaseService;
import ru.geekbrains.cloud_storage_server.entity.User;

import java.util.ArrayList;

public class DatabaseAuthService implements AuthService {

    private DatabaseService databaseService;

    @Override
    public void start() {
        databaseService = DatabaseService.getInstance();
    }

    @Override
    public void stop() {
        databaseService.close();
    }

    @Override
    public User authorizeUserByLoginAndPassword(String login, String password) {
        String nickname = databaseService.getNickByLoginAndPassword(login, password);
        if (nickname!=null) {
            User user = new User();
            user.setLogin(login);
            user.setPassword(password);
            user.setNickname(nickname);
            return user;
        } else {
            return null; // добавить здесь Exception
        }

    }

    @Override
    public User createNewUser(String login, String password) {
        return databaseService.createNewUser(login, password);
    }

    @Override
    public void changePassword(String login, String oldPass, String newPass) {

    }

    @Override
    public void deleteUser(String login, String password) {

    }

    @Override
    public String changeNickname(String login, String newNickname) {
        return null;
    }

    @Override
    public ArrayList<String> showAllUsers() {
        return databaseService.showAllUsers();
    }
}
