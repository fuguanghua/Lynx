package me.fuguanghua.net.rpc.packet;

import me.fuguanghua.net.utils.ServerTime;

public class HeartbeatPacket {

    public String fromNodeType;
    public String fromNodeId;
    public long time;

    public HeartbeatPacket(String nodeType, String nodeId) {
        this.fromNodeType = nodeType;
        this.fromNodeId = nodeId;
        this.time = ServerTime.timeMillis();
    }

    @Override
    public String toString() {
        return "HeartbeatPacket{" +
                "fromNodeType='" + fromNodeType + '\'' +
                ", fromNodeId='" + fromNodeId + '\'' +
                ", time=" + time +
                '}';
    }
}
