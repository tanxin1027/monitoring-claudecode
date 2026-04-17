# 连续告警功能实现说明

## 功能概述

实现了监控指标连续超标告警功能，支持：
1. 配置连续超标次数阈值
2. 达到阈值后自动发送短信或微信通知
3. 告警记录按实例和指标分组展示
4. 点击展开查看详细告警记录

## 数据库变更

执行以下 SQL 脚本添加新字段：

```sql
-- 为告警规则表添加连续告警配置字段
ALTER TABLE alarm_rule
ADD COLUMN continuous_threshold INT DEFAULT 3 COMMENT '连续超标次数阈值，达到此次数后发送通知' AFTER threshold_value;

ALTER TABLE alarm_rule
ADD COLUMN notify_method TINYINT DEFAULT 2 COMMENT '通知方式 1-短信 2-微信' AFTER notify_type;

-- 为告警记录表添加连续告警相关字段
ALTER TABLE alarm_record
ADD COLUMN continuous_count INT DEFAULT 1 COMMENT '当前连续告警次数' AFTER severity;

ALTER TABLE alarm_record
ADD COLUMN group_key VARCHAR(100) DEFAULT NULL COMMENT '连续告警分组标识' AFTER continuous_count;

-- 添加索引优化分组查询
ALTER TABLE alarm_record
ADD INDEX idx_group_key_status (group_key, status);
```

## 后端变更

### 1. 实体类更新

**AlarmRule.java** - 新增字段：
- `continuousThreshold` - 连续超标次数阈值
- `notifyMethod` - 通知方式（1-短信，2-微信）

**AlarmRecord.java** - 新增字段：
- `continuousCount` - 当前连续告警次数
- `groupKey` - 连续告警分组标识

### 2. 新增服务类

**AlarmNotificationService.java** - 告警通知服务：
- `sendSmsNotification()` - 发送短信通知
- `sendWechatNotification()` - 发送微信通知

### 3. 核心逻辑变更

**AlarmCheckService.java** - 新增方法：
- `handleContinuousAlarm()` - 处理连续告警逻辑
- `sendNotification()` - 发送告警通知

连续告警逻辑：
1. 生成分组标识：`metricType_instanceId_metricKey_ruleId`
2. 查询该分组下最新的未处理告警记录
3. 累加连续告警次数
4. 达到阈值时发送通知

### 4. 新增 DTO

**AlarmGroupDTO.java** - 告警分组 DTO，包含：
- 分组标识、规则信息、实例信息
- 连续告警次数、通知状态
- 首次/最近告警时间
- 告警记录列表（用于展开详情）

### 5. API 接口

**AlarmController.java** - 新增接口：
- `GET /api/alarm/record/groups` - 分页查询告警分组列表
- `GET /api/alarm/record/group/{groupKey}` - 根据分组标识查询告警记录列表

## 前端变更

### 1. 告警规则管理（RuleList.vue）

新增字段：
- 连续次数配置（输入框，默认 3 次）
- 通知方式选择（单选：短信/微信）

### 2. 告警记录管理（RecordList.vue）

新增功能：
- 视图切换：列表视图 / 分组视图
- 分组视图展示：
  - 实例名称、告警规则、指标
  - 连续告警次数（带颜色标签）
  - 通知状态、等级、处理状态
  - 首次和最近告警时间
- 可展开行查看详细告警记录列表

### 3. API（alarm.js）

新增方法：
- `getAlarmGroups()` - 获取告警分组列表
- `getAlarmRecordsByGroupKey()` - 获取分组下的告警记录

## 使用说明

### 配置告警规则

1. 进入告警规则管理页面
2. 新增或编辑规则
3. 设置"连续次数"（如 3 次）
4. 选择"通知方式"（短信或微信）
5. 保存规则

### 查看告警记录

1. 进入告警记录管理页面
2. 切换到"分组视图"
3. 查看按实例和指标分组的告警
4. 点击展开图标查看详细告警记录

### 通知逻辑

- 每次指标超标时，连续次数 +1
- 达到设定的连续次数阈值时，发送通知
- 告警被处理后，连续次数重置

## 注意事项

1. **短信/微信接口需要集成**：
   - 短信服务：阿里云短信、腾讯云短信等
   - 微信服务：企业微信机器人、公众号模板消息
   
2. **管理员联系方式配置**：
   - 当前代码中硬编码了示例手机号和 OpenID
   - 实际项目中应从系统配置表或用户表获取

3. **告警清理**：
   - 建议定期清理已处理/已忽略的历史告警
   - 避免分组数据过多影响性能

## 测试建议

1. 创建测试告警规则，设置连续次数为 3
2. 模拟指标连续超标场景
3. 验证：
   - 连续次数是否正确累加
   - 达到阈值时是否发送通知
   - 分组视图是否正确展示
   - 展开详情是否显示所有记录
