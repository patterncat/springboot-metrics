package com.example.demo.service;

import com.example.demo.dao.AuthUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * Created by patterncat on 2017-05-23.
 */
@Component
@Transactional
public class UserService {

    @Autowired
    AuthUserDao authUserDao;

    public void longTest(){
        authUserDao.findAll();
        try {
            TimeUnit.MINUTES.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
