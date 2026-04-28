<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, View, Edit, Delete } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import api, { type Subcontractor } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()

const loading = ref(false)
const tableData = ref<Subcontractor[]>([])
const total = ref(0)
const verifyingId = ref('')

const filters = reactive({
  search: '',
  cisStatus: '',
  page: 1,
  limit: 20
})

const cisStatuses = [
  { label: 'Verified', value: 'verified' },
  { label: 'Pending', value: 'pending' },
  { label: 'Expired', value: 'expired' }
]

onMounted(() => loadData())

const loadData = async () => {
  loading.value = true
  try {
    const response = await api.subcontractors.getAll({
      search: filters.search || undefined,
      cisStatus: filters.cisStatus || undefined,
      page: filters.page,
      limit: filters.limit
    })
    tableData.value = response.data.data
    total.value = response.data.total
  } catch { ElMessage.error('Failed to load subcontractors') } finally { loading.value = false }
}

const handleSearch = () => { filters.page = 1; loadData() }
const handlePageChange = (page: number) => { filters.page = page; loadData() }

const verifyCIS = async (row: Subcontractor) => {
  verifyingId.value = row.id
  try {
    await api.subcontractors.verify(row.id)
    ElMessage.success('CIS verification completed')
    loadData()
  } catch { ElMessage.error('CIS verification failed') } finally { verifyingId.value = '' }
}

const getCisStatusType = (status?: string) => {
  const map: Record<string, string> = { verified: 'success', pending: 'warning', expired: 'danger' }
  return map[status || ''] || 'info'
}

const getGateStatusType = (status?: string) => {
  return status === 'ready' ? 'success' : 'danger'
}
</script>

<template>
  <div class="subcontractors-view">
    <PageHeader title="Subcontractors" :breadcrumbs="[{ title: 'Subcontractors' }]">
      <template #actions>
        <el-button :icon="Refresh" @click="loadData">Refresh</el-button>
        <el-button type="primary" :icon="Plus">Add Subcontractor</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8">
          <el-input v-model="filters.search" placeholder="Search..." clearable :prefix-icon="Search" @keyup.enter="handleSearch" />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.cisStatus" placeholder="CIS Status" clearable @change="handleSearch">
            <el-option v-for="s in cisStatuses" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-col>
        <el-col :xs="24" :md="10" class="filter-actions">
          <el-button @click="() => { filters.search = ''; filters.cisStatus = ''; loadData() }">Reset</el-button>
          <el-button type="primary" @click="handleSearch">Search</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="name" label="Name" min-width="200" />
        <el-table-column prop="registrationNumber" label="Reg No." width="120" />
        <el-table-column label="CIS Rate" width="100">
          <template #default="{ row }">{{ row.cisRate ? `${row.cisRate}%` : '—' }}</template>
        </el-table-column>
        <el-table-column label="CIS Status" width="130">
          <template #default="{ row }">
            <el-tag :type="getCisStatusType(row.cisVerificationStatus)" size="small">
              {{ row.cisVerificationStatus?.toUpperCase() || 'N/A' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Gate Status" width="120">
          <template #default="{ row }">
            <el-tag :type="getGateStatusType(row.subbieGateStatus)" size="small">
              {{ row.subbieGateStatus === 'ready' ? 'READY' : row.subbieGateStatus === 'gate_red' ? 'GATE RED' : 'N/A' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="Phone" width="140">
          <template #default="{ row }">{{ row.phone || '—' }}</template>
        </el-table-column>
        <el-table-column label="Status" width="100">
          <template #default="{ row }"><StatusBadge :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="Actions" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="router.push(`/companies/${row.id}`)">View</el-button>
            <el-button link type="primary" :icon="Edit">Edit</el-button>
            <el-button 
              link 
              type="success" 
              :loading="verifyingId === row.id"
              @click="verifyCIS(row)"
            >
              Verify CIS
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="filters.page" :page-size="filters.limit" :total="total" @current-change="handlePageChange" />
    </el-card>

    <el-divider />

    <el-card shadow="never">
      <template #header><div class="card-header"><span>CIS300 Returns</span><el-button type="primary" size="small">Submit CIS300</el-button></div></template>
      <el-table :data="[]" stripe>
        <el-table-column prop="month" label="Month" width="120" />
        <el-table-column prop="totalValue" label="Total Value" width="150" />
        <el-table-column prop=" CISDeducted" label="CIS Deducted" width="150" />
        <el-table-column prop="submittedAt" label="Submitted" width="120" />
        <el-table-column label="Status" width="100"><el-tag type="success">Submitted</el-tag></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.subcontractors-view { .filter-card { margin-bottom: 16px; .filter-actions { display: flex; justify-content: flex-end; gap: 8px; } } }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
