package cn.patterncat.metrics.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 替换com/codahale/metrics/jvm/ClassLoadingGaugeSet.java
 * 增加currentlyLoaded指标
 */
public class ClassLoadingMetricSet implements MetricSet {
    private ClassLoadingMXBean classLoadingMXBean;

    private Gauge<Long> totalLoadedClassesGauge;
    private Gauge<Long> unloadedClassesGauge;
    private Gauge<Integer> currentlyLoadedClassesGauge;

    private Map<String, Metric> metricsByNames;

    ClassLoadingMetricSet() {
        this.classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();

        Map<String, Metric> metricsByNames = new HashMap<>();

        this.totalLoadedClassesGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return classLoadingMXBean.getTotalLoadedClassCount();
            }
        };
        metricsByNames.put("totalLoaded", totalLoadedClassesGauge);

        this.unloadedClassesGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return classLoadingMXBean.getUnloadedClassCount();
            }
        };
        metricsByNames.put("totalUnloaded", unloadedClassesGauge);

        this.currentlyLoadedClassesGauge = new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return classLoadingMXBean.getLoadedClassCount();
            }
        };
        metricsByNames.put("currentlyLoaded", currentlyLoadedClassesGauge);

        this.metricsByNames = metricsByNames;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        return Collections.unmodifiableMap(metricsByNames);
    }

    Gauge<Long> getTotalLoadedClassesGauge() {
        return totalLoadedClassesGauge;
    }

    Gauge<Long> getUnloadedClassesGauge() {
        return unloadedClassesGauge;
    }

    Gauge<Integer> getCurrentlyLoadedClassesGauge() {
        return currentlyLoadedClassesGauge;
    }
}
