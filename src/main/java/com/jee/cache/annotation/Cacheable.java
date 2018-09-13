package com.jee.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.bytebuddy.matcher.FailSafeMatcher;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cacheable {

	String name() default ""; // key前缀

	String key() default ""; // redis key

	int expire() default -1; // 过期时间 默认不过期

	String condition() default ""; // el表达式

	boolean lock() default false;
}
