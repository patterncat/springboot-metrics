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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * jvm metrics facade
 */
public class JvmTotalMetricSet implements MetricSet {
    private RuntimeMXBean runtimeMXBean;

    private BufferPoolMetricSet bufferPoolMetricSet;
    private ClassLoadingMetricSet classLoadingMetricSet;
    private ThreadMetricSet threadMetricSet;
    private MemoryUsageGaugeSet memoryMetricSet; //使用codahale自带的
    private GcIntervalMetricSet gcMetricSet; //使用非累积性的

    private Gauge<Long> uptimeInMillisGauge;

    private Map<String, Metric> metricsByNames;

    private AtomicBoolean shutdown;

    private long gcStatInterval;

    public JvmTotalMetricSet(long gcStatInterval) {
        this.gcStatInterval = gcStatInterval;
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        Map<String, Metric> metricsByNames = new HashMap<>();

        this.bufferPoolMetricSet = new BufferPoolMetricSet();
        metricsByNames.put("bufferPools", bufferPoolMetricSet);

        this.classLoadingMetricSet = new ClassLoadingMetricSet();
        metricsByNames.put("classes", classLoadingMetricSet);

        this.threadMetricSet = new ThreadMetricSet();
        metricsByNames.put("threads", threadMetricSet);

        this.memoryMetricSet = new MemoryUsageGaugeSet();
        metricsByNames.put("memory", memoryMetricSet);

        this.gcMetricSet = new GcIntervalMetricSet(new GarbageCollectorMetricSet(),gcStatInterval);
        metricsByNames.put("gc", gcMetricSet);

        this.uptimeInMillisGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return runtimeMXBean.getUptime();
            }
        };
        metricsByNames.put("uptimeInMillis", uptimeInMillisGauge);

        this.metricsByNames = metricsByNames;

        this.shutdown = new AtomicBoolean(false);
    }

    @Override
    public Map<String, Metric> getMetrics() {
        return Collections.unmodifiableMap(metricsByNames);
    }

    public void shutdown() {
        if (shutdown.getAndSet(true)) {
            return;
        }

        gcMetricSet.shutdown();
    }
}
