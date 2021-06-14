package me.fuguanghua.net.dispatch;

import me.fuguanghua.net.components.Component;
import me.fuguanghua.net.dispatch.executor.BaseExecutor;

/**
 * 派发器接口
 */
public interface Dispatcher extends Component {
    void publish(BaseExecutor executor);
    boolean isEmpty(int threadId);
}
