package com.gupao.demo.mvc.action;

import com.gupao.demo.service.IDemoService;
import com.gupao.myspring.framework.annotation.MyAutowired;
import com.gupao.myspring.framework.annotation.MyController;
import com.gupao.myspring.framework.annotation.MyRequestMapping;
import com.gupao.myspring.framework.annotation.MyRequestParam;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




//虽然，用法一样，但是没有功能
@MyController
@MyRequestMapping("/demo")
public class DemoAction {

  	@MyAutowired
	private IDemoService demoService;

	@MyRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @MyRequestParam("name") String name){
		String result = demoService.get(name);
//		String result = "My name is " + name;
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MyRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@MyRequestParam("a") Integer a, @MyRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MyRequestMapping("/sub")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@MyRequestParam("a") Double a, @MyRequestParam("b") Double b){
		try {
			resp.getWriter().write(a + "-" + b + "=" + (a - b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MyRequestMapping("/remove")
	public String  remove(@MyRequestParam("id") Integer id){
		return "" + id;
	}

}
