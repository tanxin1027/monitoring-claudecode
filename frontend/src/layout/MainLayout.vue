<template>
  <div class="main-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
        <div class="logo">
          <el-icon :size="28" color="#409EFF"><Monitor /></el-icon>
          <span v-show="!isCollapse">监控平台</span>
        </div>

        <el-menu
          :default-active="activeMenu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>

          <el-sub-menu index="hospital">
            <template #title>
              <el-icon><OfficeBuilding /></el-icon>
              <span>医院管理</span>
            </template>
            <el-menu-item index="/hospital"><el-icon><List /></el-icon>医院列表</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="server">
            <template #title>
              <el-icon><Platform /></el-icon>
              <span>服务器管理</span>
            </template>
            <el-menu-item index="/server"><el-icon><List /></el-icon>服务器列表</el-menu-item>
            <el-menu-item index="/server/mysql"><el-icon><Connection /></el-icon>MySQL 实例</el-menu-item>
            <el-menu-item index="/server/tomcat"><el-icon><Cpu /></el-icon>Tomcat 实例</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="monitor">
            <template #title>
              <el-icon><Odometer /></el-icon>
              <span>监控中心</span>
            </template>
            <el-menu-item index="/monitor/server"><el-icon><Monitor /></el-icon>服务器监控</el-menu-item>
            <el-menu-item index="/monitor/mysql"><el-icon><Connection /></el-icon>MySQL 监控</el-menu-item>
            <el-menu-item index="/monitor/tomcat"><el-icon><Cpu /></el-icon>Tomcat 监控</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="alarm">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>告警管理</span>
            </template>
            <el-menu-item index="/alarm/rule"><el-icon><Setting /></el-icon>告警规则</el-menu-item>
            <el-menu-item index="/alarm/record"><el-icon><Document /></el-icon>告警记录</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/token">
            <el-icon><Key /></el-icon>
            <template #title>Token 管理</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主体内容 -->
      <el-container>
        <!-- 顶栏 -->
        <el-header class="header">
          <div class="header-left">
            <el-icon class="collapse-btn" @click="toggleCollapse">
              <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
          </div>

          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :icon="User" />
                <span class="username">{{ userStore.userInfo?.realName || '管理员' }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    await ElMessageBox.confirm('确认退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } else if (command === 'profile') {
    ElMessage.info('个人中心功能开发中')
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.el-container {
  height: 100%;
}

.sidebar {
  background-color: #304156;
  transition: width 0.28s;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    background-color: #2b3a4b;
  }

  :deep(.el-menu) {
    border-right: none;
  }

  :deep(.el-menu-item:hover),
  :deep(.el-sub-menu__title:hover) {
    background-color: #263445 !important;
  }
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;

  .header-left {
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: #409EFF;
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;

      .username {
        color: #333;
        font-size: 14px;
      }
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
