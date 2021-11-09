package com.trace.hh;

import com.trace.hh.service.OrderService;
import com.trace.hh.service.UserService;
import com.trace.hh.spring.MyApplicationContext;

public class Test {
    public static void main(String[] args) {

        MyApplicationContext myApplicationContext = new MyApplicationContext(AppConfig.class);

        //userService 可能是代理对象
        UserService userService = (UserService) myApplicationContext.getBean("userService");
        userService.checkUser();

       /* OrderService orderService = (OrderService) applicationContext.getBean("orderService");
        orderService.listOrder();

        OrderService orderService2 = (OrderService) applicationContext.getBean("orderService");
        OrderService orderService3 = (OrderService) applicationContext.getBean("orderService");
        System.out.println(orderService);
        System.out.println(orderService2);
        System.out.println(orderService3);*/

    }
}
