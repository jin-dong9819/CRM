<template>
  <el-breadcrumb separator="/" class="breadcrumb">
    <el-breadcrumb-item v-for="(item, index) in items" :key="index">
      <span :class="{ current: index === items.length - 1 }">{{ item }}</span>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const items = computed(() => {
  const list = route.matched
    .filter((item) => item.meta?.title)
    .map((item) => item.meta.title)
  return list.length ? list : ['首页']
})
</script>

<style scoped>
.breadcrumb {
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: var(--crm-text-secondary);
  font-weight: 400;
}

.breadcrumb .current {
  color: var(--crm-text);
  font-weight: 600;
}
</style>
