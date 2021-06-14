package me.fuguanghua.net.dispatch;

import com.lmax.disruptor.WaitStrategy;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.dispatch.disruptor.DisruptorService;
import me.fuguanghua.net.dispatch.disruptor.ThreadInfo;
import me.fuguanghua.net.dispatch.executor.BaseExecutor;
import me.fuguanghua.net.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 默认的消息派发中心实现
 */
public class DispatcherComponent implements Dispatcher {
    private static Logger LOGGER = LoggerFactory.getLogger(DispatcherComponent.class);

    DisruptorService disruptorService;

    protected Lynx lynx;

    public DispatcherComponent(Lynx lynx, List<ThreadInfo> threadList) {
        this(lynx, 65536, threadList);
    }

    public DispatcherComponent(Lynx lynx, int ringBufferSize, List<ThreadInfo> threadList) {
        this.lynx = lynx;
        this.disruptorService = new DisruptorService(lynx, ringBufferSize);
        this.disruptorService.addThreads(threadList);
    }

    @Override
    public String name() {
        return Constants.Component.DISPATCH;
    }

    @Override
    public void start() {
        this.disruptorService.start();
        LOGGER.info("dispatch init complete!");
    }

    @Override
    public void afterStart() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void beforeStop() {

    }

    @Override
    public void publish(BaseExecutor executor) {
        this.disruptorService.publish(executor);
    }

    @Override
    public boolean isEmpty(int threadId) {
        return this.disruptorService.isEmpty(threadId);
    }

    public void addThread(String name, int threadId, int threadNum, WaitStrategy waitStrategy) {
        this.disruptorService.addThread(name, threadId, threadNum, waitStrategy);
    }
}
