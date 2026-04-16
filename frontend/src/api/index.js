import request from '@/utils/request'

// ==================== 用户认证 ====================

// 用户登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data
  })
}

// ==================== 仪表盘 ====================

// 仪表盘概览
export function getDashboardOverview(hospitalId) {
  return request({
    url: '/dashboard/overview',
    method: 'get',
    params: { hospitalId }
  })
}

// 获取服务器统计
export function getServerStats(hospitalId) {
  return request({
    url: '/monitor/server/stats',
    method: 'get',
    params: { hospitalId }
  })
}

// 获取告警统计
export function getAlarmStats(hospitalId) {
  return request({
    url: '/dashboard/alarm-stats',
    method: 'get',
    params: { hospitalId }
  })
}

// 获取 CPU 排行
export function getCpuTop(limit = 10) {
  return request({
    url: '/dashboard/top/cpu',
    method: 'get',
    params: { limit }
  })
}

// 获取内存排行
export function getMemoryTop(limit = 10) {
  return request({
    url: '/dashboard/top/memory',
    method: 'get',
    params: { limit }
  })
}

// ==================== 医院管理 ====================

// 医院列表
export function getHospitalList(params) {
  return request({
    url: '/hospital/list',
    method: 'get',
    params
  })
}

// 获取所有医院
export function getAllHospitals() {
  return request({
    url: '/hospital/all',
    method: 'get'
  })
}

// 新增医院
export function addHospital(data) {
  return request({
    url: '/hospital',
    method: 'post',
    data
  })
}

// 修改医院
export function updateHospital(data) {
  return request({
    url: '/hospital',
    method: 'put',
    data
  })
}

// 删除医院
export function deleteHospital(id) {
  return request({
    url: `/hospital/${id}`,
    method: 'delete'
  })
}

// ==================== 服务器管理 ====================

// 服务器列表
export function getServerList(params) {
  return request({
    url: '/monitor/server/list',
    method: 'get',
    params
  })
}

// 获取服务器详情
export function getServerDetail(id) {
  return request({
    url: `/monitor/server/${id}`,
    method: 'get'
  })
}

// 新增服务器
export function addServer(data) {
  return request({
    url: '/monitor/server',
    method: 'post',
    data
  })
}

// 修改服务器
export function updateServer(data) {
  return request({
    url: '/monitor/server',
    method: 'put',
    data
  })
}

// 删除服务器
export function deleteServer(id) {
  return request({
    url: `/monitor/server/${id}`,
    method: 'delete'
  })
}

// 获取服务器监控数据
export function getServerMetric(id) {
  return request({
    url: `/monitor/server/${id}/metric`,
    method: 'get'
  })
}

// 获取服务器历史监控数据
export function getServerHistoryMetric(id, startTime, endTime) {
  return request({
    url: `/monitor/server/${id}/metric/history`,
    method: 'get',
    params: { startTime, endTime }
  })
}

// ==================== MySQL 监控 ====================

// MySQL 实例列表
export function getMysqlInstanceList(params) {
  return request({
    url: '/monitor/mysql/list',
    method: 'get',
    params
  })
}

// 获取 MySQL 实例详情
export function getMysqlInstanceDetail(id) {
  return request({
    url: `/monitor/mysql/${id}`,
    method: 'get'
  })
}

// 获取 MySQL 监控数据
export function getMysqlMetric(id) {
  return request({
    url: `/monitor/mysql/${id}/metric`,
    method: 'get'
  })
}

// 新增 MySQL 实例
export function addMysqlInstance(data) {
  return request({
    url: '/monitor/mysql',
    method: 'post',
    data
  })
}

// 修改 MySQL 实例
export function updateMysqlInstance(data) {
  return request({
    url: '/monitor/mysql',
    method: 'put',
    data
  })
}

// 删除 MySQL 实例
export function deleteMysqlInstance(id) {
  return request({
    url: `/monitor/mysql/${id}`,
    method: 'delete'
  })
}

// ==================== Tomcat 监控 ====================

// Tomcat 实例列表
export function getTomcatInstanceList(params) {
  return request({
    url: '/monitor/tomcat/list',
    method: 'get',
    params
  })
}

// 获取 Tomcat 实例详情
export function getTomcatInstanceDetail(id) {
  return request({
    url: `/monitor/tomcat/${id}`,
    method: 'get'
  })
}

// 获取 Tomcat 监控数据
export function getTomcatMetric(id) {
  return request({
    url: `/monitor/tomcat/${id}/metric`,
    method: 'get'
  })
}

// 新增 Tomcat 实例
export function addTomcatInstance(data) {
  return request({
    url: '/monitor/tomcat',
    method: 'post',
    data
  })
}

// 修改 Tomcat 实例
export function updateTomcatInstance(data) {
  return request({
    url: '/monitor/tomcat',
    method: 'put',
    data
  })
}

// 删除 Tomcat 实例
export function deleteTomcatInstance(id) {
  return request({
    url: `/monitor/tomcat/${id}`,
    method: 'delete'
  })
}

// ==================== 告警管理 ====================

// 告警规则列表
export function getAlarmRuleList(params) {
  return request({
    url: '/alarm/rule/list',
    method: 'get',
    params
  })
}

// 新增告警规则
export function addAlarmRule(data) {
  return request({
    url: '/alarm/rule',
    method: 'post',
    data
  })
}

// 修改告警规则
export function updateAlarmRule(data) {
  return request({
    url: '/alarm/rule',
    method: 'put',
    data
  })
}

// 删除告警规则
export function deleteAlarmRule(id) {
  return request({
    url: `/alarm/rule/${id}`,
    method: 'delete'
  })
}

// 告警记录列表
export function getAlarmRecordList(params) {
  return request({
    url: '/alarm/record/list',
    method: 'get',
    params
  })
}

// 处理告警
export function handleAlarm(id, data) {
  return request({
    url: `/alarm/record/${id}/handle`,
    method: 'put',
    data
  })
}

// 批量处理告警
export function batchHandleAlarm(data) {
  return request({
    url: '/alarm/record/batch-handle',
    method: 'post',
    data
  })
}

// ==================== Token 管理 ====================

// Token 列表
export function getTokenList(params) {
  return request({
    url: '/agent/token/list',
    method: 'get',
    params
  })
}

// 生成 Token
export function generateTokenApi(data) {
  return request({
    url: '/agent/token/generate',
    method: 'post',
    params: {
      serverId: data.serverId,
      remark: data.remark || undefined
    }
  })
}

// 删除 Token
export function deleteToken(id) {
  return request({
    url: `/agent/token/${id}`,
    method: 'delete'
  })
}

// 启用/禁用 Token
export function toggleTokenStatus(id, status) {
  const action = status === 1 ? 'enable' : 'disable'
  return request({
    url: `/agent/token/${id}/${action}`,
    method: 'put'
  })
}
