package me.fuguanghua.net.event.sys;

import me.fuguanghua.net.event.AppEvent;
import me.fuguanghua.net.event.SysEventKey;

public class RpcDisconnectEvent extends AppEvent {

    private String nodeType;

    private String nodeId;

    public RpcDisconnectEvent(String nodeType, String nodeId) {
        super(SysEventKey.RPC_DISCONNECT_EVENT);
        this.nodeType = nodeType;
        this.nodeId = nodeId;
    }

    @Override
    public long dispatchHash() {
        return 0;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeType() {
        return nodeType;
    }

}
