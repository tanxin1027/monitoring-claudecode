<template>
  <div class="alarm-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>告警规则</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增规则
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="ruleName" label="规则名称" />
        <el-table-column prop="metricType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getMetricTypeColor(row.metricType)" size="small">
              {{ getMetricTypeText(row.metricType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="metricKey" label="指标" width="150" />
        <el-table-column prop="operator" label="操作符" width="80" />
        <el-table-column prop="thresholdValue" label="阈值" width="100" />
        <el-table-column prop="continuousThreshold" label="连续次数" width="100" />
        <el-table-column prop="notifyMethod" label="通知方式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.notifyMethod === 1 ? 'warning' : 'success'" size="small">
              {{ row.notifyMethod === 1 ? '短信' : '微信' }}
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
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" text @click="handleDelete(row)">
              删除
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="formData.ruleName" placeholder="如：CPU 使用率告警" />
        </el-form-item>
        <el-form-item label="监控类型" prop="metricType">
          <el-select v-model="formData.metricType" placeholder="请选择" style="width: 100%">
            <el-option label="服务器" :value="1" />
            <el-option label="MySQL" :value="2" />
            <el-option label="Tomcat" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="监控指标" prop="metricKey">
          <el-select v-model="formData.metricKey" placeholder="请选择" style="width: 100%">
            <el-option label="CPU 使用率" value="cpuUsage" />
            <el-option label="内存使用率" value="memoryUsage" />
            <el-option label="磁盘使用率" value="diskUsage" />
            <el-option label="连接数" value="connections" v-if="formData.metricType === 2" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作符" prop="operator">
          <el-select v-model="formData.operator" placeholder="请选择" style="width: 100%">
            <el-option label="大于" value=">" />
            <el-option label="小于" value="<" />
            <el-option label="等于" value="=" />
            <el-option label="大于等于" value=">=" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值" prop="thresholdValue">
          <el-input v-model="formData.thresholdValue" placeholder="如：90" />
        </el-form-item>
        <el-form-item label="连续次数" prop="continuousThreshold">
          <el-input-number v-model="formData.continuousThreshold" :min="1" :max="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="通知方式" prop="notifyMethod">
          <el-radio-group v-model="formData.notifyMethod">
            <el-radio :label="1">短信</el-radio>
            <el-radio :label="2">微信</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="等级" prop="severity">
          <el-select v-model="formData.severity" placeholder="请选择" style="width: 100%">
            <el-option label="提示" :value="1" />
            <el-option label="警告" :value="2" />
            <el-option label="严重" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAlarmRuleList, addAlarmRule, updateAlarmRule, deleteAlarmRule, toggleAlarmRule } from '@/api/alarm'

const dialogVisible = ref(false)
const dialogTitle = ref('新增规则')
const tableData = ref([])
const formRef = ref(null)
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive({
  id: null,
  ruleName: '',
  metricType: 1,
  metricKey: '',
  operator: '>',
  thresholdValue: '',
  continuousThreshold: 3,
  notifyMethod: 2,
  severity: 2,
  status: 1
})

const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  metricType: [{ required: true, message: '请选择监控类型', trigger: 'change' }],
  metricKey: [{ required: true, message: '请选择监控指标', trigger: 'change' }],
  thresholdValue: [{ required: true, message: '请输入阈值', trigger: 'blur' }]
}

const loadData = async () => {
  try {
    const res = await getAlarmRuleList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      status: null,
      keyword: null
    })
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('Load alarm rule list error:', error)
    ElMessage.error('加载规则列表失败')
  }
}

const getMetricTypeText = (type) => {
  const map = { 1: '服务器', 2: 'MySQL', 3: 'Tomcat' }
  return map[type] || '未知'
}

const getMetricTypeColor = (type) => {
  const map = { 1: '', 2: 'warning', 3: 'success' }
  return map[type] || ''
}

const getSeverityText = (severity) => {
  const map = { 1: '提示', 2: '警告', 3: '严重' }
  return map[severity] || '未知'
}

const getSeverityType = (severity) => {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[severity] || 'info'
}

const handleAdd = () => {
  dialogTitle.value = '新增规则'
  Object.assign(formData, { id: null, ruleName: '', metricType: 1, metricKey: '', operator: '>', thresholdValue: '', continuousThreshold: 3, notifyMethod: 2, severity: 2, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑规则'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该规则吗？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await deleteAlarmRule(row.id)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadData()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (error) {
        console.error('Delete alarm rule error:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const api = formData.id ? updateAlarmRule : addAlarmRule
        const res = await api(formData)
        if (res.code === 200) {
          ElMessage.success(formData.id ? '修改成功' : '新增成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.msg || (formData.id ? '修改失败' : '新增失败'))
        }
      } catch (error) {
        console.error('Submit alarm rule error:', error)
        ElMessage.error(formData.id ? '修改失败' : '新增失败')
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.alarm-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
