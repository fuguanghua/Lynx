package me.fuguanghua.net.dispatch.disruptor;

import com.lmax.disruptor.EventHandler;
import me.fuguanghua.net.Lynx;

public class MessageEventHandler implements EventHandler<MessageBuffer> {

    Lynx lynx;

    public MessageEventHandler(Lynx lynx) {
        this.lynx = lynx;
    }

    @Override
    public void onEvent(MessageBuffer buffer, long sequence, boolean endOfBatch) throws Exception {
        try {
            buffer.execute();
        } catch (Exception ex) {
            throw ex;
        } finally {
            buffer.clear();
        }
    }
}
