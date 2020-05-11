package com.gupao.myspring.framework.aop.support;

import com.gupao.myspring.framework.aop.aspect.MyAdvise;
import com.gupao.myspring.framework.aop.config.MyAopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description
 * @Author chenzk27336
 * @Date 2020/5/8 10:35
 **/
public class MyAdvisedSupport {
    private MyAopConfig config;
    private Object target;
    private Class targetClass;
    private Pattern pointCutClassPattern;

    private Map<Method, Map<String, MyAdvise>> methodCache;

    public MyAdvisedSupport(MyAopConfig config) {
        this.config = config;
    }

    private void parse() {
        //把Spring的Excpress变成Java能够识别的正则表达式
        String pointCut = config.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");
        //保存专门匹配Class的正则
        String pointCutForClassRegex = pointCut.substring(0,pointCut.lastIndexOf("\\(")-4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));

        //享元的共享池
        methodCache = new HashMap<Method, Map<String, MyAdvise>>();
        //保存专门匹配方法的正则
        Pattern pointCutPattern = Pattern.compile(pointCut);
        try {
            Class aspectCLass = Class.forName(this.config.getAspectClass());
            Map<String,Method> aspectMethods = new HashMap<String, Method>();
            for(Method method: aspectCLass.getMethods()){
                aspectMethods.put(method.getName(),method);
            }
            for(Method method : this.targetClass.getMethods()){
                String methodString = method.toString();
                if(methodString.contains("throws")){
                    methodString = methodString.substring(0,methodString.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pointCutPattern.matcher(methodString);
                if(matcher.matches()){
                    Map<String,MyAdvise> advises = new HashMap<String, MyAdvise>();
                    if(!(null==config.getAspectBefore()||"".equals(config.getAspectBefore()))){
                        advises.put("before",new MyAdvise(aspectCLass.newInstance(),aspectMethods.get(config.getAspectBefore())));
                    }
                    if(!(null==config.getAspectAfter()||"".equals(config.getAspectAfter()))){
                        advises.put("after",new MyAdvise(aspectCLass.newInstance(),aspectMethods.get(config.getAspectAfter())));
                    }
                    if(!(null==config.getAspectAfterThrow()||"".equals(config.getAspectAfterThrow()))){
                        advises.put("afterThrow",new MyAdvise(aspectCLass.newInstance(),aspectMethods.get(config.getAspectAfterThrow())));
                    }
                    methodCache.put(method,advises);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //给ApplicationContext首先IoC中的对象初始化时调用，决定要不要生成代理类的逻辑
    public boolean pointCutMath() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }
    //根据一个目标代理类的方法，获得其对应的通知
    public Map<String,MyAdvise> getAdvices(Method method, Object o) throws Exception {
        //享元设计模式的应用
        Map<String,MyAdvise> cache = methodCache.get(method);
        if(null == cache){
            Method m = targetClass.getMethod(method.getName(),method.getParameterTypes());
            cache = methodCache.get(m);
            this.methodCache.put(m,cache);
        }
        return cache;
    }

    public static void main(String[] args) {
        String pointCut = "public .* com.gupao.demo.service..*Service..*(.*)";
        pointCut = pointCut.replaceAll("\\.","\\\\.");
        pointCut = pointCut.replaceAll("\\\\.\\*",".*");
        pointCut = pointCut.replaceAll("\\(","\\\\(");
        pointCut = pointCut.replaceAll("\\)","\\\\)");
        System.out.println(pointCut);
    }
    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

}
