package com.monitor.agent.collector;

import com.monitor.agent.config.CloudServerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统信息采集器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemCollector {

    private final SystemInfo systemInfo = new SystemInfo();
    private final CloudServerProperties cloudServerProperties;

    /**
     * 采集服务器性能数据
     */
    public Map<String, Object> collectServerMetric() {
        Map<String, Object> metric = new HashMap<>();

        // 采集 CPU 信息
        metric.putAll(collectCpuInfo());

        // 采集内存信息
        metric.putAll(collectMemoryInfo());

        // 采集磁盘信息
        metric.put("disks", collectDiskInfo());

        // 采集网络信息
        metric.put("networks", collectNetworkInfo());

        // 采集进程数
        metric.put("processCount", collectProcessCount());

        // 采集时间
        metric.put("collectTime", System.currentTimeMillis() / 1000);

        // 服务器编码和 agent 版本
        metric.put("serverCode", getServerCode());
        metric.put("agentVersion", "1.0.0");

        return metric;
    }

    /**
     * 采集 CPU 信息
     */
    private Map<String, Object> collectCpuInfo() {
        Map<String, Object> cpuInfo = new HashMap<>();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();

        // CPU 使用率 - OSHI 6.x 需要传入采样时间
        double cpuUsage = processor.getSystemCpuLoad(1000) * 100;
        cpuInfo.put("cpuUsage", cpuUsage);

        // CPU 负载
        double[] loadAverage = processor.getSystemLoadAverage(3);
        if (loadAverage[0] >= 0) cpuInfo.put("cpuLoad1m", loadAverage[0]);
        if (loadAverage[1] >= 0) cpuInfo.put("cpuLoad5m", loadAverage[1]);
        if (loadAverage[2] >= 0) cpuInfo.put("cpuLoad15m", loadAverage[2]);

        log.debug("CPU Usage: {}%, Load Average: {}", cpuUsage, loadAverage);
        return cpuInfo;
    }

    /**
     * 采集内存信息
     */
    private Map<String, Object> collectMemoryInfo() {
        Map<String, Object> memoryInfo = new HashMap<>();
        GlobalMemory memory = systemInfo.getHardware().getMemory();

        long total = memory.getTotal() / 1024 / 1024; // MB
        long available = memory.getAvailable() / 1024 / 1024; // MB
        long used = total - available;
        double usage = (double) used / total * 100;

        memoryInfo.put("memoryTotal", total);
        memoryInfo.put("memoryUsed", used);
        memoryInfo.put("memoryUsage", usage);

        log.debug("Memory: {}/{} MB ({}%)", used, total, usage);
        return memoryInfo;
    }

    /**
     * 采集磁盘信息
     */
    private List<Map<String, Object>> collectDiskInfo() {
        List<Map<String, Object>> disks = new ArrayList<>();
        OperatingSystem os = systemInfo.getOperatingSystem();

        // 获取文件系统存储
        List<OSFileStore> fileStores = os.getFileSystem().getFileStores();

        for (OSFileStore fs : fileStores) {
            // 跳过特殊挂载点
            String path = fs.getMount();
            if (path.startsWith("/proc") || path.startsWith("/sys") ||
                path.startsWith("/dev") || path.startsWith("/run")) {
                continue;
            }

            Map<String, Object> disk = new HashMap<>();
            long total = fs.getTotalSpace() / 1024 / 1024; // MB
            long used = fs.getTotalSpace() - fs.getUsableSpace();
            used = used / 1024 / 1024; // MB
            double usage = total > 0 ? (double) used / total * 100 : 0;

            disk.put("path", path);
            disk.put("total", total);
            disk.put("used", used);
            disk.put("usage", usage);

            disks.add(disk);
        }

        log.debug("Disk count: {}", disks.size());
        return disks;
    }

    /**
     * 采集网络信息
     */
    private List<Map<String, Object>> collectNetworkInfo() {
        List<Map<String, Object>> networks = new ArrayList<>();
        List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();

        for (NetworkIF net : networkIFs) {
            // 跳过回环接口
            if (net.getName().equals("lo") || net.getName().equals("Loopback")) {
                continue;
            }

            Map<String, Object> network = new HashMap<>();
            network.put("iface", net.getName());
            network.put("rxBytes", net.getBytesRecv());
            network.put("txBytes", net.getBytesSent());

            networks.add(network);
        }

        log.debug("Network interfaces: {}", networks.size());
        return networks;
    }

    /**
     * 采集进程数
     */
    private int collectProcessCount() {
        OperatingSystem os = systemInfo.getOperatingSystem();
        return os.getProcesses().size();
    }

    /**
     * 获取服务器编码（从配置文件或主机名）
     */
    private String getServerCode() {
        // 可以通过主机名或配置文件获取
        // 这里优先使用配置文件，如果没有则使用主机名
        String serverCode = System.getProperty("server.code");
        if (serverCode == null || serverCode.isEmpty()) {
            try {
                serverCode = java.net.InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                serverCode = "unknown-server";
            }
        }
        return serverCode;
    }
}
