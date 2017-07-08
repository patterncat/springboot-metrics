package cn.patterncat.metrics.network;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by patterncat on 2017-01-27.
 */
public class NetstatMetricsSet implements MetricSet {

    final TcpStatWrapper tcpStatWrapper;

    final long collectIntervalInMs;

    public NetstatMetricsSet(long collectIntervalInMs) {
        this.collectIntervalInMs = collectIntervalInMs;
        tcpStatWrapper = new TcpStatWrapper(collectIntervalInMs);
    }

    @Override
    public Map<String, Metric> getMetrics() {
        try{
            Map<String, Number> data = tcpStatWrapper.query();
            final Map<String, Metric> gauges = new HashMap<String, Metric>();
            for(Map.Entry<String,Number> entry : data.entrySet()){
                gauges.put(entry.getKey(), new Gauge<Number>() {
                    @Override
                    public Number getValue() {
                        //todo how to refresh
                        return tcpStatWrapper.query().get(entry.getKey());
                    }
                });
            }
            return gauges;
        }catch (Exception e){
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }
}
