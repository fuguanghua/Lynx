package me.fuguanghua.net.rpc.client;

public interface RpcCallback<R> {

	/**
	 * 回调结果
	 * @param result
	 */
	void result(R result);

}
