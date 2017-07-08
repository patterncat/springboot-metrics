package cn.patterncat.metrics.network;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationPid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by patterncat on 2017-01-27.
 */
public class TcpStatWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpStatWrapper.class);

    private String pid = new ApplicationPid().toString();

    private String STATFILE = "/proc/" + pid + "/net/netstat";
    private String SNMPFILE = "/proc/net/snmp";

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentHashMap<String, Number> statData = new ConcurrentHashMap<>();

    public TcpStatWrapper(long intervalInMs) {
        //register shutdown hoot
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                scheduledExecutorService.shutdown();
            }
        });

        //ensure data is ready for use after new
        getNetData();

        //start to get netstat data
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    LOGGER.trace("start to read proc net");
                    getNetData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 5000 , intervalInMs, TimeUnit.MILLISECONDS);
    }

    public Map<String, Number> query() {
        return statData;
    }

    public void getNetData(){
        queryFile(SNMPFILE, "Tcp:");
        queryFile(STATFILE, "TcpExt:");
    }

    private void queryFile(String file, String prefix) {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(file));
            String line;

            while ((line = r.readLine()) != null) {
                String[] keys = line.trim().split("\\s+");
                line = r.readLine();
                String[] values = line.trim().split("\\s+");
                if (!keys[0].trim().equals(prefix))
                    continue;

                for (int i = 0; i < keys.length; i++) {
                    Double value = parseStringNumber(values[i], Double.NaN);
                    statData.put(keys[i], value);
                }
            }
            r.close();
        } catch (Exception e) {
            //ignore
        } finally {
            IOUtils.closeQuietly(r);
        }
    }

    /**
     * <p>A compact and exception free number parser.<p>
     * <p>If the string can be parsed as the specified type, it return the default value<p>
     *
     * @param toParse    The string to parse
     * @param defaultVal A default value to use it the string can't be parsed
     * @return An Number object using the same type than the default value.
     */
    @SuppressWarnings("unchecked")
    public static <NumberClass extends Number> NumberClass parseStringNumber(String toParse, NumberClass defaultVal) {
        if (toParse == null || "".equals(toParse))
            return defaultVal;

        try {
            Class<NumberClass> clazz = (Class<NumberClass>) defaultVal.getClass();
            Constructor<NumberClass> c = clazz.getConstructor(String.class);
            return c.newInstance(toParse);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return defaultVal;
    }

    public String getName() {
        return "netstat";
    }
}
