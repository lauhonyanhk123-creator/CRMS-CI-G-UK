<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Delete, View, Edit } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import type { FormInstance } from 'element-plus'
import api, { type Company, type Address, type BankDetails } from '@/services/api'; import type { ElTagType } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import dayjs from 'dayjs'

const router = useRouter()

// Table state
const loading = ref(false)
const tableData = ref<Company[]>([])
const total = ref(0)

// Filters
const filters = reactive({
  search: '',
  type: '',
  page: 1,
  limit: 20
})

// Drawer state
const drawerVisible = ref(false)
const drawerTitle = ref('Add Company')
const drawerLoading = ref(false)
const isEditing = ref(false)
const editingId = ref('')

// Form
const formRef = ref<FormInstance>()
const form = reactive({
  name: '',
  companyType: 'client' as Company['companyType'],
  registrationNumber: '',
  vatNumber: '',
  address: {
    addressLine1: '',
    addressLine2: '',
    city: '',
    county: '',
    postcode: ''
  } as Address,
  phone: '',
  email: '',
  cisStatus: 'not_applicable' as Company['cisStatus'],
  bankDetails: {
    bankName: '',
    sortCode: '',
    accountNumber: '',
    accountName: ''
  } as BankDetails,
  status: 'active' as Company['status']
})

const formRules = {
  name: [{ required: true, message: 'Company name is required', trigger: 'blur' }],
  companyType: [{ required: true, message: 'Company type is required', trigger: 'change' }]
}

const companyTypes = [
  { label: 'Client', value: 'client' },
  { label: 'Subcontractor', value: 'subcontractor' },
  { label: 'Supplier', value: 'supplier' },
  { label: 'Consultant', value: 'consultant' },
  { label: 'Other', value: 'other' }
]

const cisStatuses = [
  { label: 'Verified', value: 'verified', type: 'success' },
  { label: 'Pending', value: 'pending', type: 'warning' },
  { label: 'Expired', value: 'expired', type: 'danger' },
  { label: 'N/A', value: 'not_applicable', type: 'info' }
]

// Load data
onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const response = await api.companies.getAll({
      search: filters.search || undefined,
      type: filters.type || undefined,
      page: filters.page,
      limit: filters.limit
    })
    tableData.value = response.data.data
    total.value = response.data.total
  } catch (error) {
    ElMessage.error('Failed to load companies')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  filters.page = 1
  loadData()
}

const handlePageChange = (page: number) => {
  filters.page = page
  loadData()
}

const handleFilterChange = () => {
  filters.page = 1
  loadData()
}

const resetFilters = () => {
  filters.search = ''
  filters.type = ''
  filters.page = 1
  loadData()
}

// CRUD operations
const openAddDrawer = () => {
  isEditing.value = false
  drawerTitle.value = 'Add Company'
  resetForm()
  drawerVisible.value = true
}

const openEditDrawer = (row: Company) => {
  isEditing.value = true
  drawerTitle.value = 'Edit Company'
  editingId.value = row.id
  
  Object.assign(form, {
    name: row.name,
    companyType: row.companyType,
    registrationNumber: row.registrationNumber || '',
    vatNumber: row.vatNumber || '',
    address: { ...row.address },
    phone: row.phone || '',
    email: row.email || '',
    cisStatus: row.cisStatus || 'not_applicable',
    bankDetails: row.bankDetails ? { ...row.bankDetails } : {
      bankName: '',
      sortCode: '',
      accountNumber: '',
      accountName: ''
    },
    status: row.status
  })
  
  drawerVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    drawerLoading.value = true
    try {
      if (isEditing.value) {
        await api.companies.update(editingId.value, form)
        ElMessage.success('Company updated successfully')
      } else {
        await api.companies.create(form)
        ElMessage.success('Company created successfully')
      }
      drawerVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error(`Failed to ${isEditing.value ? 'update' : 'create'} company`)
    } finally {
      drawerLoading.value = false
    }
  })
}

const handleDelete = async (row: Company) => {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to delete "${row.name}"? This action cannot be undone.`,
      'Confirm Delete',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    await api.companies.delete(row.id)
    ElMessage.success('Company deleted successfully')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to delete company')
    }
  }
}

const resetForm = () => {
  Object.assign(form, {
    name: '',
    companyType: 'client',
    registrationNumber: '',
    vatNumber: '',
    address: {
      addressLine1: '',
      addressLine2: '',
      city: '',
      county: '',
      postcode: ''
    },
    phone: '',
    email: '',
    cisStatus: 'not_applicable',
    bankDetails: {
      bankName: '',
      sortCode: '',
      accountNumber: '',
      accountName: ''
    },
    status: 'active'
  })
  formRef.value?.resetFields()
}

const handleView = (row: Company) => {
  router.push(`/companies/${row.id}`)
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getCisStatusType = (status: string): TagType => {
  const statusMap: Record<string, TagType> = {
    verified: 'success',
    pending: 'warning',
    expired: 'danger',
    not_applicable: 'info'
  }
  return statusMap[status] ?? 'info'
}

const getCompanyTypeLabel = (type: string) => {
  return companyTypes.find(t => t.value === type)?.label || type
}
</script>

<template>
  <div class="companies-view">
    <PageHeader title="Companies" :breadcrumbs="[{ title: 'Companies' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openAddDrawer">
          Add Company
        </el-button>
      </template>
    </PageHeader>

    <!-- Filters -->
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8">
          <el-input
            v-model="filters.search"
            placeholder="Search by name..."
            clearable
            :prefix-icon="Search"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select
            v-model="filters.type"
            placeholder="Filter by type"
            clearable
            @change="handleFilterChange"
          >
            <el-option
              v-for="type in companyTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-col>
        <el-col :xs="24" :md="10" class="filter-actions">
          <el-button @click="resetFilters">Reset</el-button>
          <el-button type="primary" @click="handleSearch">Search</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- Table -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="Name" min-width="200" />
        
        <el-table-column label="Type" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getCompanyTypeLabel(row.companyType) }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="CIS Status" width="120">
          <template #default="{ row }">
            <el-tag 
              v-if="row.cisStatus" 
              :type="getCisStatusType(row.cisStatus)" 
              size="small"
            >
              {{ row.cisStatus.replace('_', ' ').toUpperCase() }}
            </el-tag>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="phone" label="Phone" width="140">
          <template #default="{ row }">
            {{ row.phone || '—' }}
          </template>
        </el-table-column>
        
        <el-table-column label="Status" width="100">
          <template #default="{ row }">
            <StatusBadge :status="row.status" />
          </template>
        </el-table-column>
        
        <el-table-column label="Actions" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="handleView(row)">
              View
            </el-button>
            <el-button link type="primary" :icon="Edit" @click="openEditDrawer(row)">
              Edit
            </el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">
              Delete
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="filters.page"
        :page-size="filters.limit"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- Add/Edit Drawer -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerTitle"
      size="600px"
      :before-close="() => drawerVisible = false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="Company Name" prop="name">
          <el-input v-model="form.name" placeholder="Enter company name" />
        </el-form-item>
        
        <el-form-item label="Company Type" prop="companyType">
          <el-select v-model="form.companyType" placeholder="Select type">
            <el-option
              v-for="type in companyTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Registration Number">
              <el-input v-model="form.registrationNumber" placeholder="Companies House No." />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="VAT Number">
              <el-input v-model="form.vatNumber" placeholder="VAT Reg No." />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">Address</el-divider>
        
        <el-form-item label="Address Line 1">
          <el-input v-model="form.address.addressLine1" placeholder="Street address" />
        </el-form-item>
        
        <el-form-item label="Address Line 2">
          <el-input v-model="form.address.addressLine2" placeholder="Building, suite, etc." />
        </el-form-item>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="City">
              <el-input v-model="form.address.city" placeholder="City" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="County">
              <el-input v-model="form.address.county" placeholder="County" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="Postcode">
          <el-input v-model="form.address.postcode" placeholder="Postcode" />
        </el-form-item>
        
        <el-divider content-position="left">Contact</el-divider>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Phone">
              <el-input v-model="form.phone" placeholder="Phone number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Email">
              <el-input v-model="form.email" placeholder="Email address" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item v-if="form.companyType === 'subcontractor'" label="CIS Status">
          <el-select v-model="form.cisStatus" placeholder="Select CIS status">
            <el-option
              v-for="status in cisStatuses"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
        
        <el-divider content-position="left">Bank Details</el-divider>
        
        <el-form-item label="Bank Name">
          <el-input v-model="form.bankDetails.bankName" placeholder="Bank name" />
        </el-form-item>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Sort Code">
              <el-input v-model="form.bankDetails.sortCode" placeholder="00-00-00" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Account Number">
              <el-input v-model="form.bankDetails.accountNumber" placeholder="Account number" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="Account Name">
          <el-input v-model="form.bankDetails.accountName" placeholder="Account holder name" />
        </el-form-item>
        
        <el-form-item label="Status">
          <el-radio-group v-model="form.status">
            <el-radio label="active">Active</el-radio>
            <el-radio label="inactive">Inactive</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">Cancel</el-button>
          <el-button type="primary" :loading="drawerLoading" @click="handleSubmit">
            {{ isEditing ? 'Update' : 'Create' }}
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.companies-view {
  .filter-card {
    margin-bottom: 16px;
    
    .filter-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
  }
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
