<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import type { FormInstance } from 'element-plus'
import api, { type Site, type Company } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()

const loading = ref(false)
const tableData = ref<Site[]>([])
const total = ref(0)
const clients = ref<Company[]>([])

const filters = reactive({
  search: '',
  status: '',
  clientId: '',
  page: 1,
  limit: 20
})

const statusOptions = [
  { label: 'Planning', value: 'planning' },
  { label: 'Active', value: 'active' },
  { label: 'On Hold', value: 'on_hold' },
  { label: 'Completed', value: 'completed' }
]

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const editingId = ref('')
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  clientId: '',
  address: {
    addressLine1: '',
    addressLine2: '',
    city: '',
    county: '',
    postcode: ''
  },
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  status: 'planning' as Site['status'],
  notes: ''
})

onMounted(() => {
  loadData()
  loadClients()
})

const loadData = async () => {
  loading.value = true
  try {
    const response = await api.sites.getAll({
      search: filters.search || undefined,
      status: filters.status || undefined,
      clientId: filters.clientId || undefined,
      page: filters.page,
      limit: filters.limit
    })
    tableData.value = response.data.data
    total.value = response.data.total
  } catch { ElMessage.error('Failed to load sites') } finally { loading.value = false }
}

const loadClients = async () => {
  try {
    const response = await api.companies.getAll({ type: 'client', limit: 100 })
    clients.value = response.data.data
  } catch {}
}

const handleSearch = () => {
  filters.page = 1
  loadData()
}

const handlePageChange = (page: number) => {
  filters.page = page
  loadData()
}

const openAddDialog = () => {
  dialogMode.value = 'add'
  editingId.value = ''
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row: Site) => {
  dialogMode.value = 'edit'
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    clientId: row.clientId,
    address: {
      addressLine1: row.address.addressLine1,
      addressLine2: row.address.addressLine2 || '',
      city: row.address.city,
      county: row.address.county || '',
      postcode: row.address.postcode
    },
    contactName: '',
    contactPhone: '',
    contactEmail: '',
    status: row.status,
    notes: ''
  })
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    dialogLoading.value = true
    try {
      if (dialogMode.value === 'edit') {
        await api.sites.update(editingId.value, form)
        ElMessage.success('Site updated')
      } else {
        await api.sites.create(form)
        ElMessage.success('Site created')
      }
      dialogVisible.value = false
      loadData()
    } catch { ElMessage.error('Failed to save site') } finally { dialogLoading.value = false }
  })
}

const handleDelete = async (row: Site) => {
  try {
    await ElMessageBox.confirm(
      `Delete site "${row.name}"?`,
      'Confirm Delete',
      { type: 'warning' }
    )
    await api.sites.delete(row.id)
    ElMessage.success('Site deleted')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to delete site')
    }
  }
}

const resetForm = () => {
  Object.assign(form, {
    name: '',
    clientId: '',
    address: { addressLine1: '', addressLine2: '', city: '', county: '', postcode: '' },
    contactName: '',
    contactPhone: '',
    contactEmail: '',
    status: 'planning',
    notes: ''
  })
  formRef.value?.resetFields()
}

const viewSite = (row: Site) => {
  router.push(`/sites/${row.id}`)
}

const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'
</script>

<template>
  <div class="sites-view">
    <PageHeader title="Sites" :breadcrumbs="[{ title: 'Sites' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openAddDialog">Add Site</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="6">
          <el-input v-model="filters.search" placeholder="Search..." clearable :prefix-icon="Search" @keyup.enter="handleSearch" />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.status" placeholder="Status" clearable @change="handleSearch">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.clientId" placeholder="Client" clearable filterable @change="handleSearch">
            <el-option v-for="c in clients" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :md="6" class="filter-actions">
          <el-button @click="() => { filters.search = ''; filters.status = ''; filters.clientId = ''; loadData() }">Reset</el-button>
          <el-button type="primary" @click="handleSearch">Search</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="siteCode" label="Code" width="100" />
        <el-table-column prop="name" label="Site Name" min-width="200" />
        <el-table-column label="Client" min-width="180">
          <template #default="{ row }">{{ row.client?.name || '—' }}</template>
        </el-table-column>
        <el-table-column prop="address.city" label="City" width="120" />
        <el-table-column label="Status" width="120">
          <template #default="{ row }"><StatusBadge :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="Start Date" width="120">
          <template #default="{ row }">{{ formatDate(row.startDate) }}</template>
        </el-table-column>
        <el-table-column label="Actions" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewSite(row)">View</el-button>
            <el-button link type="primary" @click="openEditDialog(row)">Edit</el-button>
            <el-button link type="danger" @click="handleDelete(row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="filters.page" :page-size="filters.limit" :total="total" @current-change="handlePageChange" />
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? 'Add Site' : 'Edit Site'" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-position="top">
        <el-form-item label="Site Name" prop="name" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Client" prop="clientId" required>
          <el-select v-model="form.clientId" placeholder="Select client" filterable style="width: 100%">
            <el-option v-for="c in clients" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Status" prop="status" required>
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-divider content-position="left">Address</el-divider>
        <el-form-item label="Address Line 1" prop="address.addressLine1" required>
          <el-input v-model="form.address.addressLine1" />
        </el-form-item>
        <el-form-item label="Address Line 2">
          <el-input v-model="form.address.addressLine2" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="City" prop="address.city" required>
              <el-input v-model="form.address.city" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="County">
              <el-input v-model="form.address.county" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Postcode" prop="address.postcode" required>
          <el-input v-model="form.address.postcode" />
        </el-form-item>
        <el-divider content-position="left">Site Contact</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Contact Name">
              <el-input v-model="form.contactName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Contact Phone">
              <el-input v-model="form.contactPhone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Contact Email">
          <el-input v-model="form.contactEmail" type="email" />
        </el-form-item>
        <el-form-item label="Notes">
          <el-input v-model="form.notes" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="handleSave">{{ dialogMode === 'edit' ? 'Update' : 'Create' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.sites-view { 
  .filter-card { margin-bottom: 16px; }
  .filter-actions { display: flex; justify-content: flex-end; gap: 8px; }
}
</style>