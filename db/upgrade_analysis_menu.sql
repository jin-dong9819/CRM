-- =============================================================
-- 增量脚本：在「订单管理」后面新增「数据分析」菜单
-- 适用：已经执行过 crm_schema.sql 的库，直接跑本文件即可（可重复执行）
-- =============================================================

-- 1. 菜单：数据分析（一级菜单，path /analysis，排在订单之后）
INSERT IGNORE INTO `sys_permission`
  (`id`, `parent_id`, `perm_name`, `perm_code`, `perm_type`, `path`, `component`, `icon`, `sort`)
VALUES
  (6, 0, '数据分析', 'analysis:view', 2, '/analysis', 'analysis/index', 'DataAnalysis', 5);

-- 2. 「设置」后移一位，保证数据分析排在订单与设置之间
UPDATE `sys_permission` SET `sort` = 6 WHERE `perm_code` = 'system:view' AND `sort` < 6;

-- 3. 全部角色都能查看看板
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.`id`, p.`id`
FROM `sys_role` r,
     `sys_permission` p
WHERE p.`perm_code` = 'analysis:view';
