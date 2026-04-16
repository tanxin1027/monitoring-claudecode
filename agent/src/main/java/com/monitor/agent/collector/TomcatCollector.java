package com.monitor.agent.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.management.*;
import javax.management.remote.*;
import java.io.IOException;
import java.util.*;

/**
 * Tomcat 监控采集器 (通过 JMX)
 */
@Slf4j
@Component
public class TomcatCollector {

    /**
     * 采集 Tomcat 性能数据
     * @param host JMX 主机
     * @param port JMX 端口
     * @return 性能指标
     */
    public Map<String, Object> collectTomcatMetric(String host, int port) {
        Map<String, Object> metric = new HashMap<>();

        JMXConnector connector = null;
        try {
            // 连接 JMX
            JMXServiceURL serviceURL = new JMXServiceURL(
                "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
            connector = JMXConnectorFactory.connect(serviceURL, null);
            MBeanServerConnection mbsc = connector.getMBeanServerConnection();

            log.debug("Successfully connected to Tomcat JMX: {}:{}", host, port);

            // 调试：列出所有可用的 MBean
            Set<ObjectName> allMBeans = mbsc.queryNames(null, null);
            log.debug("Available MBeans count: {}", allMBeans.size());
            for (ObjectName name : allMBeans) {
                String nameStr = name.toString();
                if (nameStr.contains("ThreadPool") || nameStr.contains("RequestProcessor") ||
                    nameStr.contains("Context") || nameStr.contains("GarbageCollector")) {
                    log.debug("Found relevant MBean: {}", nameStr);
                }
            }

            // 采集 JVM 内存
            metric.putAll(collectJvmMemory(mbsc));

            // 采集 GC 信息
            metric.putAll(collectGcInfo(mbsc));

            // 采集线程池信息
            metric.putAll(collectThreadPoolInfo(mbsc));

            // 采集 Request 信息
            metric.putAll(collectRequestInfo(mbsc));

            // 采集 Session 信息
            metric.putAll(collectSessionInfo(mbsc));

            // 采集运行时间
            metric.putAll(collectUptime(mbsc));

        } catch (IOException e) {
            log.error("Error connecting to Tomcat JMX: {}:{}", host, port, e);
            metric.put("error", e.getMessage());
        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (IOException e) {
                    log.warn("Error closing JMX connector", e);
                }
            }
        }

        metric.put("collectTime", System.currentTimeMillis() / 1000);
        log.debug("Collected Tomcat metrics: {}", metric);
        return metric;
    }

    private Map<String, Object> collectJvmMemory(MBeanServerConnection mbsc) throws IOException {
        Map<String, Object> result = new HashMap<>();

        try {
            ObjectName memoryMBean = new ObjectName("java.lang:type=Memory");
            javax.management.openmbean.CompositeData heapUsageData = (javax.management.openmbean.CompositeData)
                mbsc.getAttribute(memoryMBean, "HeapMemoryUsage");
            javax.management.openmbean.CompositeData nonHeapUsageData = (javax.management.openmbean.CompositeData)
                mbsc.getAttribute(memoryMBean, "NonHeapMemoryUsage");

            // 从 CompositeData 中提取数据（使用 Number 类型兼容 Integer 和 Long）
            Number heapUsedNum = (Number) heapUsageData.get("used");
            Number heapMaxNum = (Number) heapUsageData.get("max");
            Number nonHeapUsedNum = (Number) nonHeapUsageData.get("used");

            long heapUsed = heapUsedNum != null ? heapUsedNum.longValue() : 0;
            long heapMax = heapMaxNum != null ? heapMaxNum.longValue() : 0;
            long nonHeapUsed = nonHeapUsedNum != null ? nonHeapUsedNum.longValue() : 0;

            result.put("jvmHeapUsed", heapUsed / 1024 / 1024); // MB
            result.put("jvmHeapMax", heapMax / 1024 / 1024); // MB
            result.put("jvmNonHeapUsed", nonHeapUsed / 1024 / 1024); // MB
            log.debug("JVM Memory: used={}MB, max={}MB, nonHeap={}MB",
                heapUsed / 1024 / 1024, heapMax / 1024 / 1024, nonHeapUsed / 1024 / 1024);
        } catch (Exception e) {
            log.error("Error collecting JVM memory info", e);
        }

        return result;
    }

    private Map<String, Object> collectGcInfo(MBeanServerConnection mbsc) throws IOException {
        Map<String, Object> result = new HashMap<>();

        try {
            Set<ObjectName> gcMBeans = mbsc.queryNames(
                new ObjectName("java.lang:type=GarbageCollector,name=*"), null);
            log.debug("Found {} GarbageCollector MBeans", gcMBeans.size());

            long totalGcCount = 0;
            long totalGcTime = 0;

            for (ObjectName gcMBean : gcMBeans) {
                log.debug("Querying GC MBean: {}", gcMBean);
                Number gcCount = (Number) mbsc.getAttribute(gcMBean, "CollectionCount");
                Number gcTime = (Number) mbsc.getAttribute(gcMBean, "CollectionTime");
                long countVal = gcCount != null ? gcCount.longValue() : 0;
                long timeVal = gcTime != null ? gcTime.longValue() : 0;
                log.debug("GC MBean {} - CollectionCount={}, CollectionTime={}", gcMBean, countVal, timeVal);
                totalGcCount += countVal;
                totalGcTime += timeVal;
            }

            result.put("gcCount", totalGcCount);
            result.put("gcTime", totalGcTime);
            log.debug("Total GC: count={}, time={}ms", totalGcCount, totalGcTime);
        } catch (Exception e) {
            log.error("Error collecting GC info", e);
        }

        return result;
    }

    private Map<String, Object> collectThreadPoolInfo(MBeanServerConnection mbsc) throws IOException {
        Map<String, Object> result = new HashMap<>();

        try {
            // Tomcat 线程池 - 尝试多种 ObjectName 格式
            // Tomcat 8/9: Catalina:type=ThreadPool,name="http-nio-8080"
            // Tomcat 10+: Catalina:type=ThreadPool,name="http-nio-8080"
            Set<ObjectName> threadPools = mbsc.queryNames(
                new ObjectName("Catalina:type=ThreadPool,name=*"), null);
            log.debug("Found {} ThreadPool MBeans", threadPools.size());

            int totalThreads = 0;
            int totalBusy = 0;

            for (ObjectName tp : threadPools) {
                log.debug("Querying ThreadPool MBean: {}", tp);
                Number currentThreads = (Number) mbsc.getAttribute(tp, "currentThreadCount");
                Number busyThreads = (Number) mbsc.getAttribute(tp, "currentThreadsBusy");
                int threadsVal = currentThreads != null ? currentThreads.intValue() : 0;
                int busyVal = busyThreads != null ? busyThreads.intValue() : 0;
                log.debug("ThreadPool {} - currentThreadCount={}, currentThreadsBusy={}", tp, threadsVal, busyVal);
                totalThreads += threadsVal;
                totalBusy += busyVal;
            }

            if (totalThreads == 0) {
                log.warn("No thread pool data collected. Check if Tomcat JMX is properly configured.");
            }

            result.put("threadCount", totalThreads);
            result.put("threadBusy", totalBusy);
            log.debug("Total threads: count={}, busy={}", totalThreads, totalBusy);
        } catch (Exception e) {
            log.error("Error collecting thread pool info", e);
        }

        return result;
    }

    private Map<String, Object> collectRequestInfo(MBeanServerConnection mbsc) throws IOException {
        Map<String, Object> result = new HashMap<>();

        try {
            // Tomcat RequestProcessor - 尝试多种 ObjectName 格式
            // Tomcat 8/9: Catalina:type=GlobalRequestProcessor,name="http-nio-8080"
            Set<ObjectName> requestHandlers = mbsc.queryNames(
                new ObjectName("Catalina:type=GlobalRequestProcessor,name=*"), null);
            log.debug("Found {} GlobalRequestProcessor MBeans", requestHandlers.size());

            long totalRequestCount = 0;
            long totalErrorCount = 0;
            long totalBytesSent = 0;
            long totalBytesReceived = 0;
            long maxTime = 0;

            for (ObjectName rp : requestHandlers) {
                log.debug("Querying RequestProcessor MBean: {}", rp);
                Number requestCount = (Number) mbsc.getAttribute(rp, "requestCount");
                Number errorCount = (Number) mbsc.getAttribute(rp, "errorCount");
                Number bytesSent = (Number) mbsc.getAttribute(rp, "bytesSent");
                Number bytesReceived = (Number) mbsc.getAttribute(rp, "bytesReceived");
                Number processingTime = (Number) mbsc.getAttribute(rp, "processingTime");

                long reqCount = requestCount != null ? requestCount.longValue() : 0;
                long errCount = errorCount != null ? errorCount.longValue() : 0;
                long bSent = bytesSent != null ? bytesSent.longValue() : 0;
                long bRecv = bytesReceived != null ? bytesReceived.longValue() : 0;
                long procTime = processingTime != null ? processingTime.longValue() : 0;

                log.debug("RequestProcessor {} - requestCount={}, errorCount={}, bytesSent={}, bytesReceived={}, processingTime={}",
                    rp, reqCount, errCount, bSent, bRecv, procTime);

                totalRequestCount += reqCount;
                totalErrorCount += errCount;
                totalBytesSent += bSent;
                totalBytesReceived += bRecv;

                if (procTime > maxTime) {
                    maxTime = procTime;
                }
            }

            if (totalRequestCount == 0) {
                log.warn("No request data collected. This may be normal if no requests have been processed yet.");
            }

            result.put("requestCount", totalRequestCount);
            result.put("errorCount", totalErrorCount);
            result.put("bytesSent", totalBytesSent);
            result.put("bytesReceived", totalBytesReceived);
            result.put("maxTime", maxTime);
            log.debug("Request totals: count={}, errors={}, bytesSent={}, bytesReceived={}, maxTime={}",
                totalRequestCount, totalErrorCount, totalBytesSent, totalBytesReceived, maxTime);
        } catch (Exception e) {
            log.error("Error collecting request info", e);
        }

        return result;
    }

    private Map<String, Object> collectSessionInfo(MBeanServerConnection mbsc) throws IOException {
        Map<String, Object> result = new HashMap<>();

        try {
            // Tomcat Context - 尝试多种 ObjectName 格式
            // 注意：path 可能包含特殊字符，需要使用通配符
            // Tomcat 8/9: Catalina:type=Context,path="/",host=localhost
            Set<ObjectName> contexts = mbsc.queryNames(
                new ObjectName("Catalina:type=Context,*"), null);
            log.debug("Found {} Context MBeans", contexts.size());

            int activeSessions = 0;
            int expiredSessions = 0;

            for (ObjectName ctx : contexts) {
                log.debug("Querying Context MBean: {}", ctx);
                Number sessions = (Number) mbsc.getAttribute(ctx, "activeSessions");
                Number expired = (Number) mbsc.getAttribute(ctx, "sessionCounter");
                int sessVal = sessions != null ? sessions.intValue() : 0;
                int expVal = expired != null ? expired.intValue() : 0;
                log.debug("Context {} - activeSessions={}, sessionCounter={}", ctx, sessVal, expVal);
                activeSessions += sessVal;
                expiredSessions += expVal;
            }

            result.put("sessionCount", activeSessions);
            result.put("sessionExpired", expiredSessions);
            log.debug("Session totals: active={}, expired={}", activeSessions, expiredSessions);
        } catch (Exception e) {
            log.error("Error collecting session info", e);
        }

        return result;
    }

    private Map<String, Object> collectUptime(MBeanServerConnection mbsc) throws IOException {
        Map<String, Object> result = new HashMap<>();

        try {
            ObjectName runtimeMBean = new ObjectName("java.lang:type=Runtime");
            Number uptime = (Number) mbsc.getAttribute(runtimeMBean, "Uptime");
            long uptimeVal = uptime != null ? uptime.longValue() : 0;
            result.put("uptime", uptimeVal);
            log.debug("Uptime: {}ms", uptimeVal);
        } catch (Exception e) {
            log.error("Error collecting uptime info", e);
        }

        return result;
    }
}
