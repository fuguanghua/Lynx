package me.fuguanghua.net.websocket;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import me.fuguanghua.net.Lynx;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private SslContext sslCtx;
    private ChannelHandler[] handlers;

    Lynx lynx;

    public WebSocketServerInitializer(Lynx lynx, ChannelHandler... handlers) {
        this.lynx = lynx;
        this.handlers = handlers;
    }

    public WebSocketServerInitializer(Lynx lynx, SslContext sslCtx, ChannelHandler... handlers) {
        this.lynx = lynx;
        this.sslCtx = sslCtx;
        this.handlers = handlers;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }

        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler("/", null, true));

        if (handlers != null) {
            pipeline.addLast(handlers);
        }
    }
}