-- =============================================================
-- 增量脚本：在「部门管理」下面新增「权限管理」菜单
-- 适用：已经执行过 crm_schema.sql 的库，直接跑本文件即可
-- =============================================================

-- 1. 菜单：权限管理（挂在「设置」下，排序 4，紧跟部门管理）
INSERT IGNORE INTO `sys_permission`
  (`id`, `parent_id`, `perm_name`, `perm_code`, `perm_type`, `path`, `component`, `icon`, `sort`)
VALUES
  (54, 5, '权限管理', 'system:permission:list', 2, '/system/permission', 'system/permission/index', 'Lock', 4);

-- 2. 按钮级权限
INSERT IGNORE INTO `sys_permission`
  (`id`, `parent_id`, `perm_name`, `perm_code`, `perm_type`, `sort`)
VALUES
  (541, 54, '新增权限', 'system:permission:add',    3, 1),
  (542, 54, '编辑权限', 'system:permission:edit',   3, 2),
  (543, 54, '删除权限', 'system:permission:delete', 3, 3);

-- 3. 超级管理员默认拥有新权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.`id`, p.`id`
FROM `sys_role` r,
     `sys_permission` p
WHERE r.`role_code` = 'ADMIN'
  AND p.`perm_code` LIKE 'system:permission:%';
