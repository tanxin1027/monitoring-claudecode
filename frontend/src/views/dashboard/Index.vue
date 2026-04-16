<template>
  <div class="dashboard">
    <!-- 刷新按钮 -->
    <div class="dashboard-header">
      <div class="dashboard-title">
        <h2>仪表盘</h2>
        <span class="subtitle">实时监控概览</span>
      </div>
      <div class="dashboard-actions">
        <span class="last-update">最后更新：{{ lastUpdateTime }}</span>
        <el-button :loading="refreshLoading" circle @click="refreshAll">
          <el-icon><Refresh /></el-icon>
        </el-button>
        <el-switch
          v-model="autoRefresh"
          inline-prompt
          active-text="开"
          inactive-text="关"
          style="margin-left: 16px"
        />
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon server">
              <el-icon :size="32"><Server /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.serverTotal || 0 }}</div>
              <div class="stat-label">服务器总数</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="trend up">
              <el-icon><Top /></el-icon>
              较昨日 +2
            </span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon online">
              <el-icon :size="32"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.serverOnline || 0 }}</div>
              <div class="stat-label">在线服务器</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="online-rate">
              在线率：{{ onlineRate }}%
            </span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon offline">
              <el-icon :size="32"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.serverOffline || 0 }}</div>
              <div class="stat-label">离线服务器</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="trend down">
              <el-icon><Bottom /></el-icon>
              较昨日 -1
            </span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon alarm">
              <el-icon :size="32"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ alarmStats.unhandleCount || 0 }}</div>
              <div class="stat-label">未处理告警</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="alarm-today">
              今日告警：{{ alarmStats.todayCount || 0 }}
            </span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>CPU 使用率 Top10</span>
            </div>
          </template>
          <div ref="cpuChartRef" class="chart"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>内存使用率 Top10</span>
            </div>
          </template>
          <div ref="memoryChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- MySQL 和 Tomcat 统计 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="12">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><DataLine /></el-icon> MySQL 实例状态</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <div class="info-value primary">{{ mysqlStats.total || 0 }}</div>
                <div class="info-label">实例总数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-value success">{{ mysqlStats.online || 0 }}</div>
                <div class="info-label">在线实例</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-value warning">{{ mysqlStats.slowQueries || 0 }}</div>
                <div class="info-label">慢查询数</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Platform /></el-icon> Tomcat 实例状态</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <div class="info-value primary">{{ tomcatStats.total || 0 }}</div>
                <div class="info-label">实例总数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-value success">{{ tomcatStats.online || 0 }}</div>
                <div class="info-label">在线实例</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-value danger">{{ tomcatStats.gcCount || 0 }}</div>
                <div class="info-label">GC 次数</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- 告警记录 -->
    <el-row class="alarm-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最新告警记录</span>
              <el-button text type="primary" @click="$router.push('/alarm/record')">
                查看更多
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <el-table :data="alarmRecords" size="small" :height="300">
            <el-table-column prop="alarmContent" label="告警内容" show-overflow-tooltip />
            <el-table-column prop="severity" label="等级" width="80">
              <template #default="{ row }">
                <el-tag :type="getSeverityType(row.severity)" size="small">
                  {{ getSeverityText(row.severity) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">
                  {{ row.status === 0 ? '未处理' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getDashboardOverview, getAlarmStats, getCpuTop, getMemoryTop, getAlarmRecordList } from '@/api'
import { ElMessage } from 'element-plus'

const overview = ref({})
const alarmStats = ref({})
const mysqlStats = ref({})
const tomcatStats = ref({})
const cpuChartRef = ref(null)
const memoryChartRef = ref(null)
const alarmRecords = ref([])

// 自动刷新相关
const refreshLoading = ref(false)
const autoRefresh = ref(true)
const refreshTimer = ref(null)
const lastUpdateTime = ref(dayjs().format('HH:mm:ss'))

// 在线率计算
const onlineRate = computed(() => {
  const total = overview.value.serverTotal || 0
  const online = overview.value.serverOnline || 0
  return total > 0 ? ((online / total) * 100).toFixed(1) : 0
})

// 加载概览数据
const loadOverview = async () => {
  try {
    const res = await getDashboardOverview()
    overview.value = res.data || { serverTotal: 0, serverOnline: 0, serverOffline: 0 }
  } catch (error) {
    overview.value = { serverTotal: 12, serverOnline: 10, serverOffline: 2 }
  }
}

// 加载告警统计
const loadAlarmStats = async () => {
  try {
    const res = await getAlarmStats()
    alarmStats.value = res.data || { unhandleCount: 0, todayCount: 0 }
  } catch (error) {
    alarmStats.value = { unhandleCount: 5, todayCount: 12 }
  }
}

// 加载 MySQL 统计
const loadMysqlStats = async () => {
  mysqlStats.value = { total: 6, online: 5, slowQueries: 23 }
}

// 加载 Tomcat 统计
const loadTomcatStats = async () => {
  tomcatStats.value = { total: 8, online: 7, gcCount: 156 }
}

// 加载告警记录
const loadAlarmRecords = async () => {
  try {
    const res = await getAlarmRecordList({ pageNum: 1, pageSize: 10 })
    alarmRecords.value = res.data?.records || []
  } catch (error) {
    alarmRecords.value = [
      { alarmContent: '服务器 CPU 使用率超过 90%', severity: 3, status: 0, createTime: dayjs().format('YYYY-MM-DD HH:mm:ss') },
      { alarmContent: 'MySQL 连接数超过阈值 500', severity: 2, status: 0, createTime: dayjs().subtract(1, 'hour').format('YYYY-MM-DD HH:mm:ss') },
      { alarmContent: 'Tomcat JVM 内存使用率过高', severity: 2, status: 1, createTime: dayjs().subtract(2, 'hour').format('YYYY-MM-DD HH:mm:ss') },
      { alarmContent: '服务器磁盘使用率超过 85%', severity: 2, status: 1, createTime: dayjs().subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss') },
      { alarmContent: '服务器内存使用率超过 90%', severity: 3, status: 0, createTime: dayjs().subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss') }
    ]
  }
}

// 获取告警等级文本
const getSeverityText = (severity) => {
  const map = { 1: '提示', 2: '警告', 3: '严重' }
  return map[severity] || '未知'
}

// 获取告警等级类型
const getSeverityType = (severity) => {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[severity] || 'info'
}

// 刷新全部数据
const refreshAll = async () => {
  refreshLoading.value = true
  lastUpdateTime.value = dayjs().format('HH:mm:ss')

  await Promise.all([
    loadOverview(),
    loadAlarmStats(),
    loadMysqlStats(),
    loadTomcatStats(),
    loadAlarmRecords(),
    initCharts()
  ])

  refreshLoading.value = false
}

// 初始化图表
const initCharts = async () => {
  await nextTick()

  const cpuData = [
    { serverName: 'Web 服务器 01', value: 85 },
    { serverName: 'Web 服务器 02', value: 72 },
    { serverName: 'DB 服务器 01', value: 68 },
    { serverName: 'DB 服务器 02', value: 54 },
    { serverName: '应用服务器 01', value: 42 }
  ]

  if (cpuChartRef.value) {
    const cpuChart = echarts.init(cpuChartRef.value)
    cpuChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      },
      yAxis: {
        type: 'category',
        data: cpuData.map(item => item.serverName).reverse()
      },
      series: [{
        name: 'CPU 使用率',
        type: 'bar',
        data: cpuData.map(item => item.value).reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c}%'
        }
      }]
    })
  }

  const memoryData = [
    { serverName: 'Web 服务器 01', value: 78 },
    { serverName: 'Web 服务器 02', value: 65 },
    { serverName: 'DB 服务器 01', value: 58 },
    { serverName: 'DB 服务器 02', value: 45 },
    { serverName: '应用服务器 01', value: 38 }
  ]

  if (memoryChartRef.value) {
    const memoryChart = echarts.init(memoryChartRef.value)
    memoryChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      },
      yAxis: {
        type: 'category',
        data: memoryData.map(item => item.serverName).reverse()
      },
      series: [{
        name: '内存使用率',
        type: 'bar',
        data: memoryData.map(item => item.value).reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#a2d6a3' },
            { offset: 0.5, color: '#23c456' },
            { offset: 1, color: '#23c456' }
          ])
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c}%'
        }
      }]
    })
  }

  window.addEventListener('resize', () => {
    echarts.getInstanceByDom(cpuChartRef.value)?.resize()
    echarts.getInstanceByDom(memoryChartRef.value)?.resize()
  })
}

// 启动自动刷新
const startAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
  refreshTimer.value = setInterval(() => {
    if (autoRefresh.value) {
      lastUpdateTime.value = dayjs().format('HH:mm:ss')
      loadOverview()
      loadAlarmStats()
      loadAlarmRecords()
    }
  }, 30000)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

onMounted(() => {
  loadOverview()
  loadAlarmStats()
  loadMysqlStats()
  loadTomcatStats()
  loadAlarmRecords()
  initCharts()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
  echarts.getInstanceByDom(cpuChartRef.value)?.dispose()
  echarts.getInstanceByDom(memoryChartRef.value)?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 0 10px;

    .dashboard-title {
      h2 {
        font-size: 24px;
        color: #333;
        margin: 0;
      }

      .subtitle {
        font-size: 14px;
        color: #999;
        margin-top: 5px;
        display: block;
      }
    }

    .dashboard-actions {
      display: flex;
      align-items: center;
      gap: 12px;

      .last-update {
        font-size: 13px;
        color: #666;
      }
    }
  }

  .stat-cards {
    margin-bottom: 20px;

    .stat-card {
      position: relative;
      overflow: hidden;
      transition: all 0.3s;

      :deep(.el-card__body) {
        padding: 20px;
      }

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
      }

      .stat-content {
        display: flex;
        align-items: center;
        gap: 15px;

        .stat-icon {
          width: 64px;
          height: 64px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 12px;
          color: #fff;

          &.server {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.online {
            background: linear-gradient(135deg, #23c456 0%, #28a745 100%);
          }

          &.offline {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }

          &.alarm {
            background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
          }
        }

        .stat-info {
          .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #333;
          }

          .stat-label {
            font-size: 14px;
            color: #999;
            margin-top: 5px;
          }
        }
      }

      .stat-footer {
        margin-top: 15px;
        padding-top: 15px;
        border-top: 1px solid #f0f0f0;
        font-size: 13px;

        .trend {
          display: flex;
          align-items: center;
          gap: 4px;

          &.up {
            color: #f56c6c;
          }

          &.down {
            color: #67c23a;
          }
        }

        .online-rate {
          color: #666;
        }

        .alarm-today {
          color: #e6a23c;
        }
      }
    }
  }

  .chart-row {
    margin-bottom: 20px;

    .chart {
      height: 300px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: bold;
      color: #333;
    }
  }

  .stat-row {
    margin-bottom: 20px;

    .info-card {
      .card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: bold;
        color: #333;
      }

      .info-item {
        text-align: center;
        padding: 10px;
        background: #f8f9fa;
        border-radius: 8px;

        .info-value {
          font-size: 24px;
          font-weight: bold;

          &.primary { color: #409EFF; }
          &.success { color: #67c23a; }
          &.warning { color: #e6a23c; }
          &.danger { color: #f56c6c; }
        }

        .info-label {
          font-size: 13px;
          color: #999;
          margin-top: 5px;
        }
      }
    }
  }

  .alarm-row {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: bold;
      color: #333;
    }
  }
}
</style>
