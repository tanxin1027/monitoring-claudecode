<template>
  <div class="monitor-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务器性能监控</span>
          <el-select
            v-model="selectedServerId"
            placeholder="选择服务器"
            style="width: 300px"
            @change="loadServerMetric"
          >
            <el-option
              v-for="item in servers"
              :key="item.id"
              :label="item.serverName"
              :value="item.id"
            />
          </el-select>
        </div>
      </template>

      <!-- 实时指标卡片 -->
      <el-row :gutter="20" class="metric-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">CPU 使用率</div>
            <div class="metric-value" :class="getLevelClass(metric.cpuUsage)">
              {{ metric.cpuUsage?.toFixed(1) || 0 }}%
            </div>
            <el-progress
              :percentage="metric.cpuUsage?.toFixed(0) || 0"
              :color="getProgressColor(metric.cpuUsage)"
            />
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">内存使用率</div>
            <div class="metric-value" :class="getLevelClass(metric.memoryUsage)">
              {{ metric.memoryUsage?.toFixed(1) || 0 }}%
            </div>
            <el-progress
              :percentage="metric.memoryUsage?.toFixed(0) || 0"
              :color="getProgressColor(metric.memoryUsage)"
            />
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">CPU 负载 (1m)</div>
            <div class="metric-value">
              {{ metric.cpuLoad1m?.toFixed(2) || 0 }}
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-title">进程数</div>
            <div class="metric-value">
              {{ metric.processCount || 0 }}
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 历史趋势图 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="24">
          <h4>历史趋势</h4>
          <div ref="trendChartRef" class="chart" style="height: 400px"></div>
        </el-col>
      </el-row>

      <!-- 磁盘和网络信息 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <h4>磁盘使用</h4>
          <el-table :data="metric.disks || []" size="small" border>
            <el-table-column prop="path" label="挂载点" />
            <el-table-column prop="total" label="总容量">
              <template #default="{ row }">{{ formatSize(row.total) }}</template>
            </el-table-column>
            <el-table-column prop="used" label="已使用">
              <template #default="{ row }">{{ formatSize(row.used) }}</template>
            </el-table-column>
            <el-table-column prop="usage" label="使用率">
              <template #default="{ row }">
                <el-progress
                  :percentage="formatPercentage(row.usage)"
                  :status="getProgressStatus(row.usage)"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-col>

        <el-col :span="12">
          <h4>网络流量</h4>
          <el-table :data="metric.networks || []" size="small" border>
            <el-table-column prop="iface" label="网卡" width="100" />
            <el-table-column prop="rxBytes" label="接收">
              <template #default="{ row }">{{ formatSize(row.rxBytes, true) }}</template>
            </el-table-column>
            <el-table-column prop="txBytes" label="发送">
              <template #default="{ row }">{{ formatSize(row.txBytes, true) }}</template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import { getServerList, getServerMetric, getServerHistoryMetric } from '@/api'
import dayjs from 'dayjs'

const servers = ref([])
const selectedServerId = ref(null)
const metric = reactive({
  cpuUsage: 0,
  memoryUsage: 0,
  cpuLoad1m: 0,
  processCount: 0,
  disks: [],
  networks: []
})
const trendChartRef = ref(null)
let chartInstance = null

// 加载服务器列表
const loadServers = async () => {
  try {
    const res = await getServerList({ pageNum: 1, pageSize: 100, status: 1 })
    servers.value = res.data?.records || []
    if (servers.value.length > 0) {
      selectedServerId.value = servers.value[0].id
      loadServerMetric()
    }
  } catch (error) {
    servers.value = [
      { id: 1, serverName: 'Web 服务器 01' },
      { id: 2, serverName: 'DB 服务器 01' }
    ]
    selectedServerId.value = 1
    loadServerMetric()
  }
}

// 加载服务器性能数据
const loadServerMetric = async () => {
  if (!selectedServerId.value) return

  try {
    const res = await getServerMetric(selectedServerId.value)
    const data = res.data
    Object.assign(metric, {
      cpuUsage: data?.cpuUsage || 0,
      memoryUsage: data?.memoryUsage || 0,
      cpuLoad1m: data?.cpuLoad1m || 0,
      processCount: data?.processCount || 0,
      disks: parseJson(data?.diskData, []),
      networks: parseJson(data?.networkData, [])
    })

    // 加载历史趋势
    loadHistoryMetric()
  } catch (error) {
    // 模拟数据
    Object.assign(metric, {
      cpuUsage: Math.random() * 50 + 30,
      memoryUsage: Math.random() * 40 + 40,
      cpuLoad1m: Math.random() * 3,
      processCount: Math.floor(Math.random() * 200) + 100,
      disks: [
        { path: '/', total: 500000, used: 150000, usage: 30 },
        { path: '/data', total: 1000000, used: 500000, usage: 50 }
      ],
      networks: [
        { iface: 'eth0', rxBytes: 1024000000, txBytes: 512000000 }
      ]
    })
    loadHistoryMetric()
  }
}

// 加载历史趋势
const loadHistoryMetric = async () => {
  if (!trendChartRef.value || !selectedServerId.value) return

  const endTime = dayjs()
  const startTime = endTime.subtract(24, 'hour')

  try {
    const res = await getServerHistoryMetric(
      selectedServerId.value,
      startTime.format('YYYY-MM-DD HH:mm:ss'),
      endTime.format('YYYY-MM-DD HH:mm:ss')
    )
    console.log('历史监控数据响应:', res)
    // 后端返回的是 { code: 200, data: [...] } 格式
    const data = res.data || []
    console.log('渲染图表数据:', data)
    if (data.length === 0) {
      console.warn('没有历史数据，使用模拟数据')
      const mockData = generateMockData(24)
      renderChart(mockData)
    } else {
      renderChart(data)
    }
  } catch (error) {
    console.error('加载历史数据失败:', error)
    // 生成模拟数据
    const mockData = generateMockData(24)
    renderChart(mockData)
  }
}

// 生成模拟数据
const generateMockData = (hours) => {
  const data = []
  const now = dayjs()
  for (let i = hours; i >= 0; i--) {
    const time = now.subtract(i, 'hour')
    data.push({
      collectTime: time.format('YYYY-MM-DD HH:mm:ss'),
      cpuUsage: (Math.random() * 40 + 30).toFixed(2),
      memoryUsage: (Math.random() * 20 + 50).toFixed(2)
    })
  }
  return data
}

// 渲染图表
const renderChart = (data) => {
  if (!trendChartRef.value) return

  // 销毁旧图表
  if (chartInstance) {
    chartInstance.dispose()
  }

  const chart = echarts.init(trendChartRef.value)
  chartInstance = chart

  // 确保数据是数字类型
  const processedData = data.map(item => ({
    collectTime: item.collectTime,
    cpuUsage: Number(item.cpuUsage) || 0,
    memoryUsage: Number(item.memoryUsage) || 0
  }))

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['CPU 使用率', '内存使用率']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: processedData.map(item => {
        if (!item.collectTime) return ''
        return dayjs(item.collectTime).format('HH:mm')
      })
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: 'CPU 使用率',
        type: 'line',
        smooth: true,
        data: processedData.map(item => item.cpuUsage),
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      },
      {
        name: '内存使用率',
        type: 'line',
        smooth: true,
        data: processedData.map(item => item.memoryUsage),
        itemStyle: { color: '#67c23a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        }
      }
    ]
  })

  window.addEventListener('resize', () => chart.resize())
}

const getLevelClass = (value) => {
  if (!value) return ''
  if (value > 90) return 'danger'
  if (value > 70) return 'warning'
  return 'success'
}

const getProgressColor = (value) => {
  if (!value) return '#409EFF'
  if (value > 90) return '#f56c6c'
  if (value > 70) return '#e6a23c'
  return '#67c23a'
}

const formatPercentage = (value) => {
  if (value === null || value === undefined) return 0
  const num = typeof value === 'string' ? parseFloat(value) : value
  return isNaN(num) ? 0 : Math.round(num)
}

const getProgressStatus = (value) => {
  if (value === null || value === undefined) return ''
  const num = typeof value === 'string' ? parseFloat(value) : value
  if (isNaN(num)) return ''
  if (num > 90) return 'exception'
  if (num > 70) return 'warning'
  return ''
}

const formatSize = (bytes, isTraffic = false) => {
  if (!bytes) return '0'
  // 数据库中磁盘数据单位是 MB，需要转换为 GB 显示
  if (isTraffic) {
    const gb = bytes / 1024 / 1024 / 1024
    return `${gb.toFixed(2)} GB`
  }
  const gb = bytes / 1024  // MB 转 GB
  return `${gb.toFixed(0)} GB`
}

const parseJson = (str, defaultValue) => {
  if (!str) return defaultValue
  try {
    return JSON.parse(str)
  } catch {
    return defaultValue
  }
}

onMounted(() => {
  loadServers()
  // 定时刷新
  const timer = setInterval(loadServerMetric, 30000)
  return () => clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.monitor-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
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
        margin-bottom: 10px;

        &.danger { color: #f56c6c; }
        &.warning { color: #e6a23c; }
        &.success { color: #67c23a; }
      }
    }
  }

  .chart-row {
    margin-bottom: 20px;

    h4 {
      margin: 0 0 15px 0;
      font-size: 16px;
      color: #333;
    }

    .chart {
      width: 100%;
    }
  }

  h4 {
    margin: 20px 0 10px;
    font-size: 16px;
    color: #333;
  }
}
</style>
