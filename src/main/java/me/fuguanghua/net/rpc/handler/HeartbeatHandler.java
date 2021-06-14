package me.fuguanghua.net.rpc.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.rpc.client.NettyRpcClient;
import me.fuguanghua.net.rpc.packet.HeartbeatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class HeartbeatHandler extends BaseChannelHandler<HeartbeatPacket> {
    private static Logger LOGGER = LoggerFactory.getLogger(HeartbeatHandler.class);

    Lynx lynx;
    NettyRpcClient client;

    /**
     * use by server
     */
    public HeartbeatHandler() {
        super(HeartbeatPacket.class);
    }

//    /**
//     * use by client
//     * @param litchi
//     * @param client
//     */
//    public HeartbeatHandler(litchi litchi, NettyRpcClient client) {
//        super(HeartbeatPacket.class);
//        this.litchi = litchi;
//        this.client = client;
//
//        litchi.schedule().addEverySecond(() -> {
//            if (client.isConnect()) {
//                HeartbeatPacket packet = new HeartbeatPacket(litchi.currentNode().getNodeType(), litchi.currentNode().getNodeId());
//                client.writeRpcPacket(packet);
//            }
//        }, 30);
//    }

    @Override
    protected void onChannelRead(ChannelHandlerContext ctx, HeartbeatPacket packet) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("receive heartbeat from ... {}", packet);
        }
    }
}
