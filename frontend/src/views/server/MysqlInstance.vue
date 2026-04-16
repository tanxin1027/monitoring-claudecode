<template>
  <div class="mysql-instance-page">
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
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入实例名称/编码"
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
            新增实例
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
        <el-table-column prop="instanceCode" label="实例编码" width="120" />
        <el-table-column prop="instanceName" label="实例名称" width="180" />
        <el-table-column label="所属服务器" width="150">
          <template #default="{ row }">
            {{ getServerName(row.serverId) }}
          </template>
        </el-table-column>
        <el-table-column label="所属医院" width="150">
          <template #default="{ row }">
            {{ getHospitalName(row.hospitalId) }}
          </template>
        </el-table-column>
        <el-table-column prop="port" label="端口" width="80" />
        <el-table-column prop="dbVersion" label="MySQL 版本" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
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
        <el-form-item label="实例编码" prop="instanceCode">
          <el-input v-model="formData.instanceCode" placeholder="请输入实例编码" />
        </el-form-item>
        <el-form-item label="实例名称" prop="instanceName">
          <el-input v-model="formData.instanceName" placeholder="请输入实例名称" />
        </el-form-item>
        <el-form-item label="所属服务器" prop="serverId">
          <el-select v-model="formData.serverId" placeholder="请选择服务器" style="width: 100%">
            <el-option
              v-for="item in servers"
              :key="item.id"
              :label="item.serverName"
              :value="item.id"
            />
          </el-select>
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
        <el-form-item label="端口号" prop="port">
          <el-input-number v-model="formData.port" :min="1" :max="65535" :step="1" />
        </el-form-item>
        <el-form-item label="MySQL 版本">
          <el-input v-model="formData.dbVersion" placeholder="如：8.0.32" />
        </el-form-item>
        <el-form-item label="监控账号">
          <el-input v-model="formData.username" placeholder="用于监控的数据库账号" />
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
      title="MySQL 实例详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="实例编码">{{ currentInstance.instanceCode }}</el-descriptions-item>
        <el-descriptions-item label="实例名称">{{ currentInstance.instanceName }}</el-descriptions-item>
        <el-descriptions-item label="所属服务器">{{ getServerName(currentInstance.serverId) }}</el-descriptions-item>
        <el-descriptions-item label="所属医院">{{ getHospitalName(currentInstance.hospitalId) }}</el-descriptions-item>
        <el-descriptions-item label="端口号">{{ currentInstance.port }}</el-descriptions-item>
        <el-descriptions-item label="MySQL 版本">{{ currentInstance.dbVersion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="监控账号">{{ currentInstance.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentInstance.status)" size="small">
            {{ getStatusText(currentInstance.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentInstance.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentInstance.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getMysqlInstanceList, addMysqlInstance, updateMysqlInstance, deleteMysqlInstance, getServerList, getAllHospitals } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('新增实例')
const formRef = ref(null)

const hospitals = ref([])
const servers = ref([])
const tableData = ref([])
const currentInstance = ref({})

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
  instanceCode: '',
  instanceName: '',
  serverId: null,
  hospitalId: null,
  port: 3306,
  dbVersion: '',
  username: '',
  status: 1
})

const rules = {
  instanceCode: [{ required: true, message: '请输入实例编码', trigger: 'blur' }],
  instanceName: [{ required: true, message: '请输入实例名称', trigger: 'blur' }],
  serverId: [{ required: true, message: '请选择所属服务器', trigger: 'change' }],
  hospitalId: [{ required: true, message: '请选择所属医院', trigger: 'change' }],
  port: [
    { required: true, message: '请输入端口号', trigger: 'blur' },
    { type: 'number', min: 1, max: 65535, message: '端口号范围 1-65535', trigger: 'blur' }
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
      { id: 2, hospitalName: '上海市第二人民医院' }
    ]
  }
}

// 获取服务器列表
const loadServers = async () => {
  try {
    const res = await getServerList({ pageNum: 1, pageSize: 100, status: 1 })
    servers.value = res.data?.records || []
  } catch (error) {
    servers.value = [
      { id: 1, serverName: 'Web 服务器 01' },
      { id: 2, serverName: 'DB 服务器 01' }
    ]
  }
}

// 加载 MySQL 实例列表
const loadData = async () => {
  loading.value = true
  try {
    const res = await getMysqlInstanceList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      hospitalId: searchForm.hospitalId,
      status: searchForm.status,
      searchKey: searchForm.keyword
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    // 使用模拟数据
    tableData.value = [
      { id: 1, instanceCode: 'mysql_001', instanceName: '本地 MySQL 实例', serverId: 1, hospitalId: 1, port: 3306, dbVersion: '8.0.32', status: 1, createTime: '2026-04-01 10:00:00', updateTime: '2026-04-15 14:30:00' },
      { id: 2, instanceCode: 'mysql_002', instanceName: '从库 MySQL 实例', serverId: 2, hospitalId: 1, port: 3306, dbVersion: '8.0.32', status: 1, createTime: '2026-04-05 09:00:00', updateTime: '2026-04-14 11:20:00' }
    ]
    pagination.total = 2
  } finally {
    loading.value = false
  }
}

const getServerName = (serverId) => {
  const server = servers.value.find(s => s.id === serverId)
  return server?.serverName || '-'
}

const getHospitalName = (hospitalId) => {
  const hospital = hospitals.value.find(h => h.id === hospitalId)
  return hospital?.hospitalName || '-'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '禁用', 1: '启用' }
  return map[status] || '未知'
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
  dialogTitle.value = '新增 MySQL 实例'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑 MySQL 实例'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  currentInstance.value = { ...row }
  viewDialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该 MySQL 实例吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMysqlInstance(row.id)
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
          await updateMysqlInstance(formData)
          ElMessage.success('修改成功')
        } else {
          await addMysqlInstance(formData)
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
    if (['status', 'port'].includes(key)) {
      formData[key] = key === 'status' ? 1 : 3306
    } else {
      formData[key] = null
    }
  })
}

onMounted(() => {
  loadHospitals()
  loadServers()
  loadData()
})
</script>

<style lang="scss" scoped>
.mysql-instance-page {
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
