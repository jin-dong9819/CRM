/**
 * 扩展业务演示数据：产品、报价、交付、售后等后端尚未提供接口的模块。
 * 数据存于 localStorage，支持增删改（刷新不丢）。
 * 后端接口就绪后，把对应页面改成走 api/business.js 即可，页面结构不用变。
 */
const STORAGE_KEY = 'crm_extra_db_v5'
let cache = null

const pad = (n, len = 4) => String(n).padStart(len, '0')
const D = (offset) => {
  const t = new Date(Date.now() - offset * 86400000)
  const p = (v) => String(v).padStart(2, '0')
  return `${t.getFullYear()}-${p(t.getMonth() + 1)}-${p(t.getDate())}`
}
const DT = (offset) => `${D(offset)} ${String(9 + ((offset + 24) % 8)).padStart(2, '0')}:${String((offset * 7) % 60).padStart(2, '0')}:00`

const CUST = [
  '顺达汽修厂',
  '车之家快修（连锁）',
  '途顺汽配商行',
  '宏运汽车维修中心',
  '华信一汽-大众 4S 店',
  '安达运输车队',
  '亮洁汽车美容店',
  '鑫源快修（3 家门店）',
  '城市出租汽车公司',
  '途虎养车工场店（南山）',
  '车主 王先生',
  '恒信汽车租赁'
]
const PEOPLE = ['张伟', '李娜', '王强', '刘敏', '陈晨', '赵雪']
const CONTACT_NAME = ['陈立', '林小雅', '黄鹏', '吴静', '徐磊', '马超', '何雨', '高原', '沈悦', '曹阳', '罗敏', '谢峰']
const IND = ['综合修理厂', '连锁快修', '汽配批发', '4S 店', '物流车队', '洗车美容', '汽车租赁', '出租客运', '个人车主']

const PROD_SKUS = [
  { name: '玻璃水 -25℃',           brand: '蓝星',   model: 'BW-25-2L',   fitment: '通用',                category: '油液耗材',     unit: '瓶', price: 18,  cost: 9 },
  { name: '玻璃水 0℃',             brand: '蓝星',   model: 'BW-0-2L',    fitment: '通用',                category: '油液耗材',     unit: '瓶', price: 8,   cost: 3.5 },
  { name: '防冻液 -35℃',           brand: '长城',   model: 'LLC-35-4L',  fitment: '通用（汽油车）',      category: '油液耗材',     unit: '桶', price: 78,  cost: 42 },
  { name: '半合成机油 10W-40',     brand: '长城',   model: 'SN-4L',      fitment: '经济型轿车通用',      category: '油液耗材',     unit: '桶', price: 158, cost: 105 },
  { name: '化油器清洗剂',          brand: '标榜',   model: 'B-1088',     fitment: '通用',                category: '清洁清洗',     unit: '瓶', price: 18,  cost: 8 },
  { name: '刹车系统清洗剂',        brand: '标榜',   model: 'B-1099',     fitment: '通用',                category: '清洁清洗',     unit: '瓶', price: 25,  cost: 11 },
  { name: '洗车液（高泡型）',      brand: '龟牌',   model: 'G-4001',     fitment: '通用',                category: '清洁清洗',     unit: '瓶', price: 28,  cost: 12 },
  { name: '玻璃清洁剂',            brand: '龟牌',   model: 'G-3100',     fitment: '通用',                category: '清洁清洗',     unit: '瓶', price: 22,  cost: 9 },
  { name: '表板蜡',                brand: '龟牌',   model: 'G-5010',     fitment: '通用',                category: '养护美容',     unit: '瓶', price: 28,  cost: 12 },
  { name: '划痕修复蜡',            brand: '龟牌',   model: 'G-6010',     fitment: '通用（各色车漆）',    category: '养护美容',     unit: '支', price: 26,  cost: 11 },
  { name: '雨敌（玻璃驱水剂）',    brand: '龟牌',   model: 'G-3300',     fitment: '通用',                category: '养护美容',     unit: '瓶', price: 38,  cost: 16 },
  { name: '空调清洗剂（泡沫）',    brand: '3M',     model: 'PN39000',    fitment: '通用',                category: '除味杀菌',     unit: '瓶', price: 45,  cost: 20 },
  { name: '车内除味喷雾',          brand: '3M',     model: 'PN39200',    fitment: '通用',                category: '除味杀菌',     unit: '瓶', price: 32,  cost: 14 },
  { name: '机油滤清器',            brand: '博世',   model: '0986AF0055', fitment: '大众 1.4T/1.6L',      category: '滤清器',       unit: '个', price: 35,  cost: 18 },
  { name: '三滤保养套装',          brand: '组合装', model: 'SET-3F-01',  fitment: '大众 1.6L/1.4T',      category: '滤清器',       unit: '套', price: 128, cost: 62 },
  { name: '无骨雨刮片（对装）',    brand: '博世',   model: 'NW-24/16',   fitment: '多数轿车通用',        category: '易损小件',     unit: '对', price: 88,  cost: 45 },
  { name: '卤素灯泡 H7',           brand: '飞利浦', model: 'H7-12972',   fitment: 'H7 灯座车型',         category: '易损小件',     unit: '只', price: 15,  cost: 6 },
  { name: '火花塞（铱金）',        brand: 'NGK',    model: 'ILZKR7B11',  fitment: '大众 1.4T/1.6L',      category: '易损小件',     unit: '支', price: 68,  cost: 38 },
  { name: '补胎胶条',              brand: '通用',   model: 'BT-10',      fitment: '通用（真空胎）',      category: '补胎轮胎小件', unit: '盒', price: 8,   cost: 2.5 },
  { name: '气门嘴（4 只装）',      brand: '通用',   model: 'QM-4',       fitment: '通用',                category: '补胎轮胎小件', unit: '套', price: 15,  cost: 6 },
  { name: '补漆笔（通用色）',      brand: '通用',   model: 'BQB-01',     fitment: '通用（按车色选）',    category: '车身小件',     unit: '支', price: 15,  cost: 5 },
  { name: '硅酮密封胶',            brand: '汉高',   model: 'SG-85',      fitment: '通用',                category: '五金辅料',     unit: '支', price: 25,  cost: 11 },
  { name: '车载充电器 65W',        brand: '倍思',   model: 'CC-65W',     fitment: '通用',                category: '车用百货',     unit: '个', price: 59,  cost: 25 },
  { name: '标准洗车',              brand: '自营',   model: 'SV-WASH',    fitment: '通用',                category: '洗车养护服务', unit: '次', price: 25,  cost: 8 }
]

const PROD_NAMES = PROD_SKUS.map((p) => p.name)

function seed() {
  const productCategories = [
    { id: 1,  code: 'CAT01', name: '油液耗材',     sort: 1,  remark: '玻璃水、防冻液、机油、刹车油、助力油' },
    { id: 2,  code: 'CAT02', name: '清洁清洗',     sort: 2,  remark: '化油器/节气门/刹车清洗剂、洗车液、玻璃清洁' },
    { id: 3,  code: 'CAT03', name: '养护美容',     sort: 3,  remark: '表板蜡、划痕蜡、驱水剂、内饰护理' },
    { id: 4,  code: 'CAT04', name: '除味杀菌',     sort: 4,  remark: '空调清洗剂、除味喷雾、香薰、活性炭' },
    { id: 5,  code: 'CAT05', name: '滤清器',       sort: 5,  remark: '机油/空气/空调/燃油滤清器与保养套装' },
    { id: 6,  code: 'CAT06', name: '易损小件',     sort: 6,  remark: '雨刮、灯泡、保险丝、火花塞、卡扣' },
    { id: 7,  code: 'CAT07', name: '补胎轮胎小件', sort: 7,  remark: '补胎胶条、气门嘴、平衡块、胎压工具' },
    { id: 8,  code: 'CAT08', name: '车身小件',     sort: 8,  remark: '补漆笔、防锈润滑、反光贴、密封条' },
    { id: 9,  code: 'CAT09', name: '五金辅料',     sort: 9,  remark: '密封胶、生料带、扎带、卡箍、劳保耗材' },
    { id: 10, code: 'CAT10', name: '车用百货',     sort: 10, remark: '车内用品、洗车工具、车载电子小件' },
    { id: 11, code: 'CAT11', name: '洗车养护服务', sort: 11, remark: '洗车、打蜡美容、换油补胎等快修工时' }
  ].map((item) => ({ ...item, parentId: 0, createTime: DT(120 - item.sort) }))

  const products = PROD_SKUS.map((sku, i) => ({
    id: i + 1,
    prodNo: `AP${pad(i + 1)}`,
    name: sku.name,
    categoryName: sku.category,
    brand: sku.brand,
    model: sku.model,
    fitment: sku.fitment,
    spec: '',
    unit: sku.unit,
    price: sku.price,
    cost: sku.cost,
    status: i === 19 ? '停售' : '在售',
    owner: PEOPLE[i % PEOPLE.length],
    remark: '',
    createTime: DT(90 - i * 3)
  }))

  const customerPrices = CUST.slice(0, 10).map((name, i) => {
    const product = products[i % products.length]
    const discount = [0.95, 0.9, 0.85, 0.8, 0.88][i % 5]
    return {
      id: i + 1,
      customerName: name,
      productName: product.name,
      listPrice: product.price,
      discount,
      price: Math.round(product.price * discount),
      effectiveDate: D(60 - i * 4),
      status: i >= 8 ? '已过期' : '生效中',
      owner: PEOPLE[i % PEOPLE.length],
      remark: '',
      createTime: DT(60 - i * 4)
    }
  })

  const contacts = CUST.slice(0, 11).map((name, i) => ({
    id: i + 1,
    custName: name,
    name: CONTACT_NAME[i],
    title: ['采购经理', '店长', '技术主管', '库管', '老板'][i % 5],
    dept: ['采购部', '门店', '技术部', '仓库', '总经办'][i % 5],
    phone: `139${String(10000000 + i * 317).slice(0, 8)}`,
    email: `contact${i + 1}@yipeitong.cn`,
    isPrimary: i % 3 === 0 ? '是' : '否',
    remark: '',
    createTime: DT(70 - i * 3)
  }))

  const followContents = [
    '电话询价玻璃水与清洗剂，对比三家批发价，关注次日达',
    '上门送样，店长试用后认可清洗剂效果，先要 5 箱试销',
    '微信发送报价单与整箱价格表，等待老板确认',
    '客户反映上次刹车清洗剂喷头易堵，已答应换新',
    '补货提醒：三滤套装库存不足，主动报了一单',
    '谈年度框架，要求月结 30 天，价格再下浮 3 个点'
  ]
  const followUps = Array.from({ length: 12 }).map((_, i) => ({
    id: i + 1,
    custName: CUST[i % CUST.length],
    contact: CONTACT_NAME[i % CONTACT_NAME.length],
    type: ['电话', '上门拜访', '微信', '邮件', '会议'][i % 5],
    content: followContents[i % followContents.length],
    result: ['有意向', '需再沟通', '已报价', '已成交', '暂无需求'][i % 5],
    nextTime: D(-(i % 6) - 1),
    owner: PEOPLE[i % PEOPLE.length],
    createTime: DT(20 - i)
  }))

  const seaReasons = ['长期未跟进', '客户主动放弃', '跟进无进展', '负责人离职', '预算冻结']
  const seaCustomers = CUST.slice(0, 9).map((name, i) => ({
    id: i + 1,
    name,
    industry: IND[i % IND.length],
    level: ['重要客户', '普通客户', '潜在客户'][i % 3],
    contact: CONTACT_NAME[i],
    phone: `139${String(10000000 + i * 211).slice(0, 8)}`,
    reason: seaReasons[i % seaReasons.length],
    releaseDate: D(30 - i * 2),
    lastOwner: PEOPLE[i % PEOPLE.length],
    status: i < 7 ? '待领取' : '已领取',
    createTime: DT(30 - i * 2)
  }))

  const quotes = Array.from({ length: 10 }).map((_, i) => {
    const product = products[(i + 3) % products.length]
    const qty = ((i % 5) + 1) * 6
    return {
      id: i + 1,
      quoteNo: `BJ2026${pad(i + 1)}`,
      customerName: CUST[i % CUST.length],
      productName: product.name,
      qty,
      amount: product.price * qty,
      validUntil: D(-(i % 20) - 5),
      status: ['草稿', '已发送', '已通过', '已拒绝'][i % 4],
      owner: PEOPLE[i % PEOPLE.length],
      remark: '',
      createTime: DT(25 - i * 2)
    }
  })

  const returns = Array.from({ length: 8 }).map((_, i) => ({
    id: i + 1,
    returnNo: `TH2026${pad(i + 1)}`,
    orderNo: `DD2026${pad(i + 1)}`,
    customerName: CUST[i % CUST.length],
    type: i % 3 === 0 ? '换货' : '退货',
    reason: ['型号不符', '质量问题', '数量多订', '客户取消'][i % 4],
    amount: (i + 3) * 6800,
    status: ['待审核', '已通过', '已完成', '已拒绝'][i % 4],
    owner: PEOPLE[i % PEOPLE.length],
    createTime: DT(18 - i * 2)
  }))

  const carriers = ['顺丰速运', '德邦物流', '京东物流', '客户自提']
  const shipments = Array.from({ length: 10 }).map((_, i) => ({
    id: i + 1,
    shipNo: `FH2026${pad(i + 1)}`,
    orderNo: `DD2026${pad(i + 1)}`,
    customerName: CUST[i % CUST.length],
    productName: PROD_NAMES[i % PROD_NAMES.length],
    qty: (i % 4) + 1,
    shipDate: D(12 - i),
    carrier: carriers[i % carriers.length],
    trackingNo: `SF${String(88000000000 + i * 12345)}`,
    status: ['待发货', '运输中', '已签收'][i % 3],
    owner: PEOPLE[i % PEOPLE.length],
    createTime: DT(12 - i)
  }))

  const receipts = Array.from({ length: 10 }).map((_, i) => ({
    id: i + 1,
    receiptNo: `SK2026${pad(i + 1)}`,
    orderNo: `DD2026${pad(i + 1)}`,
    customerName: CUST[i % CUST.length],
    amount: (i + 1) * 36000,
    payMethod: ['银行转账', '承兑汇票', '现金', '支付宝'][i % 4],
    payDate: D(10 - i),
    owner: PEOPLE[i % PEOPLE.length],
    remark: i % 3 === 0 ? '首付款' : '尾款',
    createTime: DT(10 - i)
  }))

  const receivables = Array.from({ length: 9 }).map((_, i) => {
    const total = (i + 2) * 46000
    const received = i % 4 === 0 ? total : Math.round(total * [0, 0.3, 0.5, 1][i % 4])
    const due = -(i % 5) * 12 + 20
    return {
      id: i + 1,
      orderNo: `DD2026${pad(i + 1)}`,
      customerName: CUST[i % CUST.length],
      totalAmount: total,
      receivedAmount: received,
      dueDate: D(due),
      owner: PEOPLE[i % PEOPLE.length],
      status: received >= total ? '已结清' : due < 0 ? '已逾期' : '未到期',
      createTime: DT(35 - i * 2)
    }
  })

  const tickets = Array.from({ length: 10 }).map((_, i) => ({
    id: i + 1,
    ticketNo: `GD2026${pad(i + 1)}`,
    customerName: CUST[i % CUST.length],
    title: [
      '登录页面偶发 502',
      '报表导出 Excel 乱码',
      '新员工账号开通咨询',
      '移动端消息推送延迟',
      '发票信息需要变更',
      '数据同步任务失败告警'
    ][i % 6],
    type: ['故障报修', '故障报修', '使用咨询', '需求建议', '投诉'][i % 5],
    priority: ['高', '中', '低'][i % 3],
    status: ['待受理', '处理中', '已解决', '已关闭'][i % 4],
    assignee: PEOPLE[i % PEOPLE.length],
    remark: '',
    createTime: DT(14 - i)
  }))

  const visits = Array.from({ length: 9 }).map((_, i) => ({
    id: i + 1,
    visitNo: `HF2026${pad(i + 1)}`,
    customerName: CUST[i % CUST.length],
    contact: CONTACT_NAME[i % CONTACT_NAME.length],
    visitDate: D(20 - i * 2),
    type: i % 3 === 0 ? '上门回访' : '电话回访',
    satisfaction: [5, 4, 4, 3, 5][i % 5],
    content: [
      '回访配件使用情况，客户反馈供货稳定、装车无异常',
      '续约意向确认，明年计划在城南再开一家分店',
      '收集新需求：希望常用件能次日达',
      '客户对急件配送速度提出改进建议'
    ][i % 4],
    owner: PEOPLE[i % PEOPLE.length],
    createTime: DT(20 - i * 2)
  }))

  return {
    productCategories,
    products,
    customerPrices,
    contacts,
    followUps,
    seaCustomers,
    quotes,
    returns,
    shipments,
    receipts,
    receivables,
    tickets,
    visits
  }
}

export function getExtraDb() {
  if (cache) return cache
  const raw = localStorage.getItem(STORAGE_KEY)
  if (raw) {
    try {
      cache = JSON.parse(raw)
      return cache
    } catch {
      // 数据损坏则重新初始化
    }
  }
  cache = seed()
  persistExtra()
  return cache
}

export function persistExtra() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(cache))
}

export function resetExtraDb() {
  cache = seed()
  persistExtra()
  return cache
}

export function nextExtraId(list) {
  return list.reduce((max, item) => Math.max(max, Number(item.id) || 0), 0) + 1
}

export function makeExtraNo(prefix, list, noField = 'no') {
  const year = new Date().getFullYear()
  const head = `${prefix}${year}`
  const max = list.reduce((acc, item) => {
    const no = String(item[noField] ?? '')
    if (!no.startsWith(head)) return acc
    const seq = Number(no.slice(head.length))
    return Number.isFinite(seq) ? Math.max(acc, seq) : acc
  }, 0)
  return `${head}${pad(max + 1)}`
}
