<template>
  <CrudPage v-if="config" :config="config" />
  <div v-else class="page-container">
    <div class="page-card">未找到该模块的配置：{{ key }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import CrudPage from '@/components/CrudPage.vue'
import { businessConfigs } from '@/configs/business'

const route = useRoute()
const key = computed(() => route.meta?.configKey)
const config = computed(() => businessConfigs[key.value])
</script>
