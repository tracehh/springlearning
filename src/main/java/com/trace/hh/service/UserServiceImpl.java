package com.trace.hh.service;

import com.trace.hh.spring.annotation.Component;


@Component("userService")
public class UserServiceImpl implements UserService {

    public void checkUser() {
        System.out.println("check user....");
    }
}
