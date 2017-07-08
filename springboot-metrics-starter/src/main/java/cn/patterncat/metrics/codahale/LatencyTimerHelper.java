package cn.patterncat.metrics.codahale;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.util.concurrent.TimeUnit;

/**
 * helper for create LatencyUtilsReservoir
 * Created by patterncat on 2017-03-26.
 */
public class LatencyTimerHelper {

    public static void updateTimer(long duration,TimeUnit unit,String key,MetricRegistry metricRegistry){
        Timer timer = buildLatencyReservoir(metricRegistry, key);
        if(timer != null){
            timer.update(duration,unit);
        }
    }

    public static Timer buildLatencyReservoir(MetricRegistry metricRegistry,String key){
        Timer timer = metricRegistry.getTimers().get(key);
        if(timer == null){
            try{
                //这个指标不错,没有请求的时候,减为0
                timer = metricRegistry.register(key,new Timer(LatencyUtilsReservoir.create()));
            }catch (Exception e){
                return metricRegistry.getTimers().get(key);
            }
        }
        return timer;
    }
}
