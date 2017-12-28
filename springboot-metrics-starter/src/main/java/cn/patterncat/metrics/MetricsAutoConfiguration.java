package cn.patterncat.metrics;

import cn.patterncat.metrics.config.AdvancedThreadExecutor;
import cn.patterncat.metrics.config.LinuxCondition;
import cn.patterncat.metrics.config.TomcatCustomizer;
import cn.patterncat.metrics.config.TomcatConfigEndpoint;
import cn.patterncat.metrics.jvm.JvmTotalMetricSet;
import cn.patterncat.metrics.kafka.KafkaMetricRegister;
import cn.patterncat.metrics.network.NetstatMetricsSet;
import cn.patterncat.metrics.system.OperatingSystemMetricSet;
import cn.patterncat.metrics.tomcat.AdvancedTomcatMetrics;
import cn.patterncat.metrics.tomcat.TomcatDataSourceMetrics;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.actuate.endpoint.MetricsEndpointMetricReader;
import org.springframework.boot.actuate.endpoint.SystemPublicMetrics;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.servlet.Servlet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by patterncat on 2017-01-20.
 */
@Configuration
@AutoConfigureAfter(MetricRepositoryAutoConfiguration.class)
public class MetricsAutoConfiguration {

    @Autowired
    MetricRegistry metricRegistry;

    @Value("${spring.metrics.export.delay-millis:1000}")
    Long intervalInMillis;

    @Value("#{'${spring.metrics.export.kafka.consumer-groups:}'.split(',')}")
    Set<String> kafkaConsumerGroups;


    /**
     * for local mode debug
     * @return
     */
    @Bean
    @Profile("debug")
    public ConsoleReporter consoleReporter() {
        ConsoleReporter.Builder builder = ConsoleReporter.forRegistry(metricRegistry);
        ConsoleReporter reporter = builder.build();
        reporter.start(10000, TimeUnit.MILLISECONDS);
        return reporter;
    }




    /*
     * Reading all metrics that appear on the /metrics endpoint to expose them to metrics writer beans.
     */
    @Bean
    public MetricsEndpointMetricReader metricsEndpointMetricReader(MetricsEndpoint metricsEndpoint, SystemPublicMetrics systemPublicMetrics) {
        //unregister default system public metrics
        metricsEndpoint.unregisterPublicMetrics(systemPublicMetrics);
        return new MetricsEndpointMetricReader(metricsEndpoint);
    }


    /**
     * 1\metrics for system os
     * @return
     */
    @Bean
    @Conditional(LinuxCondition.class)
    public OperatingSystemMetricSet operatingSystemMetricSet(){
        OperatingSystemMetricSet operatingSystemMetricSet = new OperatingSystemMetricSet();
        metricRegistry.register("system",operatingSystemMetricSet);
        return operatingSystemMetricSet;
    }


    /**
     * 2\metrics for network
     * @return
     */
    @Bean
    @Conditional(LinuxCondition.class)
    public NetstatMetricsSet netstatMetricsSet(){
        NetstatMetricsSet netstatMetricsSet = new NetstatMetricsSet(intervalInMillis);
        metricRegistry.register("netstat",netstatMetricsSet);
        return netstatMetricsSet;
    }

    /**
     * 3\metrics for jvm
     * @return
     */
    @Bean(destroyMethod = "shutdown")
    public JvmTotalMetricSet jvmTotalMetricSet(){
        JvmTotalMetricSet jvmTotalMetricSet = new JvmTotalMetricSet(intervalInMillis);
        metricRegistry.register("jvm",jvmTotalMetricSet);
        return jvmTotalMetricSet;
    }

    /**
     * 4\metrics for tomcat
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({ Servlet.class, Tomcat.class })
    @ConditionalOnWebApplication
    public AdvancedTomcatMetrics advancedTomcatMetrics(){
        return new AdvancedTomcatMetrics();
    }

    @ConditionalOnClass(org.apache.tomcat.jdbc.pool.DataSource.class)
    static class TomcatJdbcMetricsConfigureation{
        @Bean
        public TomcatDataSourceMetrics tomcatDataSourceMetrics(MetricRegistry metricRegistry,org.apache.tomcat.jdbc.pool.DataSource dataSource){
            TomcatDataSourceMetrics tomcatDataSourceMetrics = new TomcatDataSourceMetrics(dataSource);
            metricRegistry.register("jdbc",tomcatDataSourceMetrics);
            return tomcatDataSourceMetrics;
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({ Servlet.class, Tomcat.class })
    @ConditionalOnWebApplication
    public AdvancedThreadExecutor advancedThreadExecutor(){
        return new AdvancedThreadExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({ Servlet.class, Tomcat.class })
    @ConditionalOnWebApplication
    public TomcatCustomizer tomcatConfig(AdvancedThreadExecutor advancedThreadExecutor){
        return new TomcatCustomizer(advancedThreadExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({ Servlet.class, Tomcat.class })
    @ConditionalOnWebApplication
    public TomcatConfigEndpoint tomcatConfigEndpoint(TomcatCustomizer tomcatCustomizer){
        return new TomcatConfigEndpoint(tomcatCustomizer);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.metrics.export.kafka.enabled",havingValue = "true",matchIfMissing = false)
    public KafkaMetricRegister kafkaMetricSet(){
        Set<String> trimedSet = kafkaConsumerGroups.stream().filter(e -> StringUtils.isNotBlank(e))
                .collect(Collectors.toSet());
        KafkaMetricRegister kafkaMetricRegister = new KafkaMetricRegister(metricRegistry,trimedSet);
        //NOTE 这里注册已经晚了,存在延时,因此是空的
//        metricRegistry.register("kafka",kafkaMetricRegister);
        return kafkaMetricRegister;
    }


//    @Bean
//    public HystrixMetricsPublisher hystrixMetricsPublisher() {
//        HystrixCodaHaleMetricsPublisher publisher = new HystrixCodaHaleMetricsPublisher(metricRegistry);
//        HystrixPlugins.getInstance().registerMetricsPublisher(publisher);
//        return publisher;
//    }

}
