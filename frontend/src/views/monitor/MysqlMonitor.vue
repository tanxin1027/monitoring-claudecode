<template>
  <div class="mysql-monitor-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>MySQL 监控</span>
          <div class="header-actions">
            <el-select
              v-model="selectedInstanceId"
              placeholder="选择实例"
              style="width: 300px; margin-right: 12px;"
              @change="handleInstanceChange"
            >
              <el-option
                v-for="item in instances"
                :key="item.id"
                :label="item.instanceName"
                :value="item.id"
              />
            </el-select>
            <el-button :icon="Refresh" circle @click="loadMetric" />
          </div>
        </div>
      </template>

      <!-- 实例信息 -->
      <el-alert
        v-if="selectedInstance"
        :title="`实例: ${selectedInstance.instanceName} (${selectedInstance.instanceCode})`"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <!-- 核心指标卡片 -->
      <el-row :gutter="20" class="metric-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">当前连接数</div>
            <div class="metric-value">{{ metric.connectionsCurrent || 0 }}</div>
            <div class="metric-label">最大：{{ metric.connectionsMax || 0 }}</div>
            <div class="metric-sub-label" :class="getConnectionUsageClass(metric.connectionsUsage)">
              使用率：{{ metric.connectionsUsage?.toFixed(2) || 0 }}%
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">QPS</div>
            <div class="metric-value">{{ metric.qps?.toFixed(2) || 0 }}</div>
            <div class="metric-label">总计：{{ metric.qpsTotal || 0 }}</div>
            <div class="metric-sub-label">每秒查询数</div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">TPS</div>
            <div class="metric-value">{{ metric.tps?.toFixed(2) || 0 }}</div>
            <div class="metric-label">总计：{{ metric.tpsTotal || 0 }}</div>
            <div class="metric-sub-label">每秒事务数</div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">慢查询速率</div>
            <div class="metric-value" :class="getSlowQueryClass(metric.slowQueriesRate)">
              {{ metric.slowQueriesRate || 0 }} <span class="unit">/秒</span>
            </div>
            <div class="metric-label">累计：{{ metric.slowQueriesTotal || 0 }}</div>
            <div class="metric-sub-label">当前慢查询数</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 缓冲池和性能指标 -->
      <el-row :gutter="20" class="metric-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">缓冲池命中率</div>
            <div class="metric-value" :class="getHitRateClass(metric.bufferPoolHitRate)">
              {{ metric.bufferPoolHitRate?.toFixed(2) || 0 }}%
            </div>
            <div class="metric-label">理想值 > 95%</div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">磁盘临时表比率</div>
            <div class="metric-value" :class="getDiskTableRateClass(metric.tmpDiskTableRate)">
              {{ metric.tmpDiskTableRate?.toFixed(2) || 0 }}%
            </div>
            <div class="metric-label">理想值 < 25%</div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">表锁等待率</div>
            <div class="metric-value" :class="getLockWaitClass(metric.tableLockWaitRate)">
              {{ metric.tableLockWaitRate?.toFixed(2) || 0 }}%
            </div>
            <div class="metric-label">理想值 < 5%</div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">网络流量</div>
            <div class="metric-value-small">↓ {{ formatSize(metric.bytesReceived) }}</div>
            <div class="metric-label-small">↑ {{ formatSize(metric.bytesSent) }}</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 复制状态（如果存在） -->
      <el-row v-if="showReplicationSection" :gutter="20" class="metric-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">从库 IO 状态</div>
            <div class="metric-value" :class="getReplicationStatusClass(metric.slaveIoRunning)">
              {{ metric.slaveIoRunning !== null ? (metric.slaveIoRunning ? '运行' : '停止') : '未配置' }}
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">从库 SQL 状态</div>
            <div class="metric-value" :class="getReplicationStatusClass(metric.slaveSqlRunning)">
              {{ metric.slaveSqlRunning !== null ? (metric.slaveSqlRunning ? '运行' : '停止') : '未配置' }}
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">主从延迟</div>
            <div class="metric-value" :class="getReplicationDelayClass(metric.secondsBehindMaster)">
              {{ metric.secondsBehindMaster !== null ? `${metric.secondsBehindMaster}秒` : '未配置' }}
            </div>
            <div class="metric-label">理想值 < 30秒</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细信息面板 -->
      <el-row :gutter="20" class="detail-row">
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-title">连接与线程</div>
            </template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="当前连接数">{{ metric.connectionsCurrent || 0 }}</el-descriptions-item>
              <el-descriptions-item label="最大连接数">{{ metric.connectionsMax || 0 }}</el-descriptions-item>
              <el-descriptions-item label="连接使用率">
                <span :class="getConnectionUsageClass(metric.connectionsUsage)">
                  {{ metric.connectionsUsage?.toFixed(2) || 0 }}%
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="运行线程数">{{ metric.threadsRunning || 0 }}</el-descriptions-item>
              <el-descriptions-item label="已连接线程数">{{ metric.threadsConnected || 0 }}</el-descriptions-item>
              <el-descriptions-item label="已创建线程数">{{ metric.threadsCreated || 0 }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-title">缓冲池状态</div>
            </template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="缓冲池大小">{{ formatSize(metric.bufferPoolSize) }}</el-descriptions-item>
              <el-descriptions-item label="空闲页数">{{ metric.bufferPoolFreePages || 0 }}</el-descriptions-item>
              <el-descriptions-item label="脏页数">{{ metric.bufferPoolDirtyPages || 0 }}</el-descriptions-item>
              <el-descriptions-item label="读请求数">{{ metric.bufferPoolReadRequests || 0 }}</el-descriptions-item>
              <el-descriptions-item label="写请求数">{{ metric.bufferPoolWriteRequests || 0 }}</el-descriptions-item>
              <el-descriptions-item label="物理读次数">{{ metric.bufferPoolReads || 0 }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-title">锁与排序</div>
            </template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="行锁等待时间">{{ metric.innodbRowLockTime || 0 }}ms</el-descriptions-item>
              <el-descriptions-item label="行锁等待次数">{{ metric.innodbRowLockWaits || 0 }}</el-descriptions-item>
              <el-descriptions-item label="当前行锁等待">{{ metric.innodbRowLockCurrentWaits || 0 }}</el-descriptions-item>
              <el-descriptions-item label="表锁等待次数">{{ metric.tableLocksWaited || 0 }}</el-descriptions-item>
              <el-descriptions-item label="表锁立即获得">{{ metric.tableLocksImmediate || 0 }}</el-descriptions-item>
              <el-descriptions-item label="表锁等待率">
                <span :class="getLockWaitClass(metric.tableLockWaitRate)">
                  {{ metric.tableLockWaitRate?.toFixed(2) || 0 }}%
                </span>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="detail-row">
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-title">临时表统计</div>
            </template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="临时表创建总数">{{ metric.tmpTablesCreated || 0 }}</el-descriptions-item>
              <el-descriptions-item label="磁盘临时表">{{ metric.tmpDiskTablesCreated || 0 }}</el-descriptions-item>
              <el-descriptions-item label="磁盘比率">
                <span :class="getDiskTableRateClass(metric.tmpDiskTableRate)">
                  {{ metric.tmpDiskTableRate?.toFixed(2) || 0 }}%
                </span>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-title">排序统计</div>
            </template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="排序行数">{{ metric.sortRows || 0 }}</el-descriptions-item>
              <el-descriptions-item label="全表扫描排序">{{ metric.sortScan || 0 }}</el-descriptions-item>
              <el-descriptions-item label="范围排序">{{ metric.sortRange || 0 }}</el-descriptions-item>
              <el-descriptions-item label="排序合并">{{ metric.sortMergePasses || 0 }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-title">性能趋势</div>
            </template>
            <div class="trend-charts">
              <div class="trend-item">
                <div class="trend-label">QPS 趋势</div>
                <el-progress :percentage="Math.min(100, (metric.qps || 0) / 1000 * 100)" :color="qpsColor" />
              </div>
              <div class="trend-item">
                <div class="trend-label">TPS 趋势</div>
                <el-progress :percentage="Math.min(100, (metric.tps || 0) / 100 * 100)" :color="tpsColor" />
              </div>
              <div class="trend-item">
                <div class="trend-label">慢查询率</div>
                <el-progress :percentage="Math.min(100, (metric.slowQueriesRate || 0) / 10 * 100)" :color="slowQueryColor" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getMysqlInstanceList, getMysqlMetric } from '@/api/mysql'

// 从 API 获取实例列表
const instances = ref([])
const loading = ref(false)
const selectedInstanceId = ref(null)
const selectedInstance = computed(() => {
  return instances.value.find(item => item.id === selectedInstanceId.value)
})

const metric = reactive({
  // 连接数
  connectionsCurrent: 0,
  connectionsMax: 0,
  connectionsUsage: 0,
  // QPS/TPS
  qps: 0,
  qpsTotal: 0,
  tps: 0,
  tpsTotal: 0,
  // 慢查询
  slowQueriesRate: 0,
  slowQueriesTotal: 0,
  // 线程
  threadsRunning: 0,
  threadsConnected: 0,
  threadsCreated: 0,
  // 流量
  bytesReceived: 0,
  bytesSent: 0,
  // 缓冲池
  bufferPoolSize: 0,
  bufferPoolFreePages: 0,
  bufferPoolDirtyPages: 0,
  bufferPoolReadRequests: 0,
  bufferPoolWriteRequests: 0,
  bufferPoolReads: 0,
  bufferPoolHitRate: 0,
  // InnoDB 行锁
  innodbRowLockTime: 0,
  innodbRowLockWaits: 0,
  innodbRowLockCurrentWaits: 0,
  // 表锁
  tableLocksWaited: 0,
  tableLocksImmediate: 0,
  tableLockWaitRate: 0,
  // 临时表
  tmpTablesCreated: 0,
  tmpDiskTablesCreated: 0,
  tmpDiskTableRate: 0,
  // 排序
  sortRows: 0,
  sortScan: 0,
  sortRange: 0,
  sortMergePasses: 0,
  // 复制状态
  slaveIoRunning: null,
  slaveSqlRunning: null,
  secondsBehindMaster: null
})

// 是否显示复制状态区域
const showReplicationSection = computed(() => {
  return metric.slaveIoRunning !== null || metric.slaveSqlRunning !== null
})

// 趋势图颜色
const qpsColor = computed(() => {
  const qps = metric.qps || 0
  if (qps > 500) return '#f56c6c' // 红色
  if (qps > 200) return '#e6a23c' // 黄色
  return '#67c23a' // 绿色
})

const tpsColor = computed(() => {
  const tps = metric.tps || 0
  if (tps > 100) return '#f56c6c'
  if (tps > 50) return '#e6a23c'
  return '#67c23a'
})

const slowQueryColor = computed(() => {
  const rate = metric.slowQueriesRate || 0
  if (rate > 5) return '#f56c6c'
  if (rate > 1) return '#e6a23c'
  return '#67c23a'
})

// 加载 MySQL 实例列表
const loadInstances = async () => {
  try {
    loading.value = true
    const response = await getMysqlInstanceList()
    instances.value = response.data.records || []
    // 如果有实例，默认选中第一个
    if (instances.value.length > 0 && !selectedInstanceId.value) {
      selectedInstanceId.value = instances.value[0].id
      await loadMetric()
    }
  } catch (error) {
    console.error('Failed to load MySQL instances:', error)
  } finally {
    loading.value = false
  }
}

// 加载监控数据
const loadMetric = async () => {
  if (!selectedInstanceId.value) {
    return
  }

  try {
    loading.value = true
    const response = await getMysqlMetric(selectedInstanceId.value)

    // 清空之前的数据
    Object.keys(metric).forEach(key => {
      metric[key] = 0
    })

    // 填充新的监控数据
    Object.assign(metric, response.data || {})
  } catch (error) {
    console.error('Failed to load MySQL metrics:', error)
  } finally {
    loading.value = false
  }
}

// 实例选择变化时加载新的监控数据
const handleInstanceChange = async () => {
  await loadMetric()
}

// 格式化字节单位
const formatSize = (bytes) => {
  if (!bytes) return '0'
  const gb = bytes / 1024 / 1024 / 1024
  if (gb >= 1) {
    return `${gb.toFixed(2)} GB`
  }
  const mb = bytes / 1024 / 1024
  if (mb >= 1) {
    return `${mb.toFixed(2)} MB`
  }
  const kb = bytes / 1024
  return `${kb.toFixed(2)} KB`
}

// 各种颜色状态判断函数
const getConnectionUsageClass = (usage) => {
  if (!usage) return 'success'
  if (usage > 85) return 'danger'
  if (usage > 70) return 'warning'
  return 'success'
}

const getHitRateClass = (rate) => {
  if (!rate) return 'success'
  if (rate >= 95) return 'success'
  if (rate >= 80) return 'warning'
  return 'danger'
}

const getDiskTableRateClass = (rate) => {
  if (!rate) return 'success'
  if (rate <= 25) return 'success'
  if (rate <= 50) return 'warning'
  return 'danger'
}

const getLockWaitClass = (rate) => {
  if (!rate) return 'success'
  if (rate <= 5) return 'success'
  if (rate <= 20) return 'warning'
  return 'danger'
}

const getSlowQueryClass = (rate) => {
  if (!rate) return 'success'
  if (rate === 0) return 'success'
  if (rate <= 2) return 'warning'
  return 'danger'
}

const getReplicationStatusClass = (status) => {
  if (status === null) return '' // 未配置
  return status ? 'success' : 'danger'
}

const getReplicationDelayClass = (delay) => {
  if (delay === null) return '' // 未配置
  if (delay <= 10) return 'success'
  if (delay <= 60) return 'warning'
  return 'danger'
}

onMounted(async () => {
  await loadInstances()

  // 定时刷新
  const timer = setInterval(async () => {
    if (selectedInstanceId.value) {
      await loadMetric()
    }
  }, 30000)

  return () => clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.mysql-monitor-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      align-items: center;
    }
  }

  .card-title {
    font-size: 15px;
    font-weight: bold;
    color: #333;
    margin: 0;
  }

  .metric-cards {
    margin-bottom: 20px;

    .metric-card {
      .metric-title {
        font-size: 14px;
        color: #666;
        margin-bottom: 10px;
      }

      .metric-value {
        font-size: 28px;
        font-weight: bold;
        color: #409EFF;

        &.success {
          color: #67C23A;
        }

        &.warning {
          color: #E6A23C;
        }

        &.danger {
          color: #f56c6c;
        }

        .unit {
          font-size: 14px;
          font-weight: normal;
        }
      }

      .metric-value-small {
        font-size: 18px;
        font-weight: bold;
        color: #409EFF;
        margin-bottom: 5px;
      }

      .metric-label {
        font-size: 12px;
        color: #999;
        margin-top: 5px;
      }

      .metric-sub-label {
        font-size: 11px;
        color: #999;

        &.success {
          color: #67C23A;
        }

        &.warning {
          color: #E6A23C;
        }

        &.danger {
          color: #f56c6c;
        }
      }

      .metric-label-small {
        font-size: 11px;
        color: #999;
      }
    }
  }

  .detail-row {
    margin-top: 20px;

    .card-title {
      font-size: 15px;
      font-weight: bold;
      color: #333;
      margin: 0;
    }

    :deep(.el-descriptions__label) {
      font-size: 13px;
      width: 120px;
    }

    :deep(.el-descriptions__content) {
      font-size: 14px;
      font-weight: 500;
    }
  }

  .trend-charts {
    padding: 10px 0;

    .trend-item {
      margin-bottom: 15px;

      &:last-child {
        margin-bottom: 0;
      }

      .trend-label {
        font-size: 13px;
        color: #666;
        margin-bottom: 5px;
      }
    }
  }
}
</style>