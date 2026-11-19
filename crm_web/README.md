# CRM 客户管理系统 · 前端

基于 **Vue 3 + Vite + Element Plus** 的 CRM 后台前端，采用前后端分离架构，内置本地 Mock 数据，后端接口就绪后一键切换。

## 技术栈

| 能力 | 选型 |
| --- | --- |
| 框架 | Vue 3.5（`<script setup>` 组合式 API） |
| 构建 | Vite 6 |
| UI | Element Plus 2.9 + `@element-plus/icons-vue` |
| 路由 | Vue Router 4（History 模式 + 全局登录守卫） |
| 状态 | Pinia 2 |
| 请求 | Axios（统一响应拦截、Token 注入） |

## 快速开始

先启动后端（Spring Boot，端口 8080，依赖 MySQL 库 `crm`）：

```bash
cd ../crm_backend   # 后端工程目录
mvn spring-boot:run
```

再启动前端：

```bash
cd crm_web
npm install
npm run dev      # 开发环境，默认 http://localhost:5173
npm run build    # 生产构建，产物在 dist/
```

演示账号：员工编号 `E1001`，密码 `123456`（登录页有「一键填入」）。E1002 ~ E1006 密码一致。

> 若启动后端时报 `Port 11948 was already in use`，是宿主环境注入了 `SERVER__PORT` 变量。用
> `env -u SERVER__PORT mvn spring-boot:run` 启动即可，或在 IDEA 里跑 `DemoApplication`。

## 功能范围

**登录**
- 员工编号 + 密码登录，前端表单校验（编号格式、密码长度）
- Token 与用户信息持久化，刷新不掉线
- 记住员工编号、路由守卫拦截未登录访问、退出登录二次确认
- 登录后按 `redirect` 参数回到原访问页面

**后台布局**
- 左侧菜单两级展示：线索、客户、商机、订单、设置（员工管理 / 角色管理 / 部门管理）
- 菜单由路由表 `meta` 自动生成，新增页面只需在 `src/router/index.js` 加一条路由
- 侧边栏可折叠（记忆折叠状态）、顶部面包屑、用户下拉（个人信息 / 退出登录）

**业务模块**（均为「搜索 + 列表 + 分页 + 新增 / 编辑弹窗 + 删除确认」）
- 线索：关键词 / 意向等级 / 状态 / 来源筛选，意向与状态彩色标签（接口待开发）
- 客户：关键词 / 等级 / 行业 / 状态筛选，名称列副行展示行业（接口待开发）
- 商机：阶段 / 赢率进度条、金额千分位、当前页金额合计（接口待开发）
- 订单：付款状态与订单状态双标签、金额合计（接口待开发）
- 员工：部门 / 状态筛选，**角色多选**，状态开关即时生效，默认密码提示
- 角色：角色编码唯一校验、数据范围、**分配权限（树形勾选）**、删除前校验是否仍被员工引用
- 部门：组织架构树形表格，支持新增下级、展开 / 折叠全部、移动部门自动维护祖先链

## 目录结构

```
crm_web/
├── .env.development        # 开发环境变量（VITE_USE_MOCK / 代理地址）
├── .env.production
├── vite.config.js          # @ 别名、/api 代理到 Spring Boot
└── src/
    ├── api/                # 接口层：request.js 统一封装 + 各模块接口
    ├── composables/        # useTable：分页/搜索/加载状态复用
    ├── layout/             # 布局：Sidebar / Navbar / Breadcrumb / AppMain
    ├── mock/               # Mock 适配器与本地数据（localStorage 持久化）
    ├── router/             # 路由表（菜单来源）与登录守卫
    ├── stores/             # Pinia：user（登录态）、app（侧边栏）
    ├── styles/             # 全局样式与主题变量
    ├── utils/dict.js       # 业务字典、标签颜色、金额格式化
    └── views/              # login / lead / customer / opportunity / order / system
```

## 与后端对接

开发环境已默认 `VITE_USE_MOCK=false`，直接调用 Spring Boot 的 `/api/**`。设置模块（员工 / 角色 / 部门 / 权限）
与登录鉴权已全部实现；线索、客户、商机、订单四个业务模块的接口尚未开发，页面会提示请求失败。

若想脱离后端单独调页面，把 `.env.development` 中的 `VITE_USE_MOCK` 改回 `true`（内置 Mock 数据仍在 `src/mock/`）。

对接要点：

1. `VITE_API_TARGET` 指向后端地址，Vite 把 `/api/**` 代理过去，避免跨域；
2. 后端响应统一 `{ code, msg, data }`，`code = 200` 表示成功；`401/403` 自动清理登录态并跳回登录页；
3. 除 `/api/auth/login` 外，所有接口需带 `Authorization: Bearer <token>`。

### 接口约定

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 入参 `{ empNo, password }`，返回 `{ token, user }` |
| POST | `/api/auth/logout` | 退出登录 |
| GET | `/api/auth/profile` | 当前登录员工（含角色与权限标识） |
| GET | `/api/system/employees` | 员工分页，筛选 `keyword`、`deptId`（含下级）、`status` |
| POST / PUT / DELETE | `/api/system/employees[/{id}]` | 员工增改删，入参含 `roleIds`（多角色） |
| PATCH | `/api/system/employees/{id}/status` | 启用 / 停用，入参 `{ status }` |
| PUT | `/api/system/employees/{id}/password` | 重置密码，不传则重置为 123456 |
| GET / POST / PUT / DELETE | `/api/system/roles[/{id}]` | 角色增改删，数据范围 `dataScope` 为数字 |
| GET | `/api/system/roles/all` | 角色下拉 |
| GET / PUT | `/api/system/roles/{id}/permissions` | 查询 / 保存角色权限（`{ permissionIds }`） |
| GET | `/api/system/permissions/tree` | 全量权限树（目录 / 菜单 / 按钮） |
| GET | `/api/system/depts` | 部门树（`children` 嵌套） |
| GET | `/api/system/depts/all` | 部门平铺列表（下拉用） |
| POST / PUT / DELETE | `/api/system/depts[/{id}]` | 部门增改删，移动部门自动维护 `ancestors` |
| GET / POST / PUT / DELETE | `/api/leads`、`/api/customers`、`/api/opportunities`、`/api/orders` | 待开发 |

分页响应统一为 `{ list: [], total: 0 }`。

## 说明

- 演示数据保存在浏览器 `localStorage`（key：`crm_mock_db_v1`），要恢复初始数据可在控制台执行
  `localStorage.removeItem('crm_mock_db_v1')` 后刷新。
- 角色权限目前只做了角色与数据范围的维护，按钮级权限（v-permission 指令）尚未接入，可在 `stores/user.js`
  拿到 `roleCode` 后按需扩展。
