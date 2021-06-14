package me.fuguanghua.net.rpc.packet;

import me.fuguanghua.net.utils.StringUtils;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class RequestPacket {
    private static AtomicLong sequenceBuilder = new AtomicLong();

    public long uid;

    public long sequenceId;

    public short messageId;

    /**
     * nodeType.className.methodName
     */
    public String route = "";

    /**
     * parameters arrays
     */
    public Object[] args;

    public long buildTime;

    public short statusCode;

    public RequestPacket() {
        this.sequenceId = sequenceBuilder.getAndIncrement();
        this.buildTime = System.currentTimeMillis();
    }

    public static RequestPacket valueOfRPC(String nodeType, String className, String methodName, Object[] args) {
        RequestPacket request = new RequestPacket();
        request.route = String.join(".", nodeType, className, methodName);
        request.args = args;
        return request;
    }

    public boolean validateRoute() {
        if (StringUtils.isBlank(this.route)) {
            return false;
        }

        if (this.route.split("\\.").length != 3) {
            return false;
        }
        return true;
    }

    public Object getArgs(int index) {
        if (this.args == null) {
            return null;
        }
        if (this.args.length < index) {
            return null;
        }
        return this.args[index];
    }

    public ResponsePacket toResponsePacket(byte[] data) {
        final ResponsePacket packet = new ResponsePacket();
        packet.uid = this.uid;
        packet.messageId = this.messageId;
        packet.route = this.route;
        packet.statusCode = this.statusCode;
        packet.data = data;
        return packet;
    }

    /**
     * 目标执行的服务器类型
     *
     * @return
     */
    public String nodeType() {
        String[] routeIds = route.split("\\.");
        return routeIds[0];
    }

    @Override
    public String toString() {
        return "RequestPacket{" +
                "uid=" + uid +
                ", sequenceId=" + sequenceId +
                ", messageId=" + messageId +
                ", route='" + route + '\'' +
                ", args=" + Arrays.toString(args) +
                ", buildTime=" + buildTime +
                ", statusCode=" + statusCode +
                '}';
    }
}
