package me.fuguanghua.net.rpc.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.event.AppEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class AppEventHandler extends BaseChannelHandler<AppEvent> {
    private static Logger LOGGER = LoggerFactory.getLogger(AppEventHandler.class);

    Lynx lynx;

    public AppEventHandler(Lynx lynx) {
        super(AppEvent.class);
        this.lynx = lynx;
    }

    @Override
    protected void onChannelRead(ChannelHandlerContext ctx, AppEvent packet) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.info("<---------- Event  {}", packet);
        }
        lynx.event().post(packet);
    }
}
