package com.gupao.myspring.servlet;

import com.gupao.myspring.framework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyDispatcherServlet extends HttpServlet {
    //保存application.properties配置文件中的内容
    private Properties contextConfig = new Properties();

    //保存扫描的所有的类名
    private List<String> classNames = new ArrayList<String>();

    private Map<String,Object> ioc = new HashMap<String,Object>();
    private List<Handler> handlerMapping = new ArrayList<Handler>();

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
            e.printStackTrace();
            resp.getWriter().write("500 Exection,Detail : " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3、初始化扫描到的类，并且将它们放入到ICO容器之中
        doInstance();

        //4、完成依赖注入
        doAutowired();

        //5、初始化HandlerMapping
        initHandlerMapping();

        System.out.println("GP Spring framework is init.");
    }

    private void initHandlerMapping() {
        if(ioc.isEmpty()) return;
        for(Map.Entry<String, Object> entry : ioc.entrySet()){
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(MyController.class))continue;

            String baseUrl = "";
            if(clazz.isAnnotationPresent(MyRequestMapping.class)){
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl += requestMapping.value();
            }

            for(Method method : clazz.getMethods()){
                if(!method.isAnnotationPresent(MyRequestMapping.class)) continue;
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                //优化
                // //demo///query
                String regex = ("/" + baseUrl + "/" + requestMapping.value())
                        .replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                this.handlerMapping.add(new Handler(pattern,entry.getValue(),method));
//                handlerMapping.put(url,method);
                System.out.println("Mapped :" + pattern + "," + method);
            }
        }
    }

    private void doAutowired() {
        if(ioc.isEmpty()) return;
        try {
            for(Map.Entry entry : ioc.entrySet()){
               Field[] fields  = entry.getValue().getClass().getDeclaredFields();
               for(Field field : fields){
                   if(!field.isAnnotationPresent(MyAutowired.class)) continue;
                   MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                   String beanName = autowired.value();
                   if("".equals(beanName)){
                       beanName = field.getType().getName();
                   }
                   field.setAccessible(true);
                   field.set(entry.getValue(),ioc.get(beanName));
               }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void doInstance() {
        try {
            if(classNames.isEmpty()) return;
            for(String classname : classNames){
                Class<?> clazz = Class.forName(classname);
                //为了简化代码逻辑，主要体会设计思想，只举例 @Controller和@Service,
                // @Componment...就一一举例了
                if(clazz.isAnnotationPresent(MyController.class)){
                    Object instance = clazz.newInstance();
                    ioc.put(toLowerFirstCase(clazz.getSimpleName()),instance);
                }else if(clazz.isAnnotationPresent(MyService.class)){
                    Object instance = clazz.newInstance();
                    MyService service = clazz.getAnnotation(MyService.class);
                    String beanName = service.value();
                    if("".equals(beanName)){
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    ioc.put(toLowerFirstCase(clazz.getSimpleName()),instance);
                    for(Class<?> i : clazz.getInterfaces()){
                        if(ioc.containsKey(i.getName())){
                            throw new Exception("The “" + i.getName() + "” is exists!!");
                        }
                        ioc.put(i.getName(),instance);
                    }
                }else {
                    continue;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {
        //scanPackage = com.gupaoedu.demo ，存储的是包路径
        //转换为文件路径，实际上就是把.替换为/就OK了
        //classpath
        URL url = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for(File file : classPath.listFiles()){
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else{
                if(!file.getName().endsWith(".class")) continue;
                classNames.add(scanPackage + "." + file.getName().replace(".class",""));
            }
        }

    }

    private void doLoadConfig(String contextConfigLocation) {
        //直接从类路径下找到Spring主配置文件所在的路径
        //并且将其读取出来放到Properties对象中
        //相对于scanPackage=com.gupaoedu.demo 从文件中保存到了内存中
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(is);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Handler handler = getHandler(req);
        if(handler == null){
            resp.getWriter().write("404 Not Found!!!");
            return;
        }

        //获得方法的形参列表
        Class<?> [] paramTypes = handler.getParamTypes();
        Object [] paramValues = new Object[paramTypes.length];
        Map<String,String[]> params = req.getParameterMap();
        for (Map.Entry<String, String[]> parm : params.entrySet()) {
            String value = Arrays.toString(parm.getValue()).replaceAll("\\[|\\]","")
                    .replaceAll("\\s",",");

            if(!handler.paramIndexMapping.containsKey(parm.getKey())){continue;}

            int index = handler.paramIndexMapping.get(parm.getKey());
            paramValues[index] = convert(paramTypes[index],value);
        }
        if(handler.paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }
        if(handler.paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }
        Object returnValue = handler.method.invoke(handler.controller,paramValues);
        if(returnValue == null || returnValue instanceof Void){ return; }
        resp.getWriter().write(returnValue.toString());
    }

    //url传过来的参数都是String类型的，HTTP是基于字符串协议
    //只需要把String转换为任意类型就好
    private Object convert(Class<?> type, String value) {
        //如果是int
        if(Integer.class == type){
            return Integer.valueOf(value);
        }
        else if(Double.class == type){
            return Double.valueOf(value);
        }
        //如果还有double或者其他类型，继续加if
        //这时候，我们应该想到策略模式了
        //在这里暂时不实现，希望小伙伴自己来实现
        return value;
    }

    private Handler getHandler(HttpServletRequest req) {
        if(handlerMapping.isEmpty()){return null;}
        //绝对路径
        String url = req.getRequestURI();
        //处理成相对路径
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");


        for (Handler handler : this.handlerMapping) {
            Matcher matcher = handler.getPattern().matcher(url);
            if(!matcher.matches()){ continue;}
            return handler;
        }
        return null;
    }

    //保存一个url和一个Method的关系
    public class Handler {
        //必须把url放到HandlerMapping才好理解吧
        private Pattern pattern;  //正则
        private Method method;
        private Object controller;
        private Class<?> [] paramTypes;

        public Pattern getPattern() {
            return pattern;
        }

        public Method getMethod() {
            return method;
        }

        public Object getController() {
            return controller;
        }

        public Class<?>[] getParamTypes() {
            return paramTypes;
        }

        //形参列表
        //参数的名字作为key,参数的顺序，位置作为值
        private Map<String,Integer> paramIndexMapping;

        public Handler(Pattern pattern, Object controller, Method method) {
            this.pattern = pattern;
            this.method = method;
            this.controller = controller;

            paramTypes = method.getParameterTypes();

            paramIndexMapping = new HashMap<String, Integer>();
            putParamIndexMapping(method);
        }

        private void putParamIndexMapping(Method method){

            //提取方法中加了注解的参数
            //把方法上的注解拿到，得到的是一个二维数组
            //因为一个参数可以有多个注解，而一个方法又有多个参数
            Annotation[] [] pa = method.getParameterAnnotations();
            for (int i = 0; i < pa.length ; i ++) {
                for(Annotation a : pa[i]){
                    if(a instanceof MyRequestParam){
                        String paramName = ((MyRequestParam) a).value();
                        if(!"".equals(paramName.trim())){
                            paramIndexMapping.put(paramName, i);
                        }
                    }
                }
            }

            //提取方法中的request和response参数
            Class<?> [] paramsTypes = method.getParameterTypes();
            for (int i = 0; i < paramsTypes.length ; i ++) {
                Class<?> type = paramsTypes[i];
                if(type == HttpServletRequest.class ||
                        type == HttpServletResponse.class){
                    paramIndexMapping.put(type.getName(),i);
                }
            }

        }


//        private
    }
}
