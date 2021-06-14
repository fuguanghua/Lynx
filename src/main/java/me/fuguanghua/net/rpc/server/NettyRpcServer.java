package me.fuguanghua.net.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.components.NetComponent;
import me.fuguanghua.net.rpc.codec.RpcDecoder;
import me.fuguanghua.net.rpc.codec.RpcEncoder;
import me.fuguanghua.net.rpc.handler.AppEventHandler;
import me.fuguanghua.net.rpc.handler.HeartbeatHandler;
import me.fuguanghua.net.rpc.handler.RegisterNodeHandler;
import me.fuguanghua.net.rpc.handler.RequestHandler;
import me.fuguanghua.net.session.NettySession;
import me.fuguanghua.net.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyRpcServer extends NetComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRpcServer.class);

    private int port;
    private ChannelFuture channelFuture;

    //    private static EventLoopGroup workGroup = new NioEventLoopGroup();
    private static EventLoopGroup bossGroup = new NioEventLoopGroup();

    private Lynx lynx;

    public NettyRpcServer(Lynx lynx) {
        this.lynx = lynx;
        this.port = lynx.currentNode().getRpcPort();
    }

    @Override
    public String name() {
        return Constants.Net.RPC_SERVER;
    }

    @Override
    public void start() {
    }

    @Override
    public void afterStart() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline cp = channel.pipeline();
                            cp.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 0));
                            cp.addLast(new RpcDecoder());
                            cp.addLast(new RpcEncoder());
                            cp.addLast(new IdleStateHandler(0, 0, 360));
                            cp.addLast(new RegisterNodeHandler(lynx));
                            cp.addLast(new RequestHandler(lynx));
                            cp.addLast(new AppEventHandler(lynx));
                            cp.addLast(new HeartbeatHandler());
                        }
                    })
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 12000);
            //.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)

            channelFuture = bootstrap.bind(port);
            LOGGER.info("-----> RPC connector started on port {}", port);
        } catch (Exception ex) {
            LOGGER.error("{}", ex);
        }
    }

    @Override
    public void stop() {
        try {
            if (channelFuture != null) {
                channelFuture.channel().close();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public void beforeStop() {

    }

    public void push(String nodeType, String nodeId, Object msg) {
        NettySession session = lynx.nodeSessionService().getSession(nodeType, nodeId);
        if (session != null) {
            session.writeRpcPacket(msg);
        } else {
            LOGGER.warn("session not exists. nodeType:{} nodeId:{}", nodeType, nodeId);
        }
    }

    public void push(String nodeType, Object msg) {
        for (String nodeId : lynx.nodeSessionService().getNodeIdList(nodeType)) {
            push(nodeType, nodeId, msg);
        }
    }
}
