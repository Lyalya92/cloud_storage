package ru.geekbrains.cloud_storage_client.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientConfig {
    private final int PORT;
    private final String HOST;

    private ClientConfig() {
        try (InputStream in = getClass().getResourceAsStream("/client.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            PORT = Integer.parseInt(properties.getProperty("port"));
            HOST = properties.getProperty("host");
        } catch (IOException e) {
            throw new RuntimeException("Error: property file does not exist or unreadable!", e);
        }
    }

    private static class Holder {
        static ClientConfig instance = new ClientConfig();
    }

    public static ClientConfig getInstance() {
        return Holder.instance;
    }

    public int getPORT() {
        return PORT;
    }

    public String getHOST() {
        return HOST;
    }
}
