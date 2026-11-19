import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import Layout from '@/layout/index.vue'
import { useUserStore } from '@/stores/user'

const HOME_PATH = '/dashboard/overview'

/**
 * 路由表：菜单由这里的 meta 自动生成
 * meta.title   菜单名称
 * meta.icon    Element Plus 图标组件名
 * meta.hidden  是否在菜单中隐藏
 * meta.configKey 该页使用 configs/business.js 中的哪套配置（通用增删改查页）
 */
export const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册', hidden: true }
  },

  {
    path: '/',
    redirect: HOME_PATH,
    meta: { hidden: true }
  },

  /* ---------------- 工作台 ---------------- */
  {
    path: '/dashboard',
    component: Layout,
    redirect: '/dashboard/overview',
    meta: { title: '工作台', icon: 'HomeFilled' },
    children: [
      {
        path: 'overview',
        name: 'Overview',
        component: () => import('@/views/workbench/overview.vue'),
        meta: { title: '经营概览', icon: 'Odometer' }
      },
      {
        path: 'todo',
        name: 'Todo',
        component: () => import('@/views/workbench/todo.vue'),
        meta: { title: '我的待办', icon: 'AlarmClock' }
      }
    ]
  },

  /* ---------------- 客户管理 ---------------- */
  {
    path: '/customer',
    component: Layout,
    redirect: '/customer/lead',
    meta: { title: '客户管理', icon: 'User' },
    children: [
      {
        path: 'lead',
        name: 'Lead',
        component: () => import('@/views/lead/index.vue'),
        meta: { title: '销售线索', icon: 'Aim' }
      },
      {
        path: 'sea',
        name: 'CustomerSea',
        component: () => import('@/views/customer/sea.vue'),
        meta: { title: '客户公海', icon: 'Files' }
      },
      {
        path: 'archive',
        name: 'Customer',
        component: () => import('@/views/customer/index.vue'),
        meta: { title: '客户档案', icon: 'Postcard' }
      },
      {
        path: 'contact',
        name: 'Contact',
        component: () => import('@/views/business/index.vue'),
        meta: { title: '联系人', configKey: 'contacts' }
      },
      {
        path: 'follow',
        name: 'FollowUp',
        component: () => import('@/views/business/index.vue'),
        meta: { title: '跟进记录', configKey: 'followUps' }
      }
    ]
  },

  /* ---------------- 产品管理 ---------------- */
  {
    path: '/product',
    component: Layout,
    redirect: '/product/category',
    meta: { title: '产品管理', icon: 'Goods' },
    children: [
      {
        path: 'category',
        name: 'ProductCategory',
        component: () => import('@/views/product/category.vue'),
        meta: { title: '产品分类' }
      },
      {
        path: 'archive',
        name: 'ProductArchive',
        component: () => import('@/views/product/archive.vue'),
        meta: { title: '产品档案' }
      },
      {
        path: 'price',
        name: 'CustomerPrice',
        component: () => import('@/views/business/index.vue'),
        meta: { title: '客户价格', configKey: 'customerPrices' }
      }
    ]
  },

  /* ---------------- 销售管理 ---------------- */
  {
    path: '/sales',
    component: Layout,
    redirect: '/sales/opportunity',
    meta: { title: '销售管理', icon: 'TrendCharts' },
    children: [
      {
        path: 'opportunity',
        name: 'Opportunity',
        component: () => import('@/views/opportunity/index.vue'),
        meta: { title: '销售商机', icon: 'Histogram' }
      },
      {
        path: 'quote',
        name: 'Quote',
        component: () => import('@/views/business/index.vue'),
        meta: { title: '报价单', configKey: 'quotes' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '销售订单', icon: 'Tickets' }
      },
      {
        path: 'return',
        name: 'ReturnOrder',
        component: () => import('@/views/business/index.vue'),
        meta: { title: '退换货单', configKey: 'returns' }
      }
    ]
  },

  /* ---------------- 交付与回款 ---------------- */
  {
    path: '/delivery',
    component: Layout,
    redirect: '/delivery/shipment',
    meta: { title: '交付与回款', icon: 'Van' },
    children: [
      {
        path: 'shipment',
        name: 'Shipment',
        component: () => import('@/views/business/index.vue'),
        meta: { title: '发货记录', configKey: 'shipments' }
      },
      {
        path: 'receipt',
        name: 'Receipt',
        component: () => import('@/views/business/index.vue'),
        meta: { title: '收款记录', configKey: 'receipts' }
      },
      {
        path: 'receivable',
        name: 'Receivable',
        component: () => import('@/views/delivery/receivable.vue'),
        meta: { title: '应收账款' }
      }
    ]
  },

  /* ---------------- 售后管理 ---------------- */
  {
    path: '/service',
    component: Layout,
    redirect: '/service/ticket',
    meta: { title: '售后管理', icon: 'Service' },
    children: [
      {
        path: 'ticket',
        name: 'Ticket',
        component: () => import('@/views/business/index.vue'),
        meta: { title: '售后工单', configKey: 'tickets' }
      },
      {
        path: 'visit',
        name: 'Visit',
        component: () => import('@/views/business/index.vue'),
        meta: { title: '客户回访', configKey: 'visits' }
      }
    ]
  },

  /* ---------------- 数据分析 ---------------- */
  {
    path: '/analysis',
    component: Layout,
    redirect: '/analysis/sales',
    meta: { title: '数据分析', icon: 'DataAnalysis' },
    children: [
      {
        path: 'sales',
        name: 'Analysis',
        component: () => import('@/views/analysis/index.vue'),
        meta: { title: '销售统计', icon: 'PieChart' }
      },
      {
        path: 'customer',
        name: 'CustomerAnalysis',
        component: () => import('@/views/analysis/customer.vue'),
        meta: { title: '客户采购分析' }
      },
      {
        path: 'product',
        name: 'ProductAnalysis',
        component: () => import('@/views/analysis/product.vue'),
        meta: { title: '产品销售分析' }
      }
    ]
  },

  /* ---------------- 设置 ---------------- */
  {
    path: '/system',
    component: Layout,
    redirect: '/system/employee',
    meta: { title: '设置', icon: 'Setting' },
    children: [
      {
        path: 'employee',
        name: 'Employee',
        component: () => import('@/views/system/employee/index.vue'),
        meta: { title: '员工管理', icon: 'Avatar' }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'Key' }
      },
      {
        path: 'dept',
        name: 'Dept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'permission',
        name: 'Permission',
        component: () => import('@/views/system/permission/index.vue'),
        meta: { title: '权限管理', icon: 'Lock' }
      }
    ]
  },

  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

const WHITE_LIST = ['/login', '/register']

router.beforeEach((to, from, next) => {
  NProgress.start()
  document.title = to.meta?.title ? `${to.meta.title} · 易配通汽配 CRM` : '易配通汽配 CRM'

  const userStore = useUserStore()

  if (WHITE_LIST.includes(to.path)) {
    // 已登录再访问登录页，直接回到首页
    if (userStore.token) return next(HOME_PATH)
    return next()
  }

  if (!userStore.token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
