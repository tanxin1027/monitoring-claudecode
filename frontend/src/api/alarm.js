import request from '@/utils/request'

/**
 * 告警规则管理 API
 */

// 分页查询告警规则列表
export function getAlarmRuleList(params) {
  return request({
    url: '/alarm/rule/list',
    method: 'get',
    params
  })
}

// 查询所有启用的告警规则
export function listEnabledRules() {
  return request({
    url: '/alarm/rule/enabled',
    method: 'get'
  })
}

// 根据 ID 查询告警规则
export function getAlarmRule(id) {
  return request({
    url: `/alarm/rule/${id}`,
    method: 'get'
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

// 启用/禁用告警规则
export function toggleAlarmRule(id, status) {
  return request({
    url: `/alarm/rule/${id}/toggle?status=${status}`,
    method: 'put'
  })
}

/**
 * 告警记录管理 API
 */

// 分页查询告警记录列表
export function getAlarmRecordList(params) {
  return request({
    url: '/alarm/record/list',
    method: 'get',
    params
  })
}

// 根据 ID 查询告警记录
export function getAlarmRecord(id) {
  return request({
    url: `/alarm/record/${id}`,
    method: 'get'
  })
}

// 处理告警
export function processAlarmRecord(id, handler, handleRemark) {
  return request({
    url: `/alarm/record/${id}/process?handler=${encodeURIComponent(handler)}${handleRemark ? '&handleRemark=' + encodeURIComponent(handleRemark) : ''}`,
    method: 'put'
  })
}

// 批量处理告警
export function batchProcessAlarmRecords(ids, handler, handleRemark) {
  return request({
    url: `/alarm/record/batch-process?handler=${encodeURIComponent(handler)}${handleRemark ? '&handleRemark=' + encodeURIComponent(handleRemark) : ''}`,
    method: 'put',
    data: ids
  })
}

// 忽略告警
export function ignoreAlarmRecord(id, handler) {
  return request({
    url: `/alarm/record/${id}/ignore?handler=${encodeURIComponent(handler)}`,
    method: 'put'
  })
}

// 统计未处理告警数
export function countUnprocessedAlarms() {
  return request({
    url: '/alarm/record/unprocessed-count',
    method: 'get'
  })
}

// 分页查询告警分组列表（连续告警）
export function getAlarmGroups(params) {
  return request({
    url: '/alarm/record/groups',
    method: 'get',
    params
  })
}

// 根据分组标识查询告警记录列表
export function getAlarmRecordsByGroupKey(groupKey) {
  return request({
    url: `/alarm/record/group/${encodeURIComponent(groupKey)}`,
    method: 'get'
  })
}
