package me.fuguanghua.net.dispatch.executor;

import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.dispatch.disruptor.MessageThread;
import me.fuguanghua.net.event.AppEvent;

import java.util.List;

public class AppEventExecutor implements BaseExecutor {

    Lynx lynx;
    AppEvent event;

    public AppEventExecutor(Lynx lynx, AppEvent event) {
        this.lynx = lynx;
        this.event = event;
    }

    @Override
    public int threadId() {
        return event.threadId;
    }

    @Override
    public int calcHash(List<MessageThread> list) {
        return (int) this.event.dispatchHash() % list.size();
    }

    @Override
    public void invoke() {
        lynx.event().execute(event);
    }
}
