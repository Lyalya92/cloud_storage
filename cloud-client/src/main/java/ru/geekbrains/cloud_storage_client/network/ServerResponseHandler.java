package ru.geekbrains.cloud_storage_client.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.geekbrains.cloud_storage_common.model.ClientRequest;
import ru.geekbrains.cloud_storage_common.model.ServerResponse;

public class ServerResponseHandler extends SimpleChannelInboundHandler<ServerResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerResponse msg) {
        msg.getResponse().stream().forEach((s)-> System.out.println(s));
        ctx.fireChannelRead(new ClientRequest());
    }

}
