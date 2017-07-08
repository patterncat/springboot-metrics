package cn.patterncat.metrics.config;

import com.codahale.metrics.MetricRegistry;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardThreadExecutor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by patterncat on 2017-04-02.
 */
public class AdvancedThreadExecutor extends StandardThreadExecutor {

    @Autowired
    MetricRegistry metricRegistry;

    @Override
    protected void startInternal() throws LifecycleException {
        super.namePrefix = "custom-tomcat-";
        super.startInternal();
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                //add stat
                metricRegistry.counter("tomcat.executor.reject_count").inc();
                throw new RejectedExecutionException("Task " + r.toString() +
                        " rejected from " +
                        executor.toString());
            }
        });
    }
}
