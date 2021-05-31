package com.trace.hh.spring;

import com.trace.hh.spring.annotation.Autowired;
import com.trace.hh.spring.annotation.Component;
import com.trace.hh.spring.annotation.ComponentScan;
import com.trace.hh.spring.annotation.Scope;
import com.trace.hh.spring.bean.BeanDefinition;
import com.trace.hh.spring.bean.BeanNameAware;
import com.trace.hh.spring.bean.BeanPostProcessor;
import com.trace.hh.spring.bean.InitializingBean;
import com.trace.hh.spring.bean.ScopeEnum;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xhh
 */
public class ApplicationContext {

    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<BeanPostProcessor>();

    public ApplicationContext(Class configClass) {

        //扫描得到 beanDefinitionMap
        scan(configClass);

        // 实例化单例bean并放到容器中
        createSingletonInstanceBean();
    }


    private void scan(Class configClass) {
        //扫描路径下的class 转化为BeanDefinition对象，添加到容器beanDefinitionMap中

        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        // 得到包路径
        String packagePath = componentScanAnnotation.value();

        // 得到包路径下的class
        List<Class> classList = getBeanClasses(packagePath);
        for (Class clazz : classList) {
            if (clazz.isAnnotationPresent(Component.class)) {

                //获取beanName, 要么Spring自动生成，要么从Component注解上获取
                Component component = (Component) clazz.getAnnotation(Component.class);
                String beanName = component.value();

                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setBeanClass(clazz);

                if (clazz.isAnnotationPresent(Scope.class)) {
                    Scope scope = (Scope) clazz.getAnnotation(Scope.class);
                    String scopeValue = scope.value();
                    if (ScopeEnum.singleton.name().equals(scopeValue)) {
                        beanDefinition.setScope(ScopeEnum.singleton);
                    } else {
                        beanDefinition.setScope(ScopeEnum.prototype);
                    }
                } else {
                    beanDefinition.setScope(ScopeEnum.singleton);
                }

                //放入bean定义容器
                beanDefinitionMap.put(beanName, beanDefinition);

                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                    try {
                        BeanPostProcessor beanPostProcesser = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                        beanPostProcessorList.add(beanPostProcesser);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private List<Class> getBeanClasses(String packagePath) {
        List<Class> beanClasses = new ArrayList<Class>();

        ClassLoader classLoader = this.getClass().getClassLoader();

        //com/trace/hh/service
        packagePath = packagePath.replace(".", "/");
        URL resource = classLoader.getResource(packagePath);
        File file = new File(resource.getFile());

        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            for (File listFile : listFiles) {
                String fileName = listFile.getAbsolutePath();
                if (fileName.endsWith(".class")) {
                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    className = className.replace("\\", ".");
                    System.out.println("classname... " + className);

                    try {
                        Class<?> aClass = classLoader.loadClass(className);
                        beanClasses.add(aClass);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return beanClasses;
    }


    private void createSingletonInstanceBean() {
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals(ScopeEnum.singleton)) {
                Object bean = doCreateBean(beanName, beanDefinition);
                if(bean !=null) {
                    singletonObjects.put(beanName, bean);
                }
            }
        }
    }

    /**
     * 基于BeanDefinition创建bean
     * spring bean 生命周期
     * @param beanName
     * @param beanDefinition
     * @return
     */
    private Object doCreateBean(String beanName, BeanDefinition beanDefinition) {

        //   1. 实例化
        //   2. 属性填充
        //   3. Aware回调
        //   4. 初始化
        //   5. 添加到单例池

        Class beanClass = beanDefinition.getBeanClass();

        try {
            //实例化
            Constructor declaredConstructor = beanClass.getDeclaredConstructor();
            Object newInstance = declaredConstructor.newInstance();

            //属性填充
            Field[] declaredFields = beanClass.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    String fieldName = field.getName();
                    Object bean = getBean(fieldName);
                    field.setAccessible(true);
                    field.set(newInstance, bean);
                }
            }

            //回调 Aware
            if (newInstance instanceof BeanNameAware) {
                ((BeanNameAware) newInstance).setBeanName(beanName);
            }


            //初始化前处理
            for (BeanPostProcessor beanPostProcessor: beanPostProcessorList) {
                newInstance = beanPostProcessor.postProcessBeforeInitialization(beanName, newInstance);
            }

            //初始化
            if (newInstance instanceof InitializingBean) {
                ((InitializingBean) newInstance).afterPropertiesSet();
            }

            //初始化后处理
            for (BeanPostProcessor beanPostProcessor: beanPostProcessorList) {

                //newInstance有可能是是代理对象
                newInstance = beanPostProcessor.postProcessAfterInitialization(beanName, newInstance);
            }

            return newInstance;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getBean(String beanName) {
        if (singletonObjects.containsKey(beanName)) {
            return singletonObjects.get(beanName);
        } else {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            return doCreateBean(beanName, beanDefinition);
        }
    }
}
