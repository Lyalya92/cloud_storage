package ru.geekbrains.cloud_storage_server.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import ru.geekbrains.cloud_storage_server.config.ServerConfig;

public class NettyServer {
    private final ServerConfig config = ServerConfig.getInstance();
    private ChannelFuture channelFuture;


    public void start() throws Exception {
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>()  {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                            new ObjectEncoder(),
                            new NettyServerHandler()
                    );
                }
            });
            channelFuture = serverBootstrap.bind(config.getPORT()).sync();
            System.out.println("Server started on port " + config.getPORT());
            channelFuture.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stop() {
        channelFuture.channel().close();
        System.out.println("Server stopped");
    }

}
