<!--
Tomcat 监控页面 - 测试版本
此页面使用模拟数据，用于展示 Tomcat 监控页面的 UI 和功能
当后端 API 开发完成后，可以切换到真实的 API 调用
-->
<template>
  <div class="tomcat-monitor-test">
    <el-card class="test-header">
      <template #header>
        <div class="card-header">
          <span>Tomcat 监控 - 测试页面</span>
          <el-button @click="toggleMode">
            {{ useMockData ? '切换到真实API' : '切换到模拟数据' }}
          </el-button>
        </div>
      </template>

      <div class="mode-info">
        <el-alert
          :title="`当前模式: ${useMockData ? '模拟数据' : '真实API'}${useMockData ? ' (用于开发测试)' : ' (需要后端API支持)'}`"
          :type="useMockData ? 'info' : 'warning'"
          :closable="false"
        />
      </div>
    </el-card>

    <TomcatMonitorVue />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TomcatMonitorVue from './TomcatMonitor.vue' // 导入实际的页面

const useMockData = ref(true)

const toggleMode = () => {
  useMockData.value = !useMockData.value
  alert(`切换到${useMockData.value ? '模拟数据' : '真实API'}模式`)
}
</script>

<style scoped>
.test-header {
  margin-bottom: 20px;
}

.mode-info {
  margin-top: 10px;
}
</style>