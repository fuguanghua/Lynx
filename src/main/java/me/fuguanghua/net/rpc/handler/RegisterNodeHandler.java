package me.fuguanghua.net.rpc.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.event.sys.RpcDisconnectEvent;
import me.fuguanghua.net.rpc.packet.RegisterNodePacket;
import me.fuguanghua.net.session.NettySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * register node server handler
 */
@Sharable
public class RegisterNodeHandler extends BaseChannelHandler<RegisterNodePacket> {
    protected static Logger LOGGER = LoggerFactory.getLogger(RegisterNodeHandler.class);
    protected static Logger RPC_LOGGER = LoggerFactory.getLogger("rpc_logger");

    Lynx lynx;

    public RegisterNodeHandler(Lynx lynx) {
        super(RegisterNodePacket.class);
        this.lynx = lynx;
    }

    @Override
    protected void onChannelRead(ChannelHandlerContext ctx, RegisterNodePacket packet) {
        NettySession session = lynx.nodeSessionService().getSession(ctx);
        lynx.nodeSessionService().addSessionNode(packet.nodeType, packet.nodeId, session);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        NettySession nettySession = new NettySession(ctx.channel());
        lynx.nodeSessionService().putSession(nettySession);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        Attribute<Long> attrId = ctx.channel().attr(NettySession.SESSION_ID);
        Long sessionId = attrId.get();
        if (sessionId == null) {
            return;
        }

        try {
            // rpc node disconnect event
            NettySession session = lynx.nodeSessionService().getSession(sessionId);
            String nodeType = session.channel().attr(NettySession.FROM_NODE_TYPE).get();
            String nodeId = session.channel().attr(NettySession.FROM_NODE_ID).get();
            lynx.event().post(new RpcDisconnectEvent(nodeType, nodeId));
        } finally {
            //remove
            lynx.nodeSessionService().removeSession(sessionId);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object e) {
        if (e instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) e;
            if (event.state() == IdleState.ALL_IDLE) {
                RPC_LOGGER.info("channel ALL_IDLE to close. {}", ctx);
                ctx.close();
            }
        }
    }

}