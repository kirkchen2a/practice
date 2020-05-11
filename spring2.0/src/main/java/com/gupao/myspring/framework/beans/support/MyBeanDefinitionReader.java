package com.gupao.myspring.framework.beans.support;

import com.gupao.myspring.framework.beans.config.MyBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MyBeanDefinitionReader {
    //保存扫描的结果
    private List<String> regitryBeanClasses = new ArrayList<String>();
    private Properties contextConfig = new Properties();

    public MyBeanDefinitionReader(String... configLocations) {
        for(String configLocation : configLocations){
            //加载配置文件
            doLoadConfig(configLocation);

            //扫描配置文件中的配置的相关的类
            doScanner(contextConfig.getProperty("scanPackage"));
        }

    }

    private void doScanner(String scanPackage) {

        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        //当成是一个ClassPath文件夹
        for(File file : classPath.listFiles()){
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else{
                if(!file.getName().endsWith(".class")){continue;}
                //全类名 = 包名.类名
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                regitryBeanClasses.add(className);
            }
        }
    }

    private void doLoadConfig(String configLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocation.replaceAll("classpath:",""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<MyBeanDefinition> loadBeanDefinitions() {
        List<MyBeanDefinition> result = new ArrayList<MyBeanDefinition>();
        for(String className : regitryBeanClasses){
            try {
                Class<?> beanClass = Class.forName(className);
                if(beanClass.isInterface()){continue;}
                //保存类对应的ClassName（全类名）
                //还有beanName
                //1、默认是类名首字母小写
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));
                //2、自定义
                //3、接口注入
                for (Class<?> i : beanClass.getInterfaces()) {
                    result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private MyBeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        MyBeanDefinition beanDefinition = new MyBeanDefinition();
        beanDefinition.setFactoryBeanName(beanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig() {
        return this.contextConfig;
    }
}
