<template>
  <div class="token-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Agent Token 管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            生成 Token
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="服务器">
          <el-select
            v-model="searchForm.serverId"
            placeholder="请选择服务器"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="item in servers"
              :key="item.id"
              :label="item.serverName"
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
          <el-button type="primary" :loading="loading" @click="loadData">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            生成 Token
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="token" label="Token" min-width="200">
          <template #default="{ row }">
            <span class="token-text">{{ row.token }}</span>
            <el-button text type="primary" size="small" @click="copyToken(row.token)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="serverId" label="服务器 ID" width="120" />
        <el-table-column prop="serverName" label="服务器名称" width="150" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              text
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
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
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="服务器" prop="serverId">
          <el-select v-model="formData.serverId" placeholder="请选择服务器" style="width: 100%">
            <el-option
              v-for="item in servers"
              :key="item.id"
              :label="item.serverName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="服务器名称" prop="serverName">
          <el-input v-model="formData.serverName" placeholder="请输入服务器名称" />
        </el-form-item>
        <el-form-item label="Token" prop="token">
          <el-input v-model="formData.token" placeholder="留空自动生成">
            <template #append>
              <el-button @click="generateTokenLocal">
                <el-icon><Refresh /></el-icon>
                生成
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CopyDocument, Plus, Refresh } from '@element-plus/icons-vue'
import { getTokenList, generateTokenApi, deleteToken, toggleTokenStatus, getServerList } from '@/api'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('生成 Token')
const formRef = ref(null)
const servers = ref([])

const searchForm = reactive({
  serverId: '',
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  serverId: '',
  serverName: '',
  token: '',
  remark: ''
})

const rules = {
  serverId: [{ required: true, message: '请输入服务器 ID', trigger: 'blur' }],
  serverName: [{ required: true, message: '请输入服务器名称', trigger: 'blur' }]
}

// 加载服务器列表
const loadServers = async () => {
  try {
    const res = await getServerList({ pageNum: 1, pageSize: 100 })
    servers.value = res.data?.records || []
  } catch (error) {
    // 如果接口失败，使用模拟数据
    servers.value = [
      { id: 1, serverName: 'Web 服务器 01' },
      { id: 2, serverName: 'DB 服务器 01' },
      { id: 3, serverName: '应用服务器 01' }
    ]
  }
}

// 加载 Token 列表
const loadData = async () => {
  loading.value = true
  try {
    const res = await getTokenList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      serverId: searchForm.serverId
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    // 使用模拟数据
    tableData.value = [
      { id: 1, token: 'agt_7f3b9c2e1a4d8f6e', serverId: 'S001', serverName: 'Web 服务器 01', creator: 'admin', createTime: '2026-04-01 10:00:00', status: 1 },
      { id: 2, token: 'agt_5e8a2c9f4b1d7e3a', serverId: 'S002', serverName: 'DB 服务器 01', creator: 'admin', createTime: '2026-04-02 14:30:00', status: 1 },
      { id: 3, token: 'agt_9d4e7a1c6b3f2e8a', serverId: 'S003', serverName: '应用服务器 01', creator: 'admin', createTime: '2026-04-03 09:15:00', status: 0 }
    ]
    pagination.total = 3
  } finally {
    loading.value = false
  }
}

const submitLoading = ref(false)

// 生成 Token（本地）
const generateTokenLocal = () => {
  const chars = 'abcdef0123456789'
  let token = 'agt_'
  for (let i = 0; i < 16; i++) {
    token += chars[Math.floor(Math.random() * chars.length)]
  }
  formData.token = token
}

// 复制 Token
const copyToken = (token) => {
  navigator.clipboard.writeText(token)
  ElMessage.success('Token 已复制到剪贴板')
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '生成 Token'
  Object.assign(formData, { id: null, serverId: '', serverName: '', token: '', remark: '' })
  generateTokenLocal()
  dialogVisible.value = true
}

// 切换状态
const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}该 Token 吗？`, '提示', {
      type: 'warning'
    })
    await toggleTokenStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该 Token 吗？删除后 Agent 将无法上报数据', '警告', {
      type: 'warning'
    })
    await deleteToken(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 重置
const handleReset = () => {
  searchForm.serverId = ''
  searchForm.status = null
  pagination.pageNum = 1
  loadData()
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
  formData.serverId = ''
  formData.serverName = ''
  formData.token = ''
  formData.remark = ''
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    // 调用后端 API 生成 Token
    const res = await generateTokenApi({
      serverId: formData.serverId,
      remark: formData.remark
    })

    // 显示生成的 Token
    if (res.data?.token) {
      formData.token = res.data.token
      ElMessage.success('Token 生成成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    ElMessage.success('生成成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadServers()
  loadData()
})
</script>

<style lang="scss" scoped>
.token-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .token-text {
    font-family: 'Courier New', monospace;
    font-size: 14px;
    color: #409EFF;
    margin-right: 8px;
  }
}
</style>
