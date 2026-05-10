<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
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
const companies = ref<Company[]>([])

const filters = reactive({
  search: '',
  status: '',
  clientId: '',
  startDate: '',
  endDate: '',
  page: 1,
  limit: 20
})

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const isEditing = ref(false)
const editingId = ref('')

const formRef = ref<FormInstance>()
const form = reactive({
  name: '',
  siteCode: '',
  address: {
    addressLine1: '',
    addressLine2: '',
    city: '',
    county: '',
    postcode: ''
  },
  clientId: '',
  gridReference: '',
  status: 'planning' as Site['status'],
  startDate: '',
  estimatedCompletion: ''
})

const statusOptions = [
  { label: 'Planning', value: 'planning' },
  { label: 'Active', value: 'active' },
  { label: 'Completed', value: 'completed' },
  { label: 'On Hold', value: 'on_hold' }
]

onMounted(() => {
  loadData()
  loadCompanies()
})

const loadData = async () => {
  loading.value = true
  try {
    const response = await api.sites.getAll({
      status: filters.status || undefined,
      clientId: filters.clientId || undefined,
      startDate: filters.startDate || undefined,
      endDate: filters.endDate || undefined,
      page: filters.page,
      limit: filters.limit
    })
    tableData.value = response.data.data
    total.value = response.data.total
  } catch {
    ElMessage.error('Failed to load sites')
  } finally {
    loading.value = false
  }
}

const loadCompanies = async () => {
  try {
    const response = await api.companies.getAll({ limit: 100 })
    companies.value = response.data.data
  } catch {
    console.error('Failed to load companies')
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

const openAddDrawer = () => {
  isEditing.value = false
  editingId.value = ''
  resetForm()
  drawerVisible.value = true
}

const openEditDrawer = (row: Site) => {
  isEditing.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    siteCode: row.siteCode,
    address: { ...row.address },
    clientId: row.clientId,
    gridReference: row.gridReference || '',
    status: row.status,
    startDate: row.startDate || '',
    estimatedCompletion: row.estimatedCompletion || ''
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
        await api.sites.update(editingId.value, form)
        ElMessage.success('Site updated')
      } else {
        await api.sites.create(form)
        ElMessage.success('Site created')
      }
      drawerVisible.value = false
      loadData()
    } catch {
      ElMessage.error('Failed to save site')
    } finally {
      drawerLoading.value = false
    }
  })
}

const resetForm = () => {
  Object.assign(form, {
    name: '', siteCode: '', address: { addressLine1: '', addressLine2: '', city: '', county: '', postcode: '' },
    clientId: '', gridReference: '', status: 'planning', startDate: '', estimatedCompletion: ''
  })
  formRef.value?.resetFields()
}

const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'
</script>

<template>
  <div class="projects-view">
    <PageHeader title="Sites / Projects" :breadcrumbs="[{ title: 'Sites / Projects' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openAddDrawer">Add Site</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="6">
          <el-input v-model="filters.search" placeholder="Search..." clearable :prefix-icon="Search" @keyup.enter="handleSearch" />
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
          <el-button type="primary" @click="handleSearch">Search</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="name" label="Site Name" min-width="200" />
        <el-table-column prop="siteCode" label="Code" width="100" />
        <el-table-column label="Client" min-width="150">
          <template #default="{ row }">{{ row.client?.name || '—' }}</template>
        </el-table-column>
        <el-table-column label="Status" width="120">
          <template #default="{ row }"><StatusBadge :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="Start Date" width="120">
          <template #default="{ row }">{{ formatDate(row.startDate) }}</template>
        </el-table-column>
        <el-table-column label="Completion" width="120">
          <template #default="{ row }">{{ formatDate(row.estimatedCompletion) }}</template>
        </el-table-column>
        <el-table-column label="Actions" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDrawer(row)">Edit</el-button>
            <el-button link type="primary" @click="router.push(`/projects/${row.id}`)">View</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="filters.page" :page-size="filters.limit" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
    </el-card>

    <el-drawer v-model="drawerVisible" :title="isEditing ? 'Edit Site' : 'Add Site'" size="600px">
      <el-form ref="formRef" :model="form" label-position="top">
        <el-form-item label="Site Name" prop="name" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="Site Code" prop="siteCode" required><el-input v-model="form.siteCode" /></el-form-item>
        <el-form-item label="Client" prop="clientId" required>
          <el-select v-model="form.clientId" placeholder="Select client" filterable>
            <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-select v-model="form.status"><el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" /></el-select>
        </el-form-item>
        <el-form-item label="Grid Reference"><el-input v-model="form.gridReference" placeholder="e.g. SU 12345 67890" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="Start Date"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="Est. Completion"><el-date-picker v-model="form.estimatedCompletion" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">Address</el-divider>
        <el-form-item label="Address Line 1"><el-input v-model="form.address.addressLine1" /></el-form-item>
        <el-form-item label="Address Line 2"><el-input v-model="form.address.addressLine2" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="City"><el-input v-model="form.address.city" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="County"><el-input v-model="form.address.county" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="Postcode"><el-input v-model="form.address.postcode" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="drawerLoading" @click="handleSubmit">{{ isEditing ? 'Update' : 'Create' }}</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.projects-view { .filter-card { margin-bottom: 16px; .filter-actions { display: flex; justify-content: flex-end; gap: 8px; } } }
</style>
