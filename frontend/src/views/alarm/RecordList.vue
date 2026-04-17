<template>
  <div class="alarm-record-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>告警记录</span>
          <el-form :inline="true">
            <el-form-item>
              <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
                <el-option label="未处理" :value="0" />
                <el-option label="已处理" :value="1" />
                <el-option label="已忽略" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-radio-group v-model="viewMode" size="small" @change="loadData">
                <el-radio-button label="list">列表视图</el-radio-button>
                <el-radio-button label="group">分组视图</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadData">搜索</el-button>
            </el-form-item>
          </el-form>
        </div>
      </template>

      <!-- 列表视图 -->
      <el-table v-if="viewMode === 'list'" :data="tableData" border stripe>
        <el-table-column prop="alarmContent" label="告警内容" min-width="200" />
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
        <el-table-column prop="createTime" label="告警时间" width="180" />
        <el-table-column prop="handleTime" label="处理时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              text
              @click="handleProcess(row)"
            >
              处理
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="info"
              size="small"
              text
              @click="handleIgnore(row)"
            >
              忽略
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分组视图 -->
      <el-table v-else :data="groupData" border stripe row-key="groupKey">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-content">
              <h4>告警详情（共 {{ row.records?.length || 0 }} 条）</h4>
              <el-table :data="row.records" :key="row.groupKey" border stripe size="small">
                <el-table-column prop="alarmContent" label="告警内容" min-width="180" />
                <el-table-column prop="continuousCount" label="连续次数" width="80" />
                <el-table-column prop="severity" label="等级" width="70">
                  <template #default="{ row: rec }">
                    <el-tag :type="getSeverityType(rec.severity)" size="small">
                      {{ getSeverityText(rec.severity) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="alarmTime" label="告警时间" width="160" />
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="instanceName" label="实例名称" width="150" />
        <el-table-column prop="ruleName" label="告警规则" width="150" />
        <el-table-column prop="metricKey" label="指标" width="120" />
        <el-table-column prop="continuousCount" label="连续告警次数" width="120">
          <template #default="{ row }">
            <el-tag :type="row.continuousCount >= 3 ? 'danger' : 'warning'" size="small">
              {{ row.continuousCount }} 次
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notifyStatus" label="通知状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.notifyStatus === 1 ? 'success' : 'info'" size="small">
              {{ row.notifyStatus === 1 ? '已通知' : '未通知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="等级" width="80">
          <template #default="{ row }">
            <el-tag :type="getSeverityType(row.severity)" size="small">
              {{ getSeverityText(row.severity) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">
              {{ row.status === 0 ? '未处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="告警时间" width="180">
          <template #default="{ row }">
            <div v-if="row.firstAlarmTime && row.lastAlarmTime">
              <div>首次：{{ row.firstAlarmTime }}</div>
              <div>最近：{{ row.lastAlarmTime }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              text
              @click="handleProcessGroup(row)"
            >
              批量处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 处理对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理告警" width="500px">
      <el-form :model="processForm" label-width="80px">
        <el-form-item label="处理备注">
          <el-input
            v-model="processForm.handleRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProcess">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAlarmRecordList, getAlarmGroups, processAlarmRecord, ignoreAlarmRecord, countUnprocessedAlarms } from '@/api/alarm'

const viewMode = ref('list')
const tableData = ref([])
const groupData = ref([])
const processDialogVisible = ref(false)
const currentAlarm = ref(null)

const searchForm = reactive({
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const processForm = reactive({
  handleRemark: ''
})

const loadData = async () => {
  try {
    if (viewMode.value === 'group') {
      // 分组视图
      const res = await getAlarmGroups({
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        status: searchForm.status
      })
      if (res.code === 200 && res.data) {
        groupData.value = res.data.records || []
        pagination.total = res.data.total || 0
      }
    } else {
      // 列表视图
      const res = await getAlarmRecordList({
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        status: searchForm.status,
        severity: null,
        keyword: null
      })
      if (res.code === 200 && res.data) {
        tableData.value = res.data.records || []
        pagination.total = res.data.total || 0
      }
    }
  } catch (error) {
    console.error('Load alarm record list error:', error)
    ElMessage.error('加载记录列表失败')
  }
}

const handleProcessGroup = (row) => {
  currentAlarm.value = row
  processForm.handleRemark = ''
  processDialogVisible.value = true
}

const getSeverityText = (severity) => {
  const map = { 1: '提示', 2: '警告', 3: '严重' }
  return map[severity] || '未知'
}

const getSeverityType = (severity) => {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[severity] || 'info'
}

const handleProcess = (row) => {
  currentAlarm.value = row
  processForm.handleRemark = ''
  processDialogVisible.value = true
}

const handleSubmitProcess = async () => {
  try {
    const res = await processAlarmRecord(currentAlarm.value.id, 'admin', processForm.handleRemark)
    if (res.code === 200) {
      ElMessage.success('处理成功')
      processDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.msg || '处理失败')
    }
  } catch (error) {
    console.error('Process alarm record error:', error)
    ElMessage.error('处理失败')
  }
}

const handleIgnore = async (row) => {
  try {
    const res = await ignoreAlarmRecord(row.id, 'admin')
    if (res.code === 200) {
      ElMessage.success('已忽略')
      loadData()
    } else {
      ElMessage.error(res.msg || '忽略失败')
    }
  } catch (error) {
    console.error('Ignore alarm record error:', error)
    ElMessage.error('忽略失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.alarm-record-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .expand-content {
    padding: 10px 20px;

    h4 {
      margin-bottom: 10px;
      color: #666;
      font-size: 14px;
    }
  }
}
</style>

<style lang="scss" scoped>
.alarm-record-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
