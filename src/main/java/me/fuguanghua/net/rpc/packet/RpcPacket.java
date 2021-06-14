package me.fuguanghua.net.rpc.packet;

/**
 * rpc packet shell
 */
public class RpcPacket<T> {

    public T data;

    public RpcPacket() {
    }

    public RpcPacket(T data) {
        this.data = data;
    }
}
