package me.fuguanghua.net.dispatch.disruptor;

import com.lmax.disruptor.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandler<T> implements ExceptionHandler<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    @Override
    public void handleEventException(Throwable ex, long sequence, T event) {
        LOGGER.error("{}", ex);
        LOGGER.error("buffer = {}", event);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        LOGGER.error("{}", ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        LOGGER.error("{}", ex);
    }
}
