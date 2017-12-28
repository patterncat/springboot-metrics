package com.example.demo;

import com.example.demo.consumer.DemoConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"cn.patterncat.metrics","com.example.demo"})
public class MetricsKafkaExampleApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(MetricsKafkaExampleApplication.class, args);
	}

	@Autowired
	DemoConsumer demoConsumer;

	/**
	 * sh kafka-topics.sh --create --topic demo-metric-topic --replication-factor 1 --partitions 3 --zookeeper localhost:2181
	 * sh kafka-console-producer.sh --broker-list localhost:9092 --sync --topic demo-metric-topic
	 * @param strings
	 * @throws Exception
     */
	@Override
	public void run(String... strings) throws Exception {
		String topic = "demo-metric-topic";
		String zkAddr = "localhost:2181";
		int consumerCount = 3;
		String group = "test-metric-group";
		demoConsumer.startConsume(topic,zkAddr,consumerCount,group);
	}
}
