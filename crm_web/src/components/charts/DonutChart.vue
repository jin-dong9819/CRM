<template>
  <div class="donut">
    <svg viewBox="0 0 168 168" class="donut-svg">
      <circle cx="84" cy="84" :r="R" fill="none" stroke="#f1f3f8" :stroke-width="SW" />
      <circle
        v-for="(s, i) in segments"
        :key="i"
        cx="84"
        cy="84"
        :r="R"
        fill="none"
        :stroke="s.color"
        :stroke-width="SW"
        :stroke-dasharray="`${s.len} ${CIRC - s.len}`"
        :stroke-dashoffset="-s.offset"
        transform="rotate(-90 84 84)"
        class="seg"
      >
        <title>{{ s.name }}：{{ s.display }}（{{ s.pct }}）</title>
      </circle>
      <text x="84" y="82" text-anchor="middle" class="center-value" :font-size="centerSize">{{ centerValue }}</text>
      <text x="84" y="101" text-anchor="middle" class="center-label">{{ centerLabel }}</text>
    </svg>

    <ul class="legend">
      <li v-for="(s, i) in segments" :key="i">
        <span class="dot" :style="{ background: s.color }" />
        <span class="name" :title="s.name">{{ s.name }}</span>
        <span class="val">{{ s.display }}</span>
        <span class="pct">{{ s.pct }}</span>
      </li>
      <li v-if="!segments.length" class="empty">暂无数据</li>
    </ul>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** [{ name: '已付款', value: 120000 }] color 可选 */
  items: { type: Array, default: () => [] },
  centerLabel: { type: String, default: '合计' },
  centerValue: { type: String, default: '—' },
  formatter: { type: Function, default: (v) => String(v) }
})

const PALETTE = ['#2f6bff', '#22c08a', '#f5a623', '#8b5cf6', '#f2637b', '#00b8d9', '#94a3b8']
const R = 62
const SW = 18
const CIRC = 2 * Math.PI * R

const total = computed(() => props.items.reduce((sum, item) => sum + (Number(item.value) || 0), 0))

/** 中心数值字号随文本长度收缩，避免溢出环孔 */
const centerSize = computed(() => {
  const len = String(props.centerValue || '').length
  if (len <= 3) return 17
  if (len <= 5) return 15
  return 13
})

const segments = computed(() => {
  const sum = total.value
  if (!sum) return []
  let acc = 0
  return props.items
    .filter((item) => Number(item.value) > 0)
    .map((item, index) => {
      const value = Number(item.value)
      const frac = value / sum
      const len = Math.max(frac * CIRC - 2, 1)
      const offset = acc * CIRC
      acc += frac
      return {
        name: item.name,
        color: item.color || PALETTE[index % PALETTE.length],
        len,
        offset,
        pct: `${(frac * 100).toFixed(1)}%`,
        display: props.formatter(value)
      }
    })
})
</script>

<style scoped>
.donut {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}

.donut-svg {
  width: 138px;
  height: auto;
  align-self: center;
}

.seg {
  transition: opacity 0.2s;
}

.seg:hover {
  opacity: 0.82;
}

.center-value {
  font-weight: 700;
  fill: #1f2733;
}

.center-label {
  font-size: 11px;
  fill: #93a0b0;
}

.legend {
  flex: 1;
  margin: 0;
  padding: 0;
  list-style: none;
  min-width: 0;
  width: 100%;
}

.legend li {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  line-height: 22px;
  color: var(--crm-text-secondary);
}

.legend .dot {
  width: 7px;
  height: 7px;
  border-radius: 2px;
  flex: 0 0 7px;
}

.legend .name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--crm-text);
}

.legend .val {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 12px;
  color: var(--crm-text);
}

.legend .pct {
  width: 40px;
  text-align: right;
  font-size: 11px;
  color: #93a0b0;
}

.legend .empty {
  color: #b6c0cc;
  justify-content: center;
  padding: 16px 0;
}
</style>
