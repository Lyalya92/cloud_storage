package ru.geekbrains.cloud_storage_server.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.geekbrains.cloud_storage_common.model.ServerResponse;
import ru.geekbrains.cloud_storage_common.model.TransportedFile;
import ru.geekbrains.cloud_storage_server.entity.ListOfUsers;
import ru.geekbrains.cloud_storage_server.entity.User;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class ServerFileHandler extends SimpleChannelInboundHandler<TransportedFile> {
    private static final long MAX_OBJECT_SIZE = 1024*1024*10;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TransportedFile msg)  {
        if (msg.getData() == null) {
            // Если получаем только заголовок файла, без данных, то проверяем можно ли его загрузить
            var response = checkFile(ctx, msg);
            if (response != null) {
                ctx.writeAndFlush(new ServerResponse(Arrays.asList(response)));
            } else {
                // Кидаем заголовок файла обратно в подтверждение готовности принять сам файл
                ctx.writeAndFlush(new TransportedFile(msg.getFileName(), null, msg.getFileSize()));
            }
        } else {
            // Если объект содержит байтовый массив с данными, пишем их в соответственный файл
            try {
                writeToFile(ListOfUsers.getUser(ctx), msg);
                ctx.writeAndFlush(new ServerResponse(Arrays.asList("Файл загружен на сервер")));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private String checkFile(ChannelHandlerContext ctx, TransportedFile msg) {
        long freeMemory = 500*1024*1024 - ListOfUsers.getUsedMemory(ListOfUsers.getUser(ctx));
        if (freeMemory < msg.getFileSize()) {
            return "Недостаточно свободного места!";
        }
        if (msg.getFileSize() > MAX_OBJECT_SIZE) {
            return "Размер файла не должен превышать 10 Мб";
        }
        File [] files = new File(ListOfUsers.getUser(ctx).getFolderPath()).listFiles();
        for (File f: files) {
            if (f.getName().equals(new File(msg.getFileName()).getName())) {
                return "Файл с таким именем уже существует";
            }
        }
        return null;
    }

    public void writeToFile(User user, TransportedFile msg) throws IOException {
        String fileName = msg.getFileName();
        String folder = user.getFolderPath();
        File file = new File(folder + fileName);

        if (!file.exists()) {
            file.createNewFile();
        }
        try(FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write(msg.getData(), 0, msg.getData().length);
            fos.flush();
        }
    }
}
