package me.fuguanghua.net.rpc.serializer;

import java.io.IOException;

public interface Serializer {

	<T> byte[] encode(T obj) throws IOException;

	<T> T decode(byte[] bytes, Class<T> clazz) throws IOException;
}
