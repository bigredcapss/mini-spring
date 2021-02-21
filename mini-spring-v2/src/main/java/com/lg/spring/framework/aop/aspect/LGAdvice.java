package com.lg.spring.framework.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author BigRedCaps
 * @date 2021/2/21 23:04
 */
@Data
public class LGAdvice
{
    private Object aspect;
    private Method adviceMethod;
    private String throwName;

    public LGAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }
}
