import {
  YES_NO,
  PRODUCT_STATUS,
  PRODUCT_UNIT,
  PRODUCT_CATEGORY,
  PRODUCT_BRAND,
  PRICE_STATUS,
  FOLLOW_TYPE,
  FOLLOW_RESULT,
  FOLLOW_RESULT_TAG,
  QUOTE_STATUS,
  QUOTE_STATUS_TAG,
  RETURN_TYPE,
  RETURN_STATUS,
  RETURN_STATUS_TAG,
  SHIP_STATUS,
  SHIP_STATUS_TAG,
  CARRIER,
  PAY_METHOD,
  TICKET_TYPE,
  TICKET_PRIORITY,
  TICKET_PRIORITY_TAG,
  TICKET_STATUS,
  TICKET_STATUS_TAG,
  VISIT_TYPE,
  PRODUCT_STATUS_TAG,
  PRICE_STATUS_TAG
} from '@/utils/dict'

/**
 * 业务页面配置：表格列、筛选、表单字段。
 * 由 CrudPage 统一渲染，页面组件只负责传入对应 key 的配置。
 */
export const businessConfigs = {
  /* ---------------- 客户管理 ---------------- */
  contacts: {
    title: '联系人',
    subtitle: '维护客户侧关键角色，标注主联系人便于快速对接',
    collection: 'contacts',
    searchFields: ['custName', 'name', 'phone', 'email', 'dept'],
    keywordPlaceholder: '客户 / 姓名 / 手机号 / 邮箱',
    filters: [{ prop: 'isPrimary', label: '主联系人', type: 'select', options: YES_NO, width: '110px' }],
    columns: [
      { prop: 'custName', label: '所属客户', minWidth: 135 },
      { prop: 'name', label: '姓名', width: 95 },
      { prop: 'title', label: '职位', width: 110 },
      { prop: 'dept', label: '部门', width: 122 },
      { prop: 'phone', label: '手机号', width: 128, type: 'mono' },
      { prop: 'email', label: '邮箱', minWidth: 165 },
      { prop: 'isPrimary', label: '主联系人', width: 100, align: 'center', type: 'tag', tagMap: { 是: 'success', 否: 'info' } },
      { prop: 'owner', label: '负责人', width: 95 }
    ],
    formFields: [
      { prop: 'custName', label: '所属客户', required: true, placeholder: '请输入客户名称' },
      { prop: 'name', label: '姓名', required: true, placeholder: '请输入姓名' },
      { prop: 'title', label: '职位' },
      { prop: 'dept', label: '部门' },
      { prop: 'phone', label: '手机号', maxlength: 11 },
      { prop: 'email', label: '邮箱' },
      { prop: 'isPrimary', label: '主联系人', type: 'select', options: YES_NO, default: '否' },
      { prop: 'owner', label: '负责人' },
      { prop: 'remark', label: '备注', type: 'textarea', span: 24 }
    ]
  },

  followUps: {
    title: '跟进记录',
    subtitle: '沉淀每一次沟通内容，避免交接后信息断层',
    collection: 'followUps',
    searchFields: ['custName', 'contact', 'content', 'owner'],
    keywordPlaceholder: '客户 / 联系人 / 跟进内容',
    filters: [
      { prop: 'type', label: '跟进方式', type: 'select', options: FOLLOW_TYPE, width: '120px' },
      { prop: 'result', label: '跟进结果', type: 'select', options: FOLLOW_RESULT, width: '120px' }
    ],
    columns: [
      { prop: 'custName', label: '客户', minWidth: 135 },
      { prop: 'contact', label: '联系人', width: 98 },
      { prop: 'type', label: '方式', width: 108, type: 'tag' },
      { prop: 'content', label: '跟进内容', minWidth: 220 },
      { prop: 'result', label: '结果', width: 112, type: 'tag', tagMap: FOLLOW_RESULT_TAG },
      { prop: 'nextTime', label: '下次跟进', width: 118 },
      { prop: 'owner', label: '负责人', width: 95 }
    ],
    formFields: [
      { prop: 'custName', label: '客户', required: true },
      { prop: 'contact', label: '联系人' },
      { prop: 'type', label: '跟进方式', type: 'select', options: FOLLOW_TYPE, default: '电话' },
      { prop: 'result', label: '跟进结果', type: 'select', options: FOLLOW_RESULT, default: '有意向' },
      { prop: 'content', label: '跟进内容', type: 'textarea', span: 24, required: true, placeholder: '记录客户反馈、报价、下一步动作' },
      { prop: 'nextTime', label: '下次跟进', type: 'date' },
      { prop: 'owner', label: '负责人' }
    ]
  },

  /* ---------------- 产品管理 ---------------- */
  productCategories: {
    title: '产品分类',
    subtitle: '按系统与部位划分零件目录，支撑分类统计与价格策略',
    collection: 'productCategories',
    searchFields: ['code', 'name', 'remark'],
    keywordPlaceholder: '分类编码 / 名称',
    filters: [],
    columns: [
      { prop: 'code', label: '分类编码', width: 110, type: 'mono' },
      { prop: 'name', label: '分类名称', minWidth: 160 },
      { prop: 'sort', label: '排序', width: 80, align: 'center' },
      { prop: 'remark', label: '说明', minWidth: 260 },
      { prop: 'createTime', label: '创建时间', width: 165 }
    ],
    formFields: [
      { prop: 'code', label: '分类编码', required: true },
      { prop: 'name', label: '分类名称', required: true },
      { prop: 'sort', label: '排序', type: 'number', default: 1 },
      { prop: 'remark', label: '说明', type: 'textarea', span: 24 }
    ]
  },

  products: {
    title: '产品档案',
    subtitle: '维护易损小件与养护用品清单、适用车型与进销价格，作为报价与开单的基础',
    collection: 'products',
    searchFields: ['prodNo', 'name', 'brand', 'model', 'fitment', 'categoryName'],
    keywordPlaceholder: '零件编号 / 名称 / 品牌 / 车型',
    noField: 'prodNo',
    noPrefix: 'AP',
    filters: [
      { prop: 'categoryName', label: '产品种类', type: 'select', options: PRODUCT_CATEGORY, width: '150px' },
      { prop: 'brand', label: '品牌', type: 'select', options: PRODUCT_BRAND, width: '120px' },
      { prop: 'status', label: '状态', type: 'select', options: PRODUCT_STATUS, width: '110px' }
    ],
    columns: [
      { prop: 'prodNo', label: '零件编号', width: 120, type: 'mono' },
      { prop: 'name', label: '产品名称', minWidth: 180 },
      { prop: 'brand', label: '品牌', width: 100 },
      { prop: 'categoryName', label: '产品种类', width: 130 },
      { prop: 'model', label: '型号/零件号', width: 140, type: 'mono' },
      { prop: 'fitment', label: '适用车型', minWidth: 170 },
      { prop: 'unit', label: '单位', width: 70, align: 'center' },
      { prop: 'price', label: '标准价', width: 110, align: 'right', type: 'money' },
      { prop: 'cost', label: '成本', width: 105, align: 'right', type: 'money' },
      { prop: 'status', label: '状态', width: 90, type: 'tag', tagMap: PRODUCT_STATUS_TAG },
      { prop: 'owner', label: '负责人', width: 85 }
    ],
    formFields: [
      { prop: 'name', label: '产品名称', required: true },
      { prop: 'categoryName', label: '产品种类', type: 'select', options: PRODUCT_CATEGORY, required: true },
      { prop: 'brand', label: '品牌', type: 'select', options: PRODUCT_BRAND, default: '通用' },
      { prop: 'model', label: '型号/零件号', placeholder: '如 BW-25-2L / LX1780' },
      { prop: 'unit', label: '单位', type: 'select', options: PRODUCT_UNIT, default: '件' },
      { prop: 'price', label: '标准价', type: 'number' },
      { prop: 'cost', label: '成本', type: 'number' },
      { prop: 'fitment', label: '适用车型', placeholder: '如 朗逸/宝来 1.6L，或「通用」' },
      { prop: 'spec', label: '规格参数', span: 24, placeholder: '容量、香型、材质、包装数量等' },
      { prop: 'status', label: '状态', type: 'select', options: PRODUCT_STATUS, default: '在售' },
      { prop: 'owner', label: '负责人' }
    ]
  },

  customerPrices: {
    title: '客户价格',
    subtitle: '记录客户专属折扣与协议价，报价时自动可查',
    collection: 'customerPrices',
    searchFields: ['customerName', 'productName', 'owner'],
    keywordPlaceholder: '客户 / 产品 / 负责人',
    filters: [{ prop: 'status', label: '状态', type: 'select', options: PRICE_STATUS, width: '110px' }],
    columns: [
      { prop: 'customerName', label: '客户', minWidth: 150 },
      { prop: 'productName', label: '产品', minWidth: 180 },
      { prop: 'listPrice', label: '标准价', width: 115, align: 'right', type: 'money' },
      { prop: 'discount', label: '折扣', width: 80, align: 'center' },
      { prop: 'price', label: '协议价', width: 115, align: 'right', type: 'money' },
      { prop: 'effectiveDate', label: '生效日期', width: 110 },
      { prop: 'status', label: '状态', width: 95, type: 'tag', tagMap: PRICE_STATUS_TAG },
      { prop: 'owner', label: '负责人', width: 85 }
    ],
    formFields: [
      { prop: 'customerName', label: '客户', required: true },
      { prop: 'productName', label: '产品', required: true },
      { prop: 'listPrice', label: '标准价', type: 'number' },
      { prop: 'discount', label: '折扣', type: 'number' },
      { prop: 'price', label: '协议价', type: 'number' },
      { prop: 'effectiveDate', label: '生效日期', type: 'date' },
      { prop: 'status', label: '状态', type: 'select', options: PRICE_STATUS, default: '生效中' },
      { prop: 'owner', label: '负责人' }
    ]
  },

  /* ---------------- 销售管理 ---------------- */
  quotes: {
    title: '报价单',
    subtitle: '从商机生成报价，跟踪有效期与客户答复',
    collection: 'quotes',
    searchFields: ['quoteNo', 'customerName', 'productName', 'owner'],
    keywordPlaceholder: '报价单号 / 客户 / 产品',
    noField: 'quoteNo',
    noPrefix: 'BJ',
    filters: [{ prop: 'status', label: '状态', type: 'select', options: QUOTE_STATUS, width: '110px' }],
    columns: [
      { prop: 'quoteNo', label: '报价单号', width: 120, type: 'mono' },
      { prop: 'customerName', label: '客户', minWidth: 150 },
      { prop: 'productName', label: '产品', minWidth: 175 },
      { prop: 'amount', label: '报价金额', width: 125, align: 'right', type: 'money' },
      { prop: 'validUntil', label: '有效期至', width: 110 },
      { prop: 'status', label: '状态', width: 95, type: 'tag', tagMap: QUOTE_STATUS_TAG },
      { prop: 'owner', label: '负责人', width: 85 }
    ],
    formFields: [
      { prop: 'customerName', label: '客户', required: true },
      { prop: 'productName', label: '产品', required: true },
      { prop: 'amount', label: '报价金额', type: 'number' },
      { prop: 'validUntil', label: '有效期至', type: 'date' },
      { prop: 'status', label: '状态', type: 'select', options: QUOTE_STATUS, default: '草稿' },
      { prop: 'owner', label: '负责人' },
      { prop: 'remark', label: '备注', type: 'textarea', span: 24 }
    ]
  },

  returns: {
    title: '退换货单',
    subtitle: '登记退货换货申请，联动应收与库存调整',
    collection: 'returns',
    searchFields: ['returnNo', 'orderNo', 'customerName', 'reason', 'owner'],
    keywordPlaceholder: '退换单号 / 订单号 / 客户',
    noField: 'returnNo',
    noPrefix: 'TH',
    filters: [
      { prop: 'type', label: '类型', type: 'select', options: RETURN_TYPE, width: '100px' },
      { prop: 'status', label: '状态', type: 'select', options: RETURN_STATUS, width: '110px' }
    ],
    columns: [
      { prop: 'returnNo', label: '退换单号', width: 120, type: 'mono' },
      { prop: 'orderNo', label: '原订单号', width: 120, type: 'mono' },
      { prop: 'customerName', label: '客户', minWidth: 150 },
      { prop: 'type', label: '类型', width: 80, type: 'tag' },
      { prop: 'reason', label: '原因', minWidth: 160 },
      { prop: 'amount', label: '涉及金额', width: 125, align: 'right', type: 'money' },
      { prop: 'status', label: '状态', width: 95, type: 'tag', tagMap: RETURN_STATUS_TAG },
      { prop: 'owner', label: '负责人', width: 85 }
    ],
    formFields: [
      { prop: 'orderNo', label: '原订单号' },
      { prop: 'customerName', label: '客户', required: true },
      { prop: 'type', label: '类型', type: 'select', options: RETURN_TYPE, default: '退货' },
      { prop: 'reason', label: '原因', required: true },
      { prop: 'amount', label: '涉及金额', type: 'number' },
      { prop: 'status', label: '状态', type: 'select', options: RETURN_STATUS, default: '待审核' },
      { prop: 'owner', label: '负责人' }
    ]
  },

  /* ---------------- 交付与回款 ---------------- */
  shipments: {
    title: '发货记录',
    subtitle: '跟踪订单发货进度、承运商与签收状态',
    collection: 'shipments',
    searchFields: ['shipNo', 'orderNo', 'customerName', 'productName', 'trackingNo', 'owner'],
    keywordPlaceholder: '发货单号 / 订单号 / 物流单号',
    noField: 'shipNo',
    noPrefix: 'FH',
    filters: [
      { prop: 'status', label: '状态', type: 'select', options: SHIP_STATUS, width: '110px' },
      { prop: 'carrier', label: '承运商', type: 'select', options: CARRIER, width: '130px' }
    ],
    columns: [
      { prop: 'shipNo', label: '发货单号', width: 120, type: 'mono' },
      { prop: 'orderNo', label: '订单号', width: 120, type: 'mono' },
      { prop: 'customerName', label: '客户', minWidth: 150 },
      { prop: 'productName', label: '产品', minWidth: 170 },
      { prop: 'qty', label: '数量', width: 80, align: 'center' },
      { prop: 'shipDate', label: '发货日期', width: 110 },
      { prop: 'carrier', label: '承运商', width: 110 },
      { prop: 'trackingNo', label: '物流单号', minWidth: 155, type: 'mono' },
      { prop: 'status', label: '状态', width: 95, type: 'tag', tagMap: SHIP_STATUS_TAG }
    ],
    formFields: [
      { prop: 'orderNo', label: '订单号', required: true },
      { prop: 'customerName', label: '客户', required: true },
      { prop: 'productName', label: '产品', required: true },
      { prop: 'qty', label: '数量', type: 'number', default: 1 },
      { prop: 'shipDate', label: '发货日期', type: 'date' },
      { prop: 'carrier', label: '承运商', type: 'select', options: CARRIER },
      { prop: 'trackingNo', label: '物流单号' },
      { prop: 'status', label: '状态', type: 'select', options: SHIP_STATUS, default: '待发货' },
      { prop: 'owner', label: '负责人' }
    ]
  },

  receipts: {
    title: '收款记录',
    subtitle: '按笔登记回款，自动关联订单与付款方式',
    collection: 'receipts',
    searchFields: ['receiptNo', 'orderNo', 'customerName', 'owner', 'remark'],
    keywordPlaceholder: '收款单号 / 订单号 / 客户',
    noField: 'receiptNo',
    noPrefix: 'SK',
    filters: [{ prop: 'payMethod', label: '付款方式', type: 'select', options: PAY_METHOD, width: '125px' }],
    columns: [
      { prop: 'receiptNo', label: '收款单号', width: 120, type: 'mono' },
      { prop: 'orderNo', label: '订单号', width: 120, type: 'mono' },
      { prop: 'customerName', label: '客户', minWidth: 150 },
      { prop: 'amount', label: '收款金额', width: 130, align: 'right', type: 'money' },
      { prop: 'payMethod', label: '付款方式', width: 110 },
      { prop: 'payDate', label: '收款日期', width: 110 },
      { prop: 'remark', label: '说明', minWidth: 120 },
      { prop: 'owner', label: '负责人', width: 85 }
    ],
    formFields: [
      { prop: 'orderNo', label: '订单号' },
      { prop: 'customerName', label: '客户', required: true },
      { prop: 'amount', label: '收款金额', type: 'number' },
      { prop: 'payMethod', label: '付款方式', type: 'select', options: PAY_METHOD, default: '银行转账' },
      { prop: 'payDate', label: '收款日期', type: 'date' },
      { prop: 'owner', label: '负责人' },
      { prop: 'remark', label: '说明', type: 'textarea', span: 24 }
    ]
  },

  /* ---------------- 售后管理 ---------------- */
  tickets: {
    title: '售后工单',
    subtitle: '受理客户报障与咨询，按优先级分派处理',
    collection: 'tickets',
    searchFields: ['ticketNo', 'customerName', 'title', 'assignee'],
    keywordPlaceholder: '工单号 / 客户 / 标题',
    noField: 'ticketNo',
    noPrefix: 'GD',
    filters: [
      { prop: 'type', label: '类型', type: 'select', options: TICKET_TYPE, width: '115px' },
      { prop: 'priority', label: '优先级', type: 'select', options: TICKET_PRIORITY, width: '100px' },
      { prop: 'status', label: '状态', type: 'select', options: TICKET_STATUS, width: '110px' }
    ],
    columns: [
      { prop: 'ticketNo', label: '工单号', width: 120, type: 'mono' },
      { prop: 'customerName', label: '客户', minWidth: 140 },
      { prop: 'title', label: '问题描述', minWidth: 230 },
      { prop: 'type', label: '类型', width: 105, type: 'tag' },
      { prop: 'priority', label: '优先级', width: 90, align: 'center', type: 'tag', tagMap: TICKET_PRIORITY_TAG },
      { prop: 'status', label: '状态', width: 95, type: 'tag', tagMap: TICKET_STATUS_TAG },
      { prop: 'assignee', label: '处理人', width: 90 },
      { prop: 'createTime', label: '创建时间', width: 165 }
    ],
    formFields: [
      { prop: 'customerName', label: '客户', required: true },
      { prop: 'title', label: '问题描述', required: true, span: 24 },
      { prop: 'type', label: '类型', type: 'select', options: TICKET_TYPE, default: '故障报修' },
      { prop: 'priority', label: '优先级', type: 'select', options: TICKET_PRIORITY, default: '中' },
      { prop: 'status', label: '状态', type: 'select', options: TICKET_STATUS, default: '待受理' },
      { prop: 'assignee', label: '处理人' },
      { prop: 'remark', label: '处理备注', type: 'textarea', span: 24 }
    ]
  },

  visits: {
    title: '客户回访',
    subtitle: '记录满意度与续约意向，识别流失风险客户',
    collection: 'visits',
    searchFields: ['visitNo', 'customerName', 'contact', 'content', 'owner'],
    keywordPlaceholder: '回访单号 / 客户 / 回访内容',
    noField: 'visitNo',
    noPrefix: 'HF',
    filters: [{ prop: 'type', label: '回访方式', type: 'select', options: VISIT_TYPE, width: '125px' }],
    columns: [
      { prop: 'visitNo', label: '回访单号', width: 118, type: 'mono' },
      { prop: 'customerName', label: '客户', minWidth: 130 },
      { prop: 'contact', label: '联系人', width: 100 },
      { prop: 'visitDate', label: '回访日期', width: 118 },
      { prop: 'type', label: '方式', width: 108, type: 'tag' },
      { prop: 'satisfaction', label: '满意度', width: 132, type: 'rate' },
      { prop: 'content', label: '回访内容', minWidth: 200 },
      { prop: 'owner', label: '负责人', width: 95 }
    ],
    formFields: [
      { prop: 'customerName', label: '客户', required: true },
      { prop: 'contact', label: '联系人' },
      { prop: 'visitDate', label: '回访日期', type: 'date' },
      { prop: 'type', label: '回访方式', type: 'select', options: VISIT_TYPE, default: '电话回访' },
      { prop: 'satisfaction', label: '满意度', type: 'number', default: 4 },
      { prop: 'content', label: '回访内容', type: 'textarea', span: 24 },
      { prop: 'owner', label: '负责人' }
    ]
  }
}
