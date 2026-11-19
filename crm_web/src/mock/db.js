/**
 * Mock 数据库：数据存于 localStorage，刷新页面不丢失。
 * 后端接口就绪后，把 .env 里的 VITE_USE_MOCK 改为 false 即可切换真实接口。
 */
const STORAGE_KEY = 'crm_mock_db_v4'
let cache = null

const pad = (n, len = 3) => String(n).padStart(len, '0')
const daysAgo = (n) => {
  const d = new Date(Date.now() - n * 86400000)
  const p = (v) => String(v).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}
const dateTime = (n) => `${daysAgo(n)} ${String(9 + (n % 8)).padStart(2, '0')}:${String((n * 7) % 60).padStart(2, '0')}:00`

function seed() {
  const depts = [
    { id: 1, parentId: 0, deptName: '总经办', leader: '周明远', phone: '13800000001', sort: 1, createTime: dateTime(120) },
    { id: 2, parentId: 0, deptName: '销售中心', leader: '张伟', phone: '13800000002', sort: 2, createTime: dateTime(118) },
    { id: 3, parentId: 2, deptName: '区域销售一部（华东）', leader: '李娜', phone: '13800000003', sort: 1, createTime: dateTime(110) },
    { id: 4, parentId: 2, deptName: '区域销售二部（华南）', leader: '王强', phone: '13800000004', sort: 2, createTime: dateTime(105) },
    { id: 5, parentId: 2, deptName: '渠道批发部', leader: '陈晨', phone: '13800000005', sort: 3, createTime: dateTime(100) },
    { id: 6, parentId: 0, deptName: '电商运营部', leader: '赵雪', phone: '13800000006', sort: 3, createTime: dateTime(98) },
    { id: 7, parentId: 0, deptName: '采购部', leader: '吴磊', phone: '13800000007', sort: 4, createTime: dateTime(96) },
    { id: 8, parentId: 0, deptName: '门店服务部', leader: '刘敏', phone: '13800000008', sort: 5, createTime: dateTime(94) },
    { id: 9, parentId: 0, deptName: '仓储配送部', leader: '郑海', phone: '13800000009', sort: 6, createTime: dateTime(92) },
    { id: 10, parentId: 0, deptName: '财务部', leader: '何静', phone: '13800000010', sort: 7, createTime: dateTime(90) },
    { id: 11, parentId: 0, deptName: '人力行政部', leader: '林芳', phone: '13800000011', sort: 8, createTime: dateTime(88) }
  ]

  const roles = [
    { id: 1, roleCode: 'ADMIN', roleName: '超级管理员', dataScope: '全部数据', remark: '拥有系统全部权限', createTime: dateTime(150) },
    { id: 2, roleCode: 'SALES_MANAGER', roleName: '销售经理', dataScope: '本部门及下级', remark: '管理团队客户与商机', createTime: dateTime(140) },
    { id: 3, roleCode: 'SALES', roleName: '销售专员', dataScope: '仅本人', remark: '跟进自己负责的门店与车主', createTime: dateTime(130) },
    { id: 4, roleCode: 'STORE_SERVICE', roleName: '门店服务', dataScope: '本部门', remark: '负责送货、退换与售后', createTime: dateTime(120) }
  ]

  const employees = [
    { id: 1, empNo: 'E1001', name: '张伟', gender: '男', phone: '13800000001', email: 'zhangwei@yipeitong.cn', deptId: 2, roleId: 1, jobTitle: '销售总监', status: 1, createTime: dateTime(150) },
    { id: 2, empNo: 'E1002', name: '李娜', gender: '女', phone: '13800000002', email: 'lina@yipeitong.cn', deptId: 3, roleId: 2, jobTitle: '区域销售经理（华东）', status: 1, createTime: dateTime(145) },
    { id: 3, empNo: 'E1003', name: '王强', gender: '男', phone: '13800000003', email: 'wangqiang@yipeitong.cn', deptId: 4, roleId: 2, jobTitle: '区域销售经理（华南）', status: 1, createTime: dateTime(140) },
    { id: 4, empNo: 'E1004', name: '刘敏', gender: '女', phone: '13800000004', email: 'liumin@yipeitong.cn', deptId: 8, roleId: 4, jobTitle: '门店服务主管', status: 1, createTime: dateTime(135) },
    { id: 5, empNo: 'E1005', name: '陈晨', gender: '男', phone: '13800000005', email: 'chenchen@yipeitong.cn', deptId: 5, roleId: 3, jobTitle: '渠道批发专员', status: 1, createTime: dateTime(130) },
    { id: 6, empNo: 'E1006', name: '赵雪', gender: '女', phone: '13800000006', email: 'zhaoxue@yipeitong.cn', deptId: 6, roleId: 3, jobTitle: '电商运营专员', status: 1, createTime: dateTime(90) },
    { id: 7, empNo: 'E1007', name: '孙浩', gender: '男', phone: '13800000007', email: 'sunhao@yipeitong.cn', deptId: 3, roleId: 3, jobTitle: '销售专员', status: 0, createTime: dateTime(60) },
    { id: 8, empNo: 'E1008', name: '周琳', gender: '女', phone: '13800000008', email: 'zhoulin@yipeitong.cn', deptId: 8, roleId: 4, jobTitle: '配送调度', status: 1, createTime: dateTime(30) }
  ]

  const leadNames = ['顺达汽修厂', '车之家快修（连锁）', '途顺汽配商行', '宏运汽车维修中心', '华信一汽-大众 4S 店', '安达运输车队', '亮洁汽车美容店', '鑫源快修（3 家门店）', '城市出租汽车公司', '途虎养车工场店（南山）', '车主 王先生', '恒信汽车租赁', '金顺汽修', '小拇指快修（加盟店）', '车主 李女士']
  const leadSources = ['门店拜访', '老客户介绍', '电话询价', '微信咨询', '市场扫街', '线上留资']
  const leadStatus = ['未跟进', '跟进中', '已转化', '已失效']
  const contacts = ['陈立', '林小雅', '黄鹏', '吴静', '徐磊', '马超', '何雨', '高原', '沈悦', '曹阳', '罗敏', '谢峰', '韩雪', '唐磊', '冯丹']
  const leads = leadNames.map((name, i) => ({
    id: i + 1,
    leadNo: `XS2026${pad(i + 1, 4)}`,
    name: `${name}·${['玻璃水询价', '易损件补货', '养护品上架', '保养套装试用'][i % 4]}`,
    company: name,
    contact: contacts[i],
    phone: `139${String(10000000 + i * 137).slice(0, 8)}`,
    source: leadSources[i % leadSources.length],
    level: ['A', 'B', 'C'][i % 3],
    owner: employees[i % 5].name,
    status: leadStatus[i % 4],
    nextFollowTime: daysAgo(-(i % 7) - 1),
    remark: '',
    createTime: dateTime(40 - i * 2)
  }))

  const industries = ['综合修理厂', '连锁快修', '汽配批发', '4S 店', '物流车队', '洗车美容', '汽车租赁', '出租客运', '个人车主']
  const customers = leadNames.slice(0, 12).map((name, i) => ({
    id: i + 1,
    custNo: `KH2026${pad(i + 1, 4)}`,
    name,
    industry: industries[i % industries.length],
    level: ['重要客户', '普通客户', '潜在客户'][i % 3],
    contact: contacts[i],
    phone: `139${String(10000000 + i * 211).slice(0, 8)}`,
    address: ['上海市闵行区七宝汽配城', '杭州市拱墅区汽车城', '深圳市宝安区汽配街', '北京市朝阳区汽配城'][i % 4],
    owner: employees[i % 5].name,
    status: ['合作中', '已成交', '跟进中', '已流失'][i % 4],
    remark: '',
    createTime: dateTime(80 - i * 4)
  }))

  const oppStages = ['初步接洽', '需求确认', '方案报价', '商务谈判', '赢单', '输单']
  const opportunities = Array.from({ length: 12 }).map((_, i) => ({
    id: i + 1,
    oppNo: `SJ2026${pad(i + 1, 4)}`,
    name: `${customers[i % customers.length].name}·${['易损件年度供货', '玻璃水季度采购', '养护品上架试销', '机油三滤年度框架'][i % 4]}`,
    customerId: customers[i % customers.length].id,
    customerName: customers[i % customers.length].name,
    amount: [68000, 12000, 4800, 156000][i % 4] + i * 600,
    stage: oppStages[i % oppStages.length],
    probability: [10, 30, 50, 70, 100, 0][i % 6],
    expectDate: daysAgo(-(i % 30) - 3),
    owner: employees[i % 5].name,
    remark: '',
    createTime: dateTime(60 - i * 3)
  }))

  const products = [
    { name: '玻璃水 -25℃', price: 18 },
    { name: '机油滤清器', price: 35 },
    { name: '化油器清洗剂', price: 18 },
    { name: '防冻液 -35℃', price: 78 },
    { name: '半合成机油 10W-40', price: 158 },
    { name: '无骨雨刮片（对装）', price: 88 },
    { name: '三滤保养套装', price: 128 },
    { name: '标准洗车', price: 25 }
  ]
  const orders = Array.from({ length: 14 }).map((_, i) => {
    const product = products[i % products.length]
    const qty = ((i % 4) + 1) * 6
    return {
      id: i + 1,
      orderNo: `DD2026${pad(i + 1, 4)}`,
      customerId: customers[i % customers.length].id,
      customerName: customers[i % customers.length].name,
      productName: product.name,
      qty,
      amount: product.price * qty,
      payStatus: ['未付款', '部分付款', '已付款'][i % 3],
      status: ['待审核', '已确认', '已发货', '已完成', '已取消'][i % 5],
      orderDate: daysAgo(30 - i),
      owner: employees[i % 5].name,
      remark: '',
      createTime: dateTime(30 - i)
    }
  })

  return { depts, roles, employees, leads, customers, opportunities, orders }
}

export function getDb() {
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
  persist()
  return cache
}

export function persist() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(cache))
}

export function resetDb() {
  cache = seed()
  persist()
  return cache
}

export function nextId(list) {
  return list.reduce((max, item) => Math.max(max, item.id), 0) + 1
}

export function makeNo(prefix, list, noField = 'no') {
  const year = new Date().getFullYear()
  const head = `${prefix}${year}`
  // 取已有编号的最大流水号 + 1，避免删除后编号重复
  const max = list.reduce((acc, item) => {
    const no = String(item[noField] ?? '')
    if (!no.startsWith(head)) return acc
    const seq = Number(no.slice(head.length))
    return Number.isFinite(seq) ? Math.max(acc, seq) : acc
  }, 0)
  return `${head}${pad(max + 1, 4)}`
}
