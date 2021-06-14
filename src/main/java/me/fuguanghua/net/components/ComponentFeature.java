package me.fuguanghua.net.components;

import me.fuguanghua.net.Lynx;

public interface ComponentFeature<T> {

    T createComponent(Lynx ph);
}
