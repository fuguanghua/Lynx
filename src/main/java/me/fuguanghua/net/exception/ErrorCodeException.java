package me.fuguanghua.net.exception;

/**
 * 错误码异常，用于逻辑中的错误信息返回
 */
public class ErrorCodeException extends RuntimeException {

	private static final long serialVersionUID = 1997284649430444277L;
	
	private short code;
	private Object info;
	
	public ErrorCodeException(short code) {
		super(String.valueOf(code));
		this.code = code;
	}
	
	public ErrorCodeException(short code, Object info) {
		super(String.valueOf(code));
		this.code = code;
		this.info = info;
	}

	public ErrorCodeException(short code, String text, Object... args) {
		super(String.valueOf(code));
		this.code = code;
		this.info = String.format(text, args);
	}
	
	public short getCode() {
		return code;
	}
	
	public Object getInfo() {
		return info;
	}

}