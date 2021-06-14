package me.fuguanghua.net.dispatch.executor;

import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.dispatch.disruptor.MessageThread;
import me.fuguanghua.net.router.RouteInfo;
import me.fuguanghua.net.rpc.packet.RequestPacket;
import me.fuguanghua.net.session.NettySession;

import java.util.List;

public class RequestPacketExecutor implements BaseExecutor {

    Lynx lynx;
    NettySession session;
    RequestPacket packet;

    public RequestPacketExecutor(Lynx lynx, NettySession session, RequestPacket packet) {
        this.lynx = lynx;
        this.session = session;
        this.packet = packet;
    }

    @Override
    public int threadId() {
        return lynx.route().getThreadId(packet.route);
    }

    @Override
    public int calcHash(List<MessageThread> list) {
        RouteInfo routeInfo = lynx.route().getRouteInfo(packet.route);
        long hash = routeInfo.instance.getThreadHash(session, packet);
        return (int) (hash % list.size());
    }

    @Override
    public void invoke() {
        RouteInfo routeInfo = lynx.route().getRouteInfo(packet.route);
        routeInfo.instance.onReceive(session, packet);
    }
}
