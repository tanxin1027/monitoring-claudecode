import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'hospital',
        name: 'Hospital',
        component: () => import('@/views/hospital/Index.vue'),
        meta: { title: '医院管理' }
      },
      {
        path: 'server',
        name: 'Server',
        component: () => import('@/views/server/Index.vue'),
        meta: { title: '服务器管理' }
      },
      {
        path: 'server/mysql',
        name: 'ServerMysql',
        component: () => import('@/views/server/MysqlInstance.vue'),
        meta: { title: 'MySQL 实例管理' }
      },
      {
        path: 'server/tomcat',
        name: 'ServerTomcat',
        component: () => import('@/views/server/TomcatInstance.vue'),
        meta: { title: 'Tomcat 实例管理' }
      },
      {
        path: 'monitor/server',
        name: 'MonitorServer',
        component: () => import('@/views/monitor/ServerMonitor.vue'),
        meta: { title: '服务器监控' }
      },
      {
        path: 'monitor/mysql',
        name: 'MonitorMysql',
        component: () => import('@/views/monitor/MysqlMonitor.vue'),
        meta: { title: 'MySQL 监控' }
      },
      {
        path: 'monitor/tomcat',
        name: 'MonitorTomcat',
        component: () => import('@/views/monitor/TomcatMonitor.vue'),
        meta: { title: 'Tomcat 监控' }
      },
      {
        path: 'alarm/rule',
        name: 'AlarmRule',
        component: () => import('@/views/alarm/RuleList.vue'),
        meta: { title: '告警规则' }
      },
      {
        path: 'alarm/record',
        name: 'AlarmRecord',
        component: () => import('@/views/alarm/RecordList.vue'),
        meta: { title: '告警记录' }
      },
      {
        path: 'token',
        name: 'Token',
        component: () => import('@/views/token/Index.vue'),
        meta: { title: 'Token 管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.path === '/login') {
    next()
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
