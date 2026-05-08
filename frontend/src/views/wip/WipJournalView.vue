<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api, { type WipEntry, type Contract } from '@/services/api'
import PageHeader from '@/components/common/PageHeader.vue'

const loading = ref(false)
const wipEntries = ref<WipEntry[]>([])
const contracts = ref<Contract[]>([])

// Filters
const filters = ref({
  contractId: ''
})

// Generate state
const generating = ref(false)

onMounted(() => {
  loadContracts()
})

const loadData = async () => {
  if (!filters.value.contractId) {
    wipEntries.value = []
    return
  }
  loading.value = true
  try {
    const response = await api.wip.getByContract(filters.value.contractId)
    wipEntries.value = Array.isArray(response.data) ? response.data : (response.data as any).data || []
  } catch (e) {
    console.error(e)
    wipEntries.value = []
  } finally {
    loading.value = false
  }
}

const loadContracts = async () => {
  try {
    const response = await api.contracts.getAll({ limit: 100 })
    contracts.value = (response.data as any).data || response.data
  } catch (e) {
    console.error(e)
    contracts.value = []
  }
}

const formatCurrency = (value: number) => `£${value.toLocaleString()}`
const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getStatusType = (status: string): TagType => {
  const map: Record<string, TagType> = { draft: 'info', submitted: 'warning', reviewed: 'primary', approved: 'success' }
  return map[status] ?? 'info'
}

const handleGenerate = async () => {
  if (!filters.value.contractId) {
    ElMessage.warning('Please select a contract first')
    return
  }
  generating.value = true
  try {
    await api.wip.generate(filters.value.contractId)
    ElMessage.success('WIP report generated')
    loadData()
  } catch (e) {
    console.error(e)
    ElMessage.error('Failed to generate WIP report')
  } finally {
    generating.value = false
  }
}

const handleFilter = () => loadData()
const handleReset = () => {
  filters.value = { contractId: '' }
  wipEntries.value = []
}

const getContractRef = (contractId: string) => {
  const contract = contracts.value.find(c => c.id === contractId)
  return contract?.reference || contractId
}

const getProjectCode = (contractId: string) => {
  const contract = contracts.value.find(c => c.id === contractId)
  return contract?.site?.siteCode || '—'
}
</script>

<template>
  <div class="wip-journal-view">
    <PageHeader title="Work in Progress Journal" :breadcrumbs="[{ title: 'Home', path: '/' }, { title: 'WIP Journal' }]" />

    <!-- Filters -->
    <el-card shadow="never" class="mb-4">
      <el-form :inline="true" :model="filters">
        <el-form-item label="Contract">
          <el-select v-model="filters.contractId" placeholder="Select contract" clearable style="width:250px" @change="loadData">
            <el-option v-for="c in contracts" :key="c.id" :label="`${c.reference} — ${c.title}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">Load</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>WIP Entries ({{ wipEntries.length }})</span>
          <el-button type="primary" :loading="generating" :disabled="!filters.contractId" @click="handleGenerate">
            Generate WIP Report
          </el-button>
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
        <el-table-column prop="notes" label="Notes" min-width="150" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.wip-journal-view {
  .card-header { display: flex; justify-content: space-between; align-items: center; }
  .mb-4 { margin-bottom: 16px; }
}
</style>
