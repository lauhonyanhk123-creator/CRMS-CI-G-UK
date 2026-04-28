<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import api, { type WipEntry, type Contract, type Operative } from '@/services/api'
import PageHeader from '@/components/common/PageHeader.vue'

const loading = ref(false)
const wipEntries = ref<WipEntry[]>([])
const contracts = ref<Contract[]>([])
const operatives = ref<Operative[]>([])

// Filters
const filters = ref({
  startDate: '',
  endDate: '',
  contractId: '',
  status: '',
  operativeId: ''
})

// Dialog state
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const editingId = ref('')
const form = ref({
  entryDate: '',
  contractId: '',
  operativeId: '',
  description: '',
  hours: 0,
  rate: 0,
  notes: ''
})

// Computed amount
const computedAmount = computed(() => form.value.hours * form.value.rate)

onMounted(() => {
  loadData()
  loadContracts()
  loadOperatives()
})

const loadData = async () => {
  loading.value = true
  try {
    const response = await api.wip.getAll({
      startDate: filters.value.startDate || undefined,
      endDate: filters.value.endDate || undefined,
      contractId: filters.value.contractId || undefined,
      status: filters.value.status || undefined,
      operativeId: filters.value.operativeId || undefined
    })
    wipEntries.value = response.data.data || response.data
  } catch {
    wipEntries.value = []
  } finally {
    loading.value = false
  }
}

const loadContracts = async () => {
  try {
    const response = await api.contracts.getAll({ limit: 100 })
    contracts.value = response.data.data || response.data
  } catch {
    contracts.value = []
  }
}

const loadOperatives = async () => {
  try {
    const response = await api.operatives.getAll({ limit: 100 })
    operatives.value = response.data.data || response.data
  } catch {
    operatives.value = []
  }
}

const formatCurrency = (value: number) => `£${value.toLocaleString()}`
const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'

const getStatusType = (status: string) => {
  const map: Record<string, string> = { draft: 'info', submitted: 'warning', reviewed: 'primary', approved: 'success' }
  return map[status] || 'info'
}

const handleAddEntry = () => {
  editingId.value = ''
  form.value = { entryDate: new Date().toISOString().split('T')[0], contractId: '', operativeId: '', description: '', hours: 0, rate: 0, notes: '' }
  dialogVisible.value = true
}

const handleEditEntry = (entry: WipEntry) => {
  editingId.value = entry.id
  form.value = {
    entryDate: entry.entryDate,
    contractId: entry.contractId,
    operativeId: entry.operativeId,
    description: entry.description,
    hours: entry.hours,
    rate: entry.rate,
    notes: entry.notes || ''
  }
  dialogVisible.value = true
}

const handleSaveEntry = async () => {
  dialogLoading.value = true
  try {
    const data = { ...form.value, amount: computedAmount.value }
    if (editingId.value) {
      await api.wip.update(editingId.value, data)
    } else {
      await api.wip.create(data)
    }
    ElMessage.success('WIP entry saved')
    dialogVisible.value = false
    loadData()
  } catch { ElMessage.error('Failed to save WIP entry') } finally { dialogLoading.value = false }
}

const handleDeleteEntry = async (id: string) => {
  try {
    await api.wip.delete(id)
    ElMessage.success('WIP entry deleted')
    loadData()
  } catch { ElMessage.error('Failed to delete WIP entry') }
}

const handleFilter = () => loadData()
const handleReset = () => {
  filters.value = { startDate: '', endDate: '', contractId: '', status: '', operativeId: '' }
  loadData()
}

const getContractRef = (contractId: string) => {
  const contract = contracts.value.find(c => c.id === contractId)
  return contract?.reference || contractId
}

const getProjectCode = (contractId: string) => {
  const contract = contracts.value.find(c => c.id === contractId)
  return contract?.site?.siteCode || '—'
}

const getOperativeName = (operativeId: string) => {
  const operative = operatives.value.find(o => o.id === operativeId)
  return operative ? `${operative.firstName} ${operative.lastName}` : operativeId
}
</script>

<template>
  <div class="wip-journal-view">
    <PageHeader title="Work in Progress Journal" :breadcrumbs="[{ title: 'Home', path: '/' }, { title: 'WIP Journal' }]" />

    <!-- Filters -->
    <el-card shadow="never" class="mb-4">
      <el-form :inline="true" :model="filters">
        <el-form-item label="Start Date">
          <el-date-picker v-model="filters.startDate" type="date" placeholder="Start date" style="width:150px" />
        </el-form-item>
        <el-form-item label="End Date">
          <el-date-picker v-model="filters.endDate" type="date" placeholder="End date" style="width:150px" />
        </el-form-item>
        <el-form-item label="Contract">
          <el-select v-model="filters.contractId" placeholder="All contracts" clearable style="width:200px">
            <el-option v-for="c in contracts" :key="c.id" :label="c.reference" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="filters.status" placeholder="All statuses" clearable style="width:150px">
            <el-option label="Draft" value="draft" />
            <el-option label="Submitted" value="submitted" />
            <el-option label="Reviewed" value="reviewed" />
            <el-option label="Approved" value="approved" />
          </el-select>
        </el-form-item>
        <el-form-item label="Operative">
          <el-select v-model="filters.operativeId" placeholder="All operatives" clearable style="width:200px">
            <el-option v-for="o in operatives" :key="o.id" :label="`${o.firstName} ${o.lastName}`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">Filter</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>WIP Entries ({{ wipEntries.length }})</span>
          <el-button type="primary" @click="handleAddEntry">Add WIP Entry</el-button>
        </div>
      </template>
      <el-table :data="wipEntries" stripe v-loading="loading">
        <el-table-column label="Entry Date" width="120">
          <template #default="{ row }">{{ formatDate(row.entryDate) }}</template>
        </el-table-column>
        <el-table-column label="Contract" width="120">
          <template #default="{ row }">{{ getContractRef(row.contractId) }}</template>
        </el-table-column>
        <el-table-column label="Project Code" width="120">
          <template #default="{ row }">{{ getProjectCode(row.contractId) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="Description" min-width="200" />
        <el-table-column prop="hours" label="Hours" width="80" align="right" />
        <el-table-column label="Rate" width="100" align="right">
          <template #default="{ row }">{{ formatCurrency(row.rate) }}</template>
        </el-table-column>
        <el-table-column label="Amount" width="120" align="right">
          <template #default="{ row }">{{ formatCurrency(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="Status" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Operative" width="140">
          <template #default="{ row }">{{ getOperativeName(row.operativeId) }}</template>
        </el-table-column>
        <el-table-column prop="notes" label="Notes" min-width="150" show-overflow-tooltip />
        <el-table-column label="Actions" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEditEntry(row)">Edit</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteEntry(row.id)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="editingId ? 'Edit WIP Entry' : 'Add WIP Entry'" width="600px">
      <el-form :model="form" label-position="top">
        <el-form-item label="Entry Date">
          <el-date-picker v-model="form.entryDate" type="date" style="width:100%" />
        </el-form-item>
        <el-form-item label="Contract">
          <el-select v-model="form.contractId" placeholder="Select contract" style="width:100%">
            <el-option v-for="c in contracts" :key="c.id" :label="`${c.reference} - ${c.title}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Operative">
          <el-select v-model="form.operativeId" placeholder="Select operative" style="width:100%">
            <el-option v-for="o in operatives" :key="o.id" :label="`${o.firstName} ${o.lastName}`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="Describe the work performed..." />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Hours">
              <el-input-number v-model="form.hours" :min="0" :step="0.5" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Rate (£)">
              <el-input-number v-model="form.rate" :min="0" :step="0.5" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Amount (auto-calculated)">
          <el-input :model-value="formatCurrency(computedAmount)" disabled />
        </el-form-item>
        <el-form-item label="Notes">
          <el-input v-model="form.notes" type="textarea" :rows="2" placeholder="Additional notes..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="handleSaveEntry">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.wip-journal-view {
  .card-header { display: flex; justify-content: space-between; align-items: center; }
  .mb-4 { margin-bottom: 16px; }
}
</style>
