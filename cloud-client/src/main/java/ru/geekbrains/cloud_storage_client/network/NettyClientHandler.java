package ru.geekbrains.cloud_storage_client.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.cloud_storage_common.model.ServiceCommand;
import ru.geekbrains.cloud_storage_common.model.TransportedFile;

import java.io.File;
import java.util.Scanner;


public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ServiceCommand("-connect",null));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String str = readFromConsole();
        sendMessage(ctx, str);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private String readFromConsole() {
        Scanner scanner = new Scanner(System.in);
        String  result = scanner.nextLine();
        return result;
    }

    private void sendMessage(ChannelHandlerContext ctx, String msg) throws Exception {
        var splitMessage = msg.split(" ");

        if (splitMessage[0].equals("-upload")) {
            File file = new File(splitMessage[1]);
            if (file.exists()) {
                // Отправляем имя и размер файла на проверку серверу
                ctx.writeAndFlush(new TransportedFile(splitMessage[1], null, file.length()));
            } else {
                System.out.println("Файл не найден");
            }
        } else if ((splitMessage[0].equals("-download"))) {
            ctx.writeAndFlush(new ServiceCommand("-download", splitMessage));
        } else {
            ctx.writeAndFlush(new ServiceCommand(splitMessage[0], splitMessage));
        }

    }
}
