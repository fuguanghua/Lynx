package me.fuguanghua.net.session;

import io.netty.channel.Channel;
import me.fuguanghua.net.utils.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ChannelUtils {

    public static String getRemoteIp(Channel channel) {
        String remoteIp = "";
        SocketAddress add = channel.remoteAddress();
        if (add != null) {
            remoteIp = ((InetSocketAddress) add).getAddress().getHostAddress();
        }
        if (StringUtils.isBlank(remoteIp)) {
            remoteIp = ((InetSocketAddress) channel.localAddress()).getAddress().getHostAddress();
        }
        return remoteIp;
    }
}
