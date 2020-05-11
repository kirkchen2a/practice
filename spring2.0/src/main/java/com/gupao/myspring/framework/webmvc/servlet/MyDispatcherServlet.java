package com.gupao.myspring.framework.webmvc.servlet;

import com.gupao.myspring.framework.annotation.*;
import com.gupao.myspring.framework.context.MyApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyDispatcherServlet extends HttpServlet {
    private MyApplicationContext applicationContext;
    
    private List<MyHandlerMapping> handlerMappings = new ArrayList<MyHandlerMapping>();

    private Map<MyHandlerMapping,MyHandlerAdapter> handlerAdapters = new HashMap<MyHandlerMapping, MyHandlerAdapter>();

    private List<MyViewResolver> viewResolvers = new ArrayList<MyViewResolver>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6、调用，运行阶段
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            try {
                processDispatchResult(req,resp,new MyModelAndView("500"));
            } catch (Exception e1) {
                e1.printStackTrace();
                resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化Spring核心IoC容器
        applicationContext = new MyApplicationContext(config.getInitParameter("contextConfigLocation"));

        //==============MVC部分==============
        //初始化九大组件
        initStrategies(applicationContext);

        System.out.println("My Spring framework is init.");
    }

    private void initStrategies(MyApplicationContext context) {
        //        //多文件上传的组件
//        initMultipartResolver(context);
//        //初始化本地语言环境
//        initLocaleResolver(context);
//        //初始化模板处理器
//        initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
//        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
//        //初始化视图预处理器
//        initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
//        //FlashMap管理器
//        initFlashMapManager(context);
    }

    private void initViewResolvers(MyApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File templateRootDir = new File(templateRootPath);
        for(File file : templateRootDir.listFiles()){
            this.viewResolvers.add(new MyViewResolver(templateRoot));
        }
    }

    private void initHandlerAdapters(MyApplicationContext context) {
        for(MyHandlerMapping handlerMapping : this.handlerMappings){
            this.handlerAdapters.put(handlerMapping,new MyHandlerAdapter());
        }
    }

    private void initHandlerMappings(MyApplicationContext context) {
        if(this.applicationContext.getBeanDefinitionCount() == 0){ return;}
        for (String beanName : this.applicationContext.getBeanDefinitionNames()) {
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();
            if(!clazz.isAnnotationPresent(MyController.class)){ continue; }
            //相当于提取 class上配置的url
            String baseUrl = "";
            if(clazz.isAnnotationPresent(MyRequestMapping.class)){
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = requestMapping.value();
            }
            //只获取public的方法
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(MyRequestMapping.class)){continue;}
                //提取每个方法上面配置的url
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);

                // //demo//query
                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                //handlerMapping.put(url,method);
                handlerMappings.add(new MyHandlerMapping(pattern,method,instance));
                System.out.println("Mapped : " + regex + "," + method);
            }
        }
    }
    

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        //1、通过URL获得一个HandlerMapping
        MyHandlerMapping handler = getHandler(req);
        if(handler == null){
            processDispatchResult(req,resp,new MyModelAndView("404"));
            return;
        }
        //2、根据一个HandlerMaping获得一个HandlerAdapter
        MyHandlerAdapter ha = getHandlerAdapter(handler);

        //3、解析某一个方法的形参和返回值之后，统一封装为ModelAndView对象
        MyModelAndView mv = ha.handler(req,resp,handler);

        // 就把ModelAndView变成一个ViewResolver
        processDispatchResult(req,resp,mv);
    }

    private MyHandlerAdapter getHandlerAdapter(MyHandlerMapping handler) {
        if(this.handlerAdapters.isEmpty()){return null;}
        return this.handlerAdapters.get(handler);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, MyModelAndView mv) throws Exception {
        if(null == mv){return;}
        if(this.viewResolvers.isEmpty()){return;}

        for (MyViewResolver viewResolver : this.viewResolvers) {
            MyView view = viewResolver.resolveViewName(mv.getViewName());
            //直接往浏览器输出
            view.render(mv.getModel(),req,resp);
            return;
        }
    }

    private MyHandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){return  null;}
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        for (MyHandlerMapping mapping : handlerMappings) {
            Matcher matcher = mapping.getPattern().matcher(url);
            if(!matcher.matches()){continue;}
            return mapping;
        }
        return null;
    }

}
