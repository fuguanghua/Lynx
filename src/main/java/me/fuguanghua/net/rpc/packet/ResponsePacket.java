package me.fuguanghua.net.rpc.packet;

import java.util.Arrays;

public class ResponsePacket {

    public long uid;
    public short messageId;
    public String route;
    public short statusCode;
    public byte[] data;

    public ResponsePacket() {
    }

    public static ResponsePacket valueOf(long uid, short messageId, String route, short statusCode, byte[] data) {
        ResponsePacket rsp = new ResponsePacket();
        rsp.uid = uid;
        rsp.messageId = messageId;
        rsp.route = route;
        rsp.statusCode = statusCode;
        rsp.data = data;
        return rsp;
    }

    @Override
    public String toString() {
        return "Response{" +
                "messageId=" + messageId +
                ", route='" + route + '\'' +
                ", statusCode=" + statusCode +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
