package com.lg.minispringframework.annotation;

import java.lang.annotation.*;

/**
 * 自定义自动注入注解
 * @author BigRedCaps
 * @date 2021/1/23 14:32
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LGAutowired
{
    String value() default "";
}
