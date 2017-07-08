package cn.patterncat.metrics.tomcat;

import cn.patterncat.metrics.config.AdvancedThreadExecutor;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.beans.BeansException;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by patterncat on 2017-01-27.
 */
public class AdvancedTomcatMetrics implements PublicMetrics,ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Collection<Metric<?>> metrics() {
        if (this.applicationContext instanceof EmbeddedWebApplicationContext) {
            EmbeddedServletContainer embeddedServletContainer = ((EmbeddedWebApplicationContext)applicationContext)
                    .getEmbeddedServletContainer();
            if (embeddedServletContainer instanceof TomcatEmbeddedServletContainer) {
                Connector connector = ((TomcatEmbeddedServletContainer) embeddedServletContainer).getTomcat().getConnector();
                ProtocolHandler handler = connector.getProtocolHandler();
                AdvancedThreadExecutor executor = (AdvancedThreadExecutor) handler.getExecutor();
                //register tomcat thread pool stat
                List<Metric<?>> metrics = new ArrayList<Metric<?>>();

                //queue task
                metrics.add(new Metric<Long>("tomcat.queue.completed_task_count",executor.getCompletedTaskCount()));
                metrics.add(new Metric<Integer>("tomcat.queue.current_task_size",executor.getQueueSize()));
                metrics.add(new Metric<Integer>("tomcat.queue.config_max_size",executor.getMaxQueueSize()));

                //pool
                metrics.add(new Metric<Integer>("tomcat.pool.active_thread_count",executor.getActiveCount()));
                metrics.add(new Metric<Integer>("tomcat.pool.current_thread_count",executor.getPoolSize()));
                metrics.add(new Metric<Integer>("tomcat.pool.largest_thread_count",executor.getLargestPoolSize()));
                metrics.add(new Metric<Integer>("tomcat.pool.config_core_size",executor.getCorePoolSize()));
                metrics.add(new Metric<Integer>("tomcat.pool.config_max_size",executor.getMaxThreads()));

                //对应executor的corePoolSize
//                metrics.add(new Metric<Integer>("tomcat.thread.min_spare_count",executor.getMinSpareThreads()));
                //对应executor的maximumPoolSize
//                metrics.add(new Metric<Integer>("tomcat.thread.max_count",executor.getMaxThreads()));

                return metrics;
            }
        }
        return Collections.emptySet();
    }
}
