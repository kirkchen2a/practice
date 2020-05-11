package com.gupao.myspring.framework.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @Description
 * @Author chenzk27336
 * @Date 2020/5/8 11:27
 **/
@Data
public class MyAdvise {
    private Object aspect;
    private Method adviseMethod;
    private String throwName;

    public MyAdvise(Object aspect, Method adviseMethod) {
        this.aspect = aspect;
        this.adviseMethod = adviseMethod;
    }
}
