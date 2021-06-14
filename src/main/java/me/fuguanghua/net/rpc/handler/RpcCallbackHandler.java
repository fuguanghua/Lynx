package me.fuguanghua.net.rpc.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.dispatch.executor.RpcCallbackPacketExecutor;
import me.fuguanghua.net.rpc.client.RpcCallbackContext;
import me.fuguanghua.net.rpc.client.RpcFutureContext;
import me.fuguanghua.net.rpc.packet.RpcCallbackPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class RpcCallbackHandler extends BaseChannelHandler<RpcCallbackPacket> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcCallbackHandler.class);
    protected Logger RPC_LOGGER = LoggerFactory.getLogger("remote");

    private RpcFutureContext futureContext;
    private Lynx lynx;

    public RpcCallbackHandler(Lynx lynx, RpcFutureContext futureContext) {
        super(RpcCallbackPacket.class);
        this.lynx = lynx;
        this.futureContext = futureContext;
    }

    @Override
    protected void onChannelRead(ChannelHandlerContext ctx, RpcCallbackPacket packet) {
        if (RPC_LOGGER.isDebugEnabled()) {
            RPC_LOGGER.debug("<----- receive msg. {}", packet);
        }

        //TODO 这里是性能瓶颈
        RpcCallbackContext callbackContext = futureContext.getCallback(packet.sequenceId);
        if (callbackContext != null) {
            lynx.dispatch().publish(new RpcCallbackPacketExecutor(callbackContext, packet));
        } else {
            futureContext.notifyRpcMessage(packet);
        }
    }

//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
//        LOGGER.error("----- nodeType = {}, nodeId = {} connect closed.", nodeType, nodeId);
//        LOGGER.error("{}", throwable.getMessage());
//        ctx.close();
//    }
}
