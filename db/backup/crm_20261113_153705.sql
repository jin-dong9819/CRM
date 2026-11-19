-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: crm
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'寮犱笁',100.00),(2,'鏉庡洓',0.00);
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crm_customer`
--

DROP TABLE IF EXISTS `crm_customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crm_customer` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `cust_no` varchar(32) NOT NULL COMMENT '客户编号，形如 KH20260001',
  `name` varchar(120) NOT NULL COMMENT '客户名称',
  `industry` varchar(20) DEFAULT NULL COMMENT '所属行业',
  `level` varchar(20) NOT NULL DEFAULT '普通客户' COMMENT '客户等级：重要客户/普通客户/潜在客户',
  `contact` varchar(30) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(200) DEFAULT NULL COMMENT '所在地区',
  `owner` varchar(30) DEFAULT NULL COMMENT '负责人',
  `status` varchar(20) NOT NULL DEFAULT '跟进中' COMMENT '合作状态：跟进中/合作中/已成交/已流失',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `create_by` bigint unsigned DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cust_no` (`cust_no`),
  KEY `idx_cust_name` (`name`),
  KEY `idx_cust_status` (`status`),
  KEY `idx_cust_owner` (`owner`),
  KEY `idx_cust_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crm_customer`
--

LOCK TABLES `crm_customer` WRITE;
/*!40000 ALTER TABLE `crm_customer` DISABLE KEYS */;
INSERT INTO `crm_customer` VALUES (1,'KH20260001','星辰科技','互联网','重要客户','陈立','13910000001','上海市浦东新区','张伟','合作中',NULL,0,NULL,'2026-08-24 09:33:30',NULL,'2026-11-12 09:33:30'),(2,'KH20260002','云图信息','互联网','普通客户','林小雅','13910000002','杭州市西湖区','李娜','已成交',NULL,0,NULL,'2026-08-28 09:33:30',NULL,'2026-11-12 09:33:30'),(3,'KH20260003','海联电子','制造业','潜在客户','黄鹏','13910000003','深圳市南山区','王强','跟进中',NULL,0,NULL,'2026-09-01 09:33:30',NULL,'2026-11-12 09:33:30'),(4,'KH20260004','中启智能','制造业','重要客户','吴静','13910000004','北京市海淀区','刘敏','合作中',NULL,0,NULL,'2026-09-05 09:33:30',NULL,'2026-11-12 09:33:30'),(5,'KH20260005','博远软件','互联网','普通客户','徐磊','13910000005','上海市浦东新区','陈晨','已成交',NULL,0,NULL,'2026-09-09 09:33:30',NULL,'2026-11-12 09:33:30'),(6,'KH20260006','天域网络','互联网','潜在客户','马超','13910000006','杭州市西湖区','张伟','跟进中',NULL,0,NULL,'2026-09-13 09:33:30',NULL,'2026-11-12 09:33:30'),(7,'KH20260007','鼎新实业','制造业','普通客户','何雨','13910000007','深圳市南山区','李娜','跟进中',NULL,0,NULL,'2026-09-17 09:33:30',NULL,'2026-11-12 09:33:30'),(8,'KH20260008','恒通物流','物流运输','重要客户','高原','13910000008','北京市海淀区','王强','合作中',NULL,0,NULL,'2026-09-21 09:33:30',NULL,'2026-11-12 09:33:30'),(9,'KH20260009','美佳零售','零售电商','普通客户','沈悦','13910000009','上海市浦东新区','刘敏','已流失',NULL,0,NULL,'2026-09-25 09:33:30',NULL,'2026-11-12 09:33:30'),(10,'KH20260010','睿达医疗','医疗健康','潜在客户','曹阳','13910000010','杭州市西湖区','陈晨','跟进中',NULL,0,NULL,'2026-09-29 09:33:30',NULL,'2026-11-12 09:33:30'),(11,'KH20260011','联创制造','制造业','普通客户','罗敏','13910000011','深圳市南山区','张伟','已成交',NULL,0,NULL,'2026-10-03 09:33:30',NULL,'2026-11-12 09:33:30'),(12,'KH20260012','光和能源','制造业','重要客户','谢峰','13910000012','北京市海淀区','李娜','合作中',NULL,0,NULL,'2026-10-07 09:33:30',NULL,'2026-11-12 09:33:30');
/*!40000 ALTER TABLE `crm_customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crm_lead`
--

DROP TABLE IF EXISTS `crm_lead`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crm_lead` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '线索ID',
  `lead_no` varchar(32) NOT NULL COMMENT '线索编号，形如 XS20260001',
  `name` varchar(120) NOT NULL COMMENT '线索名称',
  `company` varchar(120) DEFAULT NULL COMMENT '公司名称',
  `contact` varchar(30) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `source` varchar(20) NOT NULL DEFAULT '官网表单' COMMENT '线索来源',
  `level` varchar(10) NOT NULL DEFAULT 'B' COMMENT '意向等级：A/B/C',
  `owner` varchar(30) DEFAULT NULL COMMENT '负责人',
  `status` varchar(20) NOT NULL DEFAULT '未跟进' COMMENT '状态：未跟进/跟进中/已转化/已失效',
  `next_follow_time` date DEFAULT NULL COMMENT '下次跟进日期',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `create_by` bigint unsigned DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lead_no` (`lead_no`),
  KEY `idx_lead_status` (`status`),
  KEY `idx_lead_owner` (`owner`),
  KEY `idx_lead_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='线索表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crm_lead`
--

LOCK TABLES `crm_lead` WRITE;
/*!40000 ALTER TABLE `crm_lead` DISABLE KEYS */;
INSERT INTO `crm_lead` VALUES (1,'XS20260001','星辰科技·采购线索','星辰科技','陈立','13910001001','官网表单','A','张伟','已转化','2026-10-13',NULL,0,NULL,'2026-10-03 09:33:30',NULL,'2026-11-12 09:33:30'),(2,'XS20260002','云图信息·合作线索','云图信息','林小雅','13910001002','电话咨询','B','李娜','跟进中','2026-11-14',NULL,0,NULL,'2026-10-05 09:33:30',NULL,'2026-11-12 09:33:30'),(3,'XS20260003','海联电子·选型线索','海联电子','黄鹏','13910001003','展会活动','C','王强','未跟进','2026-11-15',NULL,0,NULL,'2026-10-07 09:33:30',NULL,'2026-11-12 09:33:30'),(4,'XS20260004','中启智能·试用线索','中启智能','吴静','13910001004','老客户介绍','A','刘敏','跟进中','2026-11-13',NULL,0,NULL,'2026-10-09 09:33:30',NULL,'2026-11-12 09:33:30'),(5,'XS20260005','博远软件·采购线索','博远软件','徐磊','13910001005','广告投放','B','陈晨','已转化','2026-11-02',NULL,0,NULL,'2026-10-11 09:33:30',NULL,'2026-11-12 09:33:30'),(6,'XS20260006','天域网络·合作线索','天域网络','马超','13910001006','陌生拜访','C','张伟','跟进中','2026-11-17',NULL,0,NULL,'2026-10-13 09:33:30',NULL,'2026-11-12 09:33:30'),(7,'XS20260007','鼎新实业·选型线索','鼎新实业','何雨','13910001007','官网表单','B','李娜','未跟进','2026-11-16',NULL,0,NULL,'2026-10-15 09:33:30',NULL,'2026-11-12 09:33:30'),(8,'XS20260008','恒通物流·试用线索','恒通物流','高原','13910001008','电话咨询','A','王强','跟进中','2026-11-18',NULL,0,NULL,'2026-10-17 09:33:30',NULL,'2026-11-12 09:33:30'),(9,'XS20260009','美佳零售·采购线索','美佳零售','沈悦','13910001009','展会活动','C','刘敏','已失效','2026-10-23',NULL,0,NULL,'2026-10-19 09:33:30',NULL,'2026-11-12 09:33:30'),(10,'XS20260010','睿达医疗·合作线索','睿达医疗','曹阳','13910001010','老客户介绍','B','陈晨','跟进中','2026-11-19',NULL,0,NULL,'2026-10-21 09:33:30',NULL,'2026-11-12 09:33:30'),(11,'XS20260011','联创制造·选型线索','联创制造','罗敏','13910001011','广告投放','A','张伟','已转化','2026-11-07',NULL,0,NULL,'2026-10-23 09:33:30',NULL,'2026-11-12 09:33:30'),(12,'XS20260012','光和能源·试用线索','光和能源','谢峰','13910001012','陌生拜访','B','李娜','未跟进','2026-11-20',NULL,0,NULL,'2026-10-25 09:33:30',NULL,'2026-11-12 09:33:30');
/*!40000 ALTER TABLE `crm_lead` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crm_opportunity`
--

DROP TABLE IF EXISTS `crm_opportunity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crm_opportunity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '商机ID',
  `opp_no` varchar(32) NOT NULL COMMENT '商机编号，形如 SJ20260001',
  `name` varchar(150) NOT NULL COMMENT '商机名称',
  `customer_id` bigint unsigned DEFAULT NULL COMMENT '关联客户ID',
  `customer_name` varchar(120) DEFAULT NULL COMMENT '客户名称（冗余）',
  `amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '商机金额',
  `stage` varchar(20) NOT NULL DEFAULT '初步接洽' COMMENT '销售阶段：初步接洽/需求确认/方案报价/商务谈判/赢单/输单',
  `probability` tinyint NOT NULL DEFAULT '10' COMMENT '赢率（%）',
  `expect_date` date DEFAULT NULL COMMENT '预计成交日期',
  `owner` varchar(30) DEFAULT NULL COMMENT '负责人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `create_by` bigint unsigned DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_opp_no` (`opp_no`),
  KEY `idx_opp_customer` (`customer_id`),
  KEY `idx_opp_stage` (`stage`),
  KEY `idx_opp_owner` (`owner`),
  KEY `idx_opp_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商机表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crm_opportunity`
--

LOCK TABLES `crm_opportunity` WRITE;
/*!40000 ALTER TABLE `crm_opportunity` DISABLE KEYS */;
INSERT INTO `crm_opportunity` VALUES (1,'SJ20260001','星辰科技·CRM 系统项目',1,'星辰科技',88000.00,'初步接洽',10,'2026-12-02','张伟',NULL,0,NULL,'2026-09-13 09:33:30',NULL,'2026-11-12 09:33:30'),(2,'SJ20260002','云图信息·数据中台项目',2,'云图信息',156000.00,'需求确认',30,'2026-11-27','李娜',NULL,0,NULL,'2026-09-16 09:33:30',NULL,'2026-11-12 09:33:30'),(3,'SJ20260003','海联电子·智能客服项目',3,'海联电子',224000.00,'方案报价',50,'2026-11-24','王强',NULL,0,NULL,'2026-09-19 09:33:30',NULL,'2026-11-12 09:33:30'),(4,'SJ20260004','中启智能·年度运维项目',4,'中启智能',292000.00,'商务谈判',70,'2026-11-21','刘敏',NULL,0,NULL,'2026-09-22 09:33:30',NULL,'2026-11-12 09:33:30'),(5,'SJ20260005','博远软件·CRM 系统项目',5,'博远软件',360000.00,'赢单',100,'2026-11-09','陈晨',NULL,0,NULL,'2026-09-25 09:33:30',NULL,'2026-11-12 09:33:30'),(6,'SJ20260006','天域网络·数据中台项目',6,'天域网络',428000.00,'输单',0,'2026-11-06','张伟',NULL,0,NULL,'2026-09-28 09:33:30',NULL,'2026-11-12 09:33:30'),(7,'SJ20260007','鼎新实业·智能客服项目',7,'鼎新实业',496000.00,'初步接洽',10,'2026-12-07','李娜',NULL,0,NULL,'2026-10-01 09:33:30',NULL,'2026-11-12 09:33:30'),(8,'SJ20260008','恒通物流·年度运维项目',8,'恒通物流',564000.00,'需求确认',30,'2026-11-30','王强',NULL,0,NULL,'2026-10-04 09:33:30',NULL,'2026-11-12 09:33:30'),(9,'SJ20260009','美佳零售·CRM 系统项目',9,'美佳零售',632000.00,'方案报价',50,'2026-11-26','刘敏',NULL,0,NULL,'2026-10-07 09:33:30',NULL,'2026-11-12 09:33:30'),(10,'SJ20260010','睿达医疗·数据中台项目',10,'睿达医疗',700000.00,'商务谈判',70,'2026-11-20','陈晨',NULL,0,NULL,'2026-10-10 09:33:30',NULL,'2026-11-12 09:33:30'),(11,'SJ20260011','联创制造·智能客服项目',11,'联创制造',768000.00,'需求确认',30,'2026-12-04','张伟',NULL,0,NULL,'2026-10-13 09:33:30',NULL,'2026-11-12 09:33:30'),(12,'SJ20260012','光和能源·年度运维项目',12,'光和能源',836000.00,'初步接洽',10,'2026-12-10','李娜',NULL,0,NULL,'2026-10-16 09:33:30',NULL,'2026-11-12 09:33:30');
/*!40000 ALTER TABLE `crm_opportunity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crm_order`
--

DROP TABLE IF EXISTS `crm_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crm_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单编号，形如 DD20260001',
  `customer_id` bigint unsigned DEFAULT NULL COMMENT '关联客户ID',
  `customer_name` varchar(120) DEFAULT NULL COMMENT '客户名称（冗余）',
  `product_name` varchar(120) NOT NULL COMMENT '订购产品',
  `amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
  `pay_status` varchar(20) NOT NULL DEFAULT '未付款' COMMENT '付款状态：未付款/部分付款/已付款',
  `status` varchar(20) NOT NULL DEFAULT '待审核' COMMENT '订单状态：待审核/已确认/已发货/已完成/已取消',
  `order_date` date DEFAULT NULL COMMENT '下单日期',
  `owner` varchar(30) DEFAULT NULL COMMENT '负责人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `create_by` bigint unsigned DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_customer` (`customer_id`),
  KEY `idx_order_status` (`status`),
  KEY `idx_order_pay_status` (`pay_status`),
  KEY `idx_order_owner` (`owner`),
  KEY `idx_order_date` (`order_date`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crm_order`
--

LOCK TABLES `crm_order` WRITE;
/*!40000 ALTER TABLE `crm_order` DISABLE KEYS */;
INSERT INTO `crm_order` VALUES (1,'DD20260001',1,'星辰科技','CRM 企业版（年）',45000.00,'已付款','已完成','2026-10-13','张伟',NULL,0,NULL,'2026-10-13 09:33:30',NULL,'2026-11-12 09:33:30'),(2,'DD20260002',2,'云图信息','CRM 专业版（年）',81000.00,'已付款','已完成','2026-10-14','李娜',NULL,0,NULL,'2026-10-14 09:33:30',NULL,'2026-11-12 09:33:30'),(3,'DD20260003',3,'海联电子','数据中台实施服务',117000.00,'部分付款','已发货','2026-10-15','王强',NULL,0,NULL,'2026-10-15 09:33:30',NULL,'2026-11-12 09:33:30'),(4,'DD20260004',4,'中启智能','定制开发服务',153000.00,'未付款','待审核','2026-10-16','刘敏',NULL,0,NULL,'2026-10-16 09:33:30',NULL,'2026-11-12 09:33:30'),(5,'DD20260005',5,'博远软件','运维支持服务',189000.00,'已付款','已确认','2026-10-17','陈晨',NULL,0,NULL,'2026-10-17 09:33:30',NULL,'2026-11-12 09:33:30'),(6,'DD20260006',6,'天域网络','CRM 企业版（年）',225000.00,'部分付款','已发货','2026-10-18','张伟',NULL,0,NULL,'2026-10-18 09:33:30',NULL,'2026-11-12 09:33:30'),(7,'DD20260007',7,'鼎新实业','CRM 专业版（年）',261000.00,'未付款','待审核','2026-10-19','李娜',NULL,0,NULL,'2026-10-19 09:33:30',NULL,'2026-11-12 09:33:30'),(8,'DD20260008',8,'恒通物流','数据中台实施服务',297000.00,'已付款','已完成','2026-10-20','王强',NULL,0,NULL,'2026-10-20 09:33:30',NULL,'2026-11-12 09:33:30'),(9,'DD20260009',9,'美佳零售','定制开发服务',333000.00,'未付款','已取消','2026-10-21','刘敏',NULL,0,NULL,'2026-10-21 09:33:30',NULL,'2026-11-12 09:33:30'),(10,'DD20260010',10,'睿达医疗','运维支持服务',369000.00,'已付款','已确认','2026-10-22','陈晨',NULL,0,NULL,'2026-10-22 09:33:30',NULL,'2026-11-12 09:33:30'),(11,'DD20260011',11,'联创制造','CRM 企业版（年）',405000.00,'部分付款','已发货','2026-10-23','张伟',NULL,0,NULL,'2026-10-23 09:33:30',NULL,'2026-11-12 09:33:30'),(12,'DD20260012',12,'光和能源','CRM 专业版（年）',441000.00,'未付款','待审核','2026-10-24','李娜',NULL,0,NULL,'2026-10-24 09:33:30',NULL,'2026-11-12 09:33:30'),(13,'DD20260013',1,'星辰科技','数据中台实施服务',477000.00,'已付款','已完成','2026-10-25','王强',NULL,0,NULL,'2026-10-25 09:33:30',NULL,'2026-11-12 09:33:30'),(14,'DD20260014',2,'云图信息','定制开发服务',513000.00,'未付款','待审核','2026-10-26','刘敏',NULL,0,NULL,'2026-10-26 09:33:30',NULL,'2026-11-12 09:33:30');
/*!40000 ALTER TABLE `crm_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crm_product`
--

DROP TABLE IF EXISTS `crm_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crm_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `product_code` varchar(32) NOT NULL COMMENT '产品编码',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `model` varchar(64) DEFAULT NULL COMMENT '型号',
  `spec` varchar(255) DEFAULT NULL COMMENT '规格参数',
  `unit` varchar(16) NOT NULL DEFAULT '台' COMMENT '计量单位',
  `price` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '标准售价（元）',
  `cost_price` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '成本价（元）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1在售 0停售',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品目录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crm_product`
--

LOCK TABLES `crm_product` WRITE;
/*!40000 ALTER TABLE `crm_product` DISABLE KEYS */;
INSERT INTO `crm_product` VALUES (1,'P1001','数控车床',2,'CK6140','最大回转直径 400mm，加工长度 750mm，配广数 980 系统','台',185000.00,128000.00,1,'入门走量机型','2026-11-10 19:09:14','2026-11-10 19:09:14'),(2,'P1002','数控车床',2,'CK6150','最大回转直径 500mm，加工长度 1000mm，液压卡盘','台',235000.00,168000.00,1,NULL,'2026-11-10 19:09:14','2026-11-10 19:09:14'),(3,'P1003','斜床身数控车床',2,'CK7520','45° 斜床身，8 工位刀塔，线轨，带排屑机','台',398000.00,285000.00,1,'高精度车削','2026-11-10 19:09:14','2026-11-10 19:09:14'),(4,'P1004','立式加工中心',3,'VMC850','行程 800×500×550mm，BT40 主轴 8000rpm，24 把刀库','台',328000.00,235000.00,1,'销量主力','2026-11-10 19:09:14','2026-11-10 19:09:14'),(5,'P1005','立式加工中心',3,'VMC1160','行程 1100×600×600mm，BT40 主轴 10000rpm','台',428000.00,312000.00,1,NULL,'2026-11-10 19:09:14','2026-11-10 19:09:14'),(6,'P1006','卧式加工中心',3,'HMC630','双交换工作台 630×630mm，BT50 主轴，40 把刀库','台',780000.00,560000.00,1,'交期 60 天','2026-11-10 19:09:14','2026-11-10 19:09:14'),(7,'P1007','光纤激光切割机',5,'LC3015-1500','1500W，加工幅面 3000×1500mm，柏楚系统','台',468000.00,335000.00,1,NULL,'2026-11-10 19:09:14','2026-11-10 19:09:14'),(8,'P1008','光纤激光切割机',5,'LC6015-3000','3000W，加工幅面 6000×1500mm，自动交换台','台',720000.00,520000.00,1,'含除尘','2026-11-10 19:09:14','2026-11-10 19:09:14'),(9,'P1009','手持光纤激光焊接机',6,'LW2000','2000W，焊枪 4 把，送丝机构','台',128000.00,86000.00,1,NULL,'2026-11-10 19:09:14','2026-11-10 19:09:14'),(10,'P1010','桁架机械手上下料单元',7,'AP-100','两轴桁架，负载 30kg，适配 2 台数控设备','套',268000.00,195000.00,1,'按现场定制','2026-11-10 19:09:14','2026-11-10 19:09:14'),(11,'P1011','机器人焊接工作站',7,'AP-200','6 轴机器人 + 双工位变位机 + 焊接电源','套',458000.00,330000.00,1,NULL,'2026-11-10 19:09:14','2026-11-10 19:09:14'),(12,'P1012','柔性加工生产线',7,'AP-300','4 台加工中心 + AGV + 中央刀库 + MES 对接','套',1280000.00,920000.00,1,'项目制，需现场勘察','2026-11-10 19:09:14','2026-11-10 19:09:14'),(13,'P1013','BT40 刀柄组套',8,'BT40-SET','含刀柄 10 支、拉钉、扳手','套',3800.00,2100.00,1,NULL,'2026-11-10 19:09:14','2026-11-10 19:09:14'),(14,'P1014','激光聚焦镜片',8,'D30-F100','石英镜片，直径 30mm，焦距 100mm','件',1500.00,780.00,1,'易损件','2026-11-10 19:09:14','2026-11-10 19:09:14'),(15,'P1015','激光切割喷嘴',8,'N-1.5','双层铜嘴，孔径 1.5mm','件',180.00,60.00,1,'易损件','2026-11-10 19:09:14','2026-11-10 19:09:14'),(16,'P1016','伺服驱动器',8,'SV-3KW','3kW 伺服驱动单元，适配自家数控系统','件',12600.00,8800.00,1,NULL,'2026-11-10 19:09:14','2026-11-10 19:09:14'),(17,'P1017','设备安装调试',9,'SVC-INST','含运输、就位、水平调整、精度校验、试切','次',8000.00,3000.00,1,'首台必选','2026-11-10 19:09:14','2026-11-10 19:09:14'),(18,'P1018','年度维保服务（标准）',9,'SVC-Y1','全年 2 次巡检，48 小时响应，不含配件','年',36000.00,12000.00,1,NULL,'2026-11-10 19:09:14','2026-11-10 19:09:14'),(19,'P1019','年度维保服务（尊享）',9,'SVC-Y2','全年 4 次巡检，24 小时响应，含易损件','年',68000.00,22000.00,1,'大客户推荐','2026-11-10 19:09:14','2026-11-10 19:09:14'),(20,'P1020','操作与编程培训',9,'SVC-TRN','现场培训 3 天，含教材与考核，每期 8 人','期',2800.00,1200.00,1,NULL,'2026-11-10 19:09:14','2026-11-10 19:09:14');
/*!40000 ALTER TABLE `crm_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crm_product_category`
--

DROP TABLE IF EXISTS `crm_product_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crm_product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父级ID，0为顶级',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1启用 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crm_product_category`
--

LOCK TABLES `crm_product_category` WRITE;
/*!40000 ALTER TABLE `crm_product_category` DISABLE KEYS */;
INSERT INTO `crm_product_category` VALUES (1,0,'金属切削设备',1,1,'2026-11-10 19:09:14'),(2,1,'数控车床',1,1,'2026-11-10 19:09:14'),(3,1,'加工中心',2,1,'2026-11-10 19:09:14'),(4,0,'激光加工设备',2,1,'2026-11-10 19:09:14'),(5,4,'激光切割机',1,1,'2026-11-10 19:09:14'),(6,4,'激光焊接机',2,1,'2026-11-10 19:09:14'),(7,0,'自动化产线',3,1,'2026-11-10 19:09:14'),(8,0,'配件耗材',4,1,'2026-11-10 19:09:14'),(9,0,'技术服务',5,1,'2026-11-10 19:09:14');
/*!40000 ALTER TABLE `crm_product_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employee`
--

DROP TABLE IF EXISTS `employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(30) NOT NULL COMMENT '姓名',
  `gender` tinyint NOT NULL DEFAULT '1' COMMENT '性别：1男 2女',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表（MyBatis 练习）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'苏琪',1,'2023-10-02'),(2,'傻娜',2,'2024-03-15'),(3,'亚鑫',1,'2022-07-01'),(4,'嘉欣',1,'2026-11-11'),(5,'嘉欣',1,'2026-11-11');
/*!40000 ALTER TABLE `employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dept`
--

DROP TABLE IF EXISTS `sys_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dept` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `parent_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '上级部门ID，0 表示顶级',
  `ancestors` varchar(500) NOT NULL DEFAULT '0' COMMENT '祖先ID链，逗号分隔',
  `dept_name` varchar(50) NOT NULL COMMENT '部门名称',
  `dept_code` varchar(50) DEFAULT NULL COMMENT '部门编码（可选）',
  `leader` varchar(30) DEFAULT NULL COMMENT '负责人姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '部门邮箱',
  `sort` int NOT NULL DEFAULT '0' COMMENT '同级排序，越小越靠前',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1启用 0停用',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `create_by` bigint unsigned DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint unsigned DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_dept_parent` (`parent_id`,`sort`),
  KEY `idx_dept_ancestors` (`ancestors`(120))
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表（多级组织架构）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dept`
--

LOCK TABLES `sys_dept` WRITE;
/*!40000 ALTER TABLE `sys_dept` DISABLE KEYS */;
INSERT INTO `sys_dept` VALUES (1,0,'0','总经办','D101','周明远','13800000001','gm@deming.cn',1,1,0,NULL,'2026-11-10 19:09:14',NULL,'2026-11-10 19:09:14'),(2,0,'0','销售中心','D110','张伟','13800000002','sales@deming.cn',2,1,0,NULL,'2026-11-10 19:09:14',NULL,'2026-11-10 19:09:14'),(3,2,'0,2','华东销售部','D111','李娜','13800000003','east@deming.cn',1,1,0,NULL,'2026-11-10 19:09:14',NULL,'2026-11-10 19:09:14'),(4,2,'0,2','华南销售部','D112','王强','13800000004','south@deming.cn',2,1,0,NULL,'2026-11-10 19:09:14',NULL,'2026-11-10 19:09:14'),(5,2,'0,2','渠道合作部','D113','陈晨','13800000005','channel@deming.cn',3,1,0,NULL,'2026-11-10 19:09:14',NULL,'2026-11-10 19:09:14'),(6,0,'0','市场部','D120','孙倩','13800000006','mkt@deming.cn',3,1,0,NULL,'2026-11-10 19:09:14',NULL,'2026-11-10 19:09:14'),(7,0,'0','技术研发部','D130','吴磊','13800000007','rd@deming.cn',4,1,0,NULL,'2026-11-10 19:09:14',NULL,'2026-11-10 19:09:14'),(8,0,'0','客户服务部','D140','刘敏','13800000008','service@deming.cn',5,1,0,NULL,'2026-11-10 19:09:14',NULL,'2026-11-10 19:09:14'),(9,0,'0','生产制造部','D150','郑海','13800000009','prod@deming.cn',6,1,0,NULL,'2026-11-10 19:09:14',NULL,'2026-11-10 19:09:14'),(10,0,'0','财务部','D160','何静','13800000010','fin@deming.cn',7,1,0,NULL,'2026-11-10 19:09:14',NULL,'2026-11-10 19:09:14'),(11,0,'0','人力行政部','D170','林芳','13800000011','hr@deming.cn',8,1,0,NULL,'2026-11-10 19:09:14',NULL,'2026-11-10 19:09:14');
/*!40000 ALTER TABLE `sys_dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_employee`
--

DROP TABLE IF EXISTS `sys_employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_employee` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `emp_no` varchar(20) NOT NULL COMMENT '员工编号（登录账号）',
  `password` varchar(100) NOT NULL COMMENT '登录密码（BCrypt 密文）',
  `name` varchar(30) NOT NULL COMMENT '姓名',
  `gender` tinyint NOT NULL DEFAULT '0' COMMENT '性别：0未知 1男 2女',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `dept_id` bigint unsigned NOT NULL COMMENT '所属部门ID',
  `job_title` varchar(50) DEFAULT NULL COMMENT '职位',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '账号状态：1启用 0停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录IP',
  `pwd_update_time` datetime DEFAULT NULL COMMENT '密码最后修改时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint unsigned DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_no` (`emp_no`),
  UNIQUE KEY `uk_emp_phone` (`phone`),
  KEY `idx_emp_dept_status` (`dept_id`,`status`),
  KEY `idx_emp_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_employee`
--

LOCK TABLES `sys_employee` WRITE;
/*!40000 ALTER TABLE `sys_employee` DISABLE KEYS */;
INSERT INTO `sys_employee` VALUES (1,'E1001','$2b$10$0j1zLN3XnWG3za1CStWKNOky31B3YAzOkIuZ/.VAHJ7YNQexa3/o2','张伟',1,'13800000001','zhangwei@deming.cn',NULL,2,'销售总监','2021-03-01',1,NULL,'2026-11-13 13:33:12','0:0:0:0:0:0:0:1',NULL,0,NULL,'2026-11-10 18:16:35',NULL,'2026-11-13 13:33:12'),(2,'E1002','$2b$10$0j1zLN3XnWG3za1CStWKNOky31B3YAzOkIuZ/.VAHJ7YNQexa3/o2','李娜',2,'13800000002','lina@deming.cn',NULL,3,'华东区域经理','2021-07-15',1,NULL,'2026-11-12 14:39:50','0:0:0:0:0:0:0:1',NULL,0,NULL,'2026-11-10 18:16:35',1,'2026-11-12 16:01:50'),(3,'E1003','$2b$10$0j1zLN3XnWG3za1CStWKNOky31B3YAzOkIuZ/.VAHJ7YNQexa3/o2','王强',1,'13800000003','wangqiang@deming.cn',NULL,4,'华南区域经理','2022-01-10',1,NULL,NULL,NULL,NULL,0,NULL,'2026-11-10 18:16:35',1,'2026-11-12 16:01:51'),(4,'E1004','$2b$10$0j1zLN3XnWG3za1CStWKNOky31B3YAzOkIuZ/.VAHJ7YNQexa3/o2','刘敏',2,'13800000004','liumin@deming.cn',NULL,8,'售后服务主管','2022-05-20',1,NULL,NULL,NULL,NULL,0,NULL,'2026-11-10 18:16:35',1,'2026-11-12 16:01:53'),(5,'E1005','$2b$10$0j1zLN3XnWG3za1CStWKNOky31B3YAzOkIuZ/.VAHJ7YNQexa3/o2','陈晨',1,'13800000005','chenchen@deming.cn',NULL,5,'渠道经理','2023-02-01',1,NULL,NULL,NULL,NULL,0,NULL,'2026-11-10 18:16:35',1,'2026-11-12 16:01:54'),(6,'E1006','$2a$10$scl68drgq7.nc.kfeh98dOPt/G0jnirQlo54uaZ0V9bsPKAoI7lT2','赵雪',2,'13800000006','zhaoxue@deming.cn',NULL,3,'销售工程师','2023-09-11',1,'负责华东区域中小客户，2023 年入职',NULL,NULL,'2026-11-10 19:24:57',0,NULL,'2026-11-10 18:16:35',1,'2026-11-12 16:01:56'),(9,'E1088','$2a$10$Tl9.PiWMt2r12aJ0Wz4fIeyMzvEYHrx9y9fX3DVxCzbEntDQQQPjy','林工',1,'13512340088','lingong@deming.cn',NULL,7,'售前工程师',NULL,1,'负责华东区售前技术方案',NULL,NULL,'2026-11-10 19:27:44',1,1,'2026-11-10 19:27:44',1,'2026-11-10 19:27:48');
/*!40000 ALTER TABLE `sys_employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_employee_role`
--

DROP TABLE IF EXISTS `sys_employee_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_employee_role` (
  `employee_id` bigint unsigned NOT NULL COMMENT '员工ID',
  `role_id` bigint unsigned NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  PRIMARY KEY (`employee_id`,`role_id`),
  KEY `idx_er_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_employee_role`
--

LOCK TABLES `sys_employee_role` WRITE;
/*!40000 ALTER TABLE `sys_employee_role` DISABLE KEYS */;
INSERT INTO `sys_employee_role` VALUES (1,1,'2026-11-10 18:16:35'),(2,2,'2026-11-10 18:16:35'),(3,2,'2026-11-10 18:16:35'),(3,4,'2026-11-10 18:16:35'),(4,4,'2026-11-10 18:16:35'),(5,3,'2026-11-10 18:16:35'),(6,3,'2026-11-10 19:24:52'),(6,4,'2026-11-10 19:24:52');
/*!40000 ALTER TABLE `sys_employee_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_permission`
--

DROP TABLE IF EXISTS `sys_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_permission` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '上级权限ID，0 表示顶级',
  `perm_name` varchar(50) NOT NULL COMMENT '权限名称',
  `perm_code` varchar(100) NOT NULL COMMENT '权限标识，如 system:employee:add',
  `perm_type` tinyint NOT NULL DEFAULT '2' COMMENT '类型：1目录 2菜单 3按钮',
  `path` varchar(200) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(200) DEFAULT NULL COMMENT '前端组件路径',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标名',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `visible` tinyint NOT NULL DEFAULT '1' COMMENT '是否显示：1显示 0隐藏',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1启用 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`),
  KEY `idx_perm_parent` (`parent_id`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=534 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限表（目录/菜单/按钮）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_permission`
--

LOCK TABLES `sys_permission` WRITE;
/*!40000 ALTER TABLE `sys_permission` DISABLE KEYS */;
INSERT INTO `sys_permission` VALUES (1,0,'线索管理','lead:list',2,'/lead','lead/index','Aim',1,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(2,0,'客户管理','customer:list',2,'/customer','customer/index','User',2,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(3,0,'商机管理','opportunity:list',2,'/opportunity','opportunity/index','TrendCharts',3,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(4,0,'订单管理','order:list',2,'/order','order/index','Tickets',4,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(5,0,'设置','system:view',1,'/system','Layout','Setting',5,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(51,5,'员工管理','system:employee:list',2,'/system/employee','system/employee/index','Avatar',1,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(52,5,'角色管理','system:role:list',2,'/system/role','system/role/index','Key',2,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(53,5,'部门管理','system:dept:list',2,'/system/dept','system/dept/index','OfficeBuilding',3,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(511,51,'新增员工','system:employee:add',3,NULL,NULL,NULL,1,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(512,51,'编辑员工','system:employee:edit',3,NULL,NULL,NULL,2,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(513,51,'删除员工','system:employee:delete',3,NULL,NULL,NULL,3,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(514,51,'重置密码','system:employee:reset',3,NULL,NULL,NULL,4,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(515,51,'启用停用','system:employee:status',3,NULL,NULL,NULL,5,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(521,52,'新增角色','system:role:add',3,NULL,NULL,NULL,1,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(522,52,'编辑角色','system:role:edit',3,NULL,NULL,NULL,2,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(523,52,'删除角色','system:role:delete',3,NULL,NULL,NULL,3,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(524,52,'分配权限','system:role:assign',3,NULL,NULL,NULL,4,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(531,53,'新增部门','system:dept:add',3,NULL,NULL,NULL,1,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(532,53,'编辑部门','system:dept:edit',3,NULL,NULL,NULL,2,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35'),(533,53,'删除部门','system:dept:delete',3,NULL,NULL,NULL,3,1,1,'2026-11-10 18:16:35','2026-11-10 18:16:35');
/*!40000 ALTER TABLE `sys_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码，如 SALES_MANAGER',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `data_scope` tinyint NOT NULL DEFAULT '4' COMMENT '数据范围：1全部 2本部门及以下 3本部门 4仅本人 5自定义',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1启用 0停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '描述',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `create_by` bigint unsigned DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint unsigned DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  UNIQUE KEY `uk_role_name` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,'ADMIN','超级管理员',1,1,1,'拥有系统全部权限',0,NULL,'2026-11-10 18:16:35',NULL,'2026-11-10 18:16:35'),(2,'SALES_MANAGER','销售经理',2,2,1,'管理本部门及下级部门的客户与商机',0,NULL,'2026-11-10 18:16:35',NULL,'2026-11-10 18:16:35'),(3,'SALES','销售专员',4,3,1,'仅能查看自己负责的数据',0,NULL,'2026-11-10 18:16:35',NULL,'2026-11-10 18:16:35'),(4,'CS','客户成功',3,4,1,'负责本部门订单交付与售后',0,NULL,'2026-11-10 18:16:35',NULL,'2026-11-10 18:16:35');
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_permission`
--

DROP TABLE IF EXISTS `sys_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_permission` (
  `role_id` bigint unsigned NOT NULL COMMENT '角色ID',
  `permission_id` bigint unsigned NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `idx_rp_permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_permission`
--

LOCK TABLES `sys_role_permission` WRITE;
/*!40000 ALTER TABLE `sys_role_permission` DISABLE KEYS */;
INSERT INTO `sys_role_permission` VALUES (1,1),(2,1),(3,1),(1,2),(2,2),(3,2),(4,2),(1,3),(2,3),(3,3),(1,4),(2,4),(3,4),(4,4),(1,5),(1,51),(1,52),(1,53),(1,511),(1,512),(1,513),(1,514),(1,515),(1,521),(1,522),(1,523),(1,524),(1,531),(1,532),(1,533);
/*!40000 ALTER TABLE `sys_role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛涓婚敭',
  `phone` varchar(20) NOT NULL COMMENT '鎵嬫満鍙',
  `password_hash` varchar(255) NOT NULL COMMENT '瀵嗙爜鍝堝笇锛岀?姝㈠瓨鍌ㄦ槑鏂囧瘑鐮',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐢ㄦ埛琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'crm'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-13 15:37:05
