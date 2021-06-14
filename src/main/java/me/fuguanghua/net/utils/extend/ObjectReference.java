package me.fuguanghua.net.utils.extend;

public class ObjectReference <T> {

	/**
	 * 软引用对象 
	 */
	private T content;

	public T get() {
		return content;
	}

	public void set(T content) {
		this.content = content;
	}

}
