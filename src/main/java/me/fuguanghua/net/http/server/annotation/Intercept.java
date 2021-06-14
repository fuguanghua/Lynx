package me.fuguanghua.net.http.server.annotation;

import me.fuguanghua.net.http.server.filter.BeforeFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * http server拦截器
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Intercept {

    Class<? extends BeforeFilter>[] value();
}
