package me.fuguanghua.net.http.server.filter;

import me.fuguanghua.net.http.server.HttpController;
import java.lang.reflect.Method;

public interface BeforeFilter {
    boolean execute(HttpController controller, Method method);
}
