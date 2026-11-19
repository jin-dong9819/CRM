-- =============================================================
-- 增量脚本：按前端新版两级菜单重建 sys_permission 菜单树
-- 适用：已执行过 crm_schema.sql 的库，直接跑本文件即可（可重复执行）
--
-- 结构（7 个一级目录 + 22 个二级菜单）：
--   工作台      经营概览 / 我的待办
--   客户管理    销售线索 / 客户公海 / 客户档案 / 联系人 / 跟进记录
--   产品管理    产品分类 / 产品档案 / 客户价格
--   销售管理    销售商机 / 报价单 / 销售订单 / 退换货单
--   交付与回款  发货记录 / 收款记录 / 应收账款
--   售后管理    售后工单 / 客户回访
--   数据分析    销售统计 / 客户采购分析 / 产品销售分析
--   设置        员工 / 角色 / 部门 / 权限
-- =============================================================

USE `crm`;

-- 1. 清理旧的一级业务菜单（保留「设置」及其按钮权限，id=5 不动）
DELETE FROM `sys_role_permission` WHERE `permission_id` IN (
  SELECT `id` FROM (
    SELECT `id` FROM `sys_permission`
    WHERE `perm_code` IN (
      'lead:list', 'customer:list', 'opportunity:list', 'order:list', 'analysis:view'
    )
  ) AS tmp
);

DELETE FROM `sys_permission`
WHERE `perm_code` IN (
  'lead:list', 'customer:list', 'opportunity:list', 'order:list', 'analysis:view'
);

-- 2. 一级目录（perm_type=1 目录）
INSERT IGNORE INTO `sys_permission`
  (`id`, `parent_id`, `perm_name`, `perm_code`, `perm_type`, `path`, `component`, `icon`, `sort`)
VALUES
  (10, 0, '工作台',     'dashboard:view',  1, '/dashboard', 'Layout', 'HomeFilled',  1),
  (11, 0, '客户管理',   'customer:view',   1, '/customer',  'Layout', 'User',        2),
  (12, 0, '产品管理',   'product:view',    1, '/product',   'Layout', 'Goods',       3),
  (13, 0, '销售管理',   'sales:view',      1, '/sales',     'Layout', 'TrendCharts', 4),
  (14, 0, '交付与回款', 'delivery:view',   1, '/delivery',  'Layout', 'Van',         5),
  (15, 0, '售后管理',   'aftersale:view',  1, '/aftersale', 'Layout', 'Service',     6),
  (16, 0, '数据分析',   'stats:view',      1, '/stats',     'Layout', 'DataAnalysis', 7);

UPDATE `sys_permission` SET `sort` = 8 WHERE `id` = 5;

-- 3. 二级菜单（perm_type=2 菜单）
INSERT IGNORE INTO `sys_permission`
  (`id`, `parent_id`, `perm_name`, `perm_code`, `perm_type`, `path`, `component`, `icon`, `sort`)
VALUES
  -- 工作台
  (101, 10, '经营概览', 'dashboard:overview', 2, '/dashboard/overview', 'workbench/overview', 'Odometer',    1),
  (102, 10, '我的待办', 'dashboard:todo',     2, '/dashboard/todo',     'workbench/todo',     'AlarmClock',  2),

  -- 客户管理
  (111, 11, '销售线索', 'customer:lead:list',    2, '/customer/lead',    'lead/index',        'Aim',       1),
  (112, 11, '客户公海', 'customer:sea:list',     2, '/customer/sea',     'customer/sea',      'Files',     2),
  (113, 11, '客户档案', 'customer:archive:list', 2, '/customer/archive', 'customer/index',    'Postcard',  3),
  (114, 11, '联系人',   'customer:contact:list', 2, '/customer/contact', 'business/index',    'User',      4),
  (115, 11, '跟进记录', 'customer:follow:list',  2, '/customer/follow',  'business/index',    'ChatDotRound', 5),

  -- 产品管理
  (121, 12, '产品分类', 'product:category:list', 2, '/product/category', 'business/index', 'Menu',  1),
  (122, 12, '产品档案', 'product:archive:list',  2, '/product/archive',  'business/index', 'Goods', 2),
  (123, 12, '客户价格', 'product:price:list',    2, '/product/price',    'business/index', 'PriceTag', 3),

  -- 销售管理
  (131, 13, '销售商机', 'sales:opportunity:list', 2, '/sales/opportunity', 'opportunity/index', 'Histogram', 1),
  (132, 13, '报价单',   'sales:quote:list',       2, '/sales/quote',       'business/index',    'Document',  2),
  (133, 13, '销售订单', 'sales:order:list',       2, '/sales/order',       'order/index',       'Tickets',   3),
  (134, 13, '退换货单', 'sales:return:list',      2, '/sales/return',      'business/index',    'RefreshLeft', 4),

  -- 交付与回款
  (141, 14, '发货记录', 'delivery:shipment:list',  2, '/delivery/shipment',   'business/index',      'Van',         1),
  (142, 14, '收款记录', 'delivery:receipt:list',   2, '/delivery/receipt',    'business/index',      'Wallet',      2),
  (143, 14, '应收账款', 'delivery:receivable:list', 2, '/delivery/receivable', 'delivery/receivable', 'Money',       3),

  -- 售后管理
  (151, 15, '售后工单', 'aftersale:ticket:list', 2, '/aftersale/ticket', 'business/index', 'Service',  1),
  (152, 15, '客户回访', 'aftersale:visit:list',  2, '/aftersale/visit',  'business/index', 'Phone',    2),

  -- 数据分析
  (161, 16, '销售统计',     'stats:sales:view',    2, '/stats/sales',    'analysis/index',   'PieChart',  1),
  (162, 16, '客户采购分析', 'stats:customer:view', 2, '/stats/customer', 'analysis/customer', 'User',     2),
  (163, 16, '产品销售分析', 'stats:product:view',  2, '/stats/product',  'analysis/product', 'Goods',     3);

-- 4. 全部角色默认拥有上述菜单的查看权限（可按需收窄）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.`id`, p.`id`
FROM `sys_role` r
JOIN `sys_permission` p
  ON p.`perm_type` = 2
 AND (p.`perm_code` LIKE 'dashboard:%' OR p.`perm_code` LIKE 'customer:%'
   OR p.`perm_code` LIKE 'product:%'   OR p.`perm_code` LIKE 'sales:%'
   OR p.`perm_code` LIKE 'delivery:%'  OR p.`perm_code` LIKE 'aftersale:%'
   OR p.`perm_code` LIKE 'stats:%');

-- 5. 一级目录也要授权，否则前端按权限过滤时看不到分组
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.`id`, p.`id`
FROM `sys_role` r
JOIN `sys_permission` p ON p.`perm_type` = 1;

-- 6. 校验
-- SELECT p1.`perm_name` AS 一级, p2.`perm_name` AS 二级, p2.`path`
-- FROM `sys_permission` p1
-- LEFT JOIN `sys_permission` p2 ON p2.`parent_id` = p1.`id`
-- WHERE p1.`perm_type` = 1 AND p1.`parent_id` = 0
-- ORDER BY p1.`sort`, p2.`sort`;
