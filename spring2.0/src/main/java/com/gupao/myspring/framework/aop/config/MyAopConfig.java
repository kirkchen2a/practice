package com.gupao.myspring.framework.aop.config;

import lombok.Data;

/**
 * @Description
 * @Author chenzk27336
 * @Date 2020/5/8 10:36
 **/
@Data
public class MyAopConfig {
    private String pointCut;
    private String aspectClass;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;
}
