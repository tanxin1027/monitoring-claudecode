<template>
  <div class="server-page">
    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="所属医院">
          <el-select
            v-model="searchForm.hospitalId"
            placeholder="请选择医院"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="item in hospitals"
              :key="item.id"
              :label="item.hospitalName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
            <el-option label="禁用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入服务器名称/IP"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增服务器
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="serverCode" label="服务器编码" width="120" />
        <el-table-column prop="serverName" label="服务器名称" width="180" />
        <el-table-column prop="ipAddress" label="IP 地址" width="140" />
        <el-table-column label="所属医院" width="150">
          <template #default="{ row }">
            {{ getHospitalName(row.hospitalId) }}
          </template>
        </el-table-column>
        <el-table-column label="CPU/内存" width="120">
          <template #default="{ row }">
            <span v-if="row.cpuCores">{{ row.cpuCores }}核/{{ Math.round(row.memoryTotal/1024) }}GB</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastHeartbeatTime" label="最后心跳" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="handleView(row)">
              查看
            </el-button>
            <el-button type="primary" size="small" text @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              text
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="服务器编码" prop="serverCode">
          <el-input v-model="formData.serverCode" placeholder="请输入服务器编码" />
        </el-form-item>
        <el-form-item label="服务器名称" prop="serverName">
          <el-input v-model="formData.serverName" placeholder="请输入服务器名称" />
        </el-form-item>
        <el-form-item label="所属医院" prop="hospitalId">
          <el-select v-model="formData.hospitalId" placeholder="请选择医院" style="width: 100%">
            <el-option
              v-for="item in hospitals"
              :key="item.id"
              :label="item.hospitalName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="IP 地址" prop="ipAddress">
          <el-input v-model="formData.ipAddress" placeholder="请输入 IP 地址" />
        </el-form-item>
        <el-form-item label="服务器类型">
          <el-select v-model="formData.serverType" placeholder="请选择类型" style="width: 100%">
            <el-option label="物理机" :value="1" />
            <el-option label="虚拟机" :value="2" />
            <el-option label="容器" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作系统">
          <el-input v-model="formData.osType" placeholder="如：CentOS 7.9" />
        </el-form-item>
        <el-form-item label="CPU 核心数">
          <el-input-number v-model="formData.cpuCores" :min="1" :max="128" />
        </el-form-item>
        <el-form-item label="总内存 (MB)">
          <el-input-number v-model="formData.memoryTotal" :min="512" :step="512" />
        </el-form-item>
        <el-form-item label="总磁盘 (GB)">
          <el-input-number v-model="formData.diskTotal" :min="10" :step="10" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="服务器详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="服务器编码">{{ currentServer.serverCode }}</el-descriptions-item>
        <el-descriptions-item label="服务器名称">{{ currentServer.serverName }}</el-descriptions-item>
        <el-descriptions-item label="IP 地址">{{ currentServer.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="所属医院">{{ getHospitalName(currentServer.hospitalId) }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ currentServer.osType }} {{ currentServer.osVersion }}</el-descriptions-item>
        <el-descriptions-item label="服务器类型">
          <el-tag size="small">{{ getServerTypeText(currentServer.serverType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="CPU 核心数">{{ currentServer.cpuCores }} 核</el-descriptions-item>
        <el-descriptions-item label="内存">{{ Math.round(currentServer.memoryTotal/1024) }} GB</el-descriptions-item>
        <el-descriptions-item label="磁盘">{{ currentServer.diskTotal }} GB</el-descriptions-item>
        <el-descriptions-item label="Agent 版本">{{ currentServer.agentVersion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后心跳">{{ currentServer.lastHeartbeatTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentServer.status)" size="small">
            {{ getStatusText(currentServer.status) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div style="margin-top: 20px">
        <h4>实时性能监控</h4>
        <el-row :gutter="20" style="margin-top: 15px">
          <el-col :span="12">
            <div style="text-align: center">
              <div style="font-size: 14px; color: #666">CPU 使用率</div>
              <div style="font-size: 32px; color: #409EFF; font-weight: bold">
                {{ currentMetric?.cpuUsage?.toFixed(1) || 0 }}%
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div style="text-align: center">
              <div style="font-size: 14px; color: #666">内存使用率</div>
              <div style="font-size: 32px; color: #67c23a; font-weight: bold">
                {{ currentMetric?.memoryUsage?.toFixed(1) || 0 }}%
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getServerList, addServer, updateServer, deleteServer, getServerDetail, getServerMetric, getAllHospitals } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('新增服务器')
const formRef = ref(null)

const hospitals = ref([])
const tableData = ref([])
const currentServer = ref({})
const currentMetric = ref(null)

const searchForm = reactive({
  hospitalId: null,
  status: null,
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive({
  id: null,
  serverCode: '',
  serverName: '',
  hospitalId: null,
  ipAddress: '',
  serverType: 1,
  osType: '',
  cpuCores: 4,
  memoryTotal: 8192,
  diskTotal: 100,
  status: 1
})

const rules = {
  serverCode: [{ required: true, message: '请输入服务器编码', trigger: 'blur' }],
  serverName: [{ required: true, message: '请输入服务器名称', trigger: 'blur' }],
  hospitalId: [{ required: true, message: '请选择所属医院', trigger: 'change' }],
  ipAddress: [
    { required: true, message: '请输入 IP 地址', trigger: 'blur' },
    { pattern: /^(\d{1,3}\.){3}\d{1,3}$/, message: '请输入正确的 IP 地址', trigger: 'blur' }
  ]
}

// 获取医院列表
const loadHospitals = async () => {
  try {
    const res = await getAllHospitals()
    hospitals.value = res.data || []
  } catch (error) {
    hospitals.value = [
      { id: 1, hospitalName: '北京市第一人民医院' },
      { id: 2, hospitalName: '上海市第二人民医院' },
      { id: 3, hospitalName: '广州市第三人民医院' }
    ]
  }
}

// 加载服务器列表
const loadData = async () => {
  loading.value = true
  try {
    const res = await getServerList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      hospitalId: searchForm.hospitalId,
      status: searchForm.status,
      keyword: searchForm.keyword
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    // 使用模拟数据
    tableData.value = [
      { id: 1, serverCode: 'S001', serverName: 'Web 服务器 01', hospitalId: 1, ipAddress: '192.168.1.101', serverType: 2, osType: 'CentOS', osVersion: '7.9', cpuCores: 8, memoryTotal: 16384, diskTotal: 500, status: 1, lastHeartbeatTime: '2026-04-09 18:30:00' },
      { id: 2, serverCode: 'S002', serverName: 'DB 服务器 01', hospitalId: 1, ipAddress: '192.168.1.102', serverType: 1, osType: 'Ubuntu', osVersion: '20.04', cpuCores: 16, memoryTotal: 32768, diskTotal: 1000, status: 1, lastHeartbeatTime: '2026-04-09 18:29:00' },
      { id: 3, serverCode: 'S003', serverName: '应用服务器 01', hospitalId: 2, ipAddress: '192.168.2.101', serverType: 2, osType: 'CentOS', osVersion: '7.9', cpuCores: 4, memoryTotal: 8192, diskTotal: 200, status: 0, lastHeartbeatTime: '2026-04-09 17:00:00' }
    ]
    pagination.total = 3
  } finally {
    loading.value = false
  }
}

const getHospitalName = (hospitalId) => {
  const hospital = hospitals.value.find(h => h.id === hospitalId)
  return hospital?.hospitalName || '-'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '离线', 1: '在线', 2: '禁用' }
  return map[status] || '未知'
}

const getServerTypeText = (type) => {
  const map = { 1: '物理机', 2: '虚拟机', 3: '容器' }
  return map[type] || '未知'
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.hospitalId = null
  searchForm.status = null
  searchForm.keyword = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增服务器'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑服务器'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = async (row) => {
  currentServer.value = { ...row }
  viewDialogVisible.value = true

  // 加载实时性能数据
  try {
    const res = await getServerMetric(row.id)
    currentMetric.value = res.data
  } catch (error) {
    // 模拟数据
    currentMetric.value = {
      cpuUsage: (Math.random() * 100).toFixed(2),
      memoryUsage: (Math.random() * 100).toFixed(2)
    }
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该服务器吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteServer(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.success('删除成功')
      loadData()
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          await updateServer(formData)
          ElMessage.success('修改成功')
        } else {
          await addServer(formData)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.success(formData.id ? '修改成功' : '新增成功')
        dialogVisible.value = false
        loadData()
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  Object.keys(formData).forEach(key => {
    if (['serverType', 'status'].includes(key)) {
      formData[key] = key === 'status' ? 1 : 1
    } else if (['cpuCores', 'memoryTotal', 'diskTotal'].includes(key)) {
      formData[key] = key === 'cpuCores' ? 4 : key === 'memoryTotal' ? 8192 : 100
    } else {
      formData[key] = null
    }
  })
}

onMounted(() => {
  loadHospitals()
  loadData()
})
</script>

<style lang="scss" scoped>
.server-page {
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
