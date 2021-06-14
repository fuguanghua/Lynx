package me.fuguanghua.net.event.sys;

import me.fuguanghua.net.event.SysEventKey;
import me.fuguanghua.net.event.UserEvent;

public class UserDisconnectEvent extends UserEvent {

    private String nodeId;

    public UserDisconnectEvent(long uid, String nodeId) {
        super(SysEventKey.ACTOR_DISCONNECT_EVENT, uid);
        this.nodeId = nodeId;
    }

    public String getServerId() {
        return nodeId;
    }

}
