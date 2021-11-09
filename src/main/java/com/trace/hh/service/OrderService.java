package com.trace.hh.service;

import com.trace.hh.spring.annotation.Autowired;
import com.trace.hh.spring.annotation.Component;
import com.trace.hh.spring.annotation.Scope;
import com.trace.hh.spring.bean.BeanNameAware;
import com.trace.hh.spring.bean.InitializingBean;

@Component("orderService")
@Scope("singleton")
public class OrderService implements BeanNameAware, InitializingBean {

    @Autowired
    private UserService userService;

    private String beanName;

    public void listOrder(){
        System.out.println("list order...");
        System.out.println("beanname... " + beanName);
        //userService.checkUser();
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("afterPropertiesSet  inital.....");
    }
}
