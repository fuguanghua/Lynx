package me.fuguanghua.net.utils.logback.property;

import ch.qos.logback.core.PropertyDefinerBase;
import me.fuguanghua.net.Lynx;

public class NodeIdProperty extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        return Lynx.call().getNodeId();
    }
}