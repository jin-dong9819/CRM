/** 业务字典：选项 + 标签颜色，页面里直接复用 */

export const LEAD_STATUS = ['未跟进', '跟进中', '已转化', '已失效']
export const LEAD_STATUS_TAG = { 未跟进: 'info', 跟进中: 'primary', 已转化: 'success', 已失效: 'danger' }

export const LEAD_SOURCE = ['官网表单', '电话咨询', '展会活动', '老客户介绍', '广告投放', '陌生拜访']

export const LEAD_LEVEL = ['A', 'B', 'C']
export const LEAD_LEVEL_TAG = { A: 'danger', B: 'warning', C: 'info' }

export const CUSTOMER_LEVEL = ['重要客户', '普通客户', '潜在客户']
export const CUSTOMER_LEVEL_TAG = { 重要客户: 'danger', 普通客户: 'primary', 潜在客户: 'info' }

export const CUSTOMER_STATUS = ['跟进中', '合作中', '已成交', '已流失']
export const CUSTOMER_STATUS_TAG = { 跟进中: 'warning', 合作中: 'primary', 已成交: 'success', 已流失: 'info' }

export const CUSTOMER_INDUSTRY = ['互联网', '制造业', '医疗健康', '教育培训', '零售电商', '物流运输', '金融保险']

export const OPP_STAGE = ['初步接洽', '需求确认', '方案报价', '商务谈判', '赢单', '输单']
export const OPP_STAGE_TAG = {
  初步接洽: 'info',
  需求确认: 'primary',
  方案报价: 'primary',
  商务谈判: 'warning',
  赢单: 'success',
  输单: 'danger'
}

export const ORDER_STATUS = ['待审核', '已确认', '已发货', '已完成', '已取消']
export const ORDER_STATUS_TAG = {
  待审核: 'warning',
  已确认: 'primary',
  已发货: 'primary',
  已完成: 'success',
  已取消: 'info'
}

export const PAY_STATUS = ['未付款', '部分付款', '已付款']
export const PAY_STATUS_TAG = { 未付款: 'danger', 部分付款: 'warning', 已付款: 'success' }

/* ---------- 产品（汽车维修零件） ---------- */

export const PRODUCT_STATUS = ['在售', '停售']
export const PRODUCT_STATUS_TAG = { 在售: 'success', 停售: 'info' }

// 产品种类：与 crm_product_category 一级分类保持一致
export const PRODUCT_CATEGORY = [
  '油液耗材',
  '清洁清洗',
  '养护美容',
  '除味杀菌',
  '滤清器',
  '易损小件',
  '补胎轮胎小件',
  '车身小件',
  '五金辅料',
  '车用百货',
  '洗车养护服务'
]

// 常用品牌，供产品表单下拉选择（可自由输入其他品牌）
export const PRODUCT_BRAND = [
  '蓝星', '长城', '壳牌', '美孚', '博世', '马勒', '曼牌', 'NGK', '飞利浦',
  '龟牌', '3M', '标榜', '雪佛龙', '汉高', 'WD-40', '倍思',
  '通用', '副厂', '组合装', '自营'
]

export const PRODUCT_UNIT = ['瓶', '桶', '盒', '支', '套', '包', '条', '对', '个', '只', '卡', '卷', '件', '次']

export const PRICE_STATUS = ['生效中', '已过期']
export const PRICE_STATUS_TAG = { 生效中: 'success', 已过期: 'info' }

/* ---------- 客户 ---------- */

export const YES_NO = ['是', '否']

export const FOLLOW_TYPE = ['电话', '上门拜访', '微信', '邮件', '会议']
export const FOLLOW_RESULT = ['有意向', '需再沟通', '已报价', '已成交', '暂无需求']
export const FOLLOW_RESULT_TAG = {
  有意向: 'primary',
  需再沟通: 'warning',
  已报价: 'primary',
  已成交: 'success',
  暂无需求: 'info'
}

export const SEA_REASON = ['长期未跟进', '客户主动放弃', '跟进无进展', '负责人离职', '预算冻结']
export const SEA_STATUS = ['待领取', '已领取']
export const SEA_STATUS_TAG = { 待领取: 'warning', 已领取: 'success' }

/* ---------- 销售 ---------- */

export const QUOTE_STATUS = ['草稿', '已发送', '已通过', '已拒绝']
export const QUOTE_STATUS_TAG = { 草稿: 'info', 已发送: 'primary', 已通过: 'success', 已拒绝: 'danger' }

export const RETURN_TYPE = ['退货', '换货']
export const RETURN_STATUS = ['待审核', '已通过', '已完成', '已拒绝']
export const RETURN_STATUS_TAG = { 待审核: 'warning', 已通过: 'primary', 已完成: 'success', 已拒绝: 'danger' }

/* ---------- 交付与回款 ---------- */

export const SHIP_STATUS = ['待发货', '运输中', '已签收']
export const SHIP_STATUS_TAG = { 待发货: 'warning', 运输中: 'primary', 已签收: 'success' }

export const CARRIER = ['顺丰速运', '德邦物流', '京东物流', '客户自提']

export const PAY_METHOD = ['银行转账', '承兑汇票', '现金', '支付宝']

export const RECEIVABLE_STATUS = ['未到期', '已逾期', '已结清']
export const RECEIVABLE_STATUS_TAG = { 未到期: 'primary', 已逾期: 'danger', 已结清: 'success' }

/* ---------- 售后 ---------- */

export const TICKET_TYPE = ['故障报修', '使用咨询', '需求建议', '投诉']
export const TICKET_PRIORITY = ['高', '中', '低']
export const TICKET_PRIORITY_TAG = { 高: 'danger', 中: 'warning', 低: 'info' }
export const TICKET_STATUS = ['待受理', '处理中', '已解决', '已关闭']
export const TICKET_STATUS_TAG = { 待受理: 'warning', 处理中: 'primary', 已解决: 'success', 已关闭: 'info' }

export const VISIT_TYPE = ['上门回访', '电话回访']

/** 权限类型：value 与库中 sys_permission.perm_type 一致 */
export const PERM_TYPE = [
  { value: 1, label: '目录' },
  { value: 2, label: '菜单' },
  { value: 3, label: '按钮' }
]
export const PERM_TYPE_TAG = { 1: 'info', 2: 'primary', 3: 'warning' }

/** 数据权限范围：value 与库中 sys_role.data_scope 一致 */
export const DATA_SCOPE = [
  { value: 1, label: '全部数据' },
  { value: 2, label: '本部门及以下' },
  { value: 3, label: '本部门' },
  { value: 4, label: '仅本人' }
]

/** 字符串数组转 el-select 需要的选项 */
export const toOptions = (arr) => arr.map((item) => ({ label: item, value: item }))

/** 金额格式化：123456 -> ¥123,456.00 */
export function formatMoney(value) {
  const num = Number(value || 0)
  return `¥${num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}
