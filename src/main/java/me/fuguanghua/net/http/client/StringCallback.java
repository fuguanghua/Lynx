package me.fuguanghua.net.http.client;

public interface StringCallback {
	void completed(String content);
	void failed(Exception ex);
}
