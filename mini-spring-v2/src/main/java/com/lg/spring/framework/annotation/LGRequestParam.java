package com.lg.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * 自定义请求参数注解
 * @author BigRedCaps
 * @date 2021/1/23 15:01
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LGRequestParam
{
    String value () default "";
}
