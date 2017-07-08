package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"cn.patterncat.metrics","com.example.demo"})
public class MetricsJdbcExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetricsJdbcExampleApplication.class, args);
	}
}
