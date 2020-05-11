package com.gupao.myspring.framework.aop;

import com.gupao.myspring.framework.aop.aspect.MyAdvise;
import com.gupao.myspring.framework.aop.support.MyAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @Description
 * @Author chenzk27336
 * @Date 2020/5/8 14:42
 **/
public class MyJdkDynamicAopProxy implements InvocationHandler {
    private MyAdvisedSupport config;
    public MyJdkDynamicAopProxy(MyAdvisedSupport config) {
        this.config = config;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String, MyAdvise> advise = config.getAdvices(method,null);
        Object returnValue;
        try {
            invokeAdivce(advise.get("before"));
            returnValue = method.invoke(this.config.getTarget(),args);
            invokeAdivce(advise.get("after"));
        }catch (Exception e){
            invokeAdivce(advise.get("afterThrow"));
            throw e;
        }

        return returnValue;
    }

    private void invokeAdivce(MyAdvise advise) {
        try {
            advise.getAdviseMethod().invoke(advise.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),this.config.getTargetClass().getInterfaces(),this);
    }
}
