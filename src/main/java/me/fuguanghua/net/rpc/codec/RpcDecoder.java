package me.fuguanghua.net.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.fuguanghua.net.rpc.RpcConfig;
import me.fuguanghua.net.rpc.packet.RpcPacket;

import java.util.List;

/**
 * decode rpc packet
 */
public class RpcDecoder extends ByteToMessageDecoder {
	
	private Class<?> genericClass;

	public RpcDecoder() {
		this.genericClass = RpcPacket.class;
	}

	public RpcDecoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < 4) {
			return;
		}
		in.markReaderIndex();
		int dataLength = in.readInt();

		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}
		byte[] data = new byte[dataLength];
		in.readBytes(data);

		Object obj = RpcConfig.getSerializer().decode(data, genericClass);
		out.add(obj);
	}

}
