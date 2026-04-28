<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, View, Edit } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import api, { type Contract, type Company, type Site } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()

const loading = ref(false)
const tableData = ref<Contract[]>([])
const total = ref(0)
const companies = ref<Company[]>([])
const sites = ref<Site[]>([])

const filters = reactive({
  search: '', status: '', clientId: '', minValue: undefined as number | undefined,
  maxValue: undefined as number | undefined, page: 1, limit: 20
})

const statusOptions = [
  { label: 'Draft', value: 'draft' },
  { label: 'Active', value: 'active' },
  { label: 'Completed', value: 'completed' },
  { label: 'Terminated', value: 'terminated' }
]

onMounted(() => { loadData(); loadCompanies(); loadSites() })

const loadData = async () => {
  loading.value = true
  try {
    const response = await api.contracts.getAll({
      status: filters.status || undefined, clientId: filters.clientId || undefined,
      minValue: filters.minValue, maxValue: filters.maxValue, page: filters.page, limit: filters.limit
    })
    tableData.value = response.data.data; total.value = response.data.total
  } catch { ElMessage.error('Failed to load contracts') } finally { loading.value = false }
}

const loadCompanies = async () => {
  try {
    const response = await api.companies.getAll({ limit: 100 })
    companies.value = response.data.data
  } catch {}
}

const loadSites = async () => {
  try {
    const response = await api.sites.getAll({ limit: 100 })
    sites.value = response.data.data
  } catch {}
}

const formatCurrency = (value: number) => `£${value.toLocaleString()}`
const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'

const viewContract = (row: Contract) => { router.push(`/contracts/${row.id}`) }
const editContract = (row: Contract) => { router.push(`/contracts/${row.id}/edit`) }
</script>

<template>
  <div class="contracts-view">
    <PageHeader title="Contracts" :breadcrumbs="[{ title: 'Contracts' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus">New Contract</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="6">
          <el-input v-model="filters.search" placeholder="Search..." clearable :prefix-icon="Search" />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.status" placeholder="Status" clearable @change="loadData">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.clientId" placeholder="Client" clearable filterable @change="loadData">
            <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :md="6" class="filter-actions">
          <el-button @click="() => { filters.search = ''; filters.status = ''; filters.clientId = ''; loadData() }">Reset</el-button>
          <el-button type="primary" @click="loadData">Search</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="contract-details">
              <el-descriptions :column="3" border size="small">
                <el-descriptions-item label="Contract Form">{{ row.contractForm || '—' }}</el-descriptions-item>
                <el-descriptions-item label="Payment Terms">{{ row.paymentTerms || '—' }}</el-descriptions-item>
                <el-descriptions-item label="Retention">{{ row.retentionPercentage }}%</el-descriptions-item>
                <el-descriptions-item label="Variations">{{ row.variations?.length || 0 }}</el-descriptions-item>
                <el-descriptions-item label="Applications">{{ row.applications?.length || 0 }}</el-descriptions-item>
                <el-descriptions-item label="Start Date">{{ formatDate(row.startDate) }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reference" label="Ref" width="120" />
        <el-table-column prop="title" label="Title" min-width="200" />
        <el-table-column label="Client" min-width="150">
          <template #default="{ row }">{{ row.client?.name || '—' }}</template>
        </el-table-column>
        <el-table-column label="Site" min-width="150">
          <template #default="{ row }">{{ row.site?.name || '—' }}</template>
        </el-table-column>
        <el-table-column label="Value" width="140">
          <template #default="{ row }">{{ formatCurrency(row.contractValue) }}</template>
        </el-table-column>
        <el-table-column label="Retention" width="100">
          <template #default="{ row }">{{ row.retentionPercentage }}%</template>
        </el-table-column>
        <el-table-column label="Status" width="120">
          <template #default="{ row }"><StatusBadge :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="Actions" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewContract(row)">View</el-button>
            <el-button link type="primary" @click="editContract(row)">Edit</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="filters.page" :page-size="filters.limit" :total="total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.contracts-view { .filter-card { margin-bottom: 16px; .filter-actions { display: flex; justify-content: flex-end; gap: 8px; } } }
.contract-details { padding: 12px 48px; }
</style>
