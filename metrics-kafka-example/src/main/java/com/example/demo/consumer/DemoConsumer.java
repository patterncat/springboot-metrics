package com.example.demo.consumer;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by patterncat on 2017-12-28.
 */
@Component
public class DemoConsumer {

    //not recommend just for demo use
    ExecutorService pool = Executors.newFixedThreadPool(10);

    @Async
    public void startConsume(String topic,String zk,int consumerCount,String group) throws UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("zookeeper.connect", zk);
        props.put("auto.offset.reset","smallest");
        props.put("group.id",group);
        props.put("zookeeper.session.timeout.ms", "10000");
        props.put("zookeeper.sync.time.ms", "2000");
        props.put("auto.commit.interval.ms", "10000");
//        props.put("consumer.timeout.ms","10000"); //设置ConsumerIterator的hasNext的超时时间,不设置则永远阻塞直到有新消息来
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY, "range");
        ConsumerConfig consumerConfig =  new kafka.consumer.ConsumerConfig(props);
        ConsumerConnector consumerConnector = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, consumerCount);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector
                .createMessageStreams(topicCountMap);
        consumerMap.get(topic).stream().forEach(stream -> {

            pool.submit(new Runnable() {
                @Override
                public void run() {
                    ConsumerIterator<byte[], byte[]> it = stream.iterator();
                    System.out.println(Thread.currentThread().getName()+" start to consume");
                    try{
                        while (it.hasNext()) {
                            System.out.println(Thread.currentThread().getName()+":"+new String(it.next().message()));
                        }
                    }catch (ConsumerTimeoutException e){
                        e.printStackTrace();
                    }

                    System.out.println(Thread.currentThread().getName()+" end");
                }
            });

        });
    }
}
