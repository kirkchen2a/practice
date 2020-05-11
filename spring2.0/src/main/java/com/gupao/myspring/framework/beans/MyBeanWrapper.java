package com.gupao.myspring.framework.beans;

public class MyBeanWrapper {
    private Object wrapperInstance;
    private Class<?> wrappedClass;
    public MyBeanWrapper(Object instance) {
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
