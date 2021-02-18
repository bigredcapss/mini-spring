package com.lg.spring.framework.beans;

/**
 * @author BigRedCaps
 * @date 2021/2/18 22:19
 */
public class LGBeanWrapper
{
    private Object wrapperInstance;
    private Class<?> wrappedClass;
    public LGBeanWrapper(Object instance) {
        this.wrapperInstance = instance;
        this.wrappedClass = instance.getClass();
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public Class<?> getWrappedClass() {
        return wrappedClass;
    }
}
