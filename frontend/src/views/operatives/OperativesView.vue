<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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

// Dialog state
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const formRef = ref()

const formData = reactive({
  id: '',
  firstName: '',
  surname: '',
  niNumber: '',
  dateOfBirth: '',
  employmentStatus: '',
  trade: '',
  cscsNumber: '',
  cscsExpiry: '',
  cscsCardType: '',
  companyId: '',
  email: '',
  phone: ''
})

const resetForm = () => {
  formData.id = ''
  formData.firstName = ''
  formData.surname = ''
  formData.niNumber = ''
  formData.dateOfBirth = ''
  formData.employmentStatus = ''
  formData.trade = ''
  formData.cscsNumber = ''
  formData.cscsExpiry = ''
  formData.cscsCardType = ''
  formData.companyId = ''
  formData.email = ''
  formData.phone = ''
}

const handleAdd = () => {
  resetForm()
  dialogMode.value = 'add'
  dialogVisible.value = true
}

const handleEdit = (row: Operative) => {
  formData.id = row.id
  formData.firstName = row.firstName || ''
  formData.surname = row.lastName || ''
  formData.niNumber = row.niNumber || ''
  formData.dateOfBirth = row.dateOfBirth || ''
  formData.employmentStatus = row.status || ''
  formData.trade = row.trade || ''
  formData.cscsNumber = row.cscsCard?.cardNumber || ''
  formData.cscsExpiry = row.cscsCard?.expiryDate || ''
  formData.cscsCardType = row.cscsCard?.cardType || ''
  formData.companyId = row.employer?.id || ''
  formData.email = row.email || ''
  formData.phone = row.phone || ''
  dialogMode.value = 'edit'
  dialogVisible.value = true
}

const handleView = (row: Operative) => {
  handleEdit(row)
  dialogMode.value = 'edit'
}

const handleDelete = async (row: Operative) => {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to delete operative "${row.firstName} ${row.lastName}"?`,
      'Confirm Delete',
      { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning' }
    )
    await api.operatives.delete(row.id)
    ElMessage.success('Operative deleted successfully')
    loadData()
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error('Failed to delete operative')
    }
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      const payload = {
        firstName: formData.firstName,
        lastName: formData.surname,
        niNumber: formData.niNumber,
        dateOfBirth: formData.dateOfBirth,
        status: formData.employmentStatus as Operative['status'],
        trade: formData.trade,
        cscsCard: formData.cscsNumber ? {
          cardNumber: formData.cscsNumber,
          expiryDate: formData.cscsExpiry,
          cardType: formData.cscsCardType
        } : undefined,
        employerId: formData.companyId || undefined,
        email: formData.email,
        phone: formData.phone
      }
      if (dialogMode.value === 'add') {
        await api.operatives.create(payload)
        ElMessage.success('Operative created successfully')
      } else {
        await api.operatives.update(formData.id, payload)
        ElMessage.success('Operative updated successfully')
      }
      dialogVisible.value = false
      loadData()
    } catch {
      ElMessage.error(`Failed to ${dialogMode.value === 'add' ? 'create' : 'update'} operative`)
    }
  })
}

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
    await api.operatives.smartCheckCard(operative.id, operative.cscsCard.id ?? '')
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

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getExpiryType = (status: string): TagType => {
  return status === 'expired' ? 'danger' : status === 'expiring' ? 'warning' : 'success'
}

const getInductionStatusType = (status: string): TagType => {
  const map: Record<string, TagType> = { complete: 'success', pending: 'warning', expired: 'danger' }
  return map[status] ?? 'info'
}
</script>

<template>
  <div class="operatives-view">
    <PageHeader title="Operatives" :breadcrumbs="[{ title: 'Operatives' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="handleAdd">Add Operative</el-button>
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
        <el-table-column label="Actions" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :icon="View" @click="handleView(row)">View</el-button>
            <el-button link type="primary" size="small" :icon="Edit" @click="handleEdit(row)">Edit</el-button>
            <el-button link type="danger" size="small" :icon="Delete" @click="handleDelete(row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="filters.page" :page-size="filters.limit" :total="total" @current-change="handlePageChange" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? 'Add Operative' : 'Edit Operative'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" label-width="140px">
        <el-form-item label="First Name" prop="firstName" :rules="[{ required: true, message: 'First name is required', trigger: 'blur' }]">
          <el-input v-model="formData.firstName" />
        </el-form-item>
        <el-form-item label="Surname" prop="surname" :rules="[{ required: true, message: 'Surname is required', trigger: 'blur' }]">
          <el-input v-model="formData.surname" />
        </el-form-item>
        <el-form-item label="NI Number" prop="niNumber">
          <el-input v-model="formData.niNumber" />
        </el-form-item>
        <el-form-item label="Date of Birth" prop="dateOfBirth">
          <el-date-picker v-model="formData.dateOfBirth" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Employment Status" prop="employmentStatus">
          <el-select v-model="formData.employmentStatus" placeholder="Select status" style="width: 100%">
            <el-option label="Active" value="ACTIVE" />
            <el-option label="Inactive" value="INACTIVE" />
            <el-option label="Suspended" value="SUSPENDED" />
            <el-option label="Terminated" value="TERMINATED" />
          </el-select>
        </el-form-item>
        <el-form-item label="Trade" prop="trade">
          <el-input v-model="formData.trade" />
        </el-form-item>
        <el-form-item label="CSCS Number" prop="cscsNumber">
          <el-input v-model="formData.cscsNumber" />
        </el-form-item>
        <el-form-item label="CSCS Expiry" prop="cscsExpiry">
          <el-date-picker v-model="formData.cscsExpiry" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="CSCS Card Type" prop="cscsCardType">
          <el-select v-model="formData.cscsCardType" placeholder="Select card type" style="width: 100%" clearable>
            <el-option label="Blue" value="BLUE" />
            <el-option label="Gold" value="GOLD" />
            <el-option label="Red" value="RED" />
            <el-option label="White" value="WHITE" />
            <el-option label="Green" value="GREEN" />
          </el-select>
        </el-form-item>
        <el-form-item label="Company" prop="companyId">
          <el-select v-model="formData.companyId" placeholder="Select company" style="width: 100%" clearable filterable>
            <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="formData.email" type="email" />
        </el-form-item>
        <el-form-item label="Phone" prop="phone">
          <el-input v-model="formData.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="handleSave">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.operatives-view {
  .filter-card {
    margin-bottom: 16px;
    .filter-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
  }
}
.operative-details {
  padding: 16px 48px;
  h4 {
    margin-bottom: 8px;
    font-size: 14px;
    color: #606266;
  }
}
.card-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.text-danger {
  color: #f56c6c;
}
.text-muted {
  color: #909399;
}
.mr-2 {
  margin-right: 8px;
}
.mb-2 {
  margin-bottom: 8px;
}
</style>
