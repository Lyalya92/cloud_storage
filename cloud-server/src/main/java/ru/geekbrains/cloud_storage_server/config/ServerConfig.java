package ru.geekbrains.cloud_storage_server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfig {
    private final int PORT;
    private final String REGEX;
    private final int MAXIMUM_OBJECT_SIZE;

    private ServerConfig() {
        try (InputStream in = getClass().getResourceAsStream("/server.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            PORT = Integer.parseInt(properties.getProperty("port"));
            REGEX = properties.getProperty("regex");
            MAXIMUM_OBJECT_SIZE = Integer.parseInt(properties.getProperty("max_obj_size"));
        } catch (IOException e) {
            throw new RuntimeException("Error: property file does not exist or unreadable!", e);
        }
    }

    private static class Holder {
        static ServerConfig instance = new ServerConfig();
    }

    public static ServerConfig getInstance() {
        return Holder.instance;
    }

    public int getPORT() {
        return PORT;
    }

    public String getREGEX() {
        return REGEX;
    }

    public int getMaxObjSize() {
        return MAXIMUM_OBJECT_SIZE;
    }
}