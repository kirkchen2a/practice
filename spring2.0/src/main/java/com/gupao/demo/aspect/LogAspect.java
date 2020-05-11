package com.gupao.demo.aspect;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author chenzk27336
 * @Date 2020/5/8 10:03
 **/

@Slf4j
public class LogAspect {
    public void before(){
        log.info("Invoker Before Method!!!");
    }

    public void after(){
        log.info("Invoker After Method!!!");
    }

    public void afterThrowing(){
        log.info("出现异常");
    }
}
