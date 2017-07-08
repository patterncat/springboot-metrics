/*
 * Copyright 2016 Centro, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package cn.patterncat.metrics.jvm;

import cn.patterncat.metrics.utils.MetricNamingUtil;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对外内存,DirectBuffer相关指标
 * 跟com/codahale/metrics/jvm/BufferPoolMetricSet.java类似
 * 只是没有写死direct\mapped这两个buffer pool
 */
public class BufferPoolMetricSet implements MetricSet {
    private List<BufferPoolMXBean> bufferPoolMXBeans;

    private Map<String, Metric> metricsByNames;

    public BufferPoolMetricSet() {
        this.bufferPoolMXBeans = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);

        Map<String, Metric> metricsByNames = new HashMap<>();

        for (final BufferPoolMXBean bufferPoolMXBean : bufferPoolMXBeans) {
            final String bufferPoolName = bufferPoolMXBean.getName();

            final Gauge<Long> sizeGauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return bufferPoolMXBean.getCount();
                }
            };
            metricsByNames.put(MetricNamingUtil.join(bufferPoolName, "size"), sizeGauge);

            final Gauge<Long> totalCapacityInBytesGauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return bufferPoolMXBean.getTotalCapacity();
                }
            };
            metricsByNames.put(MetricNamingUtil.join(bufferPoolName, "totalCapacityInBytes"), totalCapacityInBytesGauge);

            final Gauge<Long> usedMemoryInBytesGauge;
            if (bufferPoolMXBean.getMemoryUsed() >= 0) {
                usedMemoryInBytesGauge = new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        return bufferPoolMXBean.getMemoryUsed();
                    }
                };
                metricsByNames.put(MetricNamingUtil.join(bufferPoolName, "usedMemoryInBytes"), usedMemoryInBytesGauge);
            } else {
                usedMemoryInBytesGauge = null;
            }
        }
        this.metricsByNames = metricsByNames;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        return Collections.unmodifiableMap(metricsByNames);
    }
}
