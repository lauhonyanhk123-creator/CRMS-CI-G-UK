<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, View, Edit, Delete } from '@element-plus/icons-vue'
import api, { type Operative, type Company } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref<Operative[]>([])
const total = ref(0)
const companies = ref<Company[]>([])

const filters = reactive({
  search: '',
  status: '',
  employerId: '',
  page: 1,
  limit: 20
})

const smartCheckingId = ref('')

onMounted(() => { loadData(); loadCompanies() })

const loadData = async () => {
  loading.value = true
  try {
    const response = await api.operatives.getAll({
      status: filters.status || undefined,
      employerId: filters.employerId || undefined,
      page: filters.page,
      limit: filters.limit
    })
    tableData.value = response.data.data
    total.value = response.data.total
  } catch { ElMessage.error('Failed to load operatives') } finally { loading.value = false }
}

const loadCompanies = async () => {
  try {
    const response = await api.companies.getAll({ type: 'subcontractor', limit: 100 })
    companies.value = response.data.data
  } catch {}
}

const handleSearch = () => { filters.page = 1; loadData() }
const handlePageChange = (page: number) => { filters.page = page; loadData() }

const smartCheckCard = async (operative: Operative) => {
  if (!operative.cscsCard) { ElMessage.warning('No CSCS card on record'); return }
  smartCheckingId.value = operative.id
  try {
    await api.operatives.smartCheckCard(operative.id, operative.cscsCard.id)
    ElMessage.success('Smart Check completed')
    loadData()
  } catch { ElMessage.error('Smart Check failed') } finally { smartCheckingId.value = '' }
}

const getCardExpiryStatus = (card?: Operative['cscsCard']) => {
  if (!card) return 'none'
  const daysUntil = dayjs(card.expiryDate).diff(dayjs(), 'day')
  if (daysUntil < 0) return 'expired'
  if (daysUntil <= 60) return 'expiring'
  return 'valid'
}

const getExpiryType = (status: string) => {
  return status === 'expired' ? 'danger' : status === 'expiring' ? 'warning' : 'success'
}

const getInductionStatusType = (status: string) => {
  const map: Record<string, string> = { complete: 'success', pending: 'warning', expired: 'danger' }
  return map[status] || 'info'
}
</script>

<template>
  <div class="operatives-view">
    <PageHeader title="Operatives" :breadcrumbs="[{ title: 'Operatives' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus">Add Operative</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8">
          <el-input v-model="filters.search" placeholder="Search..." clearable :prefix-icon="Search" @keyup.enter="handleSearch" />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.employerId" placeholder="Employer" clearable filterable @change="handleSearch">
            <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :md="10" class="filter-actions">
          <el-button @click="() => { filters.search = ''; filters.status = ''; filters.employerId = ''; loadData() }">Reset</el-button>
          <el-button type="primary" @click="handleSearch">Search</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="operative-details">
              <el-row :gutter="20">
                <el-col :span="12">
                  <h4>Qualifications</h4>
                  <el-tag v-for="q in row.qualifications" :key="q.id" size="small" class="mr-2 mb-2">{{ q.name }} {{ q.expiryDate ? `(Exp: ${q.expiryDate})` : '' }}</el-tag>
                  <span v-if="!row.qualifications?.length" class="text-muted">No qualifications</span>
                </el-col>
                <el-col :span="12">
                  <h4>CSCS Card</h4>
                  <div v-if="row.cscsCard" class="cscs-card-info">
                    <p>Number: {{ row.cscsCard.cardNumber }}</p>
                    <p>Type: {{ row.cscsCard.cardType }}</p>
                    <p>Expiry: {{ row.cscsCard.expiryDate }}</p>
                    <p>Verified: {{ row.cscsCard.verified ? 'Yes' : 'No' }}</p>
                  </div>
                  <span v-else class="text-muted">No CSCS card on record</span>
                </el-col>
              </el-row>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="Name" min-width="180">
          <template #default="{ row }">{{ row.firstName }} {{ row.lastName }}</template>
        </el-table-column>
        <el-table-column prop="employeeRef" label="Emp Ref" width="100" />
        <el-table-column label="Employer" min-width="150">
          <template #default="{ row }">{{ row.employer?.name || '—' }}</template>
        </el-table-column>
        <el-table-column label="CSCS Card" width="150">
          <template #default="{ row }">
            <div v-if="row.cscsCard" class="card-info">
              <el-avatar :size="32" :src="row.cscsCard.photoUrl" icon="UserFilled" />
              <span :class="{ 'text-danger': getCardExpiryStatus(row.cscsCard) !== 'valid' }">
                {{ row.cscsCard.expiryDate }}
              </span>
            </div>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="Expiry Alert" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.cscsCard && getCardExpiryStatus(row.cscsCard) !== 'valid'" :type="getExpiryType(getCardExpiryStatus(row.cscsCard))" size="small">
              {{ getCardExpiryStatus(row.cscsCard) === 'expired' ? 'Expired' : 'Expiring' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Induction" width="120">
          <template #default="{ row }">
            <el-tag :type="getInductionStatusType(row.inductionStatus)" size="small">{{ row.inductionStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Status" width="100">
          <template #default="{ row }"><StatusBadge :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="Actions" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="smartCheckCard(row)" :loading="smartCheckingId === row.id">Smart Check</el-button>
            <el-button link type="primary" size="small" :icon="Edit">Edit</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="filters.page" :page-size="filters.limit" :total="total" @current-change="handlePageChange" />
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.operatives-view { .filter-card { margin-bottom: 16px; .filter-actions { display: flex; justify-content: flex-end; gap: 8px; } } }
.operative-details { padding: 16px 48px; h4 { margin-bottom: 8px; font-size: 14px; color: #606266; } }
.card-info { display: flex; align-items: center; gap: 8px; }
</style>
