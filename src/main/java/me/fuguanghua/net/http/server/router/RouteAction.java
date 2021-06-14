package me.fuguanghua.net.http.server.router;

import me.fuguanghua.net.http.server.HttpController;
import me.fuguanghua.net.http.server.filter.BeforeFilter;

import java.lang.reflect.Method;

public class RouteAction {

    public Class<? extends HttpController> controllerClazz;

    public Method method;

    public BeforeFilter[] actionFilters;

    public BeforeFilter[] controllerFilters;
    
    public static RouteAction valueOf(Class<? extends HttpController> clazz, Method method, BeforeFilter[] actionFilters, BeforeFilter[] controllerFilters) {
        RouteAction action = new RouteAction();
        action.controllerClazz = clazz;
        action.method = method;
        action.actionFilters = actionFilters;
        action.controllerFilters = controllerFilters;
        return action;
    }
}
