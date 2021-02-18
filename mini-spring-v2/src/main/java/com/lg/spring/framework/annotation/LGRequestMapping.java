package com.lg.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * 自定义请求映射注解
 * @author BigRedCaps
 * @date 2021/1/23 14:57
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LGRequestMapping
{
    String value () default "";
}
