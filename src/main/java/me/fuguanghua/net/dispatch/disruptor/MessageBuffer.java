package me.fuguanghua.net.dispatch.disruptor;

import me.fuguanghua.net.dispatch.executor.BaseExecutor;

public class MessageBuffer {

    private BaseExecutor executor;

    public void setExecutor(BaseExecutor executor) {
        this.executor = executor;
    }

    public void execute() {
        this.executor.invoke();
    }

    public void clear() {
        this.executor = null;
    }

    @Override
    public String toString() {
        return "MessageBuffer{" +
                "executor=" + executor +
                '}';
    }
}
