package me.fuguanghua.net.dispatch.executor;

import me.fuguanghua.net.dispatch.disruptor.MessageThread;
import me.fuguanghua.net.rpc.client.RpcCallbackContext;
import me.fuguanghua.net.rpc.packet.RpcCallbackPacket;

import java.util.List;

public class RpcCallbackPacketExecutor implements BaseExecutor {

    RpcCallbackPacket packet;

    RpcCallbackContext callbackContext;

    public RpcCallbackPacketExecutor(RpcCallbackContext callbackContext, RpcCallbackPacket packet) {
        this.callbackContext = callbackContext;
        this.packet = packet;
    }

    @Override
    public int threadId() {
        return callbackContext.threadId;
    }

    @Override
    public int calcHash(List<MessageThread> list) {
        return (int) (callbackContext.hash % list.size());
    }

    @Override
    public void invoke() {
        callbackContext.rpcCallback.result(packet.result);
    }
}
