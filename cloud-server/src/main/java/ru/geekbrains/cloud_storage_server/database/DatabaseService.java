package ru.geekbrains.cloud_storage_server.database;

import ru.geekbrains.cloud_storage_server.entity.User;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseService {

    private static Connection connection;
    private static DatabaseService instance;

    public static final String CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS cloud_users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "login TEXT NOT NULL UNIQUE," +
                    "password TEXT NOT NULL," +
                    "nickname TEXT NOT NULL UNIQUE);";

    public static final String CREATE_TABLE_FILES =
            "CREATE TABLE IF NOT EXISTS files (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "filename TEXT NOT NULL," +
                    "path TEXT NOT NULL);";

    public static final String INIT_DB =
            "INSERT INTO cloud_users (login, password, nickname) VALUES" +
                    "('login1', 'qwert', 'nick1')," +
                    "('login2', 'qwert', 'nick2')," +
                    "('login3', 'qwert', 'nick3');";

    public static final String SHOW_ALL_USERS =
            "SELECT * FROM cloud_users";

    public static final String SHOW_ALL_FILES =
            "SELECT * FROM files";

    public static final String GET_NICKNAME =
            "SELECT nickname FROM cloud_users " +
                    "WHERE login = ? AND password = ?";

    public static final String ADD_NEW_USER =
            "INSERT INTO cloud_users (login, password, nickname) VALUES (?,?,?)";

    public static final String DELETE_ALL_USERS =
            "DELETE FROM users";

    private DatabaseService() {
        try {
            connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createDB();
    }

    public static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:javadb.db");
        System.out.println("Connected to db");
    }

    public static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Disconnected from db");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        disconnect();
    }

    private void createDB() {
        try (var stmt = connection.createStatement();) {
            stmt.execute(CREATE_TABLE_USERS);
            stmt.execute(CREATE_TABLE_FILES);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseService getInstance() {
        if (instance != null) return instance;
        instance = new DatabaseService();
        return instance;
    }

    public String getNickByLoginAndPassword(String login, String password) {
        try (var ps = connection.prepareStatement(GET_NICKNAME)) {
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                var result = rs.getString("nickname");
                rs.close();
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User createNewUser(String login, String password) {
        boolean flag = true;

        String nickname = null;
        while (flag) {
            nickname = generateRandomNick();
            try (var ps = connection.prepareStatement(ADD_NEW_USER)) {
                ps.setString(1, login);
                ps.setString(2, password);
                ps.setString(3, nickname);
                int rows = ps.executeUpdate();
                if (rows == 1) {
                    flag = false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        User user = new User();
        user.setLogin(login);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setFolderPath(new File("cloud-server/storage/" + login + "/"));
        return user;
    }

    private String generateRandomNick() {
        List<String> adjectives = Arrays.asList("angry", "anxious", "attentive", "beautiful", "big", "boring", "brave",
                "bright", "busy", "calm", "careful", "clever", "cold", "confident", "cool", "curious", "dangerous", "dark",
                "dirty", "fair", "famous", "fast", "fat", "fresh", "friendly", "frightful", "funny", "gorgeous",
                "happy", "honest", "huge", "hungry", "important", "impossible", "independent", "interesting", "kind",
                "large", "little", "lonely", "loud", "lucky", "nice", "old", "perfect", "pleasant", "poor", "popular",
                "powerful", "quiet", "rich", "sad", "sensible", "slow", "small", "smooth", "strange", "strict", "strong",
                "successful", "suspicious", "sweet", "terrible", "tiny", "tired", "warm", "weird", "wise", "wonderful", "young");

        List<String> nouns = Arrays.asList("bird", "bear", "wolf", "fox", "badger", "seal", "hedgehog", "camel", "monkey",
                "giraffe", "elephant", "lion", "panda", "beetle", "crocodile", "bee", "spider", "turtle", "dinosaur",
                "snake", "wasp", "scorpion", "sparrow", "raven", "swallow", "dove", "owl", "penguin", "dog", "cat", "pig",
                "duck", "hamster", "mouse", "rabbit", "ostrich", "whale", "zebra", "shark", "fly");


        var amountOfAdj = adjectives.size();
        var amountOfNouns = nouns.size();

        return adjectives.get((int) (Math.random() * amountOfAdj)) + "_" + nouns.get((int) (Math.random() * amountOfNouns));
    }

    // Вспомогательный метод для просмотра содержимого базы
    public ArrayList <String> showAllUsers() {
        ArrayList <String> list = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(SHOW_ALL_USERS);
            while (rs.next()) {
                int id = rs.getInt(1);
                String login = rs.getString(2);
                String password = rs.getString(3);
                String nickname = rs.getString(4);
                list.add(id + " " + login + " " + password + " " + nickname);
            }

        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {

            try {
                stmt.close();
                rs.close();
            } catch (SQLException se) {
            }
        }
        return list;
    }
}