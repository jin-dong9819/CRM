<template>
  <aside class="sidebar" :class="{ 'is-collapsed': appStore.collapsed }">
    <div class="logo">
      <div class="logo-mark">易</div>
      <transition name="fade">
        <div v-show="!appStore.collapsed" class="logo-text">
          <span class="title">易配通</span>
          <span class="sub">汽配客户管理</span>
        </div>
      </transition>
    </div>

    <el-scrollbar class="menu-scroll">
      <el-menu
        ref="menuRef"
        :default-active="activeMenu"
        :collapse="appStore.collapsed"
        :collapse-transition="false"
        router
        background-color="#ffffff"
        text-color="#4a5568"
        active-text-color="#ffffff"
      >
        <template v-for="item in menus" :key="item.path">
          <!-- 一级菜单：无子级 -->
          <el-menu-item v-if="!item.children" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>

          <!-- 一级菜单：含子级（两级菜单） -->
          <el-sub-menu v-else :index="item.path">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
              <template #title>{{ child.title }}</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-scrollbar>

    <div class="sidebar-footer" v-show="!appStore.collapsed">v1.0.0 · 已连接后端</div>
  </aside>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { routes } from '@/router'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const route = useRoute()

const joinPath = (parent, child) => {
  if (!parent || parent === '/') return `/${child}`
  return `${parent.replace(/\/$/, '')}/${child}`
}

/**
 * 由路由表生成两级菜单：
 * 一级带 meta.title 且有子路由 -> 折叠分组（如「设置」）
 * 一级无 meta.title -> 其子路由直接作为一级菜单（如线索 / 客户 / 商机 / 订单）
 */
const menus = computed(() => {
  const result = []
  routes
    .filter((r) => !r.meta?.hidden && Array.isArray(r.children))
    .forEach((r) => {
      const children = r.children
        .filter((c) => !c.meta?.hidden)
        .map((c) => ({
          path: joinPath(r.path, c.path),
          title: c.meta?.title,
          icon: c.meta?.icon || 'Menu'
        }))

      if (r.meta?.title) {
        result.push({
          path: r.path,
          title: r.meta.title,
          icon: r.meta.icon || 'Setting',
          children
        })
      } else {
        result.push(...children)
      }
    })
  return result
})

const activeMenu = computed(() => route.meta?.activeMenu || route.path)

/** 当前路由所属的分组 path，用于切换页面时自动展开对应分组 */
const activeGroupPath = computed(() => {
  const group = menus.value.find(
    (item) => item.children?.some((child) => child.path === activeMenu.value)
  )
  return group ? group.path : null
})

const menuRef = ref()

/** 路由变化后自动展开当前分组；不收起其他分组，因此支持多个分组同时展开 */
watch(activeGroupPath, (path) => {
  if (!path) return
  nextTick(() => {
    menuRef.value?.open?.(path)
  })
})
</script>

<style scoped>
.sidebar {
  height: 100%;
  width: 220px;
  box-sizing: border-box;
  background: #ffffff;
  border-right: 1px solid #e9edf3;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.24s ease;
}

.sidebar.is-collapsed {
  width: 64px;
}

.logo {
  height: 56px;
  flex: 0 0 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid #eef1f5;
  overflow: hidden;
}

.logo-mark {
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  border-radius: 8px;
  background: linear-gradient(135deg, #2f6bff, #5b8cff);
  color: #fff;
  font-weight: 700;
  font-size: 17px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(47, 107, 255, 0.35);
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
  white-space: nowrap;
}

.logo-text .title {
  color: #1f2a3a;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
}

.logo-text .sub {
  color: #8a97a8;
  font-size: 11px;
  margin-top: 3px;
}

.menu-scroll {
  flex: 1;
  padding: 8px 0;
}

.sidebar :deep(.el-menu) {
  border-right: none;
}

.sidebar :deep(.el-menu-item),
.sidebar :deep(.el-sub-menu__title) {
  height: 42px;
  line-height: 42px;
  margin: 2px 10px;
  border-radius: 6px;
  font-size: 13.5px;
}

.sidebar.is-collapsed :deep(.el-menu-item),
.sidebar.is-collapsed :deep(.el-sub-menu__title) {
  margin: 2px 6px;
}

/* 浅色底：展开的分组标题不能再用白色（active-text-color 是给选中项的白字） */
.sidebar :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  color: #1f2a3a !important;
}

/* 当前页面所在的分组：用主题蓝区分 */
.sidebar :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #2f6bff !important;
  font-weight: 600;
}

.sidebar :deep(.el-menu-item:hover),
.sidebar :deep(.el-sub-menu__title:hover) {
  background-color: #eef2f9 !important;
  color: #1f2a3a !important;
}

.sidebar :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #2f6bff, #4b7dff) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(47, 107, 255, 0.22);
}

/* 二级菜单：缩进更深 + 左侧圆点做层级引导 */
.sidebar :deep(.el-sub-menu .el-menu) {
  padding: 2px 0;
}

.sidebar :deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  height: 38px;
  line-height: 38px;
  margin: 1px 10px 1px 20px;
  padding-left: 24px !important;
  font-size: 13px;
  position: relative;
}

.sidebar :deep(.el-sub-menu .el-menu-item)::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 50%;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #c3ccd8;
  transform: translateY(-50%);
  transition: background 0.18s;
}

.sidebar :deep(.el-sub-menu .el-menu-item:hover)::before {
  background: #8a97a8;
}

.sidebar :deep(.el-sub-menu .el-menu-item.is-active)::before {
  background: #fff;
}

.sidebar-footer {
  flex: 0 0 34px;
  height: 34px;
  line-height: 34px;
  text-align: center;
  color: #98a2b3;
  font-size: 11px;
  border-top: 1px solid #eef1f5;
  white-space: nowrap;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
