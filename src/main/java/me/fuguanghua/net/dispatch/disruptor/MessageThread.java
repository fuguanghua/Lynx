package me.fuguanghua.net.dispatch.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.dispatch.executor.BaseExecutor;
import me.fuguanghua.net.utils.threads.NamedThreadFactory;

public class MessageThread {

    private Disruptor<MessageBuffer> disruptor;

    public MessageThread(Lynx lynx, String threadName, int bufferSize, WaitStrategy waitStrategy) {
        this(threadName, bufferSize, new MessageEventHandler(lynx), waitStrategy);
    }

    public MessageThread(String threadName, int bufferSize, EventHandler<MessageBuffer> handler, WaitStrategy waitStrategy) {
        this.disruptor = new Disruptor<>(
                () -> new MessageBuffer(),
                bufferSize,
                new NamedThreadFactory(threadName),
                ProducerType.MULTI,
                waitStrategy
        );

        disruptor.handleEventsWith(handler);
        disruptor.setDefaultExceptionHandler(new ErrorHandler<>());
    }

    public void start() {
        this.disruptor.start();
    }

    public void publish(BaseExecutor executor) {
        final long seq = getRingBuffer().next();
        final MessageBuffer buffer = getRingBuffer().get(seq);
        buffer.setExecutor(executor);
        this.disruptor.getRingBuffer().publish(seq);
    }

    public RingBuffer<MessageBuffer> getRingBuffer() {
        return this.disruptor.getRingBuffer();
    }

}
