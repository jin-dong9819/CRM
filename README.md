# 易配通 CRM · 汽配经销商客户管理系统

面向汽车易损件 / 养护用品批发零售企业的客户关系管理系统。覆盖**线索 → 客户 → 商机 → 报价 → 订单 → 发货 → 回款 → 售后**的完整业务闭环，配套员工、角色、部门、权限的精细化管控与经营数据分析。

前后端分离：Spring Boot 提供 REST 接口，Vue 3 单页应用承载界面。

---

## 目录

- [功能模块](#功能模块)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [演示账号](#演示账号)
- [接口约定](#接口约定)
- [配置说明](#配置说明)
- [部署](#部署)
- [开发约定](#开发约定)
- [已知限制](#已知限制)

---

## 功能模块

### 工作台
| 页面 | 说明 |
| --- | --- |
| 经营概览 | 销售趋势、客户分布、产品排名等核心指标，含环形图 / 柱状图 / 折线图 |
| 我的待办 | 待跟进客户、待处理工单、临期应收款聚合 |

### 客户管理
| 页面 | 后端接口 | 状态 |
| --- | --- | --- |
| 销售线索 | `/api/leads` | ✅ 已实现 |
| 客户档案 | `/api/customers` | ✅ 已实现 |
| 客户公海、联系人、跟进记录 | — | 🚧 页面占位（配置驱动，待接入接口） |

### 产品管理
| 页面 | 后端接口 | 状态 |
| --- | --- | --- |
| 产品分类 | `/api/product-categories` | ✅ 已实现 |
| 产品档案 | `/api/products` | ✅ 已实现 |
| 客户价格 | — | 🚧 页面占位 |

### 销售管理
| 页面 | 后端接口 | 状态 |
| --- | --- | --- |
| 销售商机 | `/api/opportunities` | ✅ 已实现 |
| 销售订单 | `/api/orders` | ✅ 已实现 |
| 报价单、退换货单 | — | 🚧 页面占位 |

### 交付与回款 / 售后管理
发货记录、收款记录、应收账款、售后工单、客户回访 —— 均为 🚧 页面占位，前端已按「搜索 + 列表 + 分页 + 弹窗增改删」结构搭好，接口待开发。

### 数据分析
销售统计、客户采购分析、产品销售分析，图表组件已封装（`DonutChart` / `RankBar` / `TrendChart`）。

### 系统设置
| 能力 | 说明 |
| --- | --- |
| 员工管理 | 分页筛选、多角色分配、启停开关、密码重置 |
| 角色管理 | 角色编码唯一校验、数据范围、**树形勾选分配权限**、删除前校验引用 |
| 部门管理 | 组织架构树形表格，支持新增下级、移动部门自动维护祖先链 |
| 权限管理 | 目录 / 菜单 / 按钮三级权限树维护 |

### 认证
- 员工编号 + 密码登录（BCrypt 校验），Token 有效期 8 小时
- 手机号注册 + 短信验证码登录（注册用户与员工为两套独立主体）
- 路由守卫拦截未登录访问，登录后按 `redirect` 参数回跳原页面

---

## 技术栈

### 后端 `crm_backend`
| 能力 | 选型 | 版本 |
| --- | --- | --- |
| 运行时 | JDK | 17 |
| 框架 | Spring Boot | 3.5.16 |
| 持久层 | MyBatis（XML 映射 + 驼峰自动转换） | 3.0.5 |
| 数据库 | MySQL | 8.0+ |
| 密码加密 | spring-security-crypto（仅 BCrypt，未引入完整 Security） | — |
| 参数校验 | spring-boot-starter-validation | — |
| 其他 | Lombok | — |

> **不使用 Redis、不使用 Docker。** Token 与短信验证码均为单机内存存储（`TokenStore` / `SmsCodeStore`），重启即失效。需要恢复 Redis 时，取消 `pom.xml` 与 `application.yml` 中对应注释即可，业务代码无需改动。

### 前端 `crm_web`
| 能力 | 选型 |
| --- | --- |
| 框架 | Vue 3.5（`<script setup>` 组合式 API） |
| 构建 | Vite 6 |
| UI | Element Plus 2.9 + `@element-plus/icons-vue` |
| 路由 | Vue Router 4（History 模式 + 全局守卫） |
| 状态 | Pinia 2 |
| 请求 | Axios（统一响应拦截、Token 注入） |

---

## 项目结构

```
springboot-test/
├── crm_backend/                 # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/demo/
│       │   ├── common/          # Result 统一响应、PageResult 分页、异常处理
│       │   ├── config/          # WebConfig（拦截器 / CORS / BCrypt）、JacksonConfig
│       │   ├── controller/      # 11 个 REST 控制器
│       │   ├── dto/ vo/         # 入参与视图对象
│       │   ├── entity/          # 13 个实体
│       │   ├── interceptor/     # AuthInterceptor：Bearer Token 校验
│       │   ├── mapper/          # MyBatis Mapper 接口
│       │   ├── service/         # 业务层 + impl
│       │   └── util/            # TokenStore、SmsCodeStore、密码工具、短信客户端
│       └── resources/
│           ├── application.yml  # 开发配置（MySQL / MyBatis / 短信 / 日志）
│           └── mapper/*.xml     # 10 个 SQL 映射文件
├── crm_web/                     # Vue 3 前端
│   ├── vite.config.js           # @ 别名、/api 代理到 8080
│   ├── .env.development
│   ├── .env.production
│   └── src/
│       ├── api/                 # request.js 统一封装 + auth/system/business
│       ├── components/          # CrudPage 通用页、charts 图表组件
│       ├── composables/         # useTable：分页 / 搜索 / loading 复用
│       ├── configs/             # 配置驱动的业务页面定义
│       ├── layout/              # Sidebar / Navbar / Breadcrumb / AppMain
│       ├── router/              # 路由表（菜单来源）与登录守卫
│       ├── stores/              # Pinia：user / app
│       └── views/               # 21 个页面组件
├── db/                          # 数据库脚本（见下方执行顺序）
├── deploy/                      # 生产部署：DEPLOY.md / systemd / nginx / prod 配置
└── README.md
```

---

## 环境要求

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 17 | Spring Boot 3.5 最低要求 |
| Maven | 3.8+ | 仅本地打包需要 |
| MySQL | 8.0+ | 库名 `crm`，字符集 `utf8mb4` |
| Node.js | 18+ | 前端构建 |

---

## 快速开始

### 1. 初始化数据库

在 `db/` 目录下**按顺序**执行（脚本内含 `CREATE DATABASE` 与 `USE`，无需手工建库）：

```bash
cd db

mysql -u root -p < crm_schema.sql          # 系统表：部门/角色/员工/权限 + 演示数据
mysql -u root -p < sys_user.sql            # 注册用户表（手机号登录用）
mysql -u root -p < crm_business.sql        # 业务表：线索/客户/商机/订单
mysql -u root -p < crm_mfg_data.sql        # 产品分类/产品 + 汽配基础数据
mysql -u root -p < demo_account.sql        # 事务演示账户表（可选）
mysql -u root -p < upgrade_menu_tree.sql   # 增量：重建菜单树
mysql -u root -p < upgrade_permission_menu.sql
mysql -u root -p < upgrade_analysis_menu.sql
```

`upgrade_*.sql` 为可重复执行的增量脚本，仅用于已建库的菜单 / 权限补充。

### 2. 启动后端

修改 `crm_backend/src/main/resources/application.yml` 中的数据库账号密码，然后：

```bash
cd crm_backend
mvn spring-boot:run
```

服务监听 `http://localhost:8080`。

> 若报 `Port xxxxx was already in use`，通常是宿主环境注入了 `SERVER__PORT` 变量覆盖了配置。
> 用 `env -u SERVER__PORT mvn spring-boot:run` 启动，或在 IDEA 中直接运行 `DemoApplication`。
> `pom.xml` 中已用 `-Dserver.port=8080` 的 JVM 参数兜底（优先级高于环境变量）。

### 3. 启动前端

```bash
cd crm_web
npm install
npm run dev      # http://localhost:5173，已配置 /api 代理到 8080
npm run build    # 生产构建，产物在 dist/
```

前端默认直连真实后端（`VITE_USE_MOCK=false`）。若想脱离后端单独调试页面，把 `.env.development` 中的
`VITE_USE_MOCK` 改为 `true`，内置 Mock 数据位于 `crm_web/src/mock/`。

---

## 演示账号

| 类型 | 账号 | 密码 |
| --- | --- | --- |
| 员工 | `E1001` ~ `E1006` | `123456` |
| 注册用户 | 手机号（短信验证码登录） | 自行设置 |

登录页提供「一键填入」按钮。员工权限取决于所属角色的权限树配置。

---

## 接口约定

**统一响应体**

```json
{ "code": 200, "msg": "ok", "data": {} }
```

`code = 200` 为成功；`401` 登录失效（前端自动清理登录态跳登录页）；`403` 权限不足。
分页响应的 `data` 固定为 `{ "list": [], "total": 0 }`。

**鉴权**

除下列白名单外，所有 `/api/**` 接口必须携带请求头：

```
Authorization: Bearer <token>
```

白名单：`/api/auth/login`、`/api/auth/login/phone`、`/api/auth/register`、`/api/auth/send-code`。

**主要接口**

| 模块 | 路径前缀 |
| --- | --- |
| 认证 | `/api/auth/*`（login / login/phone / register / send-code / profile / logout） |
| 线索 | `/api/leads` |
| 客户 | `/api/customers` |
| 商机 | `/api/opportunities` |
| 订单 | `/api/orders` |
| 产品 | `/api/products`、`/api/product-categories` |
| 员工 | `/api/system/employees` |
| 角色 | `/api/system/roles` |
| 部门 | `/api/system/depts` |
| 权限 | `/api/system/permissions` |

业务模块统一为 RESTful 风格：`GET`（分页查询）、`POST`（新增）、`PUT /{id}`（修改）、`DELETE /{id}`（删除）。
系统模块额外提供 `PATCH /{id}/status` 启停、`GET /{id}/permissions` 权限查询等。

---

## 配置说明

| 配置项 | 位置 | 说明 |
| --- | --- | --- |
| 数据库连接 | `application.yml` → `spring.datasource` | 开发默认 `localhost:3306/crm`，账号 `root/root` |
| 服务端口 | `application.yml` → `server.port` | 开发 8080；生产 8000，仅监听 127.0.0.1 |
| 短信开发模式 | `application.yml` → `sms.dev-mode` | `true` 时不真实发送，验证码打印到日志。**上线前必须设为 false** |
| 容联云短信 | `application.yml` → `ronglian.sms` | 通过环境变量 `RONG_ACCOUNT_SID` 等覆盖，避免硬编码进仓库 |
| Token 有效期 | `TokenStore.EXPIRE_HOURS` | 默认 8 小时 |

> ⚠️ 仓库中的 `deploy/application-prod.yml` 含明文数据库密码，仅作模板参考。正式环境请改用
> systemd 的 `EnvironmentFile`（`/opt/crm/app/crm.env`，权限 600）注入，切勿提交真实凭据。

---

## 部署

生产方案为 **Ubuntu 22.04 裸机部署，不使用 Docker**：

```
浏览器 → Nginx(80) → 静态 dist + /api 反代 → 127.0.0.1:8000 (Spring Boot) → MySQL(3306)
```

完整步骤见 **[`deploy/DEPLOY.md`](deploy/DEPLOY.md)**，配套文件：

| 文件 | 用途 |
| --- | --- |
| `deploy/DEPLOY.md` | 从零部署手册（环境、建库、systemd、Nginx、HTTPS） |
| `deploy/crm.service` | systemd 单元，JVM 参数与日志重定向 |
| `deploy/nginx-crm.conf` | Nginx 站点配置（含 Vue history 路由兜底、`/api` 反代） |
| `deploy/application-prod.yml` | 生产 profile 模板 |

关键点：`proxy_pass http://127.0.0.1:8000;` **末尾不能带斜杠**，否则 `/api` 前缀会被剥掉导致后端 404。

---

## 开发约定

- **统一响应**：控制器一律返回 `Result<T>`，分页用 `PageResult<T>`，异常由 `GlobalExceptionHandler` 兜底。
- **分层**：Controller 只做参数校验与编排；业务逻辑放 Service；SQL 写在 `resources/mapper/*.xml`。
- **命名**：数据库下划线、Java 驼峰，由 `map-underscore-to-camel-case` 自动转换。
- **逻辑删除**：业务表用 `del_flag` 标记，不建物理外键，关联完整性由业务层保证。
- **密码**：只存 BCrypt 密文，禁止任何形式的明文落库或日志输出。
- **前端菜单**：由 `src/router/index.js` 的 `meta` 自动生成，新增页面只需加一条路由。
- **新增通用业务页**：在 `src/configs/business.js` 增加配置项即可套用 `CrudPage.vue`，无需重复写列表逻辑。

---

## 已知限制

| 问题 | 影响 | 后续方案 |
| --- | --- | --- |
| Token 存内存 | 后端重启后所有用户需重新登录；多实例无法共享 | 接入 Redis 替换 `TokenStore` 实现 |
| 验证码存内存 | 同上，且单机限流 | 接入 Redis + 频控 |
| 按钮级权限未接入 | 前端未做 `v-permission` 指令，仅菜单级可见性 | 基于 `user.permissions` 扩展指令 |
| 部分业务页面为占位 | 公海、联系人、跟进、报价、发货、售后等无后端接口 | 按 `crm_business.sql` 表结构续接 |
| 生产配置明文密码 | 见上方安全提示 | 改用环境变量注入 |
