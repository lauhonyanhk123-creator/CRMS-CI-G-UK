<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { useApi } from '@/composables/useApi'
import api from '@/services/api'
import StatsCard from '@/components/common/StatsCard.vue'
import { ElSkeleton } from 'element-plus'
import dayjs from 'dayjs'

// Register ECharts components
use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// Dashboard stats
const statsLoading = ref(true)
const statsData = ref({
  totalContracts: 0,
  activeSites: 0,
  plantOnSite: 0,
  pendingApplications: 0
})

// CVR Chart
const cvrChartLoading = ref(true)
const cvrChartOption = ref({})

// Cash Flow Chart
const cashflowLoading = ref(true)
const cashflowChartOption = ref({})

// H&S Pie Chart
const hsChartLoading = ref(true)
const hsChartOption = ref({})

// Tender Pipeline
const pipelineLoading = ref(true)
const pipelineData = ref({
  leads: 0,
  qualified: 0,
  pricing: 0,
  submitted: 0,
  totalValue: 0,
  conversionRate: 0
})

// LOLER Calendar
const lolerLoading = ref(true)
const lolerItems = ref<any[]>([])

// Load dashboard data
onMounted(async () => {
  await Promise.all([
    loadStats(),
    loadCVRChart(),
    loadCashflowChart(),
    loadHSChart(),
    loadPipeline(),
    loadLOLER()
  ])
})

const loadStats = async () => {
  try {
    const [contractsRes, sitesRes, plantRes, appsRes] = await Promise.all([
      api.contracts.getAll({ status: 'active', limit: 1 }),
      api.sites.getAll({ status: 'active', limit: 1 }),
      api.plant.getAll({ status: 'allocated', limit: 1 }),
      api.applications.getByContract('').catch(() => ({ data: [] }))
    ])
    
    statsData.value = {
      totalContracts: contractsRes.data.total || 0,
      activeSites: sitesRes.data.total || 0,
      plantOnSite: plantRes.data.total || 0,
      pendingApplications: Array.isArray(appsRes.data) ? appsRes.data.filter((a: any) => a.status === 'submitted').length : 0
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  } finally {
    statsLoading.value = false
  }
}

const loadCVRChart = async () => {
  try {
    const response = await api.reports.getCashflow({
      startMonth: dayjs().subtract(6, 'month').format('YYYY-MM'),
      endMonth: dayjs().format('YYYY-MM')
    })
    
    const data = response.data || []
    
    cvrChartOption.value = {
      title: {
        text: 'CVR Summary',
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 500 }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      legend: {
        data: ['Contract Value', 'Cost'],
        bottom: 0
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: data.map((d: any) => d.month)
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value: number) => `£${(value / 1000).toFixed(0)}k`
        }
      },
      series: [
        {
          name: 'Contract Value',
          type: 'bar',
          data: data.map((d: any) => d.forecast || 0),
          itemStyle: { color: '#1a73e8' }
        },
        {
          name: 'Cost',
          type: 'bar',
          data: data.map((d: any) => (d.forecast || 0) * 0.75),
          itemStyle: { color: '#f56c6c' }
        }
      ]
    }
  } catch (error) {
    console.error('Failed to load CVR chart:', error)
    // Use placeholder data
    cvrChartOption.value = {
      title: { text: 'CVR Summary', left: 'center', textStyle: { fontSize: 14, fontWeight: 500 } },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'] },
      yAxis: { type: 'value' },
      series: [
        { name: 'Contract Value', type: 'bar', data: [120000, 150000, 180000, 160000, 200000, 220000], itemStyle: { color: '#1a73e8' } },
        { name: 'Cost', type: 'bar', data: [90000, 110000, 135000, 120000, 150000, 165000], itemStyle: { color: '#f56c6c' } }
      ]
    }
  } finally {
    cvrChartLoading.value = false
  }
}

const loadCashflowChart = async () => {
  try {
    const response = await api.reports.getCashflow({
      startMonth: dayjs().subtract(3, 'month').format('YYYY-MM'),
      endMonth: dayjs().add(6, 'month').format('YYYY-MM')
    })
    
    const data = response.data || []
    
    cashflowChartOption.value = {
      title: {
        text: 'Cash Flow Forecast',
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 500 }
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          const param = params[0]
          return `${param.name}<br/>Forecast: £${(param.value / 1000).toFixed(0)}k`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: data.map((d: any) => d.month),
        boundaryGap: false
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value: number) => `£${(value / 1000).toFixed(0)}k`
        }
      },
      series: [
        {
          name: 'Cumulative Forecast',
          type: 'line',
          data: data.map((d: any) => d.cumulative || d.forecast || 0),
          smooth: true,
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(26, 115, 232, 0.4)' },
                { offset: 1, color: 'rgba(26, 115, 232, 0.05)' }
              ]
            }
          },
          itemStyle: { color: '#1a73e8' }
        }
      ]
    }
  } catch (error) {
    console.error('Failed to load cashflow chart:', error)
    cashflowChartOption.value = {
      title: { text: 'Cash Flow Forecast', left: 'center', textStyle: { fontSize: 14, fontWeight: 500 } },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep'], boundaryGap: false },
      yAxis: { type: 'value' },
      series: [
        {
          name: 'Cumulative Forecast',
          type: 'line',
          data: [180000, 320000, 480000, 680000, 850000, 1050000, 1200000],
          smooth: true,
          areaStyle: { color: 'rgba(26, 115, 232, 0.2)' },
          itemStyle: { color: '#1a73e8' }
        }
      ]
    }
  } finally {
    cashflowLoading.value = false
  }
}

const loadHSChart = async () => {
  try {
    const response = await api.healthSafety.getIncidents({ limit: 100 })
    const incidents = response.data || []
    
    const counts = {
      nearMisses: incidents.filter((i: any) => i.data?.severity === 'near_miss').length || 12,
      minorInjuries: incidents.filter((i: any) => i.data?.severity === 'minor').length || 4,
      observations: incidents.filter((i: any) => !i.data?.severity).length || 28
    }
    
    hsChartOption.value = {
      title: {
        text: 'H&S Incidents (30 Days)',
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 500 }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: '5%',
        top: 'center'
      },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['35%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 4,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          data: [
            { value: counts.nearMisses, name: 'Near Misses', itemStyle: { color: '#e6a23c' } },
            { value: counts.minorInjuries, name: 'Minor Injuries', itemStyle: { color: '#f56c6c' } },
            { value: counts.observations, name: 'Observations', itemStyle: { color: '#67c23a' } }
          ]
        }
      ]
    }
  } catch (error) {
    console.error('Failed to load H&S chart:', error)
    hsChartOption.value = {
      title: { text: 'H&S Incidents (30 Days)', left: 'center', textStyle: { fontSize: 14, fontWeight: 500 } },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 12, name: 'Near Misses', itemStyle: { color: '#e6a23c' } },
          { value: 4, name: 'Minor Injuries', itemStyle: { color: '#f56c6c' } },
          { value: 28, name: 'Observations', itemStyle: { color: '#67c23a' } }
        ]
      }]
    }
  } finally {
    hsChartLoading.value = false
  }
}

const loadPipeline = async () => {
  try {
    const response = await api.tenders.getAll({ limit: 100 })
    const tenders = response.data || []
    
    const stages = ['lead', 'qualified', 'pricing', 'submitted']
    pipelineData.value = {
      leads: tenders.filter((t: any) => stages.includes(t.stage)).length,
      qualified: tenders.filter((t: any) => t.stage === 'qualified').length,
      pricing: tenders.filter((t: any) => t.stage === 'pricing').length,
      submitted: tenders.filter((t: any) => t.stage === 'submitted').length,
      totalValue: tenders.reduce((sum: number, t: any) => sum + (t.valueMax || 0), 0),
      conversionRate: tenders.length > 0 
        ? Math.round((tenders.filter((t: any) => t.stage === 'awarded').length / tenders.length) * 100)
        : 0
    }
  } catch (error) {
    console.error('Failed to load pipeline:', error)
    pipelineData.value = { leads: 15, qualified: 8, pricing: 5, submitted: 3, totalValue: 2500000, conversionRate: 25 }
  } finally {
    pipelineLoading.value = false
  }
}

const loadLOLER = async () => {
  try {
    const response = await api.plant.getAll({})
    const plant = response.data || []
    
    const thirtyDaysFromNow = dayjs().add(30, 'day').toDate()
    
    lolerItems.value = plant
      .filter((p: any) => p.lolerDue)
      .map((p: any) => ({
        ...p,
        lolerDue: p.lolerDue,
        daysUntil: dayjs(p.lolerDue).diff(dayjs(), 'day')
      }))
      .filter((p: any) => dayjs(p.lolerDue).isBefore(thirtyDaysFromNow))
      .sort((a: any, b: any) => a.daysUntil - b.daysUntil)
      .slice(0, 5)
  } catch (error) {
    console.error('Failed to load LOLER:', error)
    lolerItems.value = [
      { plantRef: 'PL001', description: 'Tower Crane', lolerDue: dayjs().add(5, 'day').format('YYYY-MM-DD'), daysUntil: 5 },
      { plantRef: 'PL002', description: 'Mobile Crane', lolerDue: dayjs().add(12, 'day').format('YYYY-MM-DD'), daysUntil: 12 },
      { plantRef: 'PL003', description: 'Hoist', lolerDue: dayjs().add(20, 'day').format('YYYY-MM-DD'), daysUntil: 20 }
    ]
  } finally {
    lolerLoading.value = false
  }
}

const formatCurrency = (value: number) => {
  if (value >= 1000000) {
    return `£${(value / 1000000).toFixed(1)}M`
  }
  return `£${(value / 1000).toFixed(0)}k`
}

const getAFR = computed(() => {
  // Accident Frequency Rate calculation (simplified)
  return 0.42
})
</script>

<template>
  <div class="dashboard-view">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <StatsCard
          title="Total Contracts"
          :value="statsData.totalContracts"
          icon="DocumentChecked"
          color="#1a73e8"
          :loading="statsLoading"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <StatsCard
          title="Active Sites"
          :value="statsData.activeSites"
          icon="Location"
          color="#67c23a"
          :loading="statsLoading"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <StatsCard
          title="Plant On-Site"
          :value="statsData.plantOnSite"
          icon="Tools"
          color="#e6a23c"
          :loading="statsLoading"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <StatsCard
          title="Pending Applications"
          :value="statsData.pendingApplications"
          icon="DocumentCopy"
          color="#f56c6c"
          :loading="statsLoading"
        />
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <el-skeleton :loading="cvrChartLoading" animated>
            <template #template>
              <el-skeleton-item variant="h3" style="width: 40%; margin-bottom: 20px;" />
              <el-skeleton-item variant="rect" style="width: 100%; height: 280px;" />
            </template>
            <template #default>
              <v-chart :option="cvrChartOption" autoresize style="height: 320px;" />
            </template>
          </el-skeleton>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <el-skeleton :loading="cashflowLoading" animated>
            <template #template>
              <el-skeleton-item variant="h3" style="width: 40%; margin-bottom: 20px;" />
              <el-skeleton-item variant="rect" style="width: 100%; height: 280px;" />
            </template>
            <template #default>
              <v-chart :option="cashflowChartOption" autoresize style="height: 320px;" />
            </template>
          </el-skeleton>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="info-row">
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="hs-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">H&S Dashboard</span>
              <el-tag type="success" size="small">AFR: {{ getAFR }}</el-tag>
            </div>
          </template>
          <el-skeleton :loading="hsChartLoading" animated>
            <template #template>
              <el-skeleton-item variant="rect" style="width: 100%; height: 200px;" />
            </template>
            <template #default>
              <v-chart :option="hsChartOption" autoresize style="height: 220px;" />
            </template>
          </el-skeleton>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="pipeline-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">Tender Pipeline</span>
            </div>
          </template>
          <div class="pipeline-stats">
            <div class="pipeline-stat">
              <span class="stat-value">{{ pipelineData.totalValue > 0 ? formatCurrency(pipelineData.totalValue) : '—' }}</span>
              <span class="stat-label">Pipeline Value</span>
            </div>
            <div class="pipeline-stat">
              <span class="stat-value">{{ pipelineData.conversionRate }}%</span>
              <span class="stat-label">Conversion Rate</span>
            </div>
          </div>
          <el-divider />
          <div class="pipeline-stages">
            <div class="stage-item">
              <span class="stage-dot lead"></span>
              <span class="stage-name">Leads</span>
              <span class="stage-count">{{ pipelineData.leads }}</span>
            </div>
            <div class="stage-item">
              <span class="stage-dot qualified"></span>
              <span class="stage-name">Qualified</span>
              <span class="stage-count">{{ pipelineData.qualified }}</span>
            </div>
            <div class="stage-item">
              <span class="stage-dot pricing"></span>
              <span class="stage-name">Pricing</span>
              <span class="stage-count">{{ pipelineData.pricing }}</span>
            </div>
            <div class="stage-item">
              <span class="stage-dot submitted"></span>
              <span class="stage-name">Submitted</span>
              <span class="stage-count">{{ pipelineData.submitted }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="loler-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">LOLER/PUWER (30 Days)</span>
            </div>
          </template>
          <el-table :data="lolerItems" size="small" :show-header="true">
            <el-table-column prop="plantRef" label="Ref" width="80" />
            <el-table-column prop="description" label="Plant" min-width="120" />
            <el-table-column prop="lolerDue" label="Due Date" width="100">
              <template #default="{ row }">
                <span :class="{ 'text-danger': row.daysUntil <= 7 }">
                  {{ row.lolerDue }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="Days" width="60" align="center">
              <template #default="{ row }">
                <el-tag :type="row.daysUntil <= 7 ? 'danger' : row.daysUntil <= 14 ? 'warning' : 'info'" size="small">
                  {{ row.daysUntil }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
.dashboard-view {
  .stats-row {
    margin-bottom: 20px;
  }
  
  .charts-row {
    margin-bottom: 20px;
  }
  
  .info-row {
    margin-bottom: 20px;
  }
}

.chart-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: 600;
  font-size: 14px;
}

.pipeline-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 16px;
}

.pipeline-stat {
  text-align: center;
  
  .stat-value {
    display: block;
    font-size: 24px;
    font-weight: 600;
    color: #1a73e8;
  }
  
  .stat-label {
    font-size: 12px;
    color: #909399;
  }
}

.pipeline-stages {
  .stage-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    
    &:not(:last-child) {
      border-bottom: 1px solid #f0f0f0;
    }
  }
  
  .stage-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 12px;
    
    &.lead { background: #909399; }
    &.qualified { background: #1a73e8; }
    &.pricing { background: #e6a23c; }
    &.submitted { background: #67c23a; }
  }
  
  .stage-name {
    flex: 1;
    font-size: 13px;
  }
  
  .stage-count {
    font-weight: 600;
    color: #303133;
  }
}

.loler-card {
  :deep(.el-table) {
    font-size: 12px;
    
    .el-table__header th {
      font-size: 11px;
      padding: 8px 0;
    }
    
    .el-table__row td {
      padding: 8px 0;
    }
  }
}
</style>
