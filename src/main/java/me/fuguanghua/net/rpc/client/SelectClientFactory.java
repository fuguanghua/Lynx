package me.fuguanghua.net.rpc.client;

import me.fuguanghua.net.session.NettySession;

/**
 * 选择RPC客户端
 */
public interface SelectClientFactory<T> {

    /**
     * 根据不同类型的服务器进行不同的RpcClient选择
     *
     * @param session
     * @param request
     * @return
     */
    NettyRpcClient select(NettySession session, String nodeType, T request);
}
