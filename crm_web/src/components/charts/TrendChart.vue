<template>
  <div class="trend">
    <div v-if="!hasData" class="trend-empty">所选区间暂无数据</div>
    <svg v-else :viewBox="`0 0 ${W} ${H}`" class="trend-svg" preserveAspectRatio="xMidYMid meet">
      <defs>
        <linearGradient :id="gid" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" :stop-color="color" stop-opacity="0.26" />
          <stop offset="100%" :stop-color="color" stop-opacity="0.02" />
        </linearGradient>
      </defs>

      <!-- 网格线 + Y 轴刻度 -->
      <g>
        <line
          v-for="(g, i) in grid"
          :key="`grid-${i}`"
          :x1="PAD.l"
          :x2="W - PAD.r"
          :y1="g.y"
          :y2="g.y"
          stroke="#eef1f6"
          stroke-width="1"
        />
        <text
          v-for="(g, i) in grid"
          :key="`tick-${i}`"
          :x="PAD.l - 10"
          :y="g.y + 4"
          text-anchor="end"
          class="axis"
        >
          {{ g.label }}
        </text>
      </g>

      <path v-if="areaPath" :d="areaPath" :fill="`url(#${gid})`" />
      <path
        v-if="linePath"
        :d="linePath"
        fill="none"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      />

      <g v-for="(p, i) in points" :key="`pt-${i}`">
        <circle :cx="p.x" :cy="p.y" r="10" fill="transparent">
          <title>{{ p.label }}：{{ p.text }}</title>
        </circle>
        <circle :cx="p.x" :cy="p.y" r="3.5" :fill="color" stroke="#fff" stroke-width="1.5" />
        <text :x="p.x" :y="H - 10" text-anchor="middle" class="axis">{{ p.label }}</text>
      </g>
    </svg>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** [{ label: '1月', value: 120000 }] */
  data: { type: Array, default: () => [] },
  color: { type: String, default: '#2f6bff' },
  /** 悬浮提示里的单位后缀 */
  unit: { type: String, default: '' }
})

const W = 1000
const H = 292
const PAD = { l: 62, r: 20, t: 18, b: 34 }
const gid = `trend-${Math.random().toString(36).slice(2, 8)}`

const hasData = computed(() => props.data.some((item) => (Number(item.value) || 0) > 0))

/** Y 轴最大值：留 15% 余量后取「好看」的刻度 */
const niceMax = computed(() => {
  const max = Math.max(0, ...props.data.map((d) => Number(d.value) || 0))
  if (max <= 0) return 1
  const raw = max * 1.15
  const mag = Math.pow(10, Math.floor(Math.log10(raw)))
  const steps = [1, 1.2, 1.5, 2, 2.5, 3, 4, 5, 6, 8, 10]
  return (steps.find((s) => s * mag >= raw) || 10) * mag
})

const fmt = (v) => {
  if (v >= 10000) {
    const n = v / 10000
    return `${n.toFixed(n >= 100 ? 0 : 1).replace(/\.0$/, '')}万`
  }
  return String(Math.round(v))
}

const ROWS = 4
const grid = computed(() => {
  const innerH = H - PAD.t - PAD.b
  return Array.from({ length: ROWS + 1 }, (_, i) => ({
    y: PAD.t + (innerH / ROWS) * i,
    label: fmt((niceMax.value / ROWS) * (ROWS - i))
  }))
})

const points = computed(() => {
  const innerW = W - PAD.l - PAD.r
  const innerH = H - PAD.t - PAD.b
  const n = props.data.length
  return props.data.map((d, i) => {
    const value = Number(d.value) || 0
    const x = n <= 1 ? PAD.l + innerW / 2 : PAD.l + (innerW / (n - 1)) * i
    const y = PAD.t + innerH - (value / niceMax.value) * innerH
    return {
      x,
      y,
      label: d.label,
      text: `${value.toLocaleString('zh-CN')}${props.unit}`
    }
  })
})

const linePath = computed(() => {
  const pts = points.value
  if (!pts.length) return ''
  return pts.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ')
})

const areaPath = computed(() => {
  const pts = points.value
  if (!pts.length) return ''
  const bottom = H - PAD.b
  const first = pts[0]
  const last = pts[pts.length - 1]
  return `${linePath.value} L${last.x.toFixed(1)},${bottom} L${first.x.toFixed(1)},${bottom} Z`
})
</script>

<style scoped>
.trend {
  width: 100%;
}

.trend-svg {
  width: 100%;
  height: auto;
  display: block;
  overflow: visible;
}

.axis {
  font-size: 11px;
  fill: #93a0b0;
}

.trend-empty {
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #b6c0cc;
  font-size: 12px;
}
</style>
