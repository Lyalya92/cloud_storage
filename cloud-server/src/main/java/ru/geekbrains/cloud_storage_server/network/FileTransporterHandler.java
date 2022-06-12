package ru.geekbrains.cloud_storage_server.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.geekbrains.cloud_storage_common.model.ServerResponse;
import ru.geekbrains.cloud_storage_common.model.TransportedFile;
import ru.geekbrains.cloud_storage_server.entity.ListOfUsers;
import ru.geekbrains.cloud_storage_server.entity.User;

import java.io.*;
import java.util.Arrays;

public class FileTransporterHandler extends SimpleChannelInboundHandler<TransportedFile> {
    private static final long MAX_OBJECT_SIZE = 1024*1024*10;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TransportedFile msg)  {

        long freeMemory = 500*1024*1024 - ListOfUsers.getUsedMemory(ListOfUsers.getUser(ctx));
        if (freeMemory < msg.getFileSize()) {
            ctx.writeAndFlush(new ServerResponse(Arrays.asList("Недостаточно свободного места!")));
        }
        if (msg.getFileSize() > MAX_OBJECT_SIZE) {
            ctx.writeAndFlush(new ServerResponse(Arrays.asList("Размер файла не должен превышать 10 Мб")));
        } else {
            try {
                var response =  writeToFile(ListOfUsers.getUser(ctx), msg);
                if (response != null) ctx.writeAndFlush(new ServerResponse(Arrays.asList(response)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ctx.writeAndFlush(new ServerResponse(Arrays.asList("Файл загружен на сервер")));
    }

    public String writeToFile(User user, TransportedFile msg) throws IOException {
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
        return null;
    };
}
