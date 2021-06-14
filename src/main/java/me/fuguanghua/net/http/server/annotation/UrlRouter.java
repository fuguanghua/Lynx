package me.fuguanghua.net.http.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * url路由注解
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface UrlRouter {

	/**
	 * "/user"
	 * "/user/:id"
	 * @return
	 */
	String[] path() default "";

	/**
	 * 允许post
	 * @return
	 */
	boolean post() default true;

	/**
	 * 允许get
	 * @return
	 */
	boolean get() default true;
}
