package com.trace.hh;

import com.trace.hh.service.OrderService;
import com.trace.hh.service.UserService;
import com.trace.hh.service.UserServiceImpl;
import com.trace.hh.spring.ApplicationContext;

public class TestDemo {
    public static void main(String[] args) {

        ApplicationContext applicationContext = new ApplicationContext(ApplicationConfig.class);

        //userService 可能是代理对象
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.checkUser();

/*        OrderService orderService = (OrderService) applicationContext.getBean("orderService");
        orderService.listOrder();

        OrderService orderService2 = (OrderService) applicationContext.getBean("orderService");
        OrderService orderService3 = (OrderService) applicationContext.getBean("orderService");
        System.out.println(orderService);
        System.out.println(orderService2);
        System.out.println(orderService3);*/

    }
}
