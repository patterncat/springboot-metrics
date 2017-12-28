package cn.patterncat.metrics.kafka;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.management.*;
import javax.management.relation.MBeanServerNotificationFilter;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * Created by patterncat on 2017-12-28.
 */
@Deprecated
public class KafkaMetricRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMetricRegister.class);

    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    private final KafkaMBeanEventListener kafkaMBeanEventListener;

    private MetricRegistry metricRegistry;

    public KafkaMetricRegister(MetricRegistry metricRegistry, Set<String> consumerGroups) {
        this.metricRegistry = metricRegistry;
        kafkaMBeanEventListener = new KafkaMBeanEventListener(metricRegistry,mBeanServer,consumerGroups);
        MBeanServerNotificationFilter filter = new MBeanServerNotificationFilter();
        filter.enableAllObjectNames();
        try {
            mBeanServer.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME,kafkaMBeanEventListener, filter, null);
        } catch (InstanceNotFoundException e) {
            LOGGER.error(e.getMessage(),e);
        }
    }
}
