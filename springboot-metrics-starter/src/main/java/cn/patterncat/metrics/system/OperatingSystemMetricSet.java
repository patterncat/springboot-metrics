package cn.patterncat.metrics.system;

import cn.patterncat.metrics.utils.MetricNamingUtil;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.sun.management.UnixOperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * monitor os metrics
 * Created by patterncat on 2017-03-05.
 */
public class OperatingSystemMetricSet implements MetricSet {

    private static final Logger logger = LoggerFactory.getLogger(OperatingSystemMetricSet.class);

    private OperatingSystemMXBean operatingSystemMXBean;

    private File rootFilePath;

    private ScheduledExecutorService executorService;
    private AtomicReference<Double> ioWaitPercentageHolder;

    private Gauge<Integer> availableLogicalProcessorsGauge;
    private Gauge<Double> systemLoadAverageGauge;
    private Gauge<Double> systemLoadAveragePerLogicalProcessorGauge;

    private Gauge<Double> jvmCpuBusyPercentageGauge;
    private Gauge<Double> systemCpuBusyPercentageGauge;
    private Gauge<Long> committedVirtualMemorySizeInBytesGauge;
    private Gauge<Long> totalPhysicalMemorySizeInBytesGauge;
    private Gauge<Long> freePhysicalMemorySizeInBytesGauge;
    private Gauge<Double> usedPhysicalMemoryPercentageGauge;
    private Gauge<Long> totalSwapSpaceSizeInBytesGauge;
    private Gauge<Long> freeSwapSpaceSizeInBytesGauge;
    private Gauge<Double> usedSwapSpacePercentageGauge;

    private Gauge<Long> maxFileDescriptorsGauge;
    private Gauge<Long> openFileDescriptorsGauge;
    private Gauge<Double> usedFileDescriptorsPercentageGauge;

    private Gauge<Long> totalDiskSpaceInBytesGauge;
    private Gauge<Long> freeDiskSpaceInBytesGauge;
    private Gauge<Double> usedDiskSpacePercentageGauge;

    private Gauge<Double> ioWaitPercentageGauge;

    private Map<String, Metric> metricsByNames;

    private AtomicBoolean shutdown;


    public OperatingSystemMetricSet() {
        this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.rootFilePath = new File("/");

        // Set up iowait retrieval job if needed
//        Double ioWaitPercentage = fetchIoWaitPercentage();
//        if (ioWaitPercentage != null) {
//            this.ioWaitPercentageHolder = new AtomicReference<>(ioWaitPercentage);
//
//            this.executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("OperatingSystemMetricSet-%d").build());
//            this.executorService.scheduleWithFixedDelay(new Runnable() {
//                @Override
//                public void run() {
//                    Double ioWaitPercentage = fetchIoWaitPercentage();
//                    if (ioWaitPercentage != null) {
//                        ioWaitPercentageHolder.set(ioWaitPercentage);
//                    }
//                }
//            }, 5, 10, TimeUnit.SECONDS);
//        }

        // ----- Init and assign metrics -----
        this.metricsByNames = new HashMap<>();

        // Available everywhere
        this.availableLogicalProcessorsGauge = new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return operatingSystemMXBean.getAvailableProcessors();
            }
        };
        metricsByNames.put("availableLogicalProcessors", availableLogicalProcessorsGauge);

        if (operatingSystemMXBean.getSystemLoadAverage() >= 0) {    // Where available
            this.systemLoadAverageGauge = new Gauge<Double>() {
                @Override
                public Double getValue() {
                    return operatingSystemMXBean.getSystemLoadAverage();
                }
            };
            metricsByNames.put("systemLoadAverage", systemLoadAverageGauge);

            this.systemLoadAveragePerLogicalProcessorGauge = new Gauge<Double>() {
                @Override
                public Double getValue() {
                    return operatingSystemMXBean.getSystemLoadAverage() / operatingSystemMXBean.getAvailableProcessors();
                }
            };
            metricsByNames.put("systemLoadAveragePerLogicalProcessor", systemLoadAveragePerLogicalProcessorGauge);
        }

        // Sun JVMs, incl. OpenJDK
        if (com.sun.management.OperatingSystemMXBean.class.isAssignableFrom(operatingSystemMXBean.getClass())) {
            final com.sun.management.OperatingSystemMXBean sunOsMxBean = com.sun.management.OperatingSystemMXBean.class.cast(operatingSystemMXBean);

            if (sunOsMxBean.getProcessCpuLoad() >= 0) {
                this.jvmCpuBusyPercentageGauge = new Gauge<Double>() {
                    @Override
                    public Double getValue() {
                        return sunOsMxBean.getProcessCpuLoad() * 100;
                    }
                };
                metricsByNames.put("jvmCpuBusyPercentage", jvmCpuBusyPercentageGauge);
            }

            if (sunOsMxBean.getSystemCpuLoad() >= 0) {
                this.systemCpuBusyPercentageGauge = new Gauge<Double>() {
                    @Override
                    public Double getValue() {
                        return sunOsMxBean.getSystemCpuLoad() * 100;
                    }
                };
                metricsByNames.put("systemCpuBusyPercentage", systemCpuBusyPercentageGauge);
            }

            if (sunOsMxBean.getCommittedVirtualMemorySize() >= 0) {
                this.committedVirtualMemorySizeInBytesGauge = new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        return sunOsMxBean.getCommittedVirtualMemorySize();
                    }
                };
                metricsByNames.put("committedVirtualMemorySizeInBytes", committedVirtualMemorySizeInBytesGauge);
            }

            // Physical Memory
            String physicalMemoryNamespace = "physicalMemory";

            this.totalPhysicalMemorySizeInBytesGauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return sunOsMxBean.getTotalPhysicalMemorySize();
                }
            };
            metricsByNames.put(MetricNamingUtil.join(physicalMemoryNamespace, "totalInBytes"), totalPhysicalMemorySizeInBytesGauge);

            this.freePhysicalMemorySizeInBytesGauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return sunOsMxBean.getFreePhysicalMemorySize();
                }
            };
            metricsByNames.put(MetricNamingUtil.join(physicalMemoryNamespace, "freeInBytes"), freePhysicalMemorySizeInBytesGauge);

            this.usedPhysicalMemoryPercentageGauge = new Gauge<Double>() {
                @Override
                public Double getValue() {
                    long totalPhysicalMemorySize = sunOsMxBean.getTotalPhysicalMemorySize();
                    if (totalPhysicalMemorySize == 0) {
                        return 0.0;
                    }

                    long usedPhysicalMemorySize = totalPhysicalMemorySize - sunOsMxBean.getFreePhysicalMemorySize();
                    return Double.valueOf(usedPhysicalMemorySize) / totalPhysicalMemorySize * 100;
                }
            };
            metricsByNames.put(MetricNamingUtil.join(physicalMemoryNamespace, "usedPercentage"), usedPhysicalMemoryPercentageGauge);

            // Swap Space
            String swapSpaceNamespace = "swapSpace";

            this.totalSwapSpaceSizeInBytesGauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return sunOsMxBean.getTotalSwapSpaceSize();
                }
            };
            metricsByNames.put(MetricNamingUtil.join(swapSpaceNamespace, "totalInBytes"), totalSwapSpaceSizeInBytesGauge);

            this.freeSwapSpaceSizeInBytesGauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return sunOsMxBean.getFreeSwapSpaceSize();
                }
            };
            metricsByNames.put(MetricNamingUtil.join(swapSpaceNamespace, "freeInBytes"), freeSwapSpaceSizeInBytesGauge);

            this.usedSwapSpacePercentageGauge = new Gauge<Double>() {
                @Override
                public Double getValue() {
                    long totalSwapSpaceSize = sunOsMxBean.getTotalSwapSpaceSize();
                    if (totalSwapSpaceSize == 0) {
                        return 0.0;
                    }

                    long usedSwapSpaceSize = totalSwapSpaceSize - sunOsMxBean.getFreeSwapSpaceSize();
                    return Double.valueOf(usedSwapSpaceSize) / totalSwapSpaceSize * 100;
                }
            };
            metricsByNames.put(MetricNamingUtil.join(swapSpaceNamespace, "usedPercentage"), usedSwapSpacePercentageGauge);
        }

        // File descriptors (e.g., sockets)
        String fileDescriptorsNamespace = "fileDescriptors";

        if (UnixOperatingSystemMXBean.class.isAssignableFrom(operatingSystemMXBean.getClass())) {
            final UnixOperatingSystemMXBean unixOsMxBean = UnixOperatingSystemMXBean.class.cast(operatingSystemMXBean);

            this.maxFileDescriptorsGauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return unixOsMxBean.getMaxFileDescriptorCount();
                }
            };
            metricsByNames.put(MetricNamingUtil.join(fileDescriptorsNamespace, "max"), maxFileDescriptorsGauge);

            this.openFileDescriptorsGauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return unixOsMxBean.getOpenFileDescriptorCount();
                }
            };
            metricsByNames.put(MetricNamingUtil.join(fileDescriptorsNamespace, "open"), openFileDescriptorsGauge);

            this.usedFileDescriptorsPercentageGauge = new Gauge<Double>() {
                @Override
                public Double getValue() {
                    long maxFileDescriptors = unixOsMxBean.getMaxFileDescriptorCount();
                    if (maxFileDescriptors == 0) {
                        return 0.0;
                    }
                    return Double.valueOf(unixOsMxBean.getOpenFileDescriptorCount()) / maxFileDescriptors * 100;
                }
            };
            metricsByNames.put(MetricNamingUtil.join(fileDescriptorsNamespace, "usedPercentage"), usedFileDescriptorsPercentageGauge);
        }

        // Disk space
        String diskSpaceNamespace = "diskSpace";

        if (rootFilePath.getTotalSpace() > 0) {
            this.totalDiskSpaceInBytesGauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return rootFilePath.getTotalSpace();
                }
            };
            metricsByNames.put(MetricNamingUtil.join(diskSpaceNamespace, "totalInBytes"), totalDiskSpaceInBytesGauge);

            this.freeDiskSpaceInBytesGauge = new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return rootFilePath.getFreeSpace();
                }
            };
            metricsByNames.put(MetricNamingUtil.join(diskSpaceNamespace, "freeInBytes"), freeDiskSpaceInBytesGauge);

            this.usedDiskSpacePercentageGauge = new Gauge<Double>() {
                @Override
                public Double getValue() {
                    long totalDiskSpace = rootFilePath.getTotalSpace();
                    if (totalDiskSpace == 0) {
                        return 0.0;
                    }

                    long usedDiskSpace = totalDiskSpace - rootFilePath.getFreeSpace();
                    return Double.valueOf(usedDiskSpace) / totalDiskSpace * 100;
                }
            };
            metricsByNames.put(MetricNamingUtil.join(diskSpaceNamespace, "usedPercentage"), usedDiskSpacePercentageGauge);
        }

        // CPU IO Wait
        if (ioWaitPercentageHolder != null) {
            this.ioWaitPercentageGauge = new Gauge<Double>() {
                @Override
                public Double getValue() {
                    return ioWaitPercentageHolder.get();
                }
            };
            metricsByNames.put("ioWaitPercentage", ioWaitPercentageGauge);
        }

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

        if (executorService != null) {
            try {
                executorService.shutdown();
                executorService.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

//    private static Double fetchIoWaitPercentage() {
//        // Only Linux is supported
//        if (!OS.IS_LINUX) {
//            return null;
//        }
//
//        try {
//            // Take the second sample from iostat, as the first one is a static value acquired at the machine start-up
//            Process process = Runtime.getRuntime().exec(new String[] {"bash", "-c", "iostat -c 1 2 | awk '/^ /{print $4}'"});
//
//            BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//            BufferedReader resultStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//            List<String> outputLines = new ArrayList<>();
//            String line = null;
//            while ((line = resultStream.readLine()) != null) {
//                outputLines.add(line);
//            }
//
//            boolean error = false;
//            while (errorStream.readLine() != null) {
//                error = true;
//            }
//
//            errorStream.close();
//            resultStream.close();
//
//            try {
//                int result = process.waitFor();
//                if (result != 0) {
//                    logger.debug("iostat failed with return code {}", result);
//                }
//            } catch (InterruptedException e) {
//                logger.debug("iostat was interrupted");
//            }
//
//            if (!error && outputLines.size() == 2) {
//                String iowaitPercentStr = outputLines.get(outputLines.size() - 1);
//                try {
//                    return Double.parseDouble(iowaitPercentStr);
//                } catch (NumberFormatException e) {
//                    logger.debug("Error parsing iowait value from {}", iowaitPercentStr);
//                }
//            }
//        } catch (Exception e) {
//            logger.debug("Exception occurred while executing iostat command", e);
//
//            if (InterruptedException.class.isInstance(e)) {
//                Thread.currentThread().interrupt();
//            }
//        }
//
//        return null;
//    }
}
