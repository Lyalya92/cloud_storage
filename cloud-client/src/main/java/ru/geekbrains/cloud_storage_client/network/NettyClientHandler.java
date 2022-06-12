package ru.geekbrains.cloud_storage_client.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.cloud_storage_common.model.ServerResponse;
import ru.geekbrains.cloud_storage_common.model.ServiceCommand;

import java.util.List;
import java.util.Scanner;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ServiceCommand("-connect",null));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) return;
        List<String> message = ((ServerResponse) msg).getResponse();
        for (String s: message) {
            System.out.println(s);
        }
        String str = readFromConsole();
        sendMessage(ctx, str);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public void sendMessage(ChannelHandlerContext ctx, String msg) {
        var splitMessage = msg.split(" ");
        ctx.writeAndFlush(new ServiceCommand(splitMessage[0], splitMessage));
    }

    public String readFromConsole() {
        Scanner scanner = new Scanner(System.in);
        String  result = scanner.nextLine();
        return result;
    }
}
