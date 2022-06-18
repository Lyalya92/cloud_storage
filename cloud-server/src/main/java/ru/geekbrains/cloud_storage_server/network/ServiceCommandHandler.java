package ru.geekbrains.cloud_storage_server.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.geekbrains.cloud_storage_common.model.ServiceCommand;
import ru.geekbrains.cloud_storage_server.messages.MessageHandler;

public class ServiceCommandHandler extends SimpleChannelInboundHandler<ServiceCommand> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServiceCommand msg) throws Exception {
        if (msg == null) return;
        MessageHandler.handleServiceCommand(ctx, msg);
    }
}
