package me.fuguanghua.net.rpc;


import me.fuguanghua.net.rpc.serializer.ProtoStuffSerializer;
import me.fuguanghua.net.rpc.serializer.Serializer;

public class RpcConfig {

	/** 请求超时 */
	public static final int RPC_TIMEOUT = 10000;

	// public static Serializer SERIALIZER = KryoSerializer.getInstance();
	// public static Serializer SERIALIZER = new Hessian2Serializer();
	private static Serializer SERIALIZER = new ProtoStuffSerializer();

	public static Serializer getSerializer() {
		return SERIALIZER;
	}
}
