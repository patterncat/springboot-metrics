package cn.patterncat.metrics.kafka;

import cn.patterncat.metrics.utils.MetricNamingUtil;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 这个方案过于抽象实际用起来存在注册延时,比较费劲
 * Created by patterncat on 2017-12-29.
 */
@Deprecated
public class KafkaMBeanEventListener implements NotificationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMBeanEventListener.class);

    private final MBeanServer mBeanServer;

    private MetricRegistry metricRegistry;

    private Set<String> consumerGroups;

    private ObjectName CONSUMER_OBJECT_NAME;

    private ObjectName PRODUCER_OBJECT_NAME;

    public KafkaMBeanEventListener(MetricRegistry metricRegistry,MBeanServer mBeanServer,Set<String> consumerGroups) {
        this.metricRegistry = metricRegistry;
        this.mBeanServer = mBeanServer;
        this.consumerGroups = consumerGroups;
        try{
            CONSUMER_OBJECT_NAME = new ObjectName("kafka.consumer:*");
            PRODUCER_OBJECT_NAME = new ObjectName("kafka.producer:*");
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        try{
            MBeanServerNotification mbs = (MBeanServerNotification) notification;
            ObjectName objectName = mbs.getMBeanName();
            if(MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(mbs.getType())) {
                LOGGER.info("MBean Registered [" + objectName + "]");
                if(CONSUMER_OBJECT_NAME.apply(objectName) && matchConsumerGroups(objectName)){
                    registerMBeanMetrics("kafka.consumer", objectName);
                }else if(PRODUCER_OBJECT_NAME.apply(objectName) && matchConsumerGroups(objectName)){
                    registerMBeanMetrics("kafka.producer", objectName);
                }

            } else if(MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(mbs.getType())) {
                LOGGER.info("MBean Unregistered [" + objectName + "]");
            }
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }
    }

    public boolean matchConsumerGroups(ObjectName objectName){
        String mbeanName = objectName.toString();
        //filter clientId
        if(consumerGroups == null || consumerGroups.isEmpty()){
            return true;
        }
        return consumerGroups.stream().anyMatch(g -> mbeanName.contains("clientId="+g));
    }

    /**
     * todo 处理重名问题,还有producer的partition字段
     * convert mbean to metric gauge
     * @param prefix
     * @param mBeanName
     * @return
     */
    public void registerMBeanMetrics(String prefix, ObjectName mBeanName) {
        try {
            String key = MetricNamingUtil.join(prefix,mBeanName.getKeyProperty("clientId"),mBeanName.getKeyProperty("name"));
            MBeanInfo metricsBean = mBeanServer.getMBeanInfo(mBeanName);
            MBeanAttributeInfo[] metricsAttrs = metricsBean.getAttributes();
            Map<String,Metric> attMetrics = new HashMap<>();
            for (MBeanAttributeInfo metricsAttr : metricsAttrs) {

                if("double".equals(metricsAttr.getType())){
                    LOGGER.info("register mBeanName:{},attribute:{}",mBeanName,metricsAttr);
                    final Gauge<Double> gauge = new Gauge<Double>() {
                        @Override
                        public Double getValue() {
                            try {
                                Object value = mBeanServer.getAttribute(mBeanName, metricsAttr.getName());
                                LOGGER.debug("get:{} {},value:{}",mBeanName,metricsAttr.getName(),value);
                                return (Double) value;
                            } catch (Exception e) {
                                LOGGER.error("failed to get MBean {} attribute {} 's value, exception:{}", mBeanName,
                                        metricsAttr.getName(), e);
                            }
                            return 0d;
                        }
                    };
                    attMetrics.put(MetricNamingUtil.join(metricsAttr.getName()),gauge);
                }else if ("long".equals(metricsAttr.getType())){
                    LOGGER.info("register mBeanName:{},attribute:{}",mBeanName,metricsAttr);
                    final Gauge<Long> gauge = new Gauge<Long>() {
                        @Override
                        public Long getValue() {
                            try {
                                Object value = mBeanServer.getAttribute(mBeanName, metricsAttr.getName());
                                LOGGER.debug("get:{} {},value:{}",mBeanName,metricsAttr.getName(),value);
                                return (Long) value;
                            } catch (Exception e) {
                                LOGGER.error("failed to get MBean {} attribute {} 's value, exception:{}", mBeanName,
                                        metricsAttr.getName(), e);
                            }
                            return 0L;
                        }
                    };
                    attMetrics.put(MetricNamingUtil.join(metricsAttr.getName()),gauge);
                }
            }
            metricRegistry.register(key,new MetricSet() {
                @Override
                public Map<String, Metric> getMetrics() {
                    return attMetrics;
                }
            });
        } catch (Exception e) {
            LOGGER.error("failed to get MBean {} info data, exception:{}", mBeanName, e);
        }
    }
}
