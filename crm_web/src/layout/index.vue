<template>
  <div class="app-wrapper" :class="{ 'sidebar-collapsed': appStore.collapsed }">
    <Sidebar class="sidebar-container" />
    <div class="main-container">
      <Navbar />
      <AppMain />
    </div>
  </div>
</template>

<script setup>
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import AppMain from './components/AppMain.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.sidebar-container {
  flex: 0 0 220px;
  width: 220px;
  transition: width 0.24s ease, flex-basis 0.24s ease;
}

.main-container {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>

<style>
/* 折叠态需要穿透到子组件 */
.app-wrapper.sidebar-collapsed .sidebar-container {
  flex-basis: 64px;
  width: 64px;
}
</style>
