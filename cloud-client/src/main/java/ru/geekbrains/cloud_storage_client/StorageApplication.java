package ru.geekbrains.cloud_storage_client;

import ru.geekbrains.cloud_storage_client.network.NettyClient;

public class StorageApplication  {
    public static void main(String[] args) throws Exception{
        new NettyClient().start();
    }
}
