package me.fuguanghua.net.dispatch.executor;

import me.fuguanghua.net.dispatch.disruptor.MessageThread;

import java.util.List;

/**
 * dispatch to target thread executor
 */
public interface BaseExecutor {
    int threadId();
    int calcHash(List<MessageThread> list);
    void invoke();
}
