package com.example.demo.controller;

import cn.patterncat.metrics.kafka.KafkaMetricRegister;
import com.codahale.metrics.Metric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by patterncat on 2017-12-29.
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    KafkaMetricRegister kafkaMetricRegister;

//    @GetMapping("")
//    public Map<String,Metric> kafkaMetrics(){
//        return kafkaMetricRegister.getMetrics();
//    }
}
