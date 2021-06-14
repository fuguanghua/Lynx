package me.fuguanghua.net.rpc.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.rpc.packet.ResponsePacket;
import me.fuguanghua.net.session.GateSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class ResponseToClientHandler extends BaseChannelHandler<ResponsePacket> {
    private static Logger LOGGER = LoggerFactory.getLogger(ResponseToClientHandler.class);

    private Lynx lynx;

    public ResponseToClientHandler(Lynx lynx) {
        super(ResponsePacket.class);
        this.lynx = lynx;
    }

    @Override
    protected void onChannelRead(ChannelHandlerContext ctx, ResponsePacket packet) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<--- packet={}", packet);
        }

        GateSession session = lynx.sessionService().getOnlineSession(packet.uid);
        if (session != null) {
            session.writeToClient(packet);
        }
    }
}
