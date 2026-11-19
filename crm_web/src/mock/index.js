import { getDb, persist, nextId, makeNo } from './db'

/**
 * 本地 Mock 适配器：以 axios adapter 的方式接管请求，
 * 因此拦截器、响应格式与真实接口完全一致。
 */
const delay = (ms = 200) => new Promise((resolve) => setTimeout(resolve, ms))
const ok = (data = null, msg = 'ok') => ({ code: 200, msg, data })
const fail = (msg = '请求失败', code = 500) => ({ code, msg, data: null })

const USER_KEY = 'crm_mock_uid'

function paginate(list, params = {}) {
  const page = Number(params.page) || 1
  const pageSize = Number(params.pageSize) || 10
  const start = (page - 1) * pageSize
  return { list: list.slice(start, start + pageSize), total: list.length }
}

function keywordMatch(row, keyword, fields) {
  if (!keyword) return true
  const kw = String(keyword).trim().toLowerCase()
  if (!kw) return true
  return fields.some((f) => String(row[f] ?? '').toLowerCase().includes(kw))
}

function filterBy(list, params, fields) {
  return list.filter((row) => {
    if (!keywordMatch(row, params.keyword, fields)) return false
    for (const [key, value] of Object.entries(params)) {
      if (['keyword', 'page', 'pageSize', 'sort'].includes(key)) continue
      if (value === undefined || value === null || value === '') continue
      if (String(row[key]) !== String(value)) return false
    }
    return true
  })
}

/** 通用增删改查资源 */
const RESOURCE_MAP = {
  leads: {
    key: 'leads',
    searchFields: ['leadNo', 'name', 'company', 'contact', 'phone'],
    noPrefix: 'XS',
    noField: 'leadNo'
  },
  customers: {
    key: 'customers',
    searchFields: ['custNo', 'name', 'contact', 'phone', 'industry'],
    noPrefix: 'KH',
    noField: 'custNo'
  },
  opportunities: {
    key: 'opportunities',
    searchFields: ['oppNo', 'name', 'customerName', 'owner'],
    noPrefix: 'SJ',
    noField: 'oppNo'
  },
  orders: {
    key: 'orders',
    searchFields: ['orderNo', 'customerName', 'productName', 'owner'],
    noPrefix: 'DD',
    noField: 'orderNo'
  }
}

function crudRoutes(resource) {
  const conf = RESOURCE_MAP[resource]
  return [
    {
      method: 'get',
      path: `/${resource}`,
      handler: ({ params }) => {
        const db = getDb()
        const filtered = filterBy(db[conf.key], params, conf.searchFields)
        return ok(paginate(filtered, params))
      }
    },
    {
      method: 'get',
      path: new RegExp(`^/${resource}/(\\d+)$`),
      handler: ({ match }) => {
        const db = getDb()
        const row = db[conf.key].find((item) => item.id === Number(match[1]))
        return row ? ok(row) : fail('数据不存在', 404)
      }
    },
    {
      method: 'post',
      path: `/${resource}`,
      handler: ({ body }) => {
        const db = getDb()
        const id = nextId(db[conf.key])
        const row = {
          ...body,
          id,
          [conf.noField]: body[conf.noField] || makeNo(conf.noPrefix, db[conf.key], conf.noField),
          createTime: new Date().toLocaleString('zh-CN', { hour12: false })
        }
        db[conf.key].unshift(row)
        persist()
        return ok(row, '新增成功')
      }
    },
    {
      method: 'put',
      path: new RegExp(`^/${resource}/(\\d+)$`),
      handler: ({ match, body }) => {
        const db = getDb()
        const index = db[conf.key].findIndex((item) => item.id === Number(match[1]))
        if (index === -1) return fail('数据不存在', 404)
        db[conf.key][index] = { ...db[conf.key][index], ...body, id: Number(match[1]) }
        persist()
        return ok(db[conf.key][index], '修改成功')
      }
    },
    {
      method: 'delete',
      path: new RegExp(`^/${resource}/(\\d+)$`),
      handler: ({ match }) => {
        const db = getDb()
        db[conf.key] = db[conf.key].filter((item) => item.id !== Number(match[1]))
        persist()
        return ok(null, '删除成功')
      }
    }
  ]
}

function enrichEmployee(db, row) {
  const dept = db.depts.find((d) => d.id === Number(row.deptId))
  const role = db.roles.find((r) => r.id === Number(row.roleId))
  return { ...row, deptName: dept?.deptName || '-', roleName: role?.roleName || '-' }
}

function buildDeptTree(list, parentId = 0) {
  return list
    .filter((item) => Number(item.parentId) === parentId)
    .sort((a, b) => (a.sort || 0) - (b.sort || 0))
    .map((item) => {
      const children = buildDeptTree(list, item.id)
      return children.length ? { ...item, children } : { ...item }
    })
}

const routes = [
  // ---------------- 认证 ----------------
  {
    method: 'post',
    path: '/auth/login',
    handler: ({ body }) => {
      const db = getDb()
      const { empNo, password } = body || {}
      if (!empNo || !password) return fail('请输入员工编号和密码', 400)
      const emp = db.employees.find((item) => item.empNo === String(empNo).toUpperCase())
      if (!emp) return fail('员工编号不存在', 401)
      if (emp.status === 0) return fail('该员工已被停用，请联系管理员', 403)
      // Mock 环境统一密码 123456
      if (password !== '123456') return fail('密码错误，演示密码为 123456', 401)
      localStorage.setItem(USER_KEY, String(emp.id))
      const user = enrichEmployee(db, emp)
      return ok({
        token: `mock-token-${emp.id}-${Date.now()}`,
        user: {
          id: user.id,
          empNo: user.empNo,
          name: user.name,
          deptName: user.deptName,
          roleName: user.roleName,
          roleCode: db.roles.find((r) => r.id === Number(emp.roleId))?.roleCode
        }
      }, '登录成功')
    }
  },
  {
    method: 'post',
    path: '/auth/logout',
    handler: () => {
      localStorage.removeItem(USER_KEY)
      return ok(null, '已退出登录')
    }
  },
  {
    method: 'get',
    path: '/auth/profile',
    handler: () => {
      const db = getDb()
      const id = Number(localStorage.getItem(USER_KEY))
      const emp = db.employees.find((item) => item.id === id)
      if (!emp) return fail('登录状态已失效', 401)
      const user = enrichEmployee(db, emp)
      return ok({
        id: user.id,
        empNo: user.empNo,
        name: user.name,
        deptName: user.deptName,
        roleName: user.roleName
      })
    }
  },

  // ---------------- 员工 ----------------
  {
    method: 'get',
    path: '/system/employees',
    handler: ({ params }) => {
      const db = getDb()
      const filtered = filterBy(
        db.employees.map((row) => enrichEmployee(db, row)),
        params,
        ['empNo', 'name', 'phone', 'email', 'deptName', 'roleName']
      )
      return ok(paginate(filtered, params))
    }
  },
  {
    method: 'post',
    path: '/system/employees',
    handler: ({ body }) => {
      const db = getDb()
      const empNo = String(body.empNo || '').toUpperCase()
      if (db.employees.some((item) => item.empNo === empNo)) return fail('员工编号已存在', 400)
      const row = {
        ...body,
        id: nextId(db.employees),
        empNo,
        createTime: new Date().toLocaleString('zh-CN', { hour12: false })
      }
      db.employees.unshift(row)
      persist()
      return ok(row, '新增成功')
    }
  },
  {
    method: 'put',
    path: /^\/system\/employees\/(\d+)$/,
    handler: ({ match, body }) => {
      const db = getDb()
      const id = Number(match[1])
      const index = db.employees.findIndex((item) => item.id === id)
      if (index === -1) return fail('员工不存在', 404)
      const empNo = String(body.empNo || '').toUpperCase()
      if (db.employees.some((item) => item.empNo === empNo && item.id !== id)) {
        return fail('员工编号已存在', 400)
      }
      db.employees[index] = { ...db.employees[index], ...body, empNo, id }
      persist()
      return ok(db.employees[index], '修改成功')
    }
  },
  {
    method: 'delete',
    path: /^\/system\/employees\/(\d+)$/,
    handler: ({ match }) => {
      const db = getDb()
      const id = Number(match[1])
      if (id === 1) return fail('超级管理员账号不可删除', 400)
      db.employees = db.employees.filter((item) => item.id !== id)
      persist()
      return ok(null, '删除成功')
    }
  },
  {
    method: 'patch',
    path: /^\/system\/employees\/(\d+)\/status$/,
    handler: ({ match, body }) => {
      const db = getDb()
      const row = db.employees.find((item) => item.id === Number(match[1]))
      if (!row) return fail('员工不存在', 404)
      row.status = body.status
      persist()
      return ok(row, '状态已更新')
    }
  },

  // ---------------- 角色 ----------------
  {
    method: 'get',
    path: '/system/roles',
    handler: ({ params }) => {
      const db = getDb()
      const filtered = filterBy(db.roles, params, ['roleCode', 'roleName', 'remark'])
      return ok(paginate(filtered, params))
    }
  },
  {
    method: 'get',
    path: '/system/roles/all',
    handler: () => ok(getDb().roles)
  },
  {
    method: 'post',
    path: '/system/roles',
    handler: ({ body }) => {
      const db = getDb()
      const roleCode = String(body.roleCode || '').toUpperCase()
      if (db.roles.some((item) => item.roleCode === roleCode)) return fail('角色编码已存在', 400)
      const row = {
        ...body,
        id: nextId(db.roles),
        roleCode,
        createTime: new Date().toLocaleString('zh-CN', { hour12: false })
      }
      db.roles.unshift(row)
      persist()
      return ok(row, '新增成功')
    }
  },
  {
    method: 'put',
    path: /^\/system\/roles\/(\d+)$/,
    handler: ({ match, body }) => {
      const db = getDb()
      const id = Number(match[1])
      const index = db.roles.findIndex((item) => item.id === id)
      if (index === -1) return fail('角色不存在', 404)
      const roleCode = String(body.roleCode || '').toUpperCase()
      if (db.roles.some((item) => item.roleCode === roleCode && item.id !== id)) {
        return fail('角色编码已存在', 400)
      }
      db.roles[index] = { ...db.roles[index], ...body, roleCode, id }
      persist()
      return ok(db.roles[index], '修改成功')
    }
  },
  {
    method: 'delete',
    path: /^\/system\/roles\/(\d+)$/,
    handler: ({ match }) => {
      const db = getDb()
      const id = Number(match[1])
      if (db.employees.some((item) => Number(item.roleId) === id)) {
        return fail('该角色下还有员工，无法删除', 400)
      }
      db.roles = db.roles.filter((item) => item.id !== id)
      persist()
      return ok(null, '删除成功')
    }
  },

  // ---------------- 部门 ----------------
  {
    method: 'get',
    path: '/system/depts',
    handler: ({ params }) => ok(buildDeptTree(getDb().depts, Number(params.parentId ?? 0)))
  },
  {
    method: 'get',
    path: '/system/depts/all',
    handler: () => ok(getDb().depts)
  },
  {
    method: 'post',
    path: '/system/depts',
    handler: ({ body }) => {
      const db = getDb()
      const row = {
        ...body,
        id: nextId(db.depts),
        parentId: Number(body.parentId) || 0,
        createTime: new Date().toLocaleString('zh-CN', { hour12: false })
      }
      db.depts.push(row)
      persist()
      return ok(row, '新增成功')
    }
  },
  {
    method: 'put',
    path: /^\/system\/depts\/(\d+)$/,
    handler: ({ match, body }) => {
      const db = getDb()
      const id = Number(match[1])
      const index = db.depts.findIndex((item) => item.id === id)
      if (index === -1) return fail('部门不存在', 404)
      if (Number(body.parentId) === id) return fail('上级部门不能是自己', 400)
      db.depts[index] = { ...db.depts[index], ...body, parentId: Number(body.parentId) || 0, id }
      persist()
      return ok(db.depts[index], '修改成功')
    }
  },
  {
    method: 'delete',
    path: /^\/system\/depts\/(\d+)$/,
    handler: ({ match }) => {
      const db = getDb()
      const id = Number(match[1])
      if (db.depts.some((item) => Number(item.parentId) === id)) {
        return fail('存在下级部门，请先删除下级部门', 400)
      }
      if (db.employees.some((item) => Number(item.deptId) === id)) {
        return fail('该部门下还有员工，无法删除', 400)
      }
      db.depts = db.depts.filter((item) => item.id !== id)
      persist()
      return ok(null, '删除成功')
    }
  }
]

RESOURCE_MAP && ['leads', 'customers', 'opportunities', 'orders'].forEach((key) => routes.push(...crudRoutes(key)))

export default async function mockAdapter(config) {
  await delay(180)

  const method = (config.method || 'get').toLowerCase()
  const rawUrl = `${config.baseURL || ''}${config.url || ''}`
  const path = rawUrl.replace(/\?.*$/, '').replace(/^\/api/, '')
  const params = config.params || {}
  let body = {}
  if (config.data) {
    try {
      body = typeof config.data === 'string' ? JSON.parse(config.data) : config.data
    } catch {
      body = {}
    }
  }

  const route = routes.find((item) => {
    if (item.method !== method) return false
    return typeof item.path === 'string' ? item.path === path : item.path.test(path)
  })

  let payload
  if (!route) {
    payload = fail(`接口未实现：${method.toUpperCase()} ${path}`, 404)
  } else {
    const match = typeof route.path === 'string' ? null : path.match(route.path)
    try {
      payload = route.handler({ match, params, body, path })
    } catch (error) {
      payload = fail(error?.message || 'Mock 服务异常', 500)
    }
  }

  return {
    data: payload,
    status: 200,
    statusText: 'OK',
    headers: {},
    config,
    request: {}
  }
}
