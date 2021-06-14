package me.fuguanghua.net.rpc.packet;

/**
 * 结点注册数据包
 *
 */
public class RegisterNodePacket {

    /**
     * 结点类型
     */
    public String nodeType;

    /**
     * 结点id
     */
    public String nodeId;

    @Override
    public String toString() {
        return "RegisterNodePacket{" +
                "nodeType='" + nodeType + '\'' +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }
}
