<script setup lang="ts">
import { computed } from 'vue'
import { TrendCharts } from '@element-plus/icons-vue'
import type { Component } from 'vue'

// Inline trend icons since TrendUp/TrendDown may not be in @element-plus/icons-vue
const TrendUp = { template: '<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M534 192H192v256h85.4l-107.6 148.6L384 810.6l224-224V512h85.4L534 192z"/></svg>' }
const TrendDown = { template: '<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path fill="currentColor" d="M534 832H192v-256h85.4l-107.6-148.6L384 213.4l224 224V512h85.4L534 832z"/></svg>' }

interface Props {
  title: string
  value: string | number
  icon?: Component | string
  trend?: 'up' | 'down' | 'neutral'
  trendValue?: string
  color?: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  trend: 'neutral',
  color: '#1a73e8',
  loading: false
})

const trendIcon = computed(() => {
  switch (props.trend) {
    case 'up': return TrendUp
    case 'down': return TrendDown
    default: return null
  }
})

const trendClass = computed(() => {
  switch (props.trend) {
    case 'up': return 'trend-up'
    case 'down': return 'trend-down'
    default: return 'trend-neutral'
  }
})

const gradientStyle = computed(() => ({
  background: `linear-gradient(135deg, ${props.color} 0%, ${props.color}dd 100%)`
}))
</script>

<template>
  <el-card v-loading="loading" shadow="never" class="stats-card">
    <div class="stats-content">
      <div class="stats-icon" :style="gradientStyle">
        <el-icon :size="24" color="#fff">
          <component :is="icon" v-if="typeof icon === 'object'" />
          <span v-else>{{ icon }}</span>
        </el-icon>
      </div>
      <div class="stats-info">
        <span class="stats-title">{{ title }}</span>
        <span class="stats-value">{{ value }}</span>
        <div v-if="trendValue" :class="['stats-trend', trendClass]">
          <el-icon v-if="trendIcon" :size="14"><component :is="trendIcon" /></el-icon>
          <span>{{ trendValue }}</span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<style lang="scss" scoped>
.stats-card {
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
  &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
  :deep(.el-card__body) { padding: 20px; }
}
.stats-content { display: flex; align-items: flex-start; gap: 16px; }
.stats-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stats-info { display: flex; flex-direction: column; flex: 1; min-width: 0; }
.stats-title { font-size: 13px; color: #909399; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.stats-value { font-size: 24px; font-weight: 600; color: #303133; line-height: 1.2; }
.stats-trend { display: flex; align-items: center; gap: 4px; font-size: 12px; margin-top: 4px; }
.stats-trend.trend-up { color: #67c23a; }
.stats-trend.trend-down { color: #f56c6c; }
.stats-trend.trend-neutral { color: #909399; }
</style>
