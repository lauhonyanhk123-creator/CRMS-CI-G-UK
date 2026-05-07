<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, WarningFilled, Edit, Delete, View } from '@element-plus/icons-vue'
import api, {
  type AdoptionCase,
  type Bond,
  type CommutedSum,
  type SnaggingItem,
  type Company,
  type BondReduction,
  type MaintenanceSchedule,
  type SnaggingInspection
} from '@/services/api'; import type { ElTagType } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import dayjs from 'dayjs'

// State
const loading = ref(false)
const activeTab = ref('cases')
const companies = ref<Company[]>([])

// Tab data
const cases = ref<AdoptionCase[]>([])
const bonds = ref<Bond[]>([])
const commutedSums = ref<CommutedSum[]>([])
const snaggingItems = ref<SnaggingItem[]>([])

// Pagination
const caseTotal = ref(0)
const bondTotal = ref(0)
const commutedSumTotal = ref(0)
const snaggingTotal = ref(0)

// Filters
const caseFilters = reactive({ status: '', type: '', search: '', page: 1, limit: 20 })
const bondFilters = reactive({ status: '', caseId: '', page: 1, limit: 20 })
const commutedSumFilters = reactive({ status: '', caseId: '', page: 1, limit: 20 })
const snaggingFilters = reactive({ status: '', category: '', priority: '', caseId: '', page: 1, limit: 20 })

// Alerts
const bondAlerts = ref<Bond[]>([])
const snaggingAlerts = ref<SnaggingItem[]>([])

// Dialog states
const caseDialogVisible = ref(false)
const bondDialogVisible = ref(false)
const commutedSumDialogVisible = ref(false)
const snaggingDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const viewItem = ref<any>(null)
const viewType = ref<'case' | 'bond' | 'commutedSum' | 'snagging'>('case')

// Form data
const caseForm = reactive<Partial<AdoptionCase>>({
  caseRef: '',
  type: 's38',
  title: '',
  clientId: '',
  laWaterAuthority: '',
  status: 'pre_application',
  bondValue: undefined,
  bondReleaseDate: undefined
})

const bondForm = reactive<Partial<Bond>>({
  caseId: '',
  bondReference: '',
  bondType: 'maintenance',
  issuingBank: '',
  bondAmount: 0,
  effectiveDate: '',
  expiryDate: '',
  status: 'pending'
})

const commutedSumForm = reactive<Partial<CommutedSum>>({
  caseId: '',
  sumReference: '',
  sumType: 'adoption',
  description: '',
  calculatedAmount: 0,
  agreedAmount: undefined,
  status: 'calculated'
})

const snaggingForm = reactive<Partial<SnaggingItem>>({
  caseId: '',
  itemReference: '',
  category: 'road',
  location: '',
  description: '',
  priority: 'medium',
  status: 'identified',
  identifiedDate: dayjs().format('YYYY-MM-DD')
})

const editingId = ref<string | null>(null)

// Options
const typeOptions = [
  { label: 'Section 38', value: 's38' },
  { label: 'Section 278', value: 's278' },
  { label: 'Section 104', value: 's104' }
]

const statusOptions = [
  { label: 'Pre-Application', value: 'pre_application' },
  { label: 'Application', value: 'application' },
  { label: 'Technical Approval', value: 'technical_approval' },
  { label: 'Under Construction', value: 'under_construction' },
  { label: 'Adopted', value: 'adopted' },
  { label: 'Rejected', value: 'rejected' }
]

const bondTypeOptions = [
  { label: 'Maintenance Bond', value: 'maintenance' },
  { label: 'Performance Bond', value: 'performance' },
  { label: 'Parent Company Guarantee', value: 'parent_company' },
  { label: 'Insurance Bond', value: 'insurance' }
]

const bondStatusOptions = [
  { label: 'Pending', value: 'pending' },
  { label: 'Issued', value: 'issued' },
  { label: 'Called', value: 'called' },
  { label: 'Released', value: 'released' },
  { label: 'Expired', value: 'expired' },
  { label: 'Reduced', value: 'reduced' }
]

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const bondStatusColorMap: Record<string, TagType> = {
  pending: 'info',
  issued: 'primary',
  called: 'danger',
  released: 'success',
  expired: 'warning',
  reduced: 'success'
}

const commutedSumTypeOptions = [
  { label: 'Adoption', value: 'adoption' },
  { label: 'Maintenance', value: 'maintenance' },
  { label: 'Lifecycle', value: 'lifecycle' },
  { label: 'Sinking Fund', value: 'sinking_fund' },
  { label: 'Commuted', value: 'commuted' }
]

const commutedSumStatusOptions = [
  { label: 'Calculated', value: 'calculated' },
  { label: 'Negotiated', value: 'negotiated' },
  { label: 'Agreed', value: 'agreed' },
  { label: 'Paid', value: 'paid' },
  { label: 'Challenged', value: 'challenged' }
]

const snaggingCategoryOptions = [
  { label: 'Road', value: 'road' },
  { label: 'Drainage', value: 'drainage' },
  { label: 'Footway', value: 'footway' },
  { label: 'Verge', value: 'verge' },
  { label: 'Signage', value: 'signage' },
  { label: 'Lighting', value: 'lighting' },
  { label: 'Soft Landscape', value: 'soft_landscape' },
  { label: 'Hard Landscape', value: 'hard_landscape' },
  { label: 'Utility', value: 'utility' },
  { label: 'Other', value: 'other' }
]

const snaggingStatusOptions = [
  { label: 'Identified', value: 'identified' },
  { label: 'In Progress', value: 'in_progress' },
  { label: 'Awaiting Materials', value: 'awaiting_materials' },
  { label: 'Completed', value: 'completed' },
  { label: 'Passed', value: 'passed' },
  { label: 'Failed', value: 'failed' }
]

const snaggingPriorityOptions = [
  { label: 'Low', value: 'low' },
  { label: 'Medium', value: 'medium' },
  { label: 'High', value: 'high' },
  { label: 'Critical', value: 'critical' }
]

const snaggingPriorityColorMap: Record<string, TagType> = {
  low: 'info',
  medium: 'warning',
  high: 'danger',
  critical: 'danger'
}

// Lifecycle
onMounted(() => {
  loadCompanies()
  loadAllData()
})

// Methods
const loadCompanies = async () => {
  try {
    const res = await api.companies.getAll({ limit: 500 })
    companies.value = res.data.data
  } catch {}
}

const loadAllData = () => {
  loadCases()
  loadBonds()
  loadCommutedSums()
  loadSnaggingItems()
}

const loadCases = async () => {
  loading.value = true
  try {
    const res = await api.adoption.getAll({
      status: caseFilters.status || undefined,
      type: caseFilters.type || undefined,
      page: caseFilters.page,
      limit: caseFilters.limit
    })
    cases.value = res.data.data
    caseTotal.value = res.data.total
    updateBondAlerts()
  } catch { ElMessage.error('Failed to load adoption cases') }
  finally { loading.value = false }
}

const loadBonds = async () => {
  loading.value = true
  try {
    const res = await api.adoption.getBonds({
      status: bondFilters.status || undefined,
      caseId: bondFilters.caseId || undefined,
      page: bondFilters.page,
      limit: bondFilters.limit
    })
    bonds.value = res.data.data
    bondTotal.value = res.data.total
  } catch { ElMessage.error('Failed to load bonds') }
  finally { loading.value = false }
}

const loadCommutedSums = async () => {
  loading.value = true
  try {
    const res = await api.adoption.getCommutedSums({
      status: commutedSumFilters.status || undefined,
      caseId: commutedSumFilters.caseId || undefined,
      page: commutedSumFilters.page,
      limit: commutedSumFilters.limit
    })
    commutedSums.value = res.data.data
    commutedSumTotal.value = res.data.total
  } catch { ElMessage.error('Failed to load commuted sums') }
  finally { loading.value = false }
}

const loadSnaggingItems = async () => {
  loading.value = true
  try {
    const res = await api.adoption.getSnaggingItems({
      status: snaggingFilters.status || undefined,
      category: snaggingFilters.category || undefined,
      priority: snaggingFilters.priority || undefined,
      caseId: snaggingFilters.caseId || undefined,
      page: snaggingFilters.page,
      limit: snaggingFilters.limit
    })
    snaggingItems.value = res.data.data
    snaggingTotal.value = res.data.total
    updateSnaggingAlerts()
  } catch { ElMessage.error('Failed to load snagging items') }
  finally { loading.value = false }
}

const updateBondAlerts = () => {
  const thirtyDays = dayjs().add(30, 'day').toDate()
  bondAlerts.value = bonds.value.filter(b =>
    b.expiryDate && dayjs(b.expiryDate).isBefore(thirtyDays) && b.status === 'issued'
  )
}

const updateSnaggingAlerts = () => {
  snaggingAlerts.value = snaggingItems.value.filter(s =>
    s.status !== 'passed' && s.priority === 'critical'
  )
}

// CRUD Operations - Cases
const openCaseDialog = (row?: AdoptionCase) => {
  if (row) {
    Object.assign(caseForm, row)
    editingId.value = row.id
  } else {
    Object.assign(caseForm, {
      caseRef: '',
      type: 's38',
      title: '',
      clientId: '',
      laWaterAuthority: '',
      status: 'pre_application',
      bondValue: undefined,
      bondReleaseDate: undefined
    })
    editingId.value = null
  }
  caseDialogVisible.value = true
}

const saveCase = async () => {
  try {
    if (editingId.value) {
      await api.adoption.update(editingId.value, caseForm)
      ElMessage.success('Adoption case updated')
    } else {
      await api.adoption.create(caseForm as AdoptionCase)
      ElMessage.success('Adoption case created')
    }
    caseDialogVisible.value = false
    loadCases()
  } catch { ElMessage.error('Failed to save adoption case') }
}

const deleteCase = async (id: string) => {
  try {
    await ElMessageBox.confirm('Delete this adoption case?', 'Confirm', { type: 'warning' })
    await api.adoption.delete(id)
    ElMessage.success('Adoption case deleted')
    loadCases()
  } catch {}
}

// CRUD Operations - Bonds
const openBondDialog = (row?: Bond) => {
  if (row) {
    Object.assign(bondForm, row)
    editingId.value = row.id
  } else {
    Object.assign(bondForm, {
      caseId: '',
      bondReference: '',
      bondType: 'maintenance',
      issuingBank: '',
      bondAmount: 0,
      effectiveDate: dayjs().format('YYYY-MM-DD'),
      expiryDate: undefined,
      status: 'pending'
    })
    editingId.value = null
  }
  bondDialogVisible.value = true
}

const saveBond = async () => {
  try {
    if (editingId.value) {
      await api.adoption.updateBond(editingId.value, bondForm)
      ElMessage.success('Bond updated')
    } else {
      await api.adoption.createBond(bondForm as Bond)
      ElMessage.success('Bond created')
    }
    bondDialogVisible.value = false
    loadBonds()
  } catch { ElMessage.error('Failed to save bond') }
}

const deleteBond = async (id: string) => {
  try {
    await ElMessageBox.confirm('Delete this bond?', 'Confirm', { type: 'warning' })
    await api.adoption.deleteBond(id)
    ElMessage.success('Bond deleted')
    loadBonds()
  } catch {}
}

const releaseBond = async (id: string) => {
  try {
    await ElMessageBox.confirm('Release this bond?', 'Confirm', { type: 'warning' })
    await api.adoption.releaseBond(id, dayjs().format('YYYY-MM-DD'))
    ElMessage.success('Bond released')
    loadBonds()
  } catch {}
}

const callBond = async (id: string) => {
  try {
    await ElMessageBox.prompt('Enter reason for calling bond:', 'Call Bond', {
      confirmButtonText: 'Call',
      cancelButtonText: 'Cancel',
      inputErrorMessage: 'Please enter a reason'
    }).then(async ({ value }) => {
      await api.adoption.callBond(id, value)
      ElMessage.success('Bond called')
      loadBonds()
    })
  } catch {}
}

// CRUD Operations - Commuted Sums
const openCommutedSumDialog = (row?: CommutedSum) => {
  if (row) {
    Object.assign(commutedSumForm, row)
    editingId.value = row.id
  } else {
    Object.assign(commutedSumForm, {
      caseId: '',
      sumReference: '',
      sumType: 'adoption',
      description: '',
      calculatedAmount: 0,
      agreedAmount: undefined,
      status: 'calculated'
    })
    editingId.value = null
  }
  commutedSumDialogVisible.value = true
}

const saveCommutedSum = async () => {
  try {
    if (editingId.value) {
      await api.adoption.updateCommutedSum(editingId.value, commutedSumForm)
      ElMessage.success('Commuted sum updated')
    } else {
      await api.adoption.createCommutedSum(commutedSumForm as CommutedSum)
      ElMessage.success('Commuted sum created')
    }
    commutedSumDialogVisible.value = false
    loadCommutedSums()
  } catch { ElMessage.error('Failed to save commuted sum') }
}

const deleteCommutedSum = async (id: string) => {
  try {
    await ElMessageBox.confirm('Delete this commuted sum?', 'Confirm', { type: 'warning' })
    await api.adoption.deleteCommutedSum(id)
    ElMessage.success('Commuted sum deleted')
    loadCommutedSums()
  } catch {}
}

// CRUD Operations - Snagging
const openSnaggingDialog = (row?: SnaggingItem) => {
  if (row) {
    Object.assign(snaggingForm, row)
    editingId.value = row.id
  } else {
    Object.assign(snaggingForm, {
      caseId: '',
      itemReference: '',
      category: 'road',
      location: '',
      description: '',
      priority: 'medium',
      status: 'identified',
      identifiedDate: dayjs().format('YYYY-MM-DD')
    })
    editingId.value = null
  }
  snaggingDialogVisible.value = true
}

const saveSnagging = async () => {
  try {
    if (editingId.value) {
      await api.adoption.updateSnaggingItem(editingId.value, snaggingForm)
      ElMessage.success('Snagging item updated')
    } else {
      await api.adoption.createSnaggingItem(snaggingForm as SnaggingItem)
      ElMessage.success('Snagging item created')
    }
    snaggingDialogVisible.value = false
    loadSnaggingItems()
  } catch { ElMessage.error('Failed to save snagging item') }
}

const deleteSnagging = async (id: string) => {
  try {
    await ElMessageBox.confirm('Delete this snagging item?', 'Confirm', { type: 'warning' })
    await api.adoption.deleteSnaggingItem(id)
    ElMessage.success('Snagging item deleted')
    loadSnaggingItems()
  } catch {}
}

const updateSnaggingStatus = async (id: string, status: string) => {
  try {
    await api.adoption.updateSnaggingStatus(id, status)
    ElMessage.success('Status updated')
    loadSnaggingItems()
  } catch { ElMessage.error('Failed to update status') }
}

// View Details
const openViewDialog = (type: 'case' | 'bond' | 'commutedSum' | 'snagging', item: any) => {
  viewType.value = type
  viewItem.value = item
  viewDialogVisible.value = true
}

// Formatters
const formatCurrency = (value?: number) => value ? `£${value.toLocaleString()}` : '—'
const formatDate = (date?: string) => date ? dayjs(date).format('DD/MM/YYYY') : '—'

const getCaseTypeTag = (type: string): TagType | undefined => {
  const map: Record<string, TagType> = { s38: 'primary', s278: 'success', s104: 'warning' }
  return map[type]
}

const getBondAlertClass = (date?: string) => {
  if (!date) return ''
  const days = dayjs(date).diff(dayjs(), 'day')
  if (days < 0) return 'text-danger'
  if (days <= 30) return 'text-danger'
  if (days <= 90) return 'text-warning'
  return ''
}

const getStageStatusType = (status?: string): TagType => {
  const map: Record<string, TagType> = { completed: 'success', in_progress: 'primary', pending: 'info' }
  return map[status || ''] ?? 'info'
}

const getClientName = (clientId: string) => {
  const client = companies.value.find(c => c.id === clientId)
  return client?.name || '—'
}

const getCaseRef = (caseId: string) => {
  const adoptionCase = cases.value.find(c => c.id === caseId)
  return adoptionCase?.caseRef || caseId
}

const getSnaggingStatusColor = (status: string): TagType => {
  const map: Record<string, TagType> = {
    identified: 'info',
    in_progress: 'warning',
    awaiting_materials: 'warning',
    completed: 'success',
    passed: 'success',
    failed: 'danger'
  }
  return map[status] ?? 'info'
}
</script>

<template>
  <div class="adoption-view">
    <PageHeader title="Adoption" :breadcrumbs="[{ title: 'Adoption' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openCaseDialog()">New Case</el-button>
      </template>
    </PageHeader>

    <!-- Alert Cards -->
    <el-row :gutter="16" class="alert-row" v-if="bondAlerts.length || snaggingAlerts.length">
      <el-col :span="12" v-if="bondAlerts.length">
        <el-card shadow="never" class="alert-card bond-alert">
          <template #header>
            <div class="card-header">
              <el-icon color="#f56c6c"><WarningFilled /></el-icon>
              <span>Bond Expiry Alerts ({{ bondAlerts.length }})</span>
            </div>
          </template>
          <div v-for="bond in bondAlerts.slice(0, 3)" :key="bond.id" class="alert-item" @click="openViewDialog('bond', bond)">
            <strong>{{ bond.bondReference }}</strong> - Expires {{ formatDate(bond.expiryDate) }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="12" v-if="snaggingAlerts.length">
        <el-card shadow="never" class="alert-card snagging-alert">
          <template #header>
            <div class="card-header">
              <el-icon color="#e6a23c"><WarningFilled /></el-icon>
              <span>Critical Snagging ({{ snaggingAlerts.length }})</span>
            </div>
          </template>
          <div v-for="item in snaggingAlerts.slice(0, 3)" :key="item.id" class="alert-item" @click="openViewDialog('snagging', item)">
            <strong>{{ item.itemReference }}</strong> - {{ item.category }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tabs -->
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- Adoption Cases Tab -->
        <el-tab-pane label="Cases" name="cases">
          <div class="filter-bar">
            <el-select v-model="caseFilters.type" placeholder="Type" clearable @change="loadCases">
              <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
            <el-select v-model="caseFilters.status" placeholder="Status" clearable @change="loadCases">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
            <el-button @click="caseFilters.type = ''; caseFilters.status = ''; loadCases()">Reset</el-button>
          </div>

          <el-table v-loading="loading" :data="cases" stripe>
            <el-table-column type="expand">
              <template #default="{ row }">
                <div class="stage-history">
                  <h4>Stage History</h4>
                  <el-timeline v-if="row.stages?.length">
                    <el-timeline-item
                      v-for="(stage, idx) in row.stages"
                      :key="idx"
                      :type="getStageStatusType(stage.status)"
                      :timestamp="formatDate(stage.completedAt)"
                    >
                      <p><strong>{{ stage.name }}</strong></p>
                      <p v-if="stage.notes">{{ stage.notes }}</p>
                    </el-timeline-item>
                  </el-timeline>
                  <p v-else class="no-stages">No stages recorded</p>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="caseRef" label="Case Ref" width="120" />
            <el-table-column label="Type" width="100">
              <template #default="{ row }">
                <el-tag :type="getCaseTypeTag(row.type)" size="small">{{ row.type.toUpperCase() }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="Title" min-width="200" />
            <el-table-column label="Client" min-width="150">
              <template #default="{ row }">{{ getClientName(row.clientId) }}</template>
            </el-table-column>
            <el-table-column label="Status" width="140">
              <template #default="{ row }"><StatusBadge :status="row.status" /></template>
            </el-table-column>
            <el-table-column label="Bond Value" width="120">
              <template #default="{ row }">{{ formatCurrency(row.bondValue) }}</template>
            </el-table-column>
            <el-table-column label="Bond Release" width="130">
              <template #default="{ row }">
                <span :class="getBondAlertClass(row.bondReleaseDate)">{{ formatDate(row.bondReleaseDate) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="140" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="View" @click="openViewDialog('case', row)">View</el-button>
                <el-button link type="primary" :icon="Edit" @click="openCaseDialog(row)">Edit</el-button>
                <el-button link type="danger" :icon="Delete" @click="deleteCase(row.id)">Del</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="caseFilters.page"
            :page-size="caseFilters.limit"
            :total="caseTotal"
            layout="total, prev, pager, next"
            @current-change="loadCases"
          />
        </el-tab-pane>

        <!-- Bonds Tab -->
        <el-tab-pane label="Bonds" name="bonds">
          <div class="filter-bar">
            <el-select v-model="bondFilters.caseId" placeholder="Case" clearable filterable @change="loadBonds">
              <el-option v-for="c in cases" :key="c.id" :label="c.caseRef" :value="c.id" />
            </el-select>
            <el-select v-model="bondFilters.status" placeholder="Status" clearable @change="loadBonds">
              <el-option v-for="s in bondStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
            <el-button type="primary" :icon="Plus" @click="openBondDialog()">New Bond</el-button>
          </div>

          <el-table v-loading="loading" :data="bonds" stripe>
            <el-table-column type="expand">
              <template #default="{ row }">
                <div class="bond-details">
                  <el-descriptions :column="3" border size="small">
                    <el-descriptions-item label="Case">{{ getCaseRef(row.caseId) }}</el-descriptions-item>
                    <el-descriptions-item label="Type">{{ row.bondType }}</el-descriptions-item>
                    <el-descriptions-item label="Issuing Bank">{{ row.issuingBank || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="Effective Date">{{ formatDate(row.effectiveDate) }}</el-descriptions-item>
                    <el-descriptions-item label="Expiry Date">{{ formatDate(row.expiryDate) }}</el-descriptions-item>
                    <el-descriptions-item label="Release Date">{{ formatDate(row.releaseDate) }}</el-descriptions-item>
                    <el-descriptions-item label="Notes" :span="3">{{ row.notes || '—' }}</el-descriptions-item>
                  </el-descriptions>
                  <div v-if="row.reductionSchedule?.length" class="reduction-timeline mt-4">
                    <h4>Reduction Schedule</h4>
                    <el-timeline>
                      <el-timeline-item v-for="r in row.reductionSchedule" :key="r.id" type="success">
                        <p><strong>{{ formatDate(r.reductionDate) }}</strong> - £{{ r.reductionAmount.toLocaleString() }} reduction</p>
                        <p>Remaining: £{{ r.remainingAmount.toLocaleString() }}</p>
                        <p v-if="r.reason">{{ r.reason }}</p>
                      </el-timeline-item>
                    </el-timeline>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="bondReference" label="Ref" width="140" />
            <el-table-column label="Case" width="120">
              <template #default="{ row }">{{ getCaseRef(row.caseId) }}</template>
            </el-table-column>
            <el-table-column label="Type" width="140">
              <template #default="{ row }">
                <el-tag size="small">{{ row.bondType.replace('_', ' ') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Amount" width="140">
              <template #default="{ row }">{{ formatCurrency(row.bondAmount) }}</template>
            </el-table-column>
            <el-table-column label="Effective" width="110">
              <template #default="{ row }">{{ formatDate(row.effectiveDate) }}</template>
            </el-table-column>
            <el-table-column label="Expiry" width="110">
              <template #default="{ row }">
                <span :class="getBondAlertClass(row.expiryDate)">{{ formatDate(row.expiryDate) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Status" width="100">
              <template #default="{ row }">
                <el-tag :type="bondStatusColorMap[row.status]" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="160" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="View" @click="openViewDialog('bond', row)">View</el-button>
                <el-button link type="primary" :icon="Edit" @click="openBondDialog(row)">Edit</el-button>
                <el-button v-if="row.status === 'issued'" link type="success" @click="releaseBond(row.id)">Release</el-button>
                <el-button v-if="row.status === 'issued'" link type="danger" :icon="Delete" @click="deleteBond(row.id)">Del</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="bondFilters.page"
            :page-size="bondFilters.limit"
            :total="bondTotal"
            layout="total, prev, pager, next"
            @current-change="loadBonds"
          />
        </el-tab-pane>

        <!-- Commuted Sums Tab -->
        <el-tab-pane label="Commuted Sums" name="commutedSums">
          <div class="filter-bar">
            <el-select v-model="commutedSumFilters.caseId" placeholder="Case" clearable filterable @change="loadCommutedSums">
              <el-option v-for="c in cases" :key="c.id" :label="c.caseRef" :value="c.id" />
            </el-select>
            <el-select v-model="commutedSumFilters.status" placeholder="Status" clearable @change="loadCommutedSums">
              <el-option v-for="s in commutedSumStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
            <el-button type="primary" :icon="Plus" @click="openCommutedSumDialog()">New Sum</el-button>
          </div>

          <el-table v-loading="loading" :data="commutedSums" stripe>
            <el-table-column type="expand">
              <template #default="{ row }">
                <div class="commuted-sum-details">
                  <el-descriptions :column="2" border size="small">
                    <el-descriptions-item label="Case">{{ getCaseRef(row.caseId) }}</el-descriptions-item>
                    <el-descriptions-item label="Type">{{ row.sumType }}</el-descriptions-item>
                    <el-descriptions-item label="Payment Date">{{ formatDate(row.paymentDate) }}</el-descriptions-item>
                    <el-descriptions-item label="Payment Ref">{{ row.paymentReference || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="Validity Period">{{ row.validityPeriod ? `${row.validityPeriod} months` : '—' }}</el-descriptions-item>
                    <el-descriptions-item label="Renewal Date">{{ formatDate(row.renewalDate) }}</el-descriptions-item>
                    <el-descriptions-item label="Description" :span="2">{{ row.description || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="Notes" :span="2">{{ row.notes || '—' }}</el-descriptions-item>
                  </el-descriptions>
                  <div v-if="row.maintenanceSchedule?.length" class="maintenance-table mt-4">
                    <h4>Maintenance Schedule</h4>
                    <el-table :data="row.maintenanceSchedule" size="small" border>
                      <el-table-column prop="year" label="Year" width="80" />
                      <el-table-column label="Cost" width="120">
                        <template #default="{ row: m }">£{{ m.maintenanceCost.toLocaleString() }}</template>
                      </el-table-column>
                      <el-table-column label="Reviewed" width="100">
                        <template #default="{ row: m }">
                          <el-tag :type="m.reviewed ? 'success' : 'info'" size="small">{{ m.reviewed ? 'Yes' : 'No' }}</el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column prop="notes" label="Notes" />
                    </el-table>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="sumReference" label="Ref" width="140" />
            <el-table-column label="Case" width="120">
              <template #default="{ row }">{{ getCaseRef(row.caseId) }}</template>
            </el-table-column>
            <el-table-column label="Type" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ row.sumType.replace('_', ' ') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="Description" min-width="200" show-overflow-tooltip />
            <el-table-column label="Calculated" width="140">
              <template #default="{ row }">{{ formatCurrency(row.calculatedAmount) }}</template>
            </el-table-column>
            <el-table-column label="Agreed" width="140">
              <template #default="{ row }">{{ formatCurrency(row.agreedAmount) }}</template>
            </el-table-column>
            <el-table-column label="Status" width="120">
              <template #default="{ row }"><StatusBadge :status="row.status" /></template>
            </el-table-column>
            <el-table-column label="Actions" width="140" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="View" @click="openViewDialog('commutedSum', row)">View</el-button>
                <el-button link type="primary" :icon="Edit" @click="openCommutedSumDialog(row)">Edit</el-button>
                <el-button link type="danger" :icon="Delete" @click="deleteCommutedSum(row.id)">Del</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="commutedSumFilters.page"
            :page-size="commutedSumFilters.limit"
            :total="commutedSumTotal"
            layout="total, prev, pager, next"
            @current-change="loadCommutedSums"
          />
        </el-tab-pane>

        <!-- Snagging Tab -->
        <el-tab-pane label="Snagging" name="snagging">
          <div class="filter-bar">
            <el-select v-model="snaggingFilters.caseId" placeholder="Case" clearable filterable @change="loadSnaggingItems">
              <el-option v-for="c in cases" :key="c.id" :label="c.caseRef" :value="c.id" />
            </el-select>
            <el-select v-model="snaggingFilters.category" placeholder="Category" clearable @change="loadSnaggingItems">
              <el-option v-for="s in snaggingCategoryOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
            <el-select v-model="snaggingFilters.status" placeholder="Status" clearable @change="loadSnaggingItems">
              <el-option v-for="s in snaggingStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
            <el-select v-model="snaggingFilters.priority" placeholder="Priority" clearable @change="loadSnaggingItems">
              <el-option v-for="s in snaggingPriorityOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
            <el-button type="primary" :icon="Plus" @click="openSnaggingDialog()">New Item</el-button>
          </div>

          <el-table v-loading="loading" :data="snaggingItems" stripe>
            <el-table-column type="expand">
              <template #default="{ row }">
                <div class="snagging-details">
                  <el-descriptions :column="2" border size="small">
                    <el-descriptions-item label="Case">{{ getCaseRef(row.caseId) }}</el-descriptions-item>
                    <el-descriptions-item label="Assigned To">{{ row.assignedTo || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="Target Date">{{ formatDate(row.targetCompletionDate) }}</el-descriptions-item>
                    <el-descriptions-item label="Completion Date">{{ formatDate(row.actualCompletionDate) }}</el-descriptions-item>
                    <el-descriptions-item label="Description" :span="2">{{ row.description }}</el-descriptions-item>
                    <el-descriptions-item label="Notes" :span="2">{{ row.notes || '—' }}</el-descriptions-item>
                  </el-descriptions>
                  <div v-if="row.inspections?.length" class="inspections-list mt-4">
                    <h4>Inspections</h4>
                    <el-table :data="row.inspections" size="small" border>
                      <el-table-column prop="inspectionDate" label="Date" width="110">
                        <template #default="{ row: i }">{{ formatDate(i.inspectionDate) }}</template>
                      </el-table-column>
                      <el-table-column prop="inspectorName" label="Inspector" width="140" />
                      <el-table-column label="Result" width="100">
                        <template #default="{ row: i }">
                          <el-tag :type="i.result === 'pass' ? 'success' : i.result === 'fail' ? 'danger' : 'warning'" size="small">
                            {{ i.result }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column prop="comments" label="Comments" />
                      <el-table-column label="Reinspection" width="120">
                        <template #default="{ row: i }">
                          {{ i.reinspectionRequired ? formatDate(i.reinspectionDate) : 'No' }}
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="itemReference" label="Ref" width="120" />
            <el-table-column label="Case" width="120">
              <template #default="{ row }">{{ getCaseRef(row.caseId) }}</template>
            </el-table-column>
            <el-table-column label="Category" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ row.category.replace('_', ' ') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="Location" min-width="150" show-overflow-tooltip />
            <el-table-column prop="description" label="Description" min-width="200" show-overflow-tooltip />
            <el-table-column label="Priority" width="100">
              <template #default="{ row }">
                <el-tag :type="snaggingPriorityColorMap[row.priority]" size="small">{{ row.priority }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Status" width="130">
              <template #default="{ row }">
                <el-tag :type="getSnaggingStatusColor(row.status)" size="small">{{ row.status.replace('_', ' ') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="View" @click="openViewDialog('snagging', row)">View</el-button>
                <el-button link type="primary" :icon="Edit" @click="openSnaggingDialog(row)">Edit</el-button>
                <el-dropdown trigger="click">
                  <el-button link type="primary" size="small">Status</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-for="s in snaggingStatusOptions" :key="s.value" @click="updateSnaggingStatus(row.id, s.value)">
                        {{ s.label }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-button link type="danger" :icon="Delete" @click="deleteSnagging(row.id)">Del</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="snaggingFilters.page"
            :page-size="snaggingFilters.limit"
            :total="snaggingTotal"
            layout="total, prev, pager, next"
            @current-change="loadSnaggingItems"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Case Dialog -->
    <el-dialog v-model="caseDialogVisible" :title="editingId ? 'Edit Case' : 'New Case'" width="600px">
      <el-form :model="caseForm" label-width="140px">
        <el-form-item label="Case Reference" required>
          <el-input v-model="caseForm.caseRef" />
        </el-form-item>
        <el-form-item label="Type" required>
          <el-select v-model="caseForm.type" style="width: 100%">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Title" required>
          <el-input v-model="caseForm.title" />
        </el-form-item>
        <el-form-item label="Client" required>
          <el-select v-model="caseForm.clientId" filterable style="width: 100%">
            <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="LA/Water Authority">
          <el-input v-model="caseForm.laWaterAuthority" />
        </el-form-item>
        <el-form-item label="Status" required>
          <el-select v-model="caseForm.status" style="width: 100%">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Bond Value">
          <el-input-number v-model="caseForm.bondValue" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Bond Release Date">
          <el-date-picker v-model="caseForm.bondReleaseDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="caseDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveCase">Save</el-button>
      </template>
    </el-dialog>

    <!-- Bond Dialog -->
    <el-dialog v-model="bondDialogVisible" :title="editingId ? 'Edit Bond' : 'New Bond'" width="600px">
      <el-form :model="bondForm" label-width="140px">
        <el-form-item label="Adoption Case" required>
          <el-select v-model="bondForm.caseId" filterable style="width: 100%">
            <el-option v-for="c in cases" :key="c.id" :label="c.caseRef + ' - ' + c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Bond Reference" required>
          <el-input v-model="bondForm.bondReference" />
        </el-form-item>
        <el-form-item label="Bond Type" required>
          <el-select v-model="bondForm.bondType" style="width: 100%">
            <el-option v-for="t in bondTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Issuing Bank">
          <el-input v-model="bondForm.issuingBank" />
        </el-form-item>
        <el-form-item label="Bond Amount" required>
          <el-input-number v-model="bondForm.bondAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Effective Date" required>
          <el-date-picker v-model="bondForm.effectiveDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Expiry Date">
          <el-date-picker v-model="bondForm.expiryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="bondForm.status" style="width: 100%">
            <el-option v-for="s in bondStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Notes">
          <el-input v-model="bondForm.notes" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bondDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveBond">Save</el-button>
      </template>
    </el-dialog>

    <!-- Commuted Sum Dialog -->
    <el-dialog v-model="commutedSumDialogVisible" :title="editingId ? 'Edit Commuted Sum' : 'New Commuted Sum'" width="600px">
      <el-form :model="commutedSumForm" label-width="140px">
        <el-form-item label="Adoption Case" required>
          <el-select v-model="commutedSumForm.caseId" filterable style="width: 100%">
            <el-option v-for="c in cases" :key="c.id" :label="c.caseRef + ' - ' + c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Reference" required>
          <el-input v-model="commutedSumForm.sumReference" />
        </el-form-item>
        <el-form-item label="Type" required>
          <el-select v-model="commutedSumForm.sumType" style="width: 100%">
            <el-option v-for="t in commutedSumTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Description" required>
          <el-input v-model="commutedSumForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="Calculated Amount" required>
          <el-input-number v-model="commutedSumForm.calculatedAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Agreed Amount">
          <el-input-number v-model="commutedSumForm.agreedAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="commutedSumForm.status" style="width: 100%">
            <el-option v-for="s in commutedSumStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Validity Period (months)">
          <el-input-number v-model="commutedSumForm.validityPeriod" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Notes">
          <el-input v-model="commutedSumForm.notes" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="commutedSumDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveCommutedSum">Save</el-button>
      </template>
    </el-dialog>

    <!-- Snagging Dialog -->
    <el-dialog v-model="snaggingDialogVisible" :title="editingId ? 'Edit Snagging Item' : 'New Snagging Item'" width="600px">
      <el-form :model="snaggingForm" label-width="140px">
        <el-form-item label="Adoption Case" required>
          <el-select v-model="snaggingForm.caseId" filterable style="width: 100%">
            <el-option v-for="c in cases" :key="c.id" :label="c.caseRef + ' - ' + c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Item Reference" required>
          <el-input v-model="snaggingForm.itemReference" />
        </el-form-item>
        <el-form-item label="Category" required>
          <el-select v-model="snaggingForm.category" style="width: 100%">
            <el-option v-for="c in snaggingCategoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Location" required>
          <el-input v-model="snaggingForm.location" />
        </el-form-item>
        <el-form-item label="Description" required>
          <el-input v-model="snaggingForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="Priority" required>
          <el-select v-model="snaggingForm.priority" style="width: 100%">
            <el-option v-for="p in snaggingPriorityOptions" :key="p.value" :label="p.label" :value="p.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="snaggingForm.status" style="width: 100%">
            <el-option v-for="s in snaggingStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Identified Date" required>
          <el-date-picker v-model="snaggingForm.identifiedDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Target Completion">
          <el-date-picker v-model="snaggingForm.targetCompletionDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Assigned To">
          <el-input v-model="snaggingForm.assignedTo" />
        </el-form-item>
        <el-form-item label="Notes">
          <el-input v-model="snaggingForm.notes" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="snaggingDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveSnagging">Save</el-button>
      </template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewDialogVisible" :title="viewType === 'case' ? 'Case Details' : viewType === 'bond' ? 'Bond Details' : viewType === 'commutedSum' ? 'Commuted Sum Details' : 'Snagging Item Details'" width="700px">
      <template v-if="viewItem">
        <!-- Adoption Case View -->
        <el-descriptions v-if="viewType === 'case'" :column="2" border>
          <el-descriptions-item label="Case Ref">{{ viewItem.caseRef }}</el-descriptions-item>
          <el-descriptions-item label="Type">
            <el-tag size="small">{{ viewItem.type.toUpperCase() }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Title" :span="2">{{ viewItem.title }}</el-descriptions-item>
          <el-descriptions-item label="Client">{{ getClientName(viewItem.clientId) }}</el-descriptions-item>
          <el-descriptions-item label="LA/Water Authority">{{ viewItem.laWaterAuthority || '—' }}</el-descriptions-item>
          <el-descriptions-item label="Status" :span="2"><StatusBadge :status="viewItem.status" /></el-descriptions-item>
          <el-descriptions-item label="Bond Value">{{ formatCurrency(viewItem.bondValue) }}</el-descriptions-item>
          <el-descriptions-item label="Bond Release Date">{{ formatDate(viewItem.bondReleaseDate) }}</el-descriptions-item>
          <el-descriptions-item label="Created">{{ formatDate(viewItem.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="Updated">{{ formatDate(viewItem.updatedAt) }}</el-descriptions-item>
        </el-descriptions>

        <!-- Bond View -->
        <el-descriptions v-else-if="viewType === 'bond'" :column="2" border>
          <el-descriptions-item label="Reference">{{ viewItem.bondReference }}</el-descriptions-item>
          <el-descriptions-item label="Status">
            <el-tag :type="bondStatusColorMap[viewItem.status]" size="small">{{ viewItem.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Case">{{ getCaseRef(viewItem.caseId) }}</el-descriptions-item>
          <el-descriptions-item label="Type">{{ viewItem.bondType }}</el-descriptions-item>
          <el-descriptions-item label="Amount">{{ formatCurrency(viewItem.bondAmount) }}</el-descriptions-item>
          <el-descriptions-item label="Issuing Bank">{{ viewItem.issuingBank || '—' }}</el-descriptions-item>
          <el-descriptions-item label="Effective Date">{{ formatDate(viewItem.effectiveDate) }}</el-descriptions-item>
          <el-descriptions-item label="Expiry Date">{{ formatDate(viewItem.expiryDate) }}</el-descriptions-item>
          <el-descriptions-item label="Release Date">{{ formatDate(viewItem.releaseDate) }}</el-descriptions-item>
          <el-descriptions-item label="Notes" :span="2">{{ viewItem.notes || '—' }}</el-descriptions-item>
        </el-descriptions>

        <!-- Commuted Sum View -->
        <el-descriptions v-else-if="viewType === 'commutedSum'" :column="2" border>
          <el-descriptions-item label="Reference">{{ viewItem.sumReference }}</el-descriptions-item>
          <el-descriptions-item label="Status"><StatusBadge :status="viewItem.status" /></el-descriptions-item>
          <el-descriptions-item label="Case">{{ getCaseRef(viewItem.caseId) }}</el-descriptions-item>
          <el-descriptions-item label="Type">{{ viewItem.sumType }}</el-descriptions-item>
          <el-descriptions-item label="Description" :span="2">{{ viewItem.description }}</el-descriptions-item>
          <el-descriptions-item label="Calculated">{{ formatCurrency(viewItem.calculatedAmount) }}</el-descriptions-item>
          <el-descriptions-item label="Agreed">{{ formatCurrency(viewItem.agreedAmount) }}</el-descriptions-item>
          <el-descriptions-item label="Payment Date">{{ formatDate(viewItem.paymentDate) }}</el-descriptions-item>
          <el-descriptions-item label="Payment Ref">{{ viewItem.paymentReference || '—' }}</el-descriptions-item>
          <el-descriptions-item label="Validity">{{ viewItem.validityPeriod ? `${viewItem.validityPeriod} months` : '—' }}</el-descriptions-item>
          <el-descriptions-item label="Renewal Date">{{ formatDate(viewItem.renewalDate) }}</el-descriptions-item>
          <el-descriptions-item label="Notes" :span="2">{{ viewItem.notes || '—' }}</el-descriptions-item>
        </el-descriptions>

        <!-- Snagging View -->
        <el-descriptions v-else :column="2" border>
          <el-descriptions-item label="Reference">{{ viewItem.itemReference }}</el-descriptions-item>
          <el-descriptions-item label="Status">
            <el-tag :type="getSnaggingStatusColor(viewItem.status)" size="small">{{ viewItem.status.replace('_', ' ') }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Case">{{ getCaseRef(viewItem.caseId) }}</el-descriptions-item>
          <el-descriptions-item label="Category">{{ viewItem.category.replace('_', ' ') }}</el-descriptions-item>
          <el-descriptions-item label="Location">{{ viewItem.location }}</el-descriptions-item>
          <el-descriptions-item label="Priority">
            <el-tag :type="snaggingPriorityColorMap[viewItem.priority]" size="small">{{ viewItem.priority }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Description" :span="2">{{ viewItem.description }}</el-descriptions-item>
          <el-descriptions-item label="Identified">{{ formatDate(viewItem.identifiedDate) }}</el-descriptions-item>
          <el-descriptions-item label="Target">{{ formatDate(viewItem.targetCompletionDate) }}</el-descriptions-item>
          <el-descriptions-item label="Completed">{{ formatDate(viewItem.actualCompletionDate) }}</el-descriptions-item>
          <el-descriptions-item label="Assigned">{{ viewItem.assignedTo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="Notes" :span="2">{{ viewItem.notes || '—' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="viewDialogVisible = false">Close</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.adoption-view {
  .alert-row { margin-bottom: 16px; }
  .alert-card {
    &.bond-alert { background: #fef0f0; border-color: #f56c6c; }
    &.snagging-alert { background: #fdf6ec; border-color: #e6a23c; }
    .card-header {
      display: flex; align-items: center; gap: 8px;
      font-weight: 600;
      &.bond-alert { color: #f56c6c; }
      &.snagging-alert { color: #e6a23c; }
    }
    .alert-item {
      padding: 8px 0;
      border-bottom: 1px solid rgba(0,0,0,0.1);
      cursor: pointer;
      &:last-child { border-bottom: none; }
      &:hover { color: var(--el-color-primary); }
    }
  }
  .filter-bar {
    display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap;
  }
  .stage-history, .bond-details, .commuted-sum-details, .snagging-details {
    padding: 16px 48px;
    h4 { margin-bottom: 12px; font-size: 14px; color: #606266; }
    .no-stages { color: #909399; font-style: italic; }
  }
  .text-danger { color: #f56c6c; font-weight: 600; }
  .text-warning { color: #e6a23c; }
}
.mt-4 { margin-top: 16px; }
</style>