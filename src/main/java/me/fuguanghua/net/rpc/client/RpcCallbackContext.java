package me.fuguanghua.net.rpc.client;

public class RpcCallbackContext {

    public int threadId;

    public long hash;

    public RpcCallback rpcCallback;

    public RpcCallbackContext(int threadId, long hash, RpcCallback rpcCallback) {
        this.threadId = threadId;
        this.hash = hash;
        this.rpcCallback = rpcCallback;
    }
}
