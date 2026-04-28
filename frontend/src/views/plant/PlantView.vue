<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import api, { type PlantItem, type SiteAllocation } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import dayjs from 'dayjs'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent])

const loading = ref(false)
const tableData = ref<PlantItem[]>([])
const ganttData = ref<SiteAllocation[]>([])
const ganttLoading = ref(true)
const ganttOption = ref({})
const lolerItems = ref<any[]>([])

onMounted(() => { loadData(); loadGantt(); loadLOLER() })

const loadData = async () => {
  loading.value = true
  try {
    const response = await api.plant.getAll({})
    tableData.value = response.data.data
  } catch { ElMessage.error('Failed to load plant') } finally { loading.value = false }
}

const loadGantt = async () => {
  ganttLoading.value = true
  try {
    const response = await api.plant.getPlantGantt({})
    ganttData.value = response.data.allocations
    updateGanttChart()
  } catch {
    ganttData.value = []
    updateGanttChart()
  } finally { ganttLoading.value = false }
}

const loadLOLER = async () => {
  try {
    const thirtyDays = dayjs().add(30, 'day').toDate()
    lolerItems.value = tableData.value
      .filter(p => p.lolerDue)
      .map(p => ({ ...p, daysUntil: dayjs(p.lolerDue).diff(dayjs(), 'day') }))
      .filter(p => dayjs(p.lolerDue).isBefore(thirtyDays))
      .sort((a, b) => a.daysUntil - b.daysUntil)
      .slice(0, 10)
  } catch { lolerItems.value = [] }
}

const updateGanttChart = () => {
  if (!ganttData.value.length) {
    ganttOption.value = { title: { text: 'Plant Gantt', left: 'center' }, xAxis: { type: 'time' }, yAxis: { type: 'category', data: [] }, series: [{ type: 'bar' }] }
    return
  }

  const categories = [...new Set(ganttData.value.map(d => d.site?.name || 'Unassigned'))]
  ganttOption.value = {
    title: { text: 'Plant Allocation Timeline', left: 'center', textStyle: { fontSize: 14, fontWeight: 500 } },
    tooltip: { trigger: 'axis', formatter: (params: any) => `${params[0].name}<br/>${params[0].seriesName}: ${dayjs(params[0].value[0]).format('DD MMM')} - ${dayjs(params[0].value[1]).format('DD MMM')}` },
    grid: { left: '3%', right: '4%', bottom: '10%', top: '15%', containLabel: true },
    xAxis: { type: 'time', axisLabel: { formatter: (value: number) => dayjs(value).format('DD MMM') } },
    yAxis: { type: 'category', data: categories, axisLabel: { fontSize: 11 } },
    series: [{
      type: 'bar',
      barWidth: 20,
      data: ganttData.value.map(d => ({
        name: d.site?.name || 'Unassigned',
        value: [new Date(d.startDate).getTime(), d.endDate ? new Date(d.endDate).getTime() : Date.now(), d.plantId]
      })),
      itemStyle: { color: '#1a73e8', borderRadius: [4, 4, 0, 0] }
    }]
  }
}

const getCategoryType = (category: string) => {
  const map: Record<string, string> = { mechanical: '', electrical: 'success', specialist: 'warning', vehicle: 'info', tool: '' }
  return map[category] || ''
}

const getLOLERStatus = (dueDate?: string) => {
  if (!dueDate) return 'none'
  const days = dayjs(dueDate).diff(dayjs(), 'day')
  if (days < 0) return 'overdue'
  if (days <= 14) return 'critical'
  if (days <= 30) return 'warning'
  return 'ok'
}
</script>

<template>
  <div class="plant-view">
    <PageHeader title="Plant" :breadcrumbs="[{ title: 'Plant' }]">
      <template #actions>
        <el-button :icon="Refresh" @click="loadData">Refresh</el-button>
        <el-button type="primary" :icon="Plus">Add Plant</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="mb-4">
      <template #header><span>Plant Register</span></template>
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="plantRef" label="Ref" width="100" />
        <el-table-column prop="description" label="Description" min-width="180" />
        <el-table-column label="Make/Model" width="150">
          <template #default="{ row }">{{ [row.make, row.model].filter(Boolean).join(' ') || '—' }}</template>
        </el-table-column>
        <el-table-column label="Category" width="120">
          <template #default="{ row }"><el-tag :type="getCategoryType(row.category)" size="small">{{ row.category }}</el-tag></template>
        </el-table-column>
        <el-table-column label="Status" width="120">
          <template #default="{ row }"><StatusBadge :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="LOLER Due" width="120">
          <template #default="{ row }">
            <span v-if="row.lolerDue" :class="{ 'text-danger': getLOLERStatus(row.lolerDue) === 'overdue' }">{{ row.lolerDue }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="100" fixed="right">
          <template #default><el-button link type="primary" size="small">View</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="mb-4">
      <template #header><span>Plant Gantt Chart</span></template>
      <el-skeleton :loading="ganttLoading" animated>
        <template #template>
          <el-skeleton-item variant="rect" style="width: 100%; height: 300px;" />
        </template>
        <template #default>
          <v-chart :option="ganttOption" autoresize style="height: 300px;" />
        </template>
      </el-skeleton>
    </el-card>

    <el-card shadow="never">
      <template #header><span>LOLER/PUWER Calendar (Next 30 Days)</span></template>
      <el-table :data="lolerItems" stripe size="small">
        <el-table-column prop="plantRef" label="Ref" width="80" />
        <el-table-column prop="description" label="Plant" min-width="150" />
        <el-table-column prop="lolerDue" label="LOLER Due" width="120" />
        <el-table-column label="Days" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getLOLERStatus(row.lolerDue) === 'overdue' ? 'danger' : getLOLERStatus(row.lolerDue) === 'critical' ? 'danger' : 'warning'" size="small">{{ row.daysUntil }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="puwerDue" label="PUWER Due" width="120" />
        <el-table-column label="Status" width="100">
          <template #default="{ row }"><StatusBadge :status="row.status" /></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.plant-view { .mb-4 { margin-bottom: 16px; } }
</style>
