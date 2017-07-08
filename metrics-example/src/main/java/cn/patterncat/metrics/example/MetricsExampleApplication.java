package cn.patterncat.metrics.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {MetricFilterAutoConfiguration.class})
@ComponentScan(basePackages = {"cn.patterncat.metrics"})
public class MetricsExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetricsExampleApplication.class, args);
	}
}
