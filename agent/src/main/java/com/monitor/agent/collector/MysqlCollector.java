package com.monitor.agent.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MySQL 监控采集器 - 重构版
 * 提供更全面、准确的 MySQL 性能监控指标采集
 */
@Slf4j
@Component
public class MysqlCollector {

    // 各种监控指标的上次采集值和时间戳，用于计算速率
    private final Map<String, CounterInfo> counterCache = new ConcurrentHashMap<>();

    // MySQL 版本信息缓存
    private String mysqlVersion = null;
    private boolean supportsSlowQueries = true; // 假设支持，后续探测

    /**
     * 采集 MySQL 性能数据
     * @param url MySQL 连接 URL
     * @param username 用户名
     * @param password 密码
     * @return 采集到的性能数据
     */
    public Map<String, Object> collectMysqlMetric(String url, String username, String password) {
        Map<String, Object> metric = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            // 确保获取版本信息
            detectMysqlVersion(conn);

            // 采集各种性能指标 - 使用驼峰命名以匹配服务端 DTO
            metric.putAll(collectConnections(conn));
            metric.putAll(collectQpsTps(conn));
            metric.putAll(collectSlowQueries(conn));
            metric.putAll(collectThreadInfo(conn));
            metric.putAll(collectTraffic(conn));
            metric.putAll(collectInnodbInfo(conn));
            metric.putAll(collectBufferPoolInfo(conn));
            metric.putAll(collectTableLocks(conn));
            metric.putAll(collectTemporaryTables(conn));
            metric.putAll(collectSortOperations(conn));
            metric.putAll(collectReplicationStatus(conn));

            // 设置采集时间
            metric.put("collectTime", System.currentTimeMillis() / 1000);

        } catch (SQLException e) {
            log.error("Error collecting MySQL metrics: {}", e.getMessage());
            metric.put("error", e.getMessage());
        }

        return metric;
    }

    /**
     * 检测 MySQL 版本
     */
    private void detectMysqlVersion(Connection conn) {
        if (mysqlVersion == null) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
                if (rs.next()) {
                    mysqlVersion = rs.getString(1);
                    log.info("MySQL version detected: {}", mysqlVersion);

                    // 根据版本判断是否支持某些特性
                    if (mysqlVersion.startsWith("8.")) {
                        // MySQL 8.0+ 某些状态变量可能已被移除
                        supportsSlowQueries = checkSlowQueriesSupport(conn);
                    }
                }
            } catch (SQLException e) {
                log.warn("Could not detect MySQL version: {}", e.getMessage());
                mysqlVersion = "unknown";
            }
        }
    }

    /**
     * 检查是否支持 Slow_queries 变量
     */
    private boolean checkSlowQueriesSupport(Connection conn) {
        try {
            Long value = getGlobalStatus(conn, "Slow_queries");
            return value != null;
        } catch (SQLException e) {
            log.debug("Slow_queries not supported: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 采集连接数信息
     */
    private Map<String, Object> collectConnections(Connection conn) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        Long currentConn = getGlobalStatus(conn, "Threads_connected");
        Long maxConn = getGlobalStatus(conn, "Max_connections");

        if (currentConn != null) {
            result.put("connectionsCurrent", currentConn.intValue());
        }
        if (maxConn != null) {
            result.put("connectionsMax", maxConn.intValue());
        }

        // 计算连接使用率
        if (currentConn != null && maxConn != null && maxConn > 0) {
            double usage = ((double) currentConn / maxConn) * 100;
            result.put("connectionsUsage", Math.round(usage * 100) / 100.0);
        }

        return result;
    }

    /**
     * 采集 QPS/TPS 信息
     */
    private Map<String, Object> collectQpsTps(Connection conn) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        // 采集基础指标
        Long questions = getGlobalStatus(conn, "Questions");
        Long commits = getGlobalStatus(conn, "Com_commit");
        Long rollbacks = getGlobalStatus(conn, "Com_rollback");
        long currentTime = System.currentTimeMillis() / 1000;

        // 计算 QPS
        if (questions != null) {
            RateInfo qpsRate = calculateRate("qps", questions, currentTime);
            result.put("qps", qpsRate.rate);
            result.put("qpsTotal", questions);
        }

        // 计算 TPS
        if (commits != null && rollbacks != null) {
            long totalTransactions = commits + rollbacks;
            RateInfo tpsRate = calculateRate("tps", totalTransactions, currentTime);
            result.put("tps", tpsRate.rate);
            result.put("tpsTotal", totalTransactions);
        }

        return result;
    }

    /**
     * 采集慢查询信息
     */
    private Map<String, Object> collectSlowQueries(Connection conn) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        long currentTime = System.currentTimeMillis() / 1000;

        if (supportsSlowQueries) {
            Long slowQueriesTotal = getGlobalStatus(conn, "Slow_queries");
            if (slowQueriesTotal != null) {
                RateInfo rateInfo = calculateRate("slow_queries", slowQueriesTotal, currentTime);
                result.put("slowQueriesRate", rateInfo.rate);
                result.put("slowQueriesTotal", slowQueriesTotal);
            } else {
                // 如果获取不到，使用 0
                result.put("slowQueriesRate", 0L);
                result.put("slowQueriesTotal", 0L);
                supportsSlowQueries = false; // 标记为不支持
            }
        } else {
            // 明确标记为不支持
            result.put("slowQueriesRate", 0L);
            result.put("slowQueriesTotal", 0L);
        }

        return result;
    }

    /**
     * 采集线程信息
     */
    private Map<String, Object> collectThreadInfo(Connection conn) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        Long runningThreads = getGlobalStatus(conn, "Threads_running");
        Long connectedThreads = getGlobalStatus(conn, "Threads_connected");
        Long createdThreads = getGlobalStatus(conn, "Threads_created");

        if (runningThreads != null) {
            result.put("threadsRunning", runningThreads.intValue());
        }
        if (connectedThreads != null) {
            result.put("threadsConnected", connectedThreads.intValue());
        }
        if (createdThreads != null) {
            result.put("threadsCreated", createdThreads);
        }

        return result;
    }

    /**
     * 采集流量信息
     */
    private Map<String, Object> collectTraffic(Connection conn) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        Long bytesReceived = getGlobalStatus(conn, "Bytes_received");
        Long bytesSent = getGlobalStatus(conn, "Bytes_sent");

        if (bytesReceived != null) {
            result.put("bytesReceived", bytesReceived);
        }
        if (bytesSent != null) {
            result.put("bytesSent", bytesSent);
        }

        return result;
    }

    /**
     * 采集 InnoDB 信息
     */
    private Map<String, Object> collectInnodbInfo(Connection conn) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        Long rowLockTime = getGlobalStatus(conn, "Innodb_row_lock_time");
        Long rowLockWaits = getGlobalStatus(conn, "Innodb_row_lock_waits");
        Long currentRowLockWaits = getGlobalStatus(conn, "Innodb_row_lock_current_waits");

        if (rowLockTime != null) {
            result.put("innodbRowLockTime", rowLockTime);
        }
        if (rowLockWaits != null) {
            result.put("innodbRowLockWaits", rowLockWaits);
        }
        if (currentRowLockWaits != null) {
            result.put("innodbRowLockCurrentWaits", currentRowLockWaits);
        }

        return result;
    }

    /**
     * 采集缓冲池信息
     */
    private Map<String, Object> collectBufferPoolInfo(Connection conn) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        Long bufferSize = getGlobalStatus(conn, "innodb_buffer_pool_size");
        Long freePages = getGlobalStatus(conn, "Innodb_buffer_pool_pages_free");
        Long dirtyPages = getGlobalStatus(conn, "Innodb_buffer_pool_pages_dirty");
        Long readRequests = getGlobalStatus(conn, "Innodb_buffer_pool_read_requests");
        Long physicalReads = getGlobalStatus(conn, "Innodb_buffer_pool_reads");
        Long writeRequests = getGlobalStatus(conn, "Innodb_buffer_pool_write_requests");

        if (bufferSize != null) {
            result.put("bufferPoolSize", bufferSize);
        }
        if (freePages != null) {
            result.put("bufferPoolFreePages", freePages);
        }
        if (dirtyPages != null) {
            result.put("bufferPoolDirtyPages", dirtyPages);
        }
        if (readRequests != null) {
            result.put("bufferPoolReadRequests", readRequests);
        }
        if (physicalReads != null) {
            result.put("bufferPoolReads", physicalReads);
        }
        if (writeRequests != null) {
            result.put("bufferPoolWriteRequests", writeRequests);
        }

        // 计算缓冲池命中率
        if (readRequests != null && physicalReads != null && readRequests > 0) {
            double hitRate = ((double) (readRequests - physicalReads) / readRequests) * 100;
            result.put("bufferPoolHitRate", Math.round(hitRate * 100) / 100.0);
        }

        return result;
    }

    /**
     * 采集表锁信息
     */
    private Map<String, Object> collectTableLocks(Connection conn) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        Long waited = getGlobalStatus(conn, "Table_locks_waited");
        Long immediate = getGlobalStatus(conn, "Table_locks_immediate");

        if (waited != null) {
            result.put("tableLocksWaited", waited);
        }
        if (immediate != null) {
            result.put("tableLocksImmediate", immediate);
        }

        if (waited != null && immediate != null) {
            long total = waited + immediate;
            if (total > 0) {
                double waitRate = ((double) waited / total) * 100;
                result.put("tableLockWaitRate", Math.round(waitRate * 100) / 100.0);
            } else {
                result.put("tableLockWaitRate", 0.0);
            }
        }

        return result;
    }

    /**
     * 采集临时表信息
     */
    private Map<String, Object> collectTemporaryTables(Connection conn) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        Long created = getGlobalStatus(conn, "Created_tmp_tables");
        Long diskCreated = getGlobalStatus(conn, "Created_tmp_disk_tables");

        if (created != null) {
            result.put("tmpTablesCreated", created);
        }
        if (diskCreated != null) {
            result.put("tmpDiskTablesCreated", diskCreated);
        }

        if (created != null && diskCreated != null && created > 0) {
            double diskRate = ((double) diskCreated / created) * 100;
            result.put("tmpDiskTableRate", Math.round(diskRate * 100) / 100.0);
        }

        return result;
    }

    /**
     * 采集排序操作信息
     */
    private Map<String, Object> collectSortOperations(Connection conn) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        Long rows = getGlobalStatus(conn, "Sort_rows");
        Long scan = getGlobalStatus(conn, "Sort_scan");
        Long range = getGlobalStatus(conn, "Sort_range");
        Long mergePasses = getGlobalStatus(conn, "Sort_merge_passes");

        if (rows != null) {
            result.put("sortRows", rows);
        }
        if (scan != null) {
            result.put("sortScan", scan);
        }
        if (range != null) {
            result.put("sortRange", range);
        }
        if (mergePasses != null) {
            result.put("sortMergePasses", mergePasses);
        }

        return result;
    }

    /**
     * 采集复制状态信息（主从复制相关）
     */
    private Map<String, Object> collectReplicationStatus(Connection conn) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW SLAVE STATUS")) {
            if (rs.next()) {
                String ioRunning = rs.getString("Slave_IO_Running");
                String sqlRunning = rs.getString("Slave_SQL_Running");
                int secondsBehindMaster = rs.getInt("Seconds_Behind_Master");

                result.put("slaveIoRunning", "Yes".equalsIgnoreCase(ioRunning));
                result.put("slaveSqlRunning", "Yes".equalsIgnoreCase(sqlRunning));
                result.put("secondsBehindMaster", secondsBehindMaster == 0 ? 0 : secondsBehindMaster);
            } else {
                // 如果没有从库配置，设置默认值
                result.put("slaveIoRunning", null);
                result.put("slaveSqlRunning", null);
                result.put("secondsBehindMaster", null);
            }
        } catch (SQLException e) {
            // 可能没有从库配置，忽略错误
            log.debug("Could not get slave status: {}", e.getMessage());
            // 设置默认值
            result.put("slaveIoRunning", null);
            result.put("slaveSqlRunning", null);
            result.put("secondsBehindMaster", null);
        }

        return result;
    }

    /**
     * 获取全局状态值（可返回 null）
     */
    private Long getGlobalStatus(Connection conn, String variableName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SHOW GLOBAL STATUS LIKE ?")) {
            ps.setString(1, variableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long value = rs.getLong(2);
                    if (rs.wasNull()) {
                        return null; // 变量不存在
                    }
                    return value;
                }
            }
        }
        return null; // 查询无结果
    }

    /**
     * 计算速率信息
     */
    private RateInfo calculateRate(String key, Long currentValue, long currentTime) {
        if (currentValue == null) {
            return new RateInfo(0L, false);
        }

        CounterInfo prevInfo = counterCache.get(key);
        long rate = 0;

        if (prevInfo != null) {
            long timeDiff = currentTime - prevInfo.timestamp;
            if (timeDiff > 0) {
                long valueDiff = currentValue - prevInfo.value;
                if (valueDiff >= 0) { // 确保是非递减的计数器
                    rate = valueDiff / timeDiff;
                } else {
                    // 计数器重置了，使用当前值
                    rate = 0;
                }
            }
        }

        // 更新缓存
        counterCache.put(key, new CounterInfo(currentValue, currentTime));

        return new RateInfo(rate, true);
    }

    /**
     * 计数器信息类
     */
    private static class CounterInfo {
        final long value;
        final long timestamp;

        CounterInfo(long value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    /**
     * 速率信息类
     */
    private static class RateInfo {
        final long rate;
        final boolean calculated;

        RateInfo(long rate, boolean calculated) {
            this.rate = rate;
            this.calculated = calculated;
        }
    }
}