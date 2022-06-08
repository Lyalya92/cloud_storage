package ru.geekbrains.cloud_storage_client.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import ru.geekbrains.cloud_storage_client.config.ClientConfig;

public class NettyClient {
    private final ClientConfig config = ClientConfig.getInstance();
    private ChannelFuture channelFuture;


    public void start() throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(workerGroup);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                            new ObjectEncoder(),
                            new NettyClientHandler()
                    );
                }
            });

            channelFuture = clientBootstrap.connect(config.getHOST(), config.getPORT()).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void stop() {
        if (channelFuture != null) channelFuture.channel().close();
        System.out.println("Client closed connection");
    }
}
