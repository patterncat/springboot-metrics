package com.example.demo.controller;

import com.example.demo.dao.AuthUserDao;
import com.example.demo.domain.AuthUser;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by patterncat on 2017-06-13.
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    UserService userService;

    @Autowired
    AuthUserDao authUserDao;

    @RequestMapping(value = "/long",method = RequestMethod.GET)
    public List<AuthUser> longTransaction(){
        return authUserDao.findByVersion(0L);
    }

    @RequestMapping(value = "/one",method = RequestMethod.POST)
    public String createOne(){
        AuthUser authUser = new AuthUser();
        authUser.setPrincipal(UUID.randomUUID().toString());
        authUser.setDeptName(UUID.randomUUID().toString());
        authUser.setAccessToken(UUID.randomUUID().toString());
        authUser.setAuthType(UUID.randomUUID().toString());
        authUser.setOrgName(UUID.randomUUID().toString());
        authUser.setUserName(UUID.randomUUID().toString());
        authUserDao.save(authUser);
        return "hello";
    }

    @RequestMapping(value = "/batch",method = RequestMethod.POST)
    public List<AuthUser> batchInOneConn(){
        List<AuthUser> authUserList = IntStream.range(1,100000).mapToObj(i -> {
            AuthUser authUser = new AuthUser();
            authUser.setPrincipal(UUID.randomUUID().toString());
            authUser.setDeptName(UUID.randomUUID().toString());
            authUser.setAccessToken(UUID.randomUUID().toString());
            authUser.setAuthType(UUID.randomUUID().toString());
            authUser.setOrgName(UUID.randomUUID().toString());
            authUser.setUserName(UUID.randomUUID().toString());
            return authUser;
        }).collect(Collectors.toList());
        return authUserDao.save(authUserList);
    }
}
