package cn.patterncat.metrics.tomcat;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by patterncat on 2017-07-02.
 */
public class TomcatDataSourceMetrics implements MetricSet {

    DataSource datasource;

    final Map<String, Metric> gauges = new HashMap<String, Metric>();

    public TomcatDataSourceMetrics(DataSource datasource) {
        this.datasource = datasource;
        //-- constant value
        gauges.put("size", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return datasource.getSize();
            }
        });

        gauges.put("initialSize", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return datasource.getInitialSize();
            }
        });

        gauges.put("maxActive", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return datasource.getMaxActive();
            }
        });

        gauges.put("maxIdle", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return datasource.getMaxIdle();
            }
        });

        gauges.put("minIdle", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return datasource.getMinIdle();
            }
        });

        //-- var
        gauges.put("active", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return datasource.getActive();
            }
        });

        gauges.put("idle", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return datasource.getIdle();
            }
        });

        gauges.put("wait", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return datasource.getWaitCount();
            }
        });

        gauges.put("borrowed", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return datasource.getBorrowedCount();
            }
        });

        gauges.put("created", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return datasource.getCreatedCount();
            }
        });

        gauges.put("released", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return datasource.getReleasedCount();
            }
        });

        gauges.put("releasedIdle", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return datasource.getReleasedIdleCount();
            }
        });

        gauges.put("reconnectedCount", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return datasource.getReconnectedCount();
            }
        });

        gauges.put("returnedCount", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return datasource.getReturnedCount();
            }
        });

        gauges.put("removeAbandonedCount", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return datasource.getRemoveAbandonedCount();
            }
        });
    }

    @Override
    public Map<String, Metric> getMetrics() {
        return Collections.unmodifiableMap(gauges);
    }
}
