# 医院监控平台前端

基于 Vue 3 + Element Plus 的医院监控平台管理界面

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.0 | 渐进式 JavaScript 框架 |
| Vue Router | 4.2.5 | 官方路由管理器 |
| Pinia | 2.1.7 | Vue 官方状态管理 |
| Element Plus | 2.5.4 | Vue 3 UI 组件库 |
| Axios | 1.6.5 | HTTP 请求库 |
| ECharts | 5.4.3 | 数据可视化图表库 |
| Day.js | 1.11.10 | 日期处理库 |
| Vite | 5.0.12 | 前端构建工具 |
| Sass | 1.70.0 | CSS 预处理器 |

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口
│   │   └── index.js      # 统一导出所有接口
│   ├── layout/           # 布局组件
│   │   └── MainLayout.vue # 主布局（侧边栏 + 顶栏）
│   ├── router/           # 路由配置
│   │   └── index.js      # 路由定义和守卫
│   ├── store/            # 状态管理
│   │   └── user.js       # 用户状态
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios 封装
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── dashboard/    # 仪表盘
│   │   ├── hospital/     # 医院管理
│   │   ├── server/       # 服务器管理
│   │   ├── monitor/      # 监控中心
│   │   │   ├── ServerMonitor.vue  # 服务器监控
│   │   │   ├── MysqlMonitor.vue   # MySQL 监控
│   │   │   └── TomcatMonitor.vue  # Tomcat 监控
│   │   ├── alarm/        # 告警管理
│   │   │   ├── RuleList.vue   # 告警规则
│   │   │   └── RecordList.vue # 告警记录
│   │   └── token/        # Token 管理
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML 模板
├── package.json          # 依赖配置
└── vite.config.js        # Vite 配置
```

## 安装使用

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:5173

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览构建结果

```bash
npm run preview
```

## 页面说明

### 1. 登录页 (`/login`)

- 支持用户名密码登录
- 默认账号：`admin` / `admin123`
- 登录成功后跳转至仪表盘
- 自动保存 Token 到 localStorage

**功能特性：**
- 美观的渐变背景 + 浮动圆圈动画
- 表单验证
- 登录状态持久化

### 2. 仪表盘 (`/dashboard`)

- 服务器统计卡片（总数、在线、离线、未处理告警）
- CPU/内存使用率 Top10 排行图表
- MySQL/Tomcat 实例状态概览
- 最新告警记录列表
- 支持自动刷新（30 秒间隔）
- 手动刷新按钮

**功能特性：**
- 实时数据更新
- 图表可视化展示
- 在线率自动计算
- 趋势对比显示

### 3. 医院管理 (`/hospital`)

- 医院列表展示
- 新增/编辑/删除医院
- 支持按名称搜索
- 分页展示

**医院信息：**
- 编码、名称、省份、城市、地址
- 联系人、联系电话
- 状态（正常/禁用）

### 4. 服务器管理 (`/server`)

- 服务器列表展示
- 新增/编辑/删除服务器
- 支持按医院、状态、关键词搜索
- 查看详情（实时性能）

**服务器信息：**
- 编码、名称、IP 地址
- 所属医院
- 类型（物理机/虚拟机/容器）
- 配置（CPU 核数、内存、磁盘）
- 状态（在线/离线/禁用）

### 5. 服务器监控 (`/monitor/server`)

- 实时性能监控（CPU、内存、负载、进程数）
- 历史趋势图（24 小时）
- 磁盘使用情况（按挂载点）
- 网络流量统计

**功能特性：**
- 自动刷新（30 秒）
- 实例选择器
- 动态图表渲染

### 6. MySQL 监控 (`/monitor/mysql`)

- 实例选择器
- 连接数（当前/最大）
- QPS/TPS
- 慢查询数
- 线程状态
- 流量统计

**功能特性：**
- 自动刷新（30 秒）
- 实时性能指标

### 7. Tomcat 监控 (`/monitor/tomcat`)

- 实例选择器
- JVM 堆内存使用
- GC 次数/时间
- 线程池状态
- Request/Session 统计
- 运行时间

**功能特性：**
- 自动刷新（30 秒）
- 进度条展示

### 8. 告警规则 (`/alarm/rule`)

- 告警规则列表
- 新增/编辑/删除规则
- 支持服务器、MySQL、Tomcat 监控类型
- 可配置阈值和等级

**规则信息：**
- 规则名称
- 监控类型
- 监控指标
- 操作符（>/<=）
- 阈值
- 等级（提示/警告/严重）

### 9. 告警记录 (`/alarm/record`)

- 告警记录列表
- 支持按状态筛选
- 告警处理功能
- 处理备注

**告警信息：**
- 告警内容
- 等级
- 状态（未处理/已处理）
- 告警时间
- 处理时间

### 10. Token 管理 (`/token`)

- Agent Token 列表
- 生成新 Token
- 启用/禁用 Token
- 删除 Token
- 一键复制 Token

**Token 信息：**
- Token 值
- 关联服务器
- 创建人
- 创建时间
- 状态（启用/禁用）

## API 接口

所有 API 接口定义在 `src/api/index.js`，按模块分类：

### 用户认证
- `login()` - 用户登录
- `getUserInfo()` - 获取用户信息
- `changePassword()` - 修改密码

### 仪表盘
- `getDashboardOverview()` - 概览数据
- `getServerStats()` - 服务器统计
- `getAlarmStats()` - 告警统计
- `getCpuTop()` - CPU 排行
- `getMemoryTop()` - 内存排行

### 医院管理
- `getHospitalList()` - 医院列表
- `getAllHospitals()` - 所有医院
- `addHospital()` - 新增医院
- `updateHospital()` - 修改医院
- `deleteHospital()` - 删除医院

### 服务器管理
- `getServerList()` - 服务器列表
- `getServerDetail()` - 服务器详情
- `addServer()` - 新增服务器
- `updateServer()` - 修改服务器
- `deleteServer()` - 删除服务器
- `getServerMetric()` - 监控数据
- `getServerHistoryMetric()` - 历史数据

### MySQL 监控
- `getMysqlInstanceList()` - 实例列表
- `getMysqlInstanceDetail()` - 实例详情
- `getMysqlMetric()` - 监控数据
- `addMysqlInstance()` - 新增实例
- `updateMysqlInstance()` - 修改实例
- `deleteMysqlInstance()` - 删除实例

### Tomcat 监控
- `getTomcatInstanceList()` - 实例列表
- `getTomcatInstanceDetail()` - 实例详情
- `getTomcatMetric()` - 监控数据
- `addTomcatInstance()` - 新增实例
- `updateTomcatInstance()` - 修改实例
- `deleteTomcatInstance()` - 删除实例

### 告警管理
- `getAlarmRuleList()` - 规则列表
- `addAlarmRule()` - 新增规则
- `updateAlarmRule()` - 修改规则
- `deleteAlarmRule()` - 删除规则
- `getAlarmRecordList()` - 记录列表
- `handleAlarm()` - 处理告警
- `batchHandleAlarm()` - 批量处理

### Token 管理
- `getTokenList()` - Token 列表
- `generateToken()` - 生成 Token
- `deleteToken()` - 删除 Token
- `toggleTokenStatus()` - 切换状态

## 状态管理

使用 Pinia 进行状态管理：

```javascript
// store/user.js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return { token, userInfo, setToken, setUserInfo, logout }
})
```

## 路由守卫

```javascript
// router/index.js
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
```

## HTTP 请求封装

```javascript
// utils/request.js
const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器 - 自动添加 Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 - 统一错误处理
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)
```

## API 代理配置

开发环境下，API 请求会代理到后端服务：

```javascript
// vite.config.js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

## 默认登录账号

- 用户名：`admin`
- 密码：`admin123`

## 注意事项

1. 首次运行需要先启动后端服务
2. 确保后端 API 接口可用
3. 部分接口如果后端未实现，前端会使用模拟数据展示
4. 图表需要容器有明确的高度

## 开发规范

### 组件命名
- 页面组件使用 PascalCase 命名
- 组件文件命名与组件名一致

### 代码风格
- 使用 Composition API (script setup)
- 使用 Vue 3 响应式 API (ref, reactive, computed)
- 使用 Element Plus 组件库
- 使用 Sass 进行样式开发

## 待开发功能

1. 个人中心页面
2. 系统设置页面
3. 用户管理页面
4. 角色权限管理
5. 操作日志页面
6. 数据导出功能
7. 暗黑模式切换
8. 多语言支持
