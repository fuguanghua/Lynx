package me.fuguanghua.net.rpc.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.dispatch.executor.RequestPacketExecutor;
import me.fuguanghua.net.rpc.packet.RequestPacket;
import me.fuguanghua.net.session.NettySession;

@Sharable
public class RequestHandler extends BaseChannelHandler<RequestPacket> {

    private Lynx lynx;

    public RequestHandler(Lynx litchi) {
        super(RequestPacket.class);
        this.lynx = litchi;
    }

    @Override
    protected void onChannelRead(ChannelHandlerContext ctx, RequestPacket packet) {
        NettySession session = lynx.nodeSessionService().getSession(ctx);

        if (lynx.route().getRouteInfo(packet.route) == null) {
            LOGGER.warn("route not found. packet:{}", packet);
            return;
        }
        lynx.dispatch().publish(new RequestPacketExecutor(lynx, session, packet));
    }
}
