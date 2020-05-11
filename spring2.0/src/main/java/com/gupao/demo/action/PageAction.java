package com.gupao.demo.action;

import com.gupao.demo.service.IQueryService;
import com.gupao.myspring.framework.annotation.MyAutowired;
import com.gupao.myspring.framework.annotation.MyController;
import com.gupao.myspring.framework.annotation.MyRequestMapping;
import com.gupao.myspring.framework.annotation.MyRequestParam;
import com.gupao.myspring.framework.webmvc.servlet.MyModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@MyController
@MyRequestMapping("/")
public class PageAction {

    @MyAutowired
    IQueryService queryService;

    @MyRequestMapping("/first.html")
    public MyModelAndView query(@MyRequestParam("teacher") String teacher){
        String result = queryService.query(teacher);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new MyModelAndView("first.html",model);
    }

}
