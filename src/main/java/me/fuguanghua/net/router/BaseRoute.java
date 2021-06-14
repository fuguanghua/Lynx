package me.fuguanghua.net.router;

import me.fuguanghua.net.session.NettySession;

/**
 * 路由继承类请配合注解使用
 * handler类型：
 * rpc 类型：
 * event 类型:
 */
public interface BaseRoute<MSG> {

    /**
     * 获取线程池hash
     * 消息post到派发中心时，需要计算具体在哪个线程中执行
     *
     * @param packet 接收的消息
     * @return
     */
    long getThreadHash(NettySession session, MSG packet);

    /**
     * 收到消息
     *
     * @param packet 接收的消息
     */
    void onReceive(NettySession session, MSG packet);
}
