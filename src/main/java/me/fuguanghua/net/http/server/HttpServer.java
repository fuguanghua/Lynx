package me.fuguanghua.net.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.components.NetComponent;
import me.fuguanghua.net.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HttpServer extends NetComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    private NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    private int port;

    private List<ChannelHandler> handlerList = new ArrayList<>();
    
    private int maxContentLength;

    private static int DEFAULT_MAX_CONTENT_LENGTH = 1024 * 1024 * 10;

    public HttpServer(Lynx lynx) {
        this.port = lynx.currentNode().getPort();
        this.maxContentLength = lynx.currentNode().getIntOpts("httpMaxContentLength", DEFAULT_MAX_CONTENT_LENGTH);
    }

    @Override
    public String name() {
        return Constants.Net.HTTP_SERVER;
    }

    @Override
    public void start() {
    }

    public void addHandler(ChannelHandler... handlers) {
        for (ChannelHandler handler : handlers) {
            handlerList.add(handler);
        }
    }

    @Override
    public void afterStart() {
        try {
            if (handlerList.isEmpty()) {
                throw new Exception("ChannelHandler is null");
            }

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                    .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                    .option(ChannelOption.SO_BACKLOG, 12000).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast("aggregator", new HttpObjectAggregator(maxContentLength));
                            for (ChannelHandler handler : handlerList) {
                                ch.pipeline().addLast(handler);
                            }
                        }
                    });

            b.bind(port);
            LOGGER.info("-----> connector started: http://127.0.0.1:{}/", port);
        } catch (Exception e) {
            LOGGER.error("{}", e);
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
}
