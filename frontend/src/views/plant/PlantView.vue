<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import api, { type PlantItem } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import dayjs from 'dayjs'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent])

const loading = ref(false)
const tableData = ref<PlantItem[]>([])
const ganttData = ref<any[]>([])
const ganttLoading = ref(true)
const ganttOption = ref({})
const lolerItems = ref<any[]>([])

// Dialog state
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const formData = ref({
  id: '',
  description: '',
  category: 'EXCAVATOR' as PlantItem['category'],
  make: '',
  model: '',
  serialNumber: '',
  hireRate: undefined as number | undefined,
  status: 'AVAILABLE' as PlantItem['status'],
  insuranceExpiry: '',
  nextLOLER: '',
  nextPUWER: '',
  notes: ''
})
const saving = ref(false)

const categoryOptions = [
  { value: 'EXCAVATOR', label: 'Excavator' },
  { value: 'DUMPER', label: 'Dumper' },
  { value: 'ROLLER', label: 'Roller' },
  { value: 'PLANT_MIXER', label: 'Plant Mixer' },
  { value: 'CONCRETE_PUMP', label: 'Concrete Pump' },
  { value: 'TELEHANDLER', label: 'Telehandler' },
  { value: 'CRANE', label: 'Crane' },
  { value: 'ACCESS_EQUIPMENT', label: 'Access Equipment' },
  { value: 'OTHER', label: 'Other' }
]

const statusOptions = [
  { value: 'AVAILABLE', label: 'Available' },
  { value: 'HIRED_OUT', label: 'Hired Out' },
  { value: 'MAINTENANCE', label: 'Maintenance' },
  { value: 'DECOMMISSIONED', label: 'Decommissioned' }
]

onMounted(() => { loadData(); loadGantt(); loadLOLER() })

const loadData = async () => {
  loading.value = true
  try {
    const response = await api.plant.getAll({})
    tableData.value = response.data.data
    loadLOLER()
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
      .filter(p => p.nextLOLER)
      .map(p => ({ ...p, daysUntil: dayjs(p.nextLOLER).diff(dayjs(), 'day') }))
      .filter(p => dayjs(p.nextLOLER).isBefore(thirtyDays))
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
  const map: Record<string, string> = {
    EXCAVATOR: 'primary',
    DUMPER: 'success',
    ROLLER: 'warning',
    PLANT_MIXER: 'info',
    CONCRETE_PUMP: 'danger',
    TELEHANDLER: '',
    CRANE: 'warning',
    ACCESS_EQUIPMENT: 'info',
    OTHER: ''
  }
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

// CRUD handlers
const resetForm = () => {
  formData.value = {
    id: '',
    description: '',
    category: 'EXCAVATOR',
    make: '',
    model: '',
    serialNumber: '',
    hireRate: undefined,
    status: 'AVAILABLE',
    insuranceExpiry: '',
    nextLOLER: '',
    nextPUWER: '',
    notes: ''
  }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: PlantItem) => {
  dialogMode.value = 'edit'
  formData.value = {
    id: row.id,
    description: row.description,
    category: row.category,
    make: row.make || '',
    model: row.model || '',
    serialNumber: row.serialNumber || '',
    hireRate: row.hireRate,
    status: row.status,
    insuranceExpiry: row.insuranceExpiry || '',
    nextLOLER: row.nextLOLER || '',
    nextPUWER: row.nextPUWER || '',
    notes: row.notes || ''
  }
  dialogVisible.value = true
}

const handleDelete = async (row: PlantItem) => {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to delete "${row.description}"? This action cannot be undone.`,
      'Delete Plant',
      { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning' }
    )
    await api.plant.delete(row.id)
    ElMessage.success('Plant deleted successfully')
    loadData()
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || 'Failed to delete plant')
    }
  }
}

const handleSave = async () => {
  if (!formData.value.description) {
    ElMessage.warning('Description is required')
    return
  }
  
  saving.value = true
  try {
    const payload = {
      description: formData.value.description,
      category: formData.value.category,
      make: formData.value.make || undefined,
      model: formData.value.model || undefined,
      serialNumber: formData.value.serialNumber || undefined,
      hireRate: formData.value.hireRate,
      status: formData.value.status,
      insuranceExpiry: formData.value.insuranceExpiry || undefined,
      nextLOLER: formData.value.nextLOLER || undefined,
      nextPUWER: formData.value.nextPUWER || undefined,
      notes: formData.value.notes || undefined
    }
    
    if (dialogMode.value === 'edit') {
      await api.plant.update(formData.value.id, payload)
      ElMessage.success('Plant updated successfully')
    } else {
      await api.plant.create(payload)
      ElMessage.success('Plant created successfully')
    }
    dialogVisible.value = false
    loadData()
  } catch (err: any) {
    ElMessage.error(err.message || 'Failed to save plant')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="plant-view">
    <PageHeader title="Plant" :breadcrumbs="[{ title: 'Plant' }]">
      <template #actions>
        <el-button :icon="Refresh" @click="loadData">Refresh</el-button>
        <el-button type="primary" :icon="Plus" @click="handleAdd">Add Plant</el-button>
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
        <el-table-column label="Category" width="130">
          <template #default="{ row }"><el-tag :type="getCategoryType(row.category)" size="small">{{ row.category.replace('_', ' ') }}</el-tag></template>
        </el-table-column>
        <el-table-column label="Status" width="120">
          <template #default="{ row }"><StatusBadge :status="row.status" /></template>
        </el-table-column>
        <el-table-column prop="hireRate" label="Hire Rate" width="100" align="right">
          <template #default="{ row }">{{ row.hireRate ? `£${row.hireRate.toFixed(2)}` : '—' }}</template>
        </el-table-column>
        <el-table-column label="LOLER Due" width="120">
          <template #default="{ row }">
            <span v-if="row.nextLOLER" :class="{ 'text-danger': getLOLERStatus(row.nextLOLER) === 'overdue' }">{{ row.nextLOLER }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" size="small" @click="handleEdit(row)">Edit</el-button>
            <el-button link type="danger" :icon="Delete" size="small" @click="handleDelete(row)">Delete</el-button>
          </template>
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
        <el-table-column prop="nextLOLER" label="LOLER Due" width="120" />
        <el-table-column label="Days" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getLOLERStatus(row.nextLOLER) === 'overdue' || getLOLERStatus(row.nextLOLER) === 'critical' ? 'danger' : 'warning'" size="small">{{ row.daysUntil }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nextPUWER" label="PUWER Due" width="120" />
        <el-table-column label="Status" width="100">
          <template #default="{ row }"><StatusBadge :status="row.status" /></template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? 'Add Plant' : 'Edit Plant'" width="650px" destroy-on-close>
      <el-form :model="formData" label-width="130px" class="plant-form">
        <el-form-item label="Description" required>
          <el-input v-model="formData.description" placeholder="Enter plant description" />
        </el-form-item>
        <el-form-item label="Category" required>
          <el-select v-model="formData.category" style="width: 100%">
            <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Make">
          <el-input v-model="formData.make" placeholder="e.g. JCB, Caterpillar" />
        </el-form-item>
        <el-form-item label="Model">
          <el-input v-model="formData.model" placeholder="e.g. 3CX, 320" />
        </el-form-item>
        <el-form-item label="Serial Number">
          <el-input v-model="formData.serialNumber" placeholder="Enter serial number" />
        </el-form-item>
        <el-form-item label="Hire Rate">
          <el-input-number v-model="formData.hireRate" :min="0" :precision="2" :step="10" style="width: 100%" placeholder="Daily hire rate" />
        </el-form-item>
        <el-form-item label="Status" required>
          <el-select v-model="formData.status" style="width: 100%">
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Insurance Expiry">
          <el-date-picker v-model="formData.insuranceExpiry" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="Select date" />
        </el-form-item>
        <el-form-item label="Next LOLER">
          <el-date-picker v-model="formData.nextLOLER" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="Select date" />
        </el-form-item>
        <el-form-item label="Next PUWER">
          <el-date-picker v-model="formData.nextPUWER" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="Select date" />
        </el-form-item>
        <el-form-item label="Notes">
          <el-input v-model="formData.notes" type="textarea" :rows="3" placeholder="Additional notes..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ dialogMode === 'add' ? 'Create' : 'Save Changes' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.plant-view {
  .mb-4 { margin-bottom: 16px; }
  .text-danger { color: var(--el-color-danger); }
  .text-muted { color: var(--el-text-color-secondary); }
}
.plant-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}
</style>
