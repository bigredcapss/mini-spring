package com.lg.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * 自定义控制层注解
 * @author BigRedCaps
 * @date 2021/1/23 14:59
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface LGController
{
    String value () default "";
}
