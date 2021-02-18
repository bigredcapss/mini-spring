package com.lg.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * 自定义业务逻辑层注解
 * @author BigRedCaps
 * @date 2021/1/23 14:55
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LGService
{
    String value () default "";
}
