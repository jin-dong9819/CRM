-- =============================================================
-- CRM 业务模块数据库设计 v1.0（线索 / 客户 / 商机 / 订单）
-- MySQL 8.0+   字符集 utf8mb4   引擎 InnoDB
-- 依赖 crm_schema.sql（需先存在 crm 库）
--
-- 设计说明：
--   1. 状态类字段（status / level / stage / pay_status）直接存中文，
--      与前端 src/utils/dict.js 的字典值一一对应，省掉一层翻译。
--   2. customer_name / product_name 做冗余，列表页不必每次 join。
--   3. 全部采用逻辑删除（del_flag），与 sys_employee 保持一致。
-- =============================================================

USE `crm`;

-- 可重复执行：按依赖顺序清理（订单 -> 商机 -> 客户 -> 线索）
DROP TABLE IF EXISTS `crm_order`;
DROP TABLE IF EXISTS `crm_opportunity`;
DROP TABLE IF EXISTS `crm_customer`;
DROP TABLE IF EXISTS `crm_lead`;

-- -------------------------------------------------------------
-- 1. 线索表：销售最前端，转化后进入客户
--    编号形如 XS20260001（前缀 + 年份 + 4位流水）
-- -------------------------------------------------------------
CREATE TABLE `crm_lead` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '线索ID',
  `lead_no`          VARCHAR(32)     NOT NULL                COMMENT '线索编号，形如 XS20260001',
  `name`             VARCHAR(120)    NOT NULL                COMMENT '线索名称',
  `company`          VARCHAR(120)    DEFAULT NULL            COMMENT '公司名称',
  `contact`          VARCHAR(30)     DEFAULT NULL            COMMENT '联系人',
  `phone`            VARCHAR(20)     DEFAULT NULL            COMMENT '联系电话',
  `source`           VARCHAR(20)     NOT NULL DEFAULT '官网表单' COMMENT '线索来源',
  `level`            VARCHAR(10)     NOT NULL DEFAULT 'B'    COMMENT '意向等级：A/B/C',
  `owner`            VARCHAR(30)     DEFAULT NULL            COMMENT '负责人',
  `status`           VARCHAR(20)     NOT NULL DEFAULT '未跟进' COMMENT '状态：未跟进/跟进中/已转化/已失效',
  `next_follow_time` DATE            DEFAULT NULL            COMMENT '下次跟进日期',
  `remark`           VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  `del_flag`         TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记：0正常 1已删除',
  `create_by`        BIGINT UNSIGNED DEFAULT NULL            COMMENT '创建人ID',
  `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`        BIGINT UNSIGNED DEFAULT NULL            COMMENT '更新人ID',
  `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lead_no` (`lead_no`),
  KEY `idx_lead_status` (`status`),
  KEY `idx_lead_owner` (`owner`),
  KEY `idx_lead_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '线索表';

-- -------------------------------------------------------------
-- 2. 客户表：建档后的正式客户，商机与订单都挂在客户上
--    编号形如 KH20260001
-- -------------------------------------------------------------
CREATE TABLE `crm_customer` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `cust_no`     VARCHAR(32)     NOT NULL                COMMENT '客户编号，形如 KH20260001',
  `name`        VARCHAR(120)    NOT NULL                COMMENT '客户名称',
  `industry`    VARCHAR(20)     DEFAULT NULL            COMMENT '所属行业',
  `level`       VARCHAR(20)     NOT NULL DEFAULT '普通客户' COMMENT '客户等级：重要客户/普通客户/潜在客户',
  `contact`     VARCHAR(30)     DEFAULT NULL            COMMENT '联系人',
  `phone`       VARCHAR(20)     DEFAULT NULL            COMMENT '联系电话',
  `address`     VARCHAR(200)    DEFAULT NULL            COMMENT '所在地区',
  `owner`       VARCHAR(30)     DEFAULT NULL            COMMENT '负责人',
  `status`      VARCHAR(20)     NOT NULL DEFAULT '跟进中' COMMENT '合作状态：跟进中/合作中/已成交/已流失',
  `remark`      VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  `del_flag`    TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记：0正常 1已删除',
  `create_by`   BIGINT UNSIGNED DEFAULT NULL            COMMENT '创建人ID',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   BIGINT UNSIGNED DEFAULT NULL            COMMENT '更新人ID',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cust_no` (`cust_no`),
  KEY `idx_cust_name` (`name`),
  KEY `idx_cust_status` (`status`),
  KEY `idx_cust_owner` (`owner`),
  KEY `idx_cust_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '客户表';

-- -------------------------------------------------------------
-- 3. 商机表：跟进中的销售机会，靠 customer_id 关联客户
--    customer_name 冗余一份，列表页直接展示，不再 join
--    编号形如 SJ20260001
-- -------------------------------------------------------------
CREATE TABLE `crm_opportunity` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商机ID',
  `opp_no`        VARCHAR(32)     NOT NULL                COMMENT '商机编号，形如 SJ20260001',
  `name`          VARCHAR(150)    NOT NULL                COMMENT '商机名称',
  `customer_id`   BIGINT UNSIGNED DEFAULT NULL            COMMENT '关联客户ID',
  `customer_name` VARCHAR(120)    DEFAULT NULL            COMMENT '客户名称（冗余）',
  `amount`        DECIMAL(14, 2)  NOT NULL DEFAULT 0.00   COMMENT '商机金额',
  `stage`         VARCHAR(20)     NOT NULL DEFAULT '初步接洽' COMMENT '销售阶段：初步接洽/需求确认/方案报价/商务谈判/赢单/输单',
  `probability`   TINYINT         NOT NULL DEFAULT 10     COMMENT '赢率（%）',
  `expect_date`   DATE            DEFAULT NULL            COMMENT '预计成交日期',
  `owner`         VARCHAR(30)     DEFAULT NULL            COMMENT '负责人',
  `remark`        VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  `del_flag`      TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记：0正常 1已删除',
  `create_by`     BIGINT UNSIGNED DEFAULT NULL            COMMENT '创建人ID',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     BIGINT UNSIGNED DEFAULT NULL            COMMENT '更新人ID',
  `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_opp_no` (`opp_no`),
  KEY `idx_opp_customer` (`customer_id`),
  KEY `idx_opp_stage` (`stage`),
  KEY `idx_opp_owner` (`owner`),
  KEY `idx_opp_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商机表';

-- -------------------------------------------------------------
-- 4. 订单表：成交后的合同订单，含付款状态与交付状态两条线
--    编号形如 DD20260001
-- -------------------------------------------------------------
CREATE TABLE `crm_order` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no`      VARCHAR(32)     NOT NULL                COMMENT '订单编号，形如 DD20260001',
  `customer_id`   BIGINT UNSIGNED DEFAULT NULL            COMMENT '关联客户ID',
  `customer_name` VARCHAR(120)    DEFAULT NULL            COMMENT '客户名称（冗余）',
  `product_name`  VARCHAR(120)    NOT NULL                COMMENT '订购产品',
  `amount`        DECIMAL(14, 2)  NOT NULL DEFAULT 0.00   COMMENT '订单金额',
  `pay_status`    VARCHAR(20)     NOT NULL DEFAULT '未付款' COMMENT '付款状态：未付款/部分付款/已付款',
  `status`        VARCHAR(20)     NOT NULL DEFAULT '待审核' COMMENT '订单状态：待审核/已确认/已发货/已完成/已取消',
  `order_date`    DATE            DEFAULT NULL            COMMENT '下单日期',
  `owner`         VARCHAR(30)     DEFAULT NULL            COMMENT '负责人',
  `remark`        VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
  `del_flag`      TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记：0正常 1已删除',
  `create_by`     BIGINT UNSIGNED DEFAULT NULL            COMMENT '创建人ID',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     BIGINT UNSIGNED DEFAULT NULL            COMMENT '更新人ID',
  `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_customer` (`customer_id`),
  KEY `idx_order_status` (`status`),
  KEY `idx_order_pay_status` (`pay_status`),
  KEY `idx_order_owner` (`owner`),
  KEY `idx_order_date` (`order_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';

-- =============================================================
-- 演示数据：清空后重新插入，保证编号与主键对应关系可预期
-- 不需要的话，执行到这里就够了
-- =============================================================

-- 客户（商机 / 订单依赖它，先插）
INSERT INTO `crm_customer`
(`cust_no`, `name`, `industry`, `level`, `contact`, `phone`, `address`, `owner`, `status`, `create_time`) VALUES
('KH20260001', '顺达汽修厂',           '综合修理厂', '重要客户', '陈立',   '13910000001', '上海市闵行区七宝汽配城', '张伟', '合作中', DATE_SUB(NOW(), INTERVAL 80 DAY)),
('KH20260002', '车之家快修（连锁）',   '连锁快修',   '普通客户', '林小雅', '13910000002', '杭州市拱墅区汽车城',     '李娜', '已成交', DATE_SUB(NOW(), INTERVAL 76 DAY)),
('KH20260003', '途顺汽配商行',         '汽配批发',   '潜在客户', '黄鹏',   '13910000003', '深圳市宝安区汽配街',     '王强', '跟进中', DATE_SUB(NOW(), INTERVAL 72 DAY)),
('KH20260004', '宏运汽车维修中心',     '综合修理厂', '重要客户', '吴静',   '13910000004', '北京市朝阳区汽配城',     '刘敏', '合作中', DATE_SUB(NOW(), INTERVAL 68 DAY)),
('KH20260005', '华信一汽-大众 4S 店',  '4S 店',      '普通客户', '徐磊',   '13910000005', '上海市浦东新区金桥路',   '陈晨', '已成交', DATE_SUB(NOW(), INTERVAL 64 DAY)),
('KH20260006', '安达运输车队',         '物流车队',   '潜在客户', '马超',   '13910000006', '杭州市萧山区物流园',     '张伟', '跟进中', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('KH20260007', '亮洁汽车美容店',       '洗车美容',   '普通客户', '何雨',   '13910000007', '深圳市南山区科苑路',     '李娜', '跟进中', DATE_SUB(NOW(), INTERVAL 56 DAY)),
('KH20260008', '鑫源快修（3 家门店）', '连锁快修',   '重要客户', '高原',   '13910000008', '北京市丰台区看丹路',     '王强', '合作中', DATE_SUB(NOW(), INTERVAL 52 DAY)),
('KH20260009', '城市出租汽车公司',     '出租客运',   '普通客户', '沈悦',   '13910000009', '上海市宝山区淞滨路',     '刘敏', '已流失', DATE_SUB(NOW(), INTERVAL 48 DAY)),
('KH20260010', '途虎养车工场店（南山）','连锁门店',  '潜在客户', '曹阳',   '13910000010', '深圳市南山区南山大道',   '陈晨', '跟进中', DATE_SUB(NOW(), INTERVAL 44 DAY)),
('KH20260011', '车主 王先生',          '个人车主',   '普通客户', '罗敏',   '13910000011', '杭州市余杭区文一西路',   '张伟', '已成交', DATE_SUB(NOW(), INTERVAL 40 DAY)),
('KH20260012', '恒信汽车租赁',         '汽车租赁',   '重要客户', '谢峰',   '13910000012', '北京市大兴区亦庄',       '李娜', '合作中', DATE_SUB(NOW(), INTERVAL 36 DAY));

-- 线索
INSERT INTO `crm_lead`
(`lead_no`, `name`, `company`, `contact`, `phone`, `source`, `level`, `owner`, `status`, `next_follow_time`, `create_time`) VALUES
('XS20260001', '顺达汽修厂·玻璃水询价',       '顺达汽修厂',           '陈立',   '13910001001', '门店拜访',   'A', '张伟', '已转化', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 40 DAY)),
('XS20260002', '车之家快修·易损件补货',       '车之家快修（连锁）',   '林小雅', '13910001002', '老客户介绍', 'B', '李娜', '跟进中', DATE_ADD(CURDATE(), INTERVAL 2 DAY),  DATE_SUB(NOW(), INTERVAL 38 DAY)),
('XS20260003', '途顺汽配商行·养护品上架',     '途顺汽配商行',         '黄鹏',   '13910001003', '电话询价',   'C', '王强', '未跟进', DATE_ADD(CURDATE(), INTERVAL 3 DAY),  DATE_SUB(NOW(), INTERVAL 36 DAY)),
('XS20260004', '宏运汽修·保养套装试用',       '宏运汽车维修中心',     '吴静',   '13910001004', '微信咨询',   'A', '刘敏', '跟进中', DATE_ADD(CURDATE(), INTERVAL 1 DAY),  DATE_SUB(NOW(), INTERVAL 34 DAY)),
('XS20260005', '华信 4S 店·玻璃水询价',        '华信一汽-大众 4S 店',  '徐磊',   '13910001005', '门店拜访',   'B', '陈晨', '已转化', DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 32 DAY)),
('XS20260006', '安达车队·易损件补货',         '安达运输车队',         '马超',   '13910001006', '市场扫街',   'C', '张伟', '跟进中', DATE_ADD(CURDATE(), INTERVAL 5 DAY),  DATE_SUB(NOW(), INTERVAL 30 DAY)),
('XS20260007', '亮洁美容·养护品上架',         '亮洁汽车美容店',       '何雨',   '13910001007', '线上留资',   'B', '李娜', '未跟进', DATE_ADD(CURDATE(), INTERVAL 4 DAY),  DATE_SUB(NOW(), INTERVAL 28 DAY)),
('XS20260008', '鑫源快修·保养套装试用',       '鑫源快修（3 家门店）', '高原',   '13910001008', '老客户介绍', 'A', '王强', '跟进中', DATE_ADD(CURDATE(), INTERVAL 6 DAY),  DATE_SUB(NOW(), INTERVAL 26 DAY)),
('XS20260009', '城市出租·玻璃水询价',         '城市出租汽车公司',     '沈悦',   '13910001009', '电话询价',   'C', '刘敏', '已失效', DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY)),
('XS20260010', '途虎南山店·易损件补货',       '途虎养车工场店（南山）','曹阳',  '13910001010', '线上留资',   'B', '陈晨', '跟进中', DATE_ADD(CURDATE(), INTERVAL 7 DAY),  DATE_SUB(NOW(), INTERVAL 22 DAY)),
('XS20260011', '车主王先生·小件零售咨询',     '车主 王先生',          '罗敏',   '13910001011', '微信咨询',   'A', '张伟', '已转化', DATE_SUB(CURDATE(), INTERVAL 5 DAY),  DATE_SUB(NOW(), INTERVAL 20 DAY)),
('XS20260012', '恒信租赁·机油三滤框架',       '恒信汽车租赁',         '谢峰',   '13910001012', '门店拜访',   'B', '李娜', '未跟进', DATE_ADD(CURDATE(), INTERVAL 8 DAY),  DATE_SUB(NOW(), INTERVAL 18 DAY)),
('XS20260013', '金顺汽修·玻璃水询价',         '金顺汽修',             '陈立',   '13910001013', '市场扫街',   'C', '王强', '未跟进', DATE_ADD(CURDATE(), INTERVAL 9 DAY),  DATE_SUB(NOW(), INTERVAL 16 DAY)),
('XS20260014', '小拇指快修·养护品上架',       '小拇指快修（加盟店）', '林小雅', '13910001014', '电话询价',   'B', '陈晨', '跟进中', DATE_ADD(CURDATE(), INTERVAL 6 DAY),  DATE_SUB(NOW(), INTERVAL 14 DAY)),
('XS20260015', '车主李女士·刹车清洗剂咨询',   '车主 李女士',          '黄鹏',   '13910001015', '线上留资',   'C', '刘敏', '未跟进', DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY));

-- 商机（customer_id 与上面 KH20260001..12 顺序对应）
INSERT INTO `crm_opportunity`
(`opp_no`, `name`, `customer_id`, `customer_name`, `amount`, `stage`, `probability`, `expect_date`, `owner`, `create_time`) VALUES
('SJ20260001', '顺达汽修厂·易损件年度供货',       1,  '顺达汽修厂',            68000.00,  '初步接洽', 10,  DATE_ADD(CURDATE(), INTERVAL 20 DAY), '张伟', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('SJ20260002', '车之家快修·玻璃水季度采购',       2,  '车之家快修（连锁）',    12600.00,  '需求确认', 30,  DATE_ADD(CURDATE(), INTERVAL 15 DAY), '李娜', DATE_SUB(NOW(), INTERVAL 57 DAY)),
('SJ20260003', '途顺汽配商行·养护品上架试销',     3,  '途顺汽配商行',          6000.00,   '方案报价', 50,  DATE_ADD(CURDATE(), INTERVAL 12 DAY), '王强', DATE_SUB(NOW(), INTERVAL 54 DAY)),
('SJ20260004', '宏运汽修·机油三滤年度框架',       4,  '宏运汽车维修中心',      157800.00, '商务谈判', 70,  DATE_ADD(CURDATE(), INTERVAL 9 DAY),  '刘敏', DATE_SUB(NOW(), INTERVAL 51 DAY)),
('SJ20260005', '华信 4S 店·易损件年度供货',        5,  '华信一汽-大众 4S 店',   70400.00,  '赢单',     100, DATE_SUB(CURDATE(), INTERVAL 3 DAY),  '陈晨', DATE_SUB(NOW(), INTERVAL 48 DAY)),
('SJ20260006', '安达车队·玻璃水季度采购',         6,  '安达运输车队',          15000.00,  '输单',     0,   DATE_SUB(CURDATE(), INTERVAL 6 DAY),  '张伟', DATE_SUB(NOW(), INTERVAL 45 DAY)),
('SJ20260007', '亮洁美容·养护品上架试销',         7,  '亮洁汽车美容店',        8400.00,   '初步接洽', 10,  DATE_ADD(CURDATE(), INTERVAL 25 DAY), '李娜', DATE_SUB(NOW(), INTERVAL 42 DAY)),
('SJ20260008', '鑫源快修·机油三滤年度框架',       8,  '鑫源快修（3 家门店）',  160200.00, '需求确认', 30,  DATE_ADD(CURDATE(), INTERVAL 18 DAY), '王强', DATE_SUB(NOW(), INTERVAL 39 DAY)),
('SJ20260009', '城市出租·易损件年度供货',         9,  '城市出租汽车公司',      72800.00,  '方案报价', 50,  DATE_ADD(CURDATE(), INTERVAL 14 DAY), '刘敏', DATE_SUB(NOW(), INTERVAL 36 DAY)),
('SJ20260010', '途虎南山店·玻璃水季度采购',       10, '途虎养车工场店（南山）',17400.00,  '商务谈判', 70,  DATE_ADD(CURDATE(), INTERVAL 8 DAY),  '陈晨', DATE_SUB(NOW(), INTERVAL 33 DAY)),
('SJ20260011', '车主王先生·养护品上架试销',       11, '车主 王先生',           10800.00,  '需求确认', 30,  DATE_ADD(CURDATE(), INTERVAL 22 DAY), '张伟', DATE_SUB(NOW(), INTERVAL 30 DAY)),
('SJ20260012', '恒信租赁·机油三滤年度框架',       12, '恒信汽车租赁',          162600.00, '初步接洽', 10,  DATE_ADD(CURDATE(), INTERVAL 28 DAY), '李娜', DATE_SUB(NOW(), INTERVAL 27 DAY));

-- 订单
INSERT INTO `crm_order`
(`order_no`, `customer_id`, `customer_name`, `product_name`, `amount`, `pay_status`, `status`, `order_date`, `owner`, `create_time`) VALUES
('DD20260001', 1,  '顺达汽修厂',            '玻璃水 -25℃',        108.00,   '已付款',   '已完成', DATE_SUB(CURDATE(), INTERVAL 30 DAY), '张伟', DATE_SUB(NOW(), INTERVAL 30 DAY)),
('DD20260002', 2,  '车之家快修（连锁）',    '机油滤清器',          420.00,   '已付款',   '已完成', DATE_SUB(CURDATE(), INTERVAL 29 DAY), '李娜', DATE_SUB(NOW(), INTERVAL 29 DAY)),
('DD20260003', 3,  '途顺汽配商行',          '化油器清洗剂',        324.00,   '部分付款', '已发货', DATE_SUB(CURDATE(), INTERVAL 28 DAY), '王强', DATE_SUB(NOW(), INTERVAL 28 DAY)),
('DD20260004', 4,  '宏运汽车维修中心',      '防冻液 -35℃',         1872.00,  '未付款',   '待审核', DATE_SUB(CURDATE(), INTERVAL 27 DAY), '刘敏', DATE_SUB(NOW(), INTERVAL 27 DAY)),
('DD20260005', 5,  '华信一汽-大众 4S 店',   '半合成机油 10W-40',   948.00,   '已付款',   '已确认', DATE_SUB(CURDATE(), INTERVAL 26 DAY), '陈晨', DATE_SUB(NOW(), INTERVAL 26 DAY)),
('DD20260006', 6,  '安达运输车队',          '无骨雨刮片（对装）',  1056.00,  '部分付款', '已发货', DATE_SUB(CURDATE(), INTERVAL 25 DAY), '张伟', DATE_SUB(NOW(), INTERVAL 25 DAY)),
('DD20260007', 7,  '亮洁汽车美容店',        '三滤保养套装',        2304.00,  '未付款',   '待审核', DATE_SUB(CURDATE(), INTERVAL 24 DAY), '李娜', DATE_SUB(NOW(), INTERVAL 24 DAY)),
('DD20260008', 8,  '鑫源快修（3 家门店）',  '标准洗车',            600.00,   '已付款',   '已完成', DATE_SUB(CURDATE(), INTERVAL 23 DAY), '王强', DATE_SUB(NOW(), INTERVAL 23 DAY)),
('DD20260009', 9,  '城市出租汽车公司',      '玻璃水 -25℃',        108.00,   '未付款',   '已取消', DATE_SUB(CURDATE(), INTERVAL 22 DAY), '刘敏', DATE_SUB(NOW(), INTERVAL 22 DAY)),
('DD20260010', 10, '途虎养车工场店（南山）','机油滤清器',          420.00,   '已付款',   '已确认', DATE_SUB(CURDATE(), INTERVAL 21 DAY), '陈晨', DATE_SUB(NOW(), INTERVAL 21 DAY)),
('DD20260011', 11, '车主 王先生',           '化油器清洗剂',        36.00,    '部分付款', '已发货', DATE_SUB(CURDATE(), INTERVAL 20 DAY), '张伟', DATE_SUB(NOW(), INTERVAL 20 DAY)),
('DD20260012', 12, '恒信汽车租赁',          '防冻液 -35℃',         1872.00,  '未付款',   '待审核', DATE_SUB(CURDATE(), INTERVAL 19 DAY), '李娜', DATE_SUB(NOW(), INTERVAL 19 DAY)),
('DD20260013', 1,  '顺达汽修厂',            '三滤保养套装',        2304.00,  '已付款',   '已完成', DATE_SUB(CURDATE(), INTERVAL 18 DAY), '王强', DATE_SUB(NOW(), INTERVAL 18 DAY)),
('DD20260014', 2,  '车之家快修（连锁）',    '玻璃水 -25℃',        108.00,   '未付款',   '待审核', DATE_SUB(CURDATE(), INTERVAL 17 DAY), '刘敏', DATE_SUB(NOW(), INTERVAL 17 DAY));
