package me.fuguanghua.net.event.annotation;

import java.lang.annotation.*;

/**
 * receive event annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface EventReceive {

    /**
     * 事件名
     *
     * @return
     */
    String[] name();

    /**
     * 执行的线程池id
     *
     * @return
     */
    int threadId() default -1;

}
