<template>
  <div class="tomcat-monitor-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Tomcat 监控</span>
          <div class="header-actions">
            <el-select
              v-model="selectedInstanceId"
              placeholder="选择实例"
              style="width: 300px; margin-right: 12px;"
              @change="handleInstanceChange"
              :loading="loading"
            >
              <el-option
                v-for="item in instances"
                :key="item.id"
                :label="item.instanceName"
                :value="item.id"
              />
            </el-select>
            <el-button :icon="Refresh" circle @click="loadMetric" :loading="loading" />
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

      <!-- 加载状态 -->
      <el-empty v-if="loading && instances.length === 0" description="加载中..." />

      <!-- 错误状态 -->
      <el-empty
        v-else-if="error"
        :image="errorImage"
        :description="errorMessage"
      >
        <el-button type="primary" @click="retryLoad">重试</el-button>
        <el-button
          v-if="errorMessage.includes('暂无 Tomcat 实例')"
          type="success"
          @click="showAddInstanceGuide"
          style="margin-left: 10px;"
        >
          添加实例指南
        </el-button>
      </el-empty>

      <!-- 监控数据 -->
      <div v-else>
        <!-- 核心指标卡片 -->
        <el-row :gutter="20" class="metric-cards">
          <el-col :span="6">
            <el-card shadow="hover" class="metric-card">
              <div class="metric-title">JVM 堆内存</div>
              <div class="metric-value">{{ formatMemory(metric.jvmHeapUsed) }} / {{ formatMemory(metric.jvmHeapMax) }}</div>
              <div class="metric-label" v-if="metric.jvmHeapMax">
                <el-progress
                  :percentage="((metric.jvmHeapUsed / metric.jvmHeapMax) * 100) || 0"
                  :color="getMemoryUsageColor((metric.jvmHeapUsed / metric.jvmHeapMax) * 100)"
                  :stroke-width="8"
                />
                <div class="metric-sub-label">使用率：{{ ((metric.jvmHeapUsed / metric.jvmHeapMax) * 100).toFixed(2) || 0 }}%</div>
              </div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card shadow="hover" class="metric-card">
              <div class="metric-title">非堆内存</div>
              <div class="metric-value">{{ formatMemory(metric.jvmNonHeapUsed) }}</div>
              <div class="metric-label">当前使用</div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card shadow="hover" class="metric-card">
              <div class="metric-title">GC 次数</div>
              <div class="metric-value">{{ metric.gcCount || 0 }}</div>
              <div class="metric-label">总 GC 次数</div>
              <div class="metric-sub-label">GC 时间：{{ metric.gcTime || 0 }} ms</div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card shadow="hover" class="metric-card">
              <div class="metric-title">线程数</div>
              <div class="metric-value">{{ metric.threadCount || 0 }}</div>
              <div class="metric-label">总计：{{ metric.threadCount || 0 }}</div>
              <div class="metric-sub-label" :class="getThreadUsageClass(metric.threadBusy, metric.threadCount)">繁忙：{{ metric.threadBusy || 0 }}</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 性能指标卡片 -->
        <el-row :gutter="20" class="metric-cards">
          <el-col :span="6">
            <el-card shadow="hover" class="metric-card">
              <div class="metric-title">请求数</div>
              <div class="metric-value">{{ formatNumber(metric.requestCount) }}</div>
              <div class="metric-label">累计请求数</div>
              <div class="metric-sub-label" :class="getErrorRateClass(metric.errorCount, metric.requestCount)">错误：{{ metric.errorCount || 0 }}</div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card shadow="hover" class="metric-card">
              <div class="metric-title">最大处理时间</div>
              <div class="metric-value" :class="getResponseTimeClass(metric.maxTime)">{{ formatTime(metric.maxTime) }}</div>
              <div class="metric-label">请求响应时间</div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card shadow="hover" class="metric-card">
              <div class="metric-title">网络流量</div>
              <div class="metric-value-small">↓ {{ formatSize(metric.bytesReceived) }}</div>
              <div class="metric-label-small">↑ {{ formatSize(metric.bytesSent) }}</div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card shadow="hover" class="metric-card">
              <div class="metric-title">运行时间</div>
              <div class="metric-value">{{ formatUptime(metric.uptime) }}</div>
              <div class="metric-label">服务运行时长</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- Session 指标 -->
        <el-row :gutter="20" class="metric-cards">
          <el-col :span="6">
            <el-card shadow="hover" class="metric-card">
              <div class="metric-title">活跃会话数</div>
              <div class="metric-value">{{ metric.sessionCount || 0 }}</div>
              <div class="metric-label">当前活跃会话</div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card shadow="hover" class="metric-card">
              <div class="metric-title">过期会话数</div>
              <div class="metric-value">{{ metric.sessionExpired || 0 }}</div>
              <div class="metric-label">累计过期会话</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 详细信息面板 -->
        <el-row :gutter="20" class="detail-row">
          <el-col :span="8">
            <el-card>
              <template #header>
                <div class="card-title">JVM 内存状态</div>
              </template>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="堆内存使用">
                  <div>{{ formatMemory(metric.jvmHeapUsed) }} / {{ formatMemory(metric.jvmHeapMax) }}</div>
                  <el-progress
                    v-if="metric.jvmHeapMax"
                    :percentage="((metric.jvmHeapUsed / metric.jvmHeapMax) * 100) || 0"
                    :color="getMemoryUsageColor((metric.jvmHeapUsed / metric.jvmHeapMax) * 100)"
                  />
                </el-descriptions-item>
                <el-descriptions-item label="非堆内存使用">{{ formatMemory(metric.jvmNonHeapUsed) }}</el-descriptions-item>
                <el-descriptions-item label="GC 次数">{{ metric.gcCount || 0 }}</el-descriptions-item>
                <el-descriptions-item label="GC 总时间">{{ metric.gcTime || 0 }} ms</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>

          <el-col :span="8">
            <el-card>
              <template #header>
                <div class="card-title">线程与连接</div>
              </template>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="总线程数">{{ metric.threadCount || 0 }}</el-descriptions-item>
                <el-descriptions-item label="繁忙线程数">{{ metric.threadBusy || 0 }}</el-descriptions-item>
                <el-descriptions-item label="线程使用率">
                  <span :class="getThreadUsageClass(metric.threadBusy, metric.threadCount)">
                    {{ metric.threadCount ? ((metric.threadBusy / metric.threadCount) * 100).toFixed(2) : 0 }}%
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="活跃会话数">{{ metric.sessionCount || 0 }}</el-descriptions-item>
                <el-descriptions-item label="过期会话数">{{ metric.sessionExpired || 0 }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>

          <el-col :span="8">
            <el-card>
              <template #header>
                <div class="card-title">请求与性能</div>
              </template>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="总请求数">{{ formatNumber(metric.requestCount) }}</el-descriptions-item>
                <el-descriptions-item label="错误数">{{ metric.errorCount || 0 }}</el-descriptions-item>
                <el-descriptions-item label="错误率">
                  <span :class="getErrorRateClass(metric.errorCount, metric.requestCount)">
                    {{ metric.requestCount ? ((metric.errorCount / metric.requestCount) * 100).toFixed(4) : 0 }}%
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="最大响应时间">{{ formatTime(metric.maxTime) }}</el-descriptions-item>
                <el-descriptions-item label="运行时间">{{ formatUptime(metric.uptime) }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="detail-row">
          <el-col :span="8">
            <el-card>
              <template #header>
                <div class="card-title">网络流量统计</div>
              </template>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="接收流量">{{ formatSize(metric.bytesReceived) }}</el-descriptions-item>
                <el-descriptions-item label="发送流量">{{ formatSize(metric.bytesSent) }}</el-descriptions-item>
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
                  <div class="trend-label">内存使用率</div>
                  <el-progress :percentage="metric.jvmHeapMax ? Math.min(100, (metric.jvmHeapUsed / metric.jvmHeapMax * 100)) : 0" :color="memoryColor" />
                </div>
                <div class="trend-item">
                  <div class="trend-label">线程繁忙率</div>
                  <el-progress :percentage="metric.threadCount ? Math.min(100, (metric.threadBusy / metric.threadCount * 100)) : 0" :color="threadColor" />
                </div>
                <div class="trend-item">
                  <div class="trend-label">错误率</div>
                  <el-progress :percentage="metric.requestCount ? Math.min(100, (metric.errorCount / metric.requestCount * 100)) : 0" :color="errorColor" />
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :span="8">
            <el-card>
              <template #header>
                <div class="card-title">健康状态</div>
              </template>
              <div class="health-status">
                <div class="health-item">
                  <i :class="getHealthIcon(getMemoryUsageLevel(metric.jvmHeapUsed, metric.jvmHeapMax))"></i>
                  <span>JVM内存</span>
                </div>
                <div class="health-item">
                  <i :class="getHealthIcon(getThreadUsageLevel(metric.threadBusy, metric.threadCount))"></i>
                  <span>线程池</span>
                </div>
                <div class="health-item">
                  <i :class="getHealthIcon(getErrorRateLevel(metric.errorCount, metric.requestCount))"></i>
                  <span>错误率</span>
                </div>
                <div class="health-item">
                  <i :class="getHealthIcon(getResponseTimeLevel(metric.maxTime))"></i>
                  <span>响应时间</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getTomcatInstanceList, getTomcatMetric } from '@/api'

// 图片资源（用于错误状态）
const errorImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTMwIDBDMTMuNDMxNSAwIDAgMTMuNDMxNSAwIDMwczEzLjQzMTUgMzAgMzAgMzAgMzAtMTMuNDMxNSAzMC0zMFM0Ni41Njg1IDAgMzAgMHptMCA1NkMxNS42NzYgNTYgNCA0NC4zMjQgNCAzMFMxNS42NzYgNCAzMCA0czI2IDE1LjY3NiAyNiAyNlM0NC4zMjQgNTYgMzAgNTZ6IiBmaWxsPSIjZmZmIi8+PHBhdGggZD0iTTE4Ljk2ODggMzYuNjg3NUwxMi4zMTI1IDIzLjE4NzVDOS45MDYyNSAxOC4zNzUgMTMuMDkzOCA5LjM3NSAxOC4zMTI1IDkuMzc1YzIuODc1IDAgNS40MDYyIDMuMTg3NSA2LjI1IDcuODEyNVYyMEgyNy4zMTI1VjkuMzc1YzAtLjgxMjUtLjgtMS41LTEuNzUtMS41SDE1LjMxMjVjLS45Mzc1IDAtMS43NS42ODc1LTEuNzUgMS41djEuNTU5NWwyLjY1NjIgNS4zMTI1TDI3LjMxMjUgMzguNzgxMlY1MC42MjVDLjYyNSAzNy4yNSA0LjY4NzUgMjAuOTM3NSAxOC45Njg4IDM2LjY4NzV6TTM2LjU2MjUgNDMuNzVDNDEuMjc1IDQzLjc1IDQ1IDM5LjU2MjUgNDUgMzQuMzc1YzAtMy4xMjUtMS42ODc1LTYuMTg3NS00LjU2MjUtOC4wNjI1bC00LjY4NzUgNC41NjI1YzEuMDYyNSAxLjU2MjUgMS42ODc1IDMuNTYyNSAxLjY4NzUgNS43NSAwIDQuMTg3NS0zLjU2MjUgNy41LTYuODc1IDcuNXMtNi44NzUtMy4zMzI1LTYuODc1LTcuNXoiIGZpbGw9IiNGRjM3NEYiLz48L3N2Zz4='

// 数据和状态
const instances = ref([])
const loading = ref(false)
const error = ref(false)
const errorMessage = ref('')
const selectedInstanceId = ref(null)

const selectedInstance = computed(() => {
  return instances.value.find(item => item.id === selectedInstanceId.value)
})

const metric = reactive({
  jvmHeapUsed: 0,
  jvmHeapMax: 0,
  jvmNonHeapUsed: 0,
  gcCount: 0,
  gcTime: 0,
  threadCount: 0,
  threadBusy: 0,
  requestCount: 0,
  errorCount: 0,
  bytesSent: 0,
  bytesReceived: 0,
  maxTime: 0,
  uptime: 0,
  sessionCount: 0,
  sessionExpired: 0
})

// 趋势图颜色
const memoryColor = computed(() => {
  const usage = metric.jvmHeapMax ? (metric.jvmHeapUsed / metric.jvmHeapMax * 100) : 0
  if (usage > 85) return '#f56c6c' // 红色
  if (usage > 70) return '#e6a23c' // 黄色
  return '#67c23a' // 绿色
})

const threadColor = computed(() => {
  const usage = metric.threadCount ? (metric.threadBusy / metric.threadCount * 100) : 0
  if (usage > 85) return '#f56c6c'
  if (usage > 70) return '#e6a23c'
  return '#67c23a'
})

const errorColor = computed(() => {
  const rate = metric.requestCount ? (metric.errorCount / metric.requestCount * 100) : 0
  if (rate > 1) return '#f56c6c'
  if (rate > 0.1) return '#e6a23c'
  return '#67c23a'
})

// 加载 Tomcat 实例列表
const loadInstances = async () => {
  try {
    loading.value = true
    error.value = false

    const response = await getTomcatInstanceList({ pageNum: 1, pageSize: 100 }) // 获取所有实例
    instances.value = response.data?.records || []

    // 如果有实例，默认选中第一个
    if (instances.value.length > 0 && !selectedInstanceId.value) {
      selectedInstanceId.value = instances.value[0].id
      await loadMetric()
    } else if (instances.value.length === 0) {
      // 如果没有实例，显示提示信息
      errorMessage.value = '暂无 Tomcat 实例'
      error.value = true
    }
  } catch (err) {
    console.error('Failed to load Tomcat instances:', err)
    error.value = true
    errorMessage.value = `加载实例失败: ${err.message || '网络错误'}`
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
    error.value = false

    const response = await getTomcatMetric(selectedInstanceId.value)

    // 清空之前的数据
    Object.keys(metric).forEach(key => {
      metric[key] = 0
    })

    // 填充新的监控数据
    Object.assign(metric, response.data || {})
  } catch (err) {
    console.error('Failed to load Tomcat metrics:', err)
    // 不显示错误，而是使用默认的0值数据
    Object.keys(metric).forEach(key => {
      metric[key] = 0
    })
  } finally {
    loading.value = false
  }
}

// 实例选择变化时加载新的监控数据
const handleInstanceChange = async () => {
  await loadMetric()
}

// 重试加载
const retryLoad = async () => {
  error.value = false
  await loadInstances()
}

// 显示添加实例指南
const showAddInstanceGuide = () => {
  alert(`Tomcat 实例添加指南：

1. 首先确保 Tomcat 服务正在运行
2. 确保 JMX 远程监控已启用（在 catalina.sh 中添加 JMX 参数）
3. 执行数据库初始化脚本 (database/schema.sql)
   - 该脚本会创建必要的表结构和初始数据
4. 如果使用 Docker，可以通过以下命令初始化：
   docker exec -i mysql-container mysql -u root -p123456 hospital_monitor < database/schema.sql
5. 或者直接连接到 MySQL 并运行 schema.sql 文件
6. 重启后端服务以确保连接正常

注意：schema.sql 文件中包含了初始化的 Tomcat 实例数据。
实例代码：tomcat_001
实例名称：本地 Tomcat 实例`)
}

// 格式化内存单位
const formatMemory = (bytes) => {
  if (!bytes) return '0 MB'
  const mb = bytes
  return `${mb} MB`
}

// 格式化数字
const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 1000000) {
    return `${(num / 1000000).toFixed(2)}M`
  }
  if (num >= 1000) {
    return `${(num / 1000).toFixed(2)}K`
  }
  return num.toString()
}

// 格式化时间
const formatTime = (ms) => {
  if (!ms) return '0 ms'
  if (ms >= 1000) {
    return `${(ms / 1000).toFixed(2)} s`
  }
  return `${ms} ms`
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

// 格式化运行时间
const formatUptime = (ms) => {
  if (!ms) return '0'
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (days > 0) return `${days}天${hours % 24}小时`
  if (hours > 0) return `${hours}小时${minutes % 60}分`
  if (minutes > 0) return `${minutes}分钟`
  return `${seconds}秒`
}

// 各种颜色状态判断函数
const getMemoryUsageColor = (percentage) => {
  if (percentage > 85) return '#f56c6c'
  if (percentage > 70) return '#e6a23c'
  return '#67c23a'
}

const getThreadUsageClass = (busy, total) => {
  if (!total) return 'success'
  const usage = (busy / total) * 100
  if (usage > 85) return 'danger'
  if (usage > 70) return 'warning'
  return 'success'
}

const getErrorRateClass = (errors, total) => {
  if (!total) return 'success'
  const rate = (errors / total) * 100
  if (rate > 1) return 'danger'
  if (rate > 0.1) return 'warning'
  return 'success'
}

const getResponseTimeClass = (time) => {
  if (!time) return 'success'
  if (time > 5000) return 'danger'
  if (time > 1000) return 'warning'
  return 'success'
}

// 健康状态相关函数
const getMemoryUsageLevel = (used, max) => {
  if (!max) return 1
  const percentage = (used / max) * 100
  if (percentage > 85) return 3
  if (percentage > 70) return 2
  return 1
}

const getThreadUsageLevel = (busy, total) => {
  if (!total) return 1
  const percentage = (busy / total) * 100
  if (percentage > 85) return 3
  if (percentage > 70) return 2
  return 1
}

const getErrorRateLevel = (errors, total) => {
  if (!total) return 1
  const rate = (errors / total) * 100
  if (rate > 1) return 3
  if (rate > 0.1) return 2
  return 1
}

const getResponseTimeLevel = (time) => {
  if (!time) return 1
  if (time > 5000) return 3
  if (time > 1000) return 2
  return 1
}

const getHealthIcon = (level) => {
  switch (level) {
    case 1: return 'el-icon-circle-check' // 正常
    case 2: return 'el-icon-warning' // 警告
    case 3: return 'el-icon-circle-close' // 危险
    default: return 'el-icon-circle-check'
  }
}

// 定时刷新
let refreshTimer = null

onMounted(async () => {
  await loadInstances()

  // 定时刷新
  refreshTimer = setInterval(async () => {
    if (selectedInstanceId.value) {
      await loadMetric()
    }
  }, 30000)
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
.tomcat-monitor-page {
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
        font-size: 24px;
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

        :deep(.el-progress) {
          margin-top: 5px;
        }
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

  .health-status {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;

    .health-item {
      display: flex;
      align-items: center;
      font-size: 14px;

      i {
        margin-right: 8px;

        &.el-icon-circle-check {
          color: #67C23A;
        }

        &.el-icon-warning {
          color: #E6A23C;
        }

        &.el-icon-circle-close {
          color: #f56c6c;
        }
      }
    }
  }
}
</style>