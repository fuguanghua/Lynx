package me.fuguanghua.net.http.server;

import java.util.Collection;

public interface ControllerFactory {
    Collection<Class<? extends HttpController>> getControllers();
    HttpController newInstance(Class<? extends HttpController> controller);
}
