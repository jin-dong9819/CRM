<template>
  <main class="app-main">
    <el-scrollbar>
      <router-view v-slot="{ Component, route }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </el-scrollbar>
  </main>
</template>

<script setup>
// 内容区：滚动由内部的 el-scrollbar 承担，切换路由带过渡动画
</script>

<style scoped>
.app-main {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  background: var(--crm-bg);
}

.app-main :deep(.el-scrollbar__view) {
  min-height: 100%;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
