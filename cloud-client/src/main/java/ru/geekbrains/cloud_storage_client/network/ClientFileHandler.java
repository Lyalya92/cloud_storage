package ru.geekbrains.cloud_storage_client.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.geekbrains.cloud_storage_common.model.ClientRequest;
import ru.geekbrains.cloud_storage_common.model.TransportedFile;
import ru.geekbrains.cloud_storage_server.entity.ListOfUsers;
import ru.geekbrains.cloud_storage_server.entity.User;

import java.io.*;

public class ClientFileHandler extends SimpleChannelInboundHandler<TransportedFile> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TransportedFile msg) throws Exception {
        if (msg.getData() == null) {
            // Если сервер готов принять файл, отправляем его
            uploadFile(ctx, msg.getFileName());
        } else {
            writeToFile(ListOfUsers.getUser(ctx), msg);
            System.out.println("Файл скачан с сервера");
            ctx.fireChannelRead(new ClientRequest());
        }
    }

    private void uploadFile(ChannelHandlerContext ctx, String filePath) {
        File file = new File(filePath);
        int fileSize = (int) file.length();
        if (!file.exists()) {
            System.out.println("Файл не найден");
            return;
        }
        try(FileInputStream fis = new FileInputStream(file)) {
            byte [] buffer = new byte[fileSize];
            fis.read(buffer);
            sendFileToServer(buffer, ctx, file.getName(), fileSize);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFileToServer(byte[] bytes, ChannelHandlerContext ctx, String fileName, long fileSize) {
        ctx.writeAndFlush(new TransportedFile(fileName, bytes, fileSize));
    }

    public void writeToFile(User user, TransportedFile msg) throws IOException {
        String fileName = msg.getFileName();
        String folder = "C:\\Users\\Lyalya\\Desktop\\_test\\";
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
