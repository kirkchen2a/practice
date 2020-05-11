package com.gupao.demo.service.impl;

import com.gupao.demo.service.IDemoService;
import com.gupao.myspring.framework.annotation.MyService;

@MyService
public class DemoService implements IDemoService {
    public String get(String name) {
        return "My name is " + name + " from service.";
    }
}
