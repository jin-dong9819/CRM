-- =============================================================
-- CRM 系统数据库设计 v1.1（设置模块：员工 / 部门 / 角色 / 权限）
-- MySQL 8.0+   字符集 utf8mb4   引擎 InnoDB
-- 员工与角色：多对多（sys_employee_role）
-- =============================================================

CREATE DATABASE IF NOT EXISTS `crm`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `crm`;

-- 可重复执行：按依赖顺序清理
DROP TABLE IF EXISTS `sys_role_permission`;
DROP TABLE IF EXISTS `sys_employee_role`;
DROP TABLE IF EXISTS `sys_permission`;
DROP TABLE IF EXISTS `sys_employee`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_dept`;

-- -------------------------------------------------------------
-- 1. 部门表：多级树。parent_id 邻接表 + ancestors 祖先路径
--    ancestors 形如 '0,1,3'，表示 顶级 -> 1 -> 3 这条链路
--    新增：ancestors = 父级.ancestors + ',' + 父级.id
--    移动：自身改 ancestors，子孙 ancestors 用 REPLACE 前缀批量替换
-- -------------------------------------------------------------
CREATE TABLE `sys_dept` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `parent_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0      COMMENT '上级部门ID，0 表示顶级',
  `ancestors`   VARCHAR(500)    NOT NULL DEFAULT '0'    COMMENT '祖先ID链，逗号分隔',
  `dept_name`   VARCHAR(50)     NOT NULL                COMMENT '部门名称',
  `dept_code`   VARCHAR(50)     DEFAULT NULL            COMMENT '部门编码（可选）',
  `leader`      VARCHAR(30)     DEFAULT NULL            COMMENT '负责人姓名',
  `phone`       VARCHAR(20)     DEFAULT NULL            COMMENT '联系电话',
  `email`       VARCHAR(100)    DEFAULT NULL            COMMENT '部门邮箱',
  `sort`        INT             NOT NULL DEFAULT 0      COMMENT '同级排序，越小越靠前',
  `status`      TINYINT         NOT NULL DEFAULT 1      COMMENT '状态：1启用 0停用',
  `del_flag`    TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记：0正常 1已删除',
  `create_by`   BIGINT UNSIGNED DEFAULT NULL            COMMENT '创建人ID',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   BIGINT UNSIGNED DEFAULT NULL            COMMENT '更新人ID',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_dept_parent` (`parent_id`, `sort`),
  KEY `idx_dept_ancestors` (`ancestors`(120))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '部门表（多级组织架构）';

-- -------------------------------------------------------------
-- 2. 角色表：权限集合靠 sys_role_permission 关联，人员靠 sys_employee_role
--    data_scope 决定该角色能看到哪些数据（数据权限）
-- -------------------------------------------------------------
CREATE TABLE `sys_role` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code`   VARCHAR(50)     NOT NULL                COMMENT '角色编码，如 SALES_MANAGER',
  `role_name`   VARCHAR(50)     NOT NULL                COMMENT '角色名称',
  `data_scope`  TINYINT         NOT NULL DEFAULT 4      COMMENT '数据范围：1全部 2本部门及以下 3本部门 4仅本人 5自定义',
  `sort`        INT             NOT NULL DEFAULT 0      COMMENT '排序',
  `status`      TINYINT         NOT NULL DEFAULT 1      COMMENT '状态：1启用 0停用',
  `remark`      VARCHAR(255)    DEFAULT NULL            COMMENT '描述',
  `del_flag`    TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记：0正常 1已删除',
  `create_by`   BIGINT UNSIGNED DEFAULT NULL,
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT UNSIGNED DEFAULT NULL,
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  UNIQUE KEY `uk_role_name` (`role_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';

-- -------------------------------------------------------------
-- 3. 员工表：emp_no 即登录账号；部门决定组织归属，角色在关联表里
--    密码存 BCrypt 密文（$2a$/$2b$ 60 位），严禁明文/MD5
-- -------------------------------------------------------------
CREATE TABLE `sys_employee` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `emp_no`          VARCHAR(20)     NOT NULL                COMMENT '员工编号（登录账号）',
  `password`        VARCHAR(100)    NOT NULL                COMMENT '登录密码（BCrypt 密文）',
  `name`            VARCHAR(30)     NOT NULL                COMMENT '姓名',
  `gender`          TINYINT         NOT NULL DEFAULT 0      COMMENT '性别：0未知 1男 2女',
  `phone`           VARCHAR(20)     DEFAULT NULL            COMMENT '手机号',
  `email`           VARCHAR(100)    DEFAULT NULL            COMMENT '邮箱',
  `avatar`          VARCHAR(255)    DEFAULT NULL            COMMENT '头像地址',
  `dept_id`         BIGINT UNSIGNED NOT NULL                COMMENT '所属部门ID',
  `job_title`       VARCHAR(50)     DEFAULT NULL            COMMENT '职位',
  `hire_date`       DATE            DEFAULT NULL            COMMENT '入职日期',
  `status`          TINYINT         NOT NULL DEFAULT 1      COMMENT '账号状态：1启用 0停用',
  `remark`          VARCHAR(255)    DEFAULT NULL            COMMENT '备注',
  `last_login_time` DATETIME        DEFAULT NULL            COMMENT '最后登录时间',
  `last_login_ip`   VARCHAR(64)     DEFAULT NULL            COMMENT '最后登录IP',
  `pwd_update_time` DATETIME        DEFAULT NULL            COMMENT '密码最后修改时间',
  `del_flag`        TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记：0正常 1已删除',
  `create_by`       BIGINT UNSIGNED DEFAULT NULL,
  `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`       BIGINT UNSIGNED DEFAULT NULL,
  `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_no` (`emp_no`),
  UNIQUE KEY `uk_emp_phone` (`phone`),
  KEY `idx_emp_dept_status` (`dept_id`, `status`),
  KEY `idx_emp_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工表';

-- -------------------------------------------------------------
-- 4. 员工-角色关联表：一个员工可挂多个角色
--    权限 = 各角色权限并集；数据范围取最宽的一档（data_scope 数值最小）
-- -------------------------------------------------------------
CREATE TABLE `sys_employee_role` (
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `role_id`     BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  PRIMARY KEY (`employee_id`, `role_id`),
  KEY `idx_er_role` (`role_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工角色关联表';

-- -------------------------------------------------------------
-- 5. 权限表：目录 / 菜单 / 按钮 三类，树形结构
--    perm_code 是后端鉴权与前端 v-permission 的唯一标识
-- -------------------------------------------------------------
CREATE TABLE `sys_permission` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0      COMMENT '上级权限ID，0 表示顶级',
  `perm_name`   VARCHAR(50)     NOT NULL                COMMENT '权限名称',
  `perm_code`   VARCHAR(100)    NOT NULL                COMMENT '权限标识，如 system:employee:add',
  `perm_type`   TINYINT         NOT NULL DEFAULT 2      COMMENT '类型：1目录 2菜单 3按钮',
  `path`        VARCHAR(200)    DEFAULT NULL            COMMENT '路由地址',
  `component`   VARCHAR(200)    DEFAULT NULL            COMMENT '前端组件路径',
  `icon`        VARCHAR(50)     DEFAULT NULL            COMMENT '图标名',
  `sort`        INT             NOT NULL DEFAULT 0      COMMENT '排序',
  `visible`     TINYINT         NOT NULL DEFAULT 1      COMMENT '是否显示：1显示 0隐藏',
  `status`      TINYINT         NOT NULL DEFAULT 1      COMMENT '状态：1启用 0停用',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`),
  KEY `idx_perm_parent` (`parent_id`, `sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '权限表（目录/菜单/按钮）';

-- -------------------------------------------------------------
-- 6. 角色-权限关联表：多对多，联合主键天然去重
-- -------------------------------------------------------------
CREATE TABLE `sys_role_permission` (
  `role_id`       BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`, `permission_id`),
  KEY `idx_rp_permission` (`permission_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色权限关联表';

-- =============================================================
-- 初始化数据
-- =============================================================

-- 部门（三级示例：销售中心 -> 华东销售一部 -> 上海一组）
INSERT INTO `sys_dept` (`id`, `parent_id`, `ancestors`, `dept_name`, `dept_code`, `leader`, `phone`, `sort`) VALUES
  (1, 0, '0',     '销售中心',     'D100', '张伟', '13800000001', 1),
  (2, 1, '0,1',   '华东销售一部', 'D110', '李娜', '13800000002', 1),
  (3, 1, '0,1',   '华南销售部',   'D120', '王强', '13800000003', 2),
  (4, 0, '0',     '客户成功部',   'D200', '刘敏', '13800000004', 2),
  (5, 0, '0',     '产品技术部',   'D300', '陈晨', '13800000005', 3),
  (6, 2, '0,1,2', '上海一组',     'D111', '赵雪', '13800000006', 1);

-- 角色
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `data_scope`, `sort`, `remark`) VALUES
  (1, 'ADMIN',         '超级管理员', 1, 1, '拥有系统全部权限'),
  (2, 'SALES_MANAGER', '销售经理',   2, 2, '管理本部门及下级部门的客户与商机'),
  (3, 'SALES',         '销售专员',   4, 3, '仅能查看自己负责的数据'),
  (4, 'CS',            '客户成功',   3, 4, '负责本部门订单交付与售后');

-- 权限：一级菜单
INSERT INTO `sys_permission` (`id`, `parent_id`, `perm_name`, `perm_code`, `perm_type`, `path`, `component`, `icon`, `sort`) VALUES
  (1, 0, '线索管理', 'lead:list',        2, '/lead',        'lead/index',        'Aim',         1),
  (2, 0, '客户管理', 'customer:list',    2, '/customer',    'customer/index',    'User',        2),
  (3, 0, '商机管理', 'opportunity:list', 2, '/opportunity', 'opportunity/index', 'TrendCharts', 3),
  (4, 0, '订单管理', 'order:list',       2, '/order',       'order/index',       'Tickets',     4),
  (6, 0, '数据分析', 'analysis:view',    2, '/analysis',    'analysis/index',    'DataAnalysis', 5),
  (5, 0, '设置',     'system:view',      1, '/system',      'Layout',            'Setting',     6);

-- 权限：设置下的三个菜单
INSERT INTO `sys_permission` (`id`, `parent_id`, `perm_name`, `perm_code`, `perm_type`, `path`, `component`, `icon`, `sort`) VALUES
  (51, 5, '员工管理', 'system:employee:list', 2, '/system/employee', 'system/employee/index', 'Avatar',         1),
  (52, 5, '角色管理', 'system:role:list',     2, '/system/role',     'system/role/index',     'Key',            2),
  (53, 5, '部门管理', 'system:dept:list',     2, '/system/dept',     'system/dept/index',     'OfficeBuilding', 3),
  (54, 5, '权限管理', 'system:permission:list', 2, '/system/permission', 'system/permission/index', 'Lock', 4);

-- 权限：按钮级
INSERT INTO `sys_permission` (`id`, `parent_id`, `perm_name`, `perm_code`, `perm_type`, `sort`) VALUES
  (511, 51, '新增员工', 'system:employee:add',    3, 1),
  (512, 51, '编辑员工', 'system:employee:edit',   3, 2),
  (513, 51, '删除员工', 'system:employee:delete', 3, 3),
  (514, 51, '重置密码', 'system:employee:reset',  3, 4),
  (515, 51, '启用停用', 'system:employee:status', 3, 5),
  (521, 52, '新增角色', 'system:role:add',        3, 1),
  (522, 52, '编辑角色', 'system:role:edit',       3, 2),
  (523, 52, '删除角色', 'system:role:delete',     3, 3),
  (524, 52, '分配权限', 'system:role:assign',     3, 4),
  (531, 53, '新增部门', 'system:dept:add',        3, 1),
  (532, 53, '编辑部门', 'system:dept:edit',       3, 2),
  (533, 53, '删除部门', 'system:dept:delete',     3, 3),
  (541, 54, '新增权限', 'system:permission:add',    3, 1),
  (542, 54, '编辑权限', 'system:permission:edit',   3, 2),
  (543, 54, '删除权限', 'system:permission:delete', 3, 3);

-- 角色授权：管理员拥有全部；销售经理/专员拥有四个业务菜单；客户成功只有客户与订单
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
  SELECT 1, `id` FROM `sys_permission`;
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
  (2, 1), (2, 2), (2, 3), (2, 4), (2, 6),
  (3, 1), (3, 2), (3, 3), (3, 4), (3, 6),
  (4, 2), (4, 4), (4, 6);

-- 员工：演示密码统一 123456（BCrypt 密文）
INSERT INTO `sys_employee` (`id`, `emp_no`, `password`, `name`, `gender`, `phone`, `email`, `dept_id`, `job_title`, `hire_date`, `status`) VALUES
  (1, 'E1001', '$2b$10$0j1zLN3XnWG3za1CStWKNOky31B3YAzOkIuZ/.VAHJ7YNQexa3/o2', '张伟', 1, '13800000001', 'zhangwei@yipeitong.cn',  2, '销售总监',    '2021-03-01', 1),
  (2, 'E1002', '$2b$10$0j1zLN3XnWG3za1CStWKNOky31B3YAzOkIuZ/.VAHJ7YNQexa3/o2', '李娜', 2, '13800000002', 'lina@yipeitong.cn',      3, '区域销售经理（华东）', '2021-07-15', 1),
  (3, 'E1003', '$2b$10$0j1zLN3XnWG3za1CStWKNOky31B3YAzOkIuZ/.VAHJ7YNQexa3/o2', '王强', 1, '13800000003', 'wangqiang@yipeitong.cn', 4, '区域销售经理（华南）', '2022-01-10', 1),
  (4, 'E1004', '$2b$10$0j1zLN3XnWG3za1CStWKNOky31B3YAzOkIuZ/.VAHJ7YNQexa3/o2', '刘敏', 2, '13800000004', 'liumin@yipeitong.cn',    8, '门店服务主管', '2022-05-20', 1),
  (5, 'E1005', '$2b$10$0j1zLN3XnWG3za1CStWKNOky31B3YAzOkIuZ/.VAHJ7YNQexa3/o2', '陈晨', 1, '13800000005', 'chenchen@yipeitong.cn',  5, '渠道批发专员', '2023-02-01', 1),
  (6, 'E1006', '$2b$10$0j1zLN3XnWG3za1CStWKNOky31B3YAzOkIuZ/.VAHJ7YNQexa3/o2', '赵雪', 2, '13800000006', 'zhaoxue@yipeitong.cn',   6, '电商运营专员', '2023-09-11', 1);

-- 员工角色：E1003、E1006 故意配两个角色，用来验证多角色并集逻辑
INSERT INTO `sys_employee_role` (`employee_id`, `role_id`) VALUES
  (1, 1),
  (2, 2),
  (3, 2), (3, 4),
  (4, 4),
  (5, 3),
  (6, 3), (6, 4);

-- =============================================================
-- 校验脚本（按需执行）
-- =============================================================
-- 部门树（带完整路径）
-- SELECT d.id, d.dept_name, d.ancestors, CONCAT(d.ancestors, ',', d.id) AS path
-- FROM sys_dept d WHERE d.del_flag = 0 ORDER BY d.ancestors, d.sort;
--
-- 某部门及其所有下级
-- SELECT * FROM sys_dept WHERE id = 1 OR FIND_IN_SET(1, ancestors);
--
-- 员工及其角色
-- SELECT e.emp_no, e.name, GROUP_CONCAT(r.role_name ORDER BY r.id) AS roles
-- FROM sys_employee e
-- LEFT JOIN sys_employee_role er ON er.employee_id = e.id
-- LEFT JOIN sys_role r ON r.id = er.role_id
-- WHERE e.del_flag = 0 GROUP BY e.id;
--
-- 角色拥有的权限数量
-- SELECT r.role_name, COUNT(rp.permission_id) AS perm_count
-- FROM sys_role r LEFT JOIN sys_role_permission rp ON rp.role_id = r.id
-- GROUP BY r.id, r.role_name;
