package me.fuguanghua.net.exception;

public class CoreException extends RuntimeException {
    public CoreException(String text, Object... args) {
        super(String.format(text, args));
    }
}
