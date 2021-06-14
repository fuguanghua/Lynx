package me.fuguanghua.net.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述服务接口信息，rpc服务器部份使用
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Rpc {

    /**
     * 默认读取@Route注解的defaultThreadId()
     * 否则以该值为准
     *
     * @return
     */
    int threadId() default -1;
}
