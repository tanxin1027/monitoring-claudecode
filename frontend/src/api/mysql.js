// API 接口定义 - MySQL 监控
// 文件: frontend/src/api/mysql.js

import request from '@/utils/request'

/**
 * 获取 MySQL 实例列表
 */
export function getMysqlInstanceList(params) {
  return request({
    url: '/monitor/mysql/list',
    method: 'get',
    params
  })
}

/**
 * 获取 MySQL 实例详情
 */
export function getMysqlInstanceDetail(instanceId) {
  return request({
    url: `/monitor/mysql/${instanceId}`,
    method: 'get'
  })
}

/**
 * 获取 MySQL 监控数据
 */
export function getMysqlMetric(instanceId) {
  return request({
    url: `/monitor/mysql/${instanceId}/metric`,
    method: 'get'
  })
}

/**
 * 获取 MySQL 历史监控数据
 */
export function getMysqlHistoryMetric(instanceId, params) {
  return request({
    url: `/monitor/mysql/${instanceId}/metric/history`,
    method: 'get',
    params
  })
}

/**
 * 新增 MySQL 实例
 */
export function addMysqlInstance(data) {
  return request({
    url: '/monitor/mysql',
    method: 'post',
    data
  })
}

/**
 * 修改 MySQL 实例
 */
export function updateMysqlInstance(data) {
  return request({
    url: '/monitor/mysql',
    method: 'put',
    data
  })
}

/**
 * 删除 MySQL 实例
 */
export function deleteMysqlInstance(instanceId) {
  return request({
    url: `/monitor/mysql/${instanceId}`,
    method: 'delete'
  })
}