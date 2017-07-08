package cn.patterncat.metrics.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by patterncat on 2017-05-29.
 */
@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping("")
    public String sayHello(){
        return "hello";
    }
}
