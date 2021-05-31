package com.trace.hh.service;

import com.trace.hh.spring.annotation.Component;
import com.trace.hh.spring.bean.BeanPostProcessor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    public Object postProcessBeforeInitialization(String beanName, Object bean) {
        if(beanName.equals("userService")){
            System.out.println("MyBeanPostProcessor初始化前");
        }
        return bean;
    }

    /**
     * AOP 功能
     *
     * @param beanName
     * @param bean
     * @return
     */
    public Object postProcessAfterInitialization(String beanName, final Object bean) {
        if (beanName.equals("userService")) {
            System.out.println("MyBeanPostProcessor初始化后");

            Object proxyInstance = Proxy.newProxyInstance(this.getClass().getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                    System.out.println("代理逻辑");
                    return method.invoke(bean, args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
