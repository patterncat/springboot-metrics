package cn.patterncat.metrics.kafka;

import cn.patterncat.metrics.utils.MetricNamingUtil;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * Created by patterncat on 2017-12-28.
 */
public class KafkaMetricSet implements MetricSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMetricSet.class);

    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    private Set<String> consumerGroups;

    private final Map<String, Metric> metricsByNames = new HashMap<>();

    public KafkaMetricSet(Set<String> consumerGroups) {
        this.consumerGroups = consumerGroups;
        Map<String, Metric> consumerMetrics = registerMBeanMetrics("kafka.consumer:", "kafka.consumer:*");
        Map<String, Metric> producerMetrics = registerMBeanMetrics("kafka.producer:", "kafka.producer:*");
        metricsByNames.putAll(consumerMetrics);
        metricsByNames.putAll(producerMetrics);
    }


    @Override
    public Map<String, Metric> getMetrics() {
        return Collections.unmodifiableMap(metricsByNames);
    }

    /**
     * convert mbean to metric gauge
     * @param prefix
     * @param objName
     * @return
     */
    public Map<String, Metric> registerMBeanMetrics(String prefix, String objName) {
        Map<String, Metric> metricsMap = new HashMap<>();
        try{
            ObjectName oName = new ObjectName(objName);
            Set<ObjectName> metricsBeans = mBeanServer.queryNames(oName, new QueryExp(){

                @Override
                public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException {
                    String mbeanName = name.toString();
                    //filter clientId
                    if(consumerGroups == null || consumerGroups.isEmpty()){
                        return true;
                    }
                    return consumerGroups.stream().anyMatch(g -> mbeanName.contains("clientId="+g));
                }

                @Override
                public void setMBeanServer(MBeanServer s) {

                }
            });

            for (ObjectName mBeanName : metricsBeans) {
                try {
                    MBeanInfo metricsBean = mBeanServer.getMBeanInfo(mBeanName);
                    MBeanAttributeInfo[] metricsAttrs = metricsBean.getAttributes();
                    for (MBeanAttributeInfo metricsAttr : metricsAttrs) {
                        LOGGER.info("register mBeanName:{},attributeName:{}",mBeanName,metricsAttr.getName());

                        final Gauge gauge = new Gauge() {
                            @Override
                            public Object getValue() {
                                try {
                                    return mBeanServer.getAttribute(mBeanName, metricsAttr.getName());
                                } catch (Exception e) {
                                    LOGGER.error("failed to get MBean {} attribute {} 's value, exception:{}", mBeanName,
                                            metricsAttr.getName(), e);
                                }
                                return null;
                            }
                        };

                        metricsMap.put(MetricNamingUtil.join(prefix,metricsAttr.getName()),gauge);

                    }
                } catch (Exception e) {
                    LOGGER.error("failed to get MBean {} info data, exception:{}", mBeanName, e);
                }
            }
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }
        return metricsMap;
    }
}
