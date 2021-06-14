package me.fuguanghua.net.dispatch.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.dispatch.executor.BaseExecutor;
import me.fuguanghua.net.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DisruptorService {
    private static Logger LOGGER = LoggerFactory.getLogger(DisruptorService.class);

    private Lynx lynx;
    private int ringBufferSize;

    /**
     * 线程管理集合 Key:DispatchType,value:List<MessageThread>
     */
    private Map<Integer, List<MessageThread>> threadMaps = new ConcurrentHashMap<>();

    public DisruptorService() {
        this.ringBufferSize = 32768;
    }

    public DisruptorService(Lynx lynx, int ringBufferSize) {
        this.lynx = lynx;
        this.ringBufferSize = ringBufferSize;
    }

    /**
     * @param infoList thread info list
     */
    public void addThreads(List<ThreadInfo> infoList) {
        for (ThreadInfo info : infoList) {
            addThread(info.name, info.threadId, info.threadNum,info.waitStrategy);
        }
    }

    public void addThread(String name, int threadId, int threadNum, WaitStrategy waitStrategy) {
        List<MessageThread> threadList = threadMaps.getOrDefault(threadId, new ArrayList<>());
        if (!threadMaps.containsKey(threadId)) {
            threadMaps.put(threadId, threadList);
        }

        for (int t = 0; t < threadNum; t++) {
            MessageThread thread = new MessageThread(lynx, name + "-" + t, ringBufferSize, waitStrategy);
            threadList.add(thread);
        }
    }

    public void start() {
        threadMaps.forEach((key, value) -> value.forEach(thread -> {
            thread.start();
        }));
    }

    public void publish(BaseExecutor executor) {
        List<MessageThread> list = threadMaps.get(executor.threadId());
        if (list == null) {
            LOGGER.warn("thread not found. executor= {}", executor);
            return;
        }

        final int index = executor.calcHash(list);
        if (index >= list.size()) {
            throw new CoreException("executor calcHash value is bigger than list. executor= %s", executor.toString());
        }

        MessageThread thread = list.get(index);
        thread.publish(executor);
    }

    public boolean isEmpty(int threadId) {
        List<MessageThread> list = threadMaps.get(threadId);
        if (list == null) {
            return true;
        }

        for (MessageThread thread : list) {
            RingBuffer<MessageBuffer> ringBuffer = thread.getRingBuffer();
            if (ringBuffer.getBufferSize() != ringBuffer.remainingCapacity()) {
                return false;
            }
        }
        return true;
    }

}
