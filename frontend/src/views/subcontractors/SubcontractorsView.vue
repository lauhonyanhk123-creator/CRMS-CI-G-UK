<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Search, Refresh, View, Edit, Delete, Upload } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import api, { type Subcontractor } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()

const loading = ref(false)
const tableData = ref<Subcontractor[]>([])
const total = ref(0)
const verifyingId = ref('')

// Dialog state
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const saving = ref(false)

// Form data
const formData = reactive({
  companyName: '',
  registrationNumber: '',
  vatNumber: '',
  street: '',
  city: '',
  postcode: '',
  phone: '',
  email: '',
  cisStatus: '' as '' | 'verified' | 'pending' | 'expired' | 'not_applicable',
  cisRate: 20,
  insuranceExpiry: '',
  notes: ''
})

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

const cisStatusOptions = [
  { label: 'Verified', value: 'verified' },
  { label: 'Pending', value: 'pending' },
  { label: 'Expired', value: 'expired' },
  { label: 'Not Applicable', value: 'not_applicable' }
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

// Add/Edit handlers
const handleAdd = () => {
  dialogMode.value = 'add'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: Subcontractor) => {
  dialogMode.value = 'edit'
  formData.companyName = row.name || ''
  formData.registrationNumber = row.registrationNumber || ''
  formData.vatNumber = row.vatNumber || ''
  formData.street = row.address?.addressLine1 || ''
  formData.city = row.address?.city || ''
  formData.postcode = row.address?.postcode || ''
  formData.phone = row.phone || ''
  formData.email = row.email || ''
  formData.cisStatus = row.cisStatus || ''
  formData.cisRate = row.cisRate || 20
  formData.insuranceExpiry = row.insuranceExpiry || ''
  formData.notes = row.notes || ''
  dialogVisible.value = true
}

const handleDelete = async (row: Subcontractor) => {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to delete subcontractor "${row.name}"? This action cannot be undone.`,
      'Confirm Delete',
      { confirmButtonText: 'Delete', cancelButtonText: 'Cancel', type: 'warning' }
    )
    await api.subcontractors.delete(row.id)
    ElMessage.success('Subcontractor deleted successfully')
    loadData()
  } catch (err: any) {
    if (err !== 'cancel') ElMessage.error('Failed to delete subcontractor')
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        name: formData.companyName,
        companyType: 'subcontractor' as const,
        registrationNumber: formData.registrationNumber,
        vatNumber: formData.vatNumber,
        address: {
          addressLine1: formData.street,
          city: formData.city,
          postcode: formData.postcode
        },
        phone: formData.phone,
        email: formData.email,
        cisStatus: formData.cisStatus || undefined,
        cisRate: formData.cisRate,
        insuranceExpiry: formData.insuranceExpiry || undefined,
        notes: formData.notes || undefined
      }
      
      if (dialogMode.value === 'add') {
        await api.subcontractors.create(payload)
        ElMessage.success('Subcontractor created successfully')
      } else {
        const row = tableData.value.find(r => r.name === formData.companyName)
        if (row) {
          await api.subcontractors.update(row.id, payload)
          ElMessage.success('Subcontractor updated successfully')
        }
      }
      dialogVisible.value = false
      loadData()
    } catch { ElMessage.error('Failed to save subcontractor') } finally { saving.value = false }
  })
}

const resetForm = () => {
  formData.companyName = ''
  formData.registrationNumber = ''
  formData.vatNumber = ''
  formData.street = ''
  formData.city = ''
  formData.postcode = ''
  formData.phone = ''
  formData.email = ''
  formData.cisStatus = ''
  formData.cisRate = 20
  formData.insuranceExpiry = ''
  formData.notes = ''
  formRef.value?.resetFields()
}

// CIS300 Submit handler
const submitCIS300 = async (row: Subcontractor) => {
  try {
    await ElMessageBox.confirm(
      `Submit CIS300 for subcontractor "${row.name}"? This will send the monthly CIS return to HMRC.`,
      'Confirm CIS300 Submission',
      { confirmButtonText: 'Submit', cancelButtonText: 'Cancel', type: 'warning' }
    )
    await api.cis.submitCisReturn({ subcontractorId: row.id })
    ElMessage.success('CIS300 submitted successfully')
    loadData()
  } catch (err: any) {
    if (err !== 'cancel') ElMessage.error('Failed to submit CIS300')
  }
}

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
        <el-button type="primary" :icon="Plus" @click="handleAdd">Add Subcontractor</el-button>
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
        <el-table-column label="Actions" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="router.push(`/companies/${row.id}`)">View</el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">Edit</el-button>
            <el-button 
              link 
              type="danger" 
              :icon="Delete"
              @click="handleDelete(row)"
            >
              Delete
            </el-button>
            <el-button 
              link 
              type="success" 
              :loading="verifyingId === row.id"
              @click="verifyCIS(row)"
            >
              Verify CIS
            </el-button>
            <el-button 
              link 
              type="warning" 
              :icon="Upload"
              @click="submitCIS300(row)"
            >
              CIS300
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

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? 'Add Subcontractor' : 'Edit Subcontractor'" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="formData" label-width="140px" class="subcontractor-form">
        <el-form-item label="Company Name" prop="companyName" :rules="[{ required: true, message: 'Company name is required' }]">
          <el-input v-model="formData.companyName" placeholder="Enter company name" />
        </el-form-item>
        <el-form-item label="Registration No." prop="registrationNumber">
          <el-input v-model="formData.registrationNumber" placeholder="Company registration number" />
        </el-form-item>
        <el-form-item label="VAT Number" prop="vatNumber">
          <el-input v-model="formData.vatNumber" placeholder="VAT registration number" />
        </el-form-item>
        <el-form-item label="Street Address" prop="street">
          <el-input v-model="formData.street" placeholder="Street address" />
        </el-form-item>
        <el-form-item label="City" prop="city">
          <el-input v-model="formData.city" placeholder="City" />
        </el-form-item>
        <el-form-item label="Postcode" prop="postcode">
          <el-input v-model="formData.postcode" placeholder="Postcode" />
        </el-form-item>
        <el-form-item label="Phone" prop="phone">
          <el-input v-model="formData.phone" placeholder="Phone number" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="formData.email" placeholder="Email address" type="email" />
        </el-form-item>
        <el-form-item label="CIS Status" prop="cisStatus">
          <el-select v-model="formData.cisStatus" placeholder="Select CIS status" clearable>
            <el-option v-for="opt in cisStatusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="CIS Rate (%)" prop="cisRate">
          <el-input-number v-model="formData.cisRate" :min="0" :max="100" :step="5" />
        </el-form-item>
        <el-form-item label="Insurance Expiry" prop="insuranceExpiry">
          <el-date-picker v-model="formData.insuranceExpiry" type="date" placeholder="Select expiry date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Notes" prop="notes">
          <el-input v-model="formData.notes" type="textarea" :rows="3" placeholder="Additional notes" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.subcontractors-view { .filter-card { margin-bottom: 16px; .filter-actions { display: flex; justify-content: flex-end; gap: 8px; } } }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.subcontractor-form { max-width: 500px; }
</style>