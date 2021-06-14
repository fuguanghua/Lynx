package me.fuguanghua.net.exception;

public class RpcException extends RuntimeException {
	private static final long serialVersionUID = 8239654991256889053L;

	public RpcException() {
		super();
	}

	public RpcException(String message) {
		super(message);
	}

	public RpcException(String message, Object... args) {
		super(String.format(message, args));
	}

	public RpcException(String message, Throwable thr) {
		super(message, thr);
	}

	public RpcException(Throwable thr) {
		super(thr);
	}

}
