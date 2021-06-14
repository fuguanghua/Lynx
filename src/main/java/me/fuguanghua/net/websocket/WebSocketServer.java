package me.fuguanghua.net.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.components.NetComponent;
import me.fuguanghua.net.exception.CoreException;
import me.fuguanghua.net.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class WebSocketServer extends NetComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);

    Lynx lynx;
    private int port;

    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private ChannelInitializer<SocketChannel> channelInitializer;

    public WebSocketServer(Lynx lynx) {
        this.lynx = lynx;
        this.port = lynx.currentNode().getPort();
        this.channelInitializer = new WebSocketServerInitializer(
                lynx,
                new WebSocketDecoder(),
                new WebSocketEncoder(),
                new WebSocketServerHandler(lynx)
        );
    }

    public WebSocketServer(Lynx lynx, ChannelHandler... handlers) {
        this(lynx, new WebSocketServerInitializer(lynx, handlers));
    }

    public WebSocketServer(Lynx lynx, SslContext sslContext, ChannelHandler... handlers) {
        this(lynx, new WebSocketServerInitializer(lynx, sslContext, handlers));
    }

    public WebSocketServer(Lynx lynx, ChannelInitializer<SocketChannel> channelInitializer) {
        this.lynx = lynx;
        this.port = lynx.currentNode().getPort();
        this.channelInitializer = channelInitializer;
    }

    @Override
    public void start() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer);

            Channel ch = bootstrap.bind(port).sync().channel();
            LOGGER.info(" ------> url : ws://127.0.0.1:{}", port);
            new Thread(() -> {
                try {
                    ch.closeFuture().sync();
                } catch (InterruptedException e) {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            }).start();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public void beforeStop() {

    }

    @Override
    public String name() {
        return Constants.Net.WEB_SOCKET_SERVER;
    }

    @Override
    public void afterStart() {

    }

    /**
     * @param filePath *.pem file
     * @param keyPath  *.key file
     * @return
     */
    public static SslContext createSSLContext(String filePath, String keyPath) {
        try {
            File file = new File(filePath);
            File key = new File(keyPath);

            SslContext sslCtx = SslContextBuilder.forServer(file, key).build();
            return sslCtx;
        } catch (Exception ex) {
            throw new CoreException("ssl context create fail. ", ex);
        }
    }
}