package ru.geekbrains.cloud_storage_server.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.cloud_storage_common.model.AbstractMessage;
import ru.geekbrains.cloud_storage_server.messages.MessageHandler;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) return;
        MessageHandler.handleMessage(ctx, (AbstractMessage) msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
