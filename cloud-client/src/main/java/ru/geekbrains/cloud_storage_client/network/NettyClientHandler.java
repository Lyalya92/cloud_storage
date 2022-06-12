package ru.geekbrains.cloud_storage_client.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.cloud_storage_common.model.ServerResponse;
import ru.geekbrains.cloud_storage_common.model.ServiceCommand;
import ru.geekbrains.cloud_storage_common.model.TransportedFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ServiceCommand("-connect",null));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) return;
        if (msg instanceof ServerResponse) { ((ServerResponse) msg).getResponse().stream().forEach((s)-> System.out.println(s));}
        if (msg instanceof TransportedFile) {
            downloadFile(msg); // Получение/скачивание файла с сервера
        }
        String str = readFromConsole();
        sendMessage(ctx, str);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void sendMessage(ChannelHandlerContext ctx, String msg) throws Exception {
        var splitMessage = msg.split(" ");

        if (splitMessage[0].equals("-upload")) {
            uploadFile(ctx, splitMessage[1]);
        } else {
            ctx.writeAndFlush(new ServiceCommand(splitMessage[0], splitMessage));
        }

    }

    private void uploadFile(ChannelHandlerContext ctx, String filePath) {
        File file = new File(filePath);
        int fileSize = (int) file.length();
        if (!file.exists()) {
            System.out.println("Файл не найден");
            return;
        }
        int readBytes = 0;
        try(FileInputStream fis = new FileInputStream(file)) {
            byte [] buffer = new byte[fileSize];
            while((readBytes = fis.read(buffer))!=-1);
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

    private String readFromConsole() {
        Scanner scanner = new Scanner(System.in);
        String  result = scanner.nextLine();
        return result;
    }

    private void downloadFile(Object msg) {
        // Реализовать копирование файла с сервера

    }
}
