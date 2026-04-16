-- ===========================================
-- 医院监控平台数据库设计
-- 数据库：hospital_monitor
-- ===========================================

CREATE DATABASE IF NOT EXISTS hospital_monitor DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE hospital_monitor;

-- ===========================================
-- 1. 系统管理模块
-- ===========================================

-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户 ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码 (加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='系统用户表';

-- 角色表
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色 ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB COMMENT='角色表';

-- 菜单权限表
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单 ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单 ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_type TINYINT NOT NULL COMMENT '类型 1-目录 2-菜单 3-按钮',
    path VARCHAR(255) COMMENT '路由路径',
    component VARCHAR(255) COMMENT '组件路径',
    perms VARCHAR(100) COMMENT '权限标识',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_menu_name (menu_name)
) ENGINE=InnoDB COMMENT='菜单权限表';

-- 用户角色关联表
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_menu_id (menu_id)
) ENGINE=InnoDB COMMENT='角色菜单关联表';

-- ===========================================
-- 2. 医院管理模块
-- ===========================================

-- 医院信息表
CREATE TABLE hospital_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '医院 ID',
    hospital_code VARCHAR(50) NOT NULL COMMENT '医院编码',
    hospital_name VARCHAR(100) NOT NULL COMMENT '医院名称',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    address VARCHAR(255) COMMENT '地址',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_hospital_code (hospital_code)
) ENGINE=InnoDB COMMENT='医院信息表';

-- 用户医院权限关联表（数据权限）
CREATE TABLE sys_user_hospital (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    hospital_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_hospital (user_id, hospital_id),
    KEY idx_hospital_id (hospital_id)
) ENGINE=InnoDB COMMENT='用户医院权限表';

-- ===========================================
-- 3. 监控服务器管理模块
-- ===========================================

-- 服务器信息表
CREATE TABLE server_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '服务器 ID',
    server_code VARCHAR(50) NOT NULL COMMENT '服务器编码',
    server_name VARCHAR(100) NOT NULL COMMENT '服务器名称',
    hospital_id BIGINT NOT NULL COMMENT '所属医院 ID',
    ip_address VARCHAR(50) NOT NULL COMMENT 'IP 地址',
    server_type TINYINT DEFAULT 1 COMMENT '类型 1-物理机 2-虚拟机 3-容器',
    os_type VARCHAR(50) COMMENT '操作系统类型',
    os_version VARCHAR(50) COMMENT '操作系统版本',
    cpu_cores INT COMMENT 'CPU 核心数',
    memory_total BIGINT COMMENT '总内存 (MB)',
    disk_total BIGINT COMMENT '总磁盘 (GB)',
    agent_version VARCHAR(20) COMMENT 'Agent 版本',
    last_heartbeat_time DATETIME COMMENT '最后心跳时间',
    status TINYINT DEFAULT 1 COMMENT '状态 0-离线 1-在线 2-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_server_code (server_code),
    KEY idx_hospital_id (hospital_id),
    KEY idx_status (status)
) ENGINE=InnoDB COMMENT='服务器信息表';

-- MySQL 实例信息表
CREATE TABLE mysql_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '实例 ID',
    instance_code VARCHAR(50) NOT NULL COMMENT '实例编码',
    instance_name VARCHAR(100) NOT NULL COMMENT '实例名称',
    server_id BIGINT NOT NULL COMMENT '所属服务器 ID',
    hospital_id BIGINT NOT NULL COMMENT '所属医院 ID',
    port INT DEFAULT 3306 COMMENT '端口号',
    db_version VARCHAR(50) COMMENT 'MySQL 版本',
    username VARCHAR(50) COMMENT '监控账号',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_instance_code (instance_code),
    KEY idx_server_id (server_id),
    KEY idx_hospital_id (hospital_id)
) ENGINE=InnoDB COMMENT='MySQL 实例表';

-- Tomcat 实例信息表
CREATE TABLE tomcat_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '实例 ID',
    instance_code VARCHAR(50) NOT NULL COMMENT '实例编码',
    instance_name VARCHAR(100) NOT NULL COMMENT '实例名称',
    server_id BIGINT NOT NULL COMMENT '所属服务器 ID',
    hospital_id BIGINT NOT NULL COMMENT '所属医院 ID',
    install_path VARCHAR(255) COMMENT '安装路径',
    http_port INT COMMENT 'HTTP 端口',
    shutdown_port INT COMMENT '关闭端口',
    jmx_port INT COMMENT 'JMX 端口',
    tomcat_version VARCHAR(50) COMMENT 'Tomcat 版本',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_instance_code (instance_code),
    KEY idx_server_id (server_id),
    KEY idx_hospital_id (hospital_id)
) ENGINE=InnoDB COMMENT='Tomcat 实例表';

-- ===========================================
-- 4. 监控数据采集模块
-- ===========================================

-- 服务器性能监控数据表（按时间分表）
CREATE TABLE server_metric_202604 (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    server_id BIGINT NOT NULL COMMENT '服务器 ID',
    cpu_usage DECIMAL(5,2) COMMENT 'CPU 使用率 (%)',
    cpu_load_1m DECIMAL(5,2) COMMENT '1 分钟负载',
    cpu_load_5m DECIMAL(5,2) COMMENT '5 分钟负载',
    cpu_load_15m DECIMAL(5,2) COMMENT '15 分钟负载',
    memory_used BIGINT COMMENT '已用内存 (MB)',
    memory_total BIGINT COMMENT '总内存 (MB)',
    memory_usage DECIMAL(5,2) COMMENT '内存使用率 (%)',
    disk_data TEXT COMMENT '磁盘数据 JSON [{path,used,total,usage}]',
    network_data TEXT COMMENT '网络数据 JSON [{iface,rx,tx}]',
    process_count INT COMMENT '进程数',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_server_time (server_id, collect_time),
    KEY idx_collect_time (collect_time)
) ENGINE=InnoDB COMMENT='服务器性能监控数据表 2026 年 4 月';

-- MySQL 监控数据表
CREATE TABLE mysql_metric_202604 (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    instance_id BIGINT NOT NULL COMMENT '实例 ID',
    connections_current INT COMMENT '当前连接数',
    connections_max INT COMMENT '最大连接数',
    connections_usage DECIMAL(5,2) COMMENT '连接使用率（%）',
    qps DECIMAL(10,2) COMMENT '每秒查询数',
    qps_total BIGINT COMMENT '查询总数',
    tps DECIMAL(10,2) COMMENT '每秒事务数',
    tps_total BIGINT COMMENT '事务总数',
    slow_queries_rate BIGINT COMMENT '慢查询速率（每秒）',
    slow_queries_total BIGINT COMMENT '慢查询总数',
    threads_running INT COMMENT '运行线程数',
    threads_connected INT COMMENT '已连接线程数',
    threads_created BIGINT COMMENT '已创建线程数',
    bytes_received BIGINT COMMENT '接收字节数',
    bytes_sent BIGINT COMMENT '发送字节数',
    buffer_pool_size BIGINT COMMENT '缓冲池大小（字节）',
    buffer_pool_free_pages BIGINT COMMENT '缓冲池空闲页数',
    buffer_pool_dirty_pages BIGINT COMMENT '缓冲池脏页数',
    buffer_pool_read_requests BIGINT COMMENT '缓冲池读请求数',
    buffer_pool_write_requests BIGINT COMMENT '缓冲池写入请求数',
    buffer_pool_reads BIGINT COMMENT '缓冲池物理读次数',
    buffer_pool_hit_rate DECIMAL(5,2) COMMENT '缓冲池命中率（%）',
    innodb_row_lock_time BIGINT COMMENT '行锁等待时间 (ms)',
    innodb_row_lock_waits BIGINT COMMENT '行锁等待次数',
    innodb_row_lock_current_waits BIGINT COMMENT '当前行锁等待数',
    table_locks_waited BIGINT COMMENT '表锁等待次数',
    table_locks_immediate BIGINT COMMENT '表锁立即获得次数',
    table_lock_wait_rate DECIMAL(5,2) COMMENT '表锁等待率（%）',
    tmp_tables_created BIGINT COMMENT '临时表创建总数',
    tmp_disk_tables_created BIGINT COMMENT '磁盘临时表创建数',
    tmp_disk_table_rate DECIMAL(5,2) COMMENT '磁盘临时表比率（%）',
    sort_rows BIGINT COMMENT '排序行数',
    sort_scan BIGINT COMMENT '全表扫描排序次数',
    sort_range BIGINT COMMENT '范围排序次数',
    sort_merge_passes BIGINT COMMENT '排序合并通过次数',
    slave_io_running TINYINT COMMENT '从库 IO 线程运行状态（1=运行，0=停止）',
    slave_sql_running TINYINT COMMENT '从库 SQL 线程运行状态（1=运行，0=停止）',
    seconds_behind_master INT COMMENT '落后主库的秒数',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_instance_time (instance_id, collect_time),
    KEY idx_collect_time (collect_time)
) ENGINE=InnoDB COMMENT='MySQL 监控数据表 2026 年 4 月';

-- Tomcat 监控数据表
CREATE TABLE tomcat_metric_202604 (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    instance_id BIGINT NOT NULL COMMENT '实例 ID',
    jvm_heap_used BIGINT COMMENT '堆内存已用 (MB)',
    jvm_heap_max BIGINT COMMENT '堆内存最大 (MB)',
    jvm_non_heap_used BIGINT COMMENT '非堆内存已用 (MB)',
    gc_count BIGINT COMMENT 'GC 次数',
    gc_time BIGINT COMMENT 'GC 总时间 (ms)',
    thread_count INT COMMENT '线程数',
    thread_busy INT COMMENT '繁忙线程数',
    request_count BIGINT COMMENT '请求数',
    error_count BIGINT COMMENT '错误数',
    bytes_sent BIGINT COMMENT '发送字节数',
    bytes_received BIGINT COMMENT '接收字节数',
    max_time BIGINT COMMENT '最大处理时间 (ms)',
    uptime BIGINT COMMENT '运行时间 (ms)',
    session_count INT COMMENT '活跃会话数',
    session_expired INT COMMENT '过期会话数',
    datasource_data TEXT COMMENT '数据源详情 JSON',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_instance_time (instance_id, collect_time),
    KEY idx_collect_time (collect_time)
) ENGINE=InnoDB COMMENT='Tomcat 监控数据表 2026 年 4 月';

-- ===========================================
-- 5. 告警模块
-- ===========================================

-- 告警规则表
CREATE TABLE alarm_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    metric_type TINYINT NOT NULL COMMENT '指标类型 1-服务器 2-MySQL 3-Tomcat',
    metric_key VARCHAR(50) NOT NULL COMMENT '指标键',
    operator VARCHAR(10) NOT NULL COMMENT '操作符 > < >= <= = between',
    threshold_value VARCHAR(50) COMMENT '阈值',
    severity TINYINT DEFAULT 2 COMMENT '严重程度 1-提示 2-警告 3-严重',
    notify_type VARCHAR(50) COMMENT '通知方式 email,sms,webhook',
    notify_target VARCHAR(255) COMMENT '通知目标',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='告警规则表';

-- 告警记录表
CREATE TABLE alarm_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT NOT NULL COMMENT '规则 ID',
    server_id BIGINT COMMENT '服务器 ID',
    instance_id BIGINT COMMENT '实例 ID',
    instance_type TINYINT COMMENT '实例类型',
    alarm_content TEXT COMMENT '告警内容',
    metric_value DECIMAL(20,4) COMMENT '指标值',
    severity TINYINT COMMENT '严重程度',
    status TINYINT DEFAULT 0 COMMENT '状态 0-未处理 1-已处理 2-已忽略',
    handle_user BIGINT COMMENT '处理人',
    handle_time DATETIME COMMENT '处理时间',
    handle_remark VARCHAR(500) COMMENT '处理备注',
    notify_status TINYINT DEFAULT 0 COMMENT '通知状态 0-未通知 1-已通知',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='告警记录表';

-- ===========================================
-- 6. 系统日志模块
-- ===========================================

-- 操作日志表
CREATE TABLE sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户 ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(100) COMMENT '操作',
    module VARCHAR(50) COMMENT '模块',
    method VARCHAR(200) COMMENT '方法',
    params VARCHAR(2000) COMMENT '参数',
    time_cost INT COMMENT '耗时 (ms)',
    ip VARCHAR(50) COMMENT 'IP 地址',
    status TINYINT DEFAULT 1 COMMENT '状态 0-失败 1-成功',
    error_msg VARCHAR(2000) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_time (user_id, create_time),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='操作日志表';

-- Agent Token 表
CREATE TABLE agent_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    token VARCHAR(100) NOT NULL COMMENT 'Token 值',
    server_id BIGINT COMMENT '关联服务器 ID',
    remark VARCHAR(255) COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_used_time DATETIME COMMENT '最后使用时间',
    UNIQUE KEY uk_token (token),
    KEY idx_server_id (server_id)
) ENGINE=InnoDB COMMENT='Agent Token 表';

-- Agent 上线下线日志
CREATE TABLE agent_heartbeat_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    server_id BIGINT NOT NULL COMMENT '服务器 ID',
    event_type TINYINT NOT NULL COMMENT '事件类型 1-上线 2-下线 3-心跳',
    agent_version VARCHAR(20) COMMENT 'Agent 版本',
    ip_address VARCHAR(50) COMMENT '上报 IP',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_server_time (server_id, create_time)
) ENGINE=InnoDB COMMENT='Agent 心跳日志表';

-- ===========================================
-- 初始化数据
-- ===========================================

-- 初始化管理员用户 (密码 admin123，使用 BCrypt 加密)
INSERT INTO sys_user (username, password, real_name, status) VALUES
('admin', '$2a$10$7fxE9.FqGKQlJ1hR1zN1p.vK9J1zN1p.vK9J1zN1p.vK9J1zN1p', '系统管理员', 1);

-- 初始化角色
INSERT INTO sys_role (role_code, role_name, description) VALUES
('SUPER_ADMIN', '超级管理员', '系统最高权限'),
('HOSPITAL_ADMIN', '医院管理员', '医院内部管理权限'),
('VIEWER', '普通查看', '仅查看权限');

-- 初始化菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, perms, icon, sort_order) VALUES
(0, '系统管理', 1, '/system', NULL, NULL, 'setting', 100),
(0, '监控管理', 1, '/monitor', NULL, NULL, 'dashboard', 1),
(0, '告警管理', 1, '/alarm', NULL, NULL, 'bell', 2),
(0, '报表统计', 1, '/report', NULL, NULL, 'chart', 3);

-- 插入子菜单（需要先获取父菜单 ID）
-- 系统管理子菜单 (parent_id = 1)
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, perms, icon, sort_order) VALUES
(1, '用户管理', 2, '/system/user', 'system/user/index', 'system:user:list', 'user', 1),
(1, '角色管理', 2, '/system/role', 'system/role/index', 'system:role:list', 'peoples', 2),
(1, '菜单管理', 2, '/system/menu', 'system/menu/index', 'system:menu:list', 'tree-table', 3),
(1, '医院管理', 2, '/system/hospital', 'system/hospital/index', 'system:hospital:list', 'hospital', 4);

-- 监控管理子菜单 (parent_id = 2)
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, perms, icon, sort_order) VALUES
(2, '服务器监控', 2, '/monitor/server', 'monitor/server/index', 'monitor:server:list', 'server', 1),
(2, 'MySQL 监控', 2, '/monitor/mysql', 'monitor/mysql/index', 'monitor:mysql:list', 'mysql', 2),
(2, 'Tomcat 监控', 2, '/monitor/tomcat', 'monitor/tomcat/index', 'monitor:tomcat:list', 'tomcat', 3);

-- 告警管理子菜单 (parent_id = 3)
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, perms, icon, sort_order) VALUES
(3, '告警规则', 2, '/alarm/rule', 'alarm/rule/index', 'alarm:rule:list', 'rule', 1),
(3, '告警记录', 2, '/alarm/record', 'alarm/record/index', 'alarm:record:list', 'record', 2);

-- 报表统计子菜单 (parent_id = 4)
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, perms, icon, sort_order) VALUES
(4, '性能报表', 2, '/report/performance', 'report/performance/index', 'report:performance:list', 'performance', 1),
(4, '历史趋势', 2, '/report/trend', 'report/trend/index', 'report:trend:list', 'trend', 2);

-- 初始化 Agent Token 示例
INSERT INTO agent_token (token, server_id, remark, status) VALUES
('agt_test_token_12345', NULL, '测试 Token', 1),
('agt_962d39baf20a490297ecff8d35295ac2', NULL, '默认 Agent Token', 1);

-- 初始化测试医院
INSERT INTO hospital_info (hospital_code, hospital_name, province, city, status) VALUES
('hospital_001', '测试医院', '北京', '北京市', 1);

-- 初始化测试服务器
INSERT INTO server_info (server_code, server_name, hospital_id, ip_address, server_type, status) VALUES
('server_001', '测试服务器', 1, '192.168.1.100', 2, 1);

-- 初始化 MySQL 实例（对应 Agent 配置中的 mysql_001）
INSERT INTO mysql_instance (instance_code, instance_name, server_id, hospital_id, port, status) VALUES
('mysql_001', '本地 MySQL 实例', 1, 1, 3306, 1);

-- 初始化 Tomcat 实例（示例）
INSERT INTO tomcat_instance (instance_code, instance_name, server_id, hospital_id, http_port, jmx_port, status) VALUES
('tomcat_001', '本地 Tomcat 实例', 1, 1, 8080, 9999, 0);
