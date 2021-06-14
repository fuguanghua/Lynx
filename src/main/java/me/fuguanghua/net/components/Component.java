package me.fuguanghua.net.components;

public interface Component {
    String name();
    void start();
    void afterStart();
    void beforeStop();
    void stop();
}
