package ru.geekbrains.cloud_storage_server;

import ru.geekbrains.cloud_storage_server.network.NettyServer;

public class ServerStarter {

    public static void main(String[] args) throws Exception {
        new NettyServer().start();
    }
}
