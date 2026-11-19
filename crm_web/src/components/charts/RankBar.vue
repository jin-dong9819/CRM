<template>
  <ul class="rank">
    <li v-for="(item, i) in rows" :key="item.name + i">
      <div class="row">
        <span class="idx" :class="{ top: i < 3 }">{{ i + 1 }}</span>
        <span class="name" :title="item.name">{{ item.name }}</span>
        <span v-if="item.extra" class="extra">{{ item.extra }}</span>
        <span class="value">{{ item.display }}</span>
      </div>
      <div class="track">
        <div class="fill" :style="{ width: item.width, background: item.color }" />
      </div>
    </li>
    <li v-if="!rows.length" class="empty">暂无数据</li>
  </ul>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** [{ name: '张伟', value: 120000, extra: '3 笔', color: '#2f6bff' }] */
  items: { type: Array, default: () => [] },
  formatter: { type: Function, default: (v) => String(v) },
  /** 统一色；不传则前三位高亮，其余为浅色 */
  color: { type: String, default: '' }
})

const rows = computed(() => {
  const max = Math.max(0, ...props.items.map((item) => Number(item.value) || 0))
  return props.items.map((item, i) => {
    const value = Number(item.value) || 0
    const width = max > 0 ? Math.max((value / max) * 100, value > 0 ? 3 : 0) : 0
    return {
      ...item,
      value,
      width: `${width}%`,
      display: props.formatter(value),
      color: item.color || props.color || (i < 3 ? '#2f6bff' : '#9fb7ff')
    }
  })
})
</script>

<style scoped>
.rank {
  margin: 0;
  padding: 0;
  list-style: none;
}

.rank li {
  margin-bottom: 10px;
}

.rank li:last-child {
  margin-bottom: 2px;
}

.row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  margin-bottom: 4px;
}

.idx {
  width: 16px;
  height: 16px;
  flex: 0 0 16px;
  border-radius: 4px;
  background: #f1f3f8;
  color: #93a0b0;
  font-size: 10px;
  line-height: 16px;
  text-align: center;
}

.idx.top {
  background: #eaf0ff;
  color: var(--crm-primary);
  font-weight: 600;
}

.name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--crm-text);
}

.extra {
  color: #a3aebd;
  font-size: 11px;
}

.value {
  font-family: 'SFMono-Regular', Consolas, monospace;
  color: var(--crm-text);
}

.track {
  height: 6px;
  border-radius: 3px;
  background: #f4f6fa;
  overflow: hidden;
}

.fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.4s ease;
}

.empty {
  color: #b6c0cc;
  font-size: 12px;
  text-align: center;
  padding: 24px 0;
}
</style>
