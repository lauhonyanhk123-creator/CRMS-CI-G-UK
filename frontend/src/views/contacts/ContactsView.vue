<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import api, { type Contact, type Company } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const loading = ref(false)
const tableData = ref<Contact[]>([])
const total = ref(0)
const companies = ref<Company[]>([])

const filters = reactive({
  search: '',
  companyId: '',
  page: 1,
  limit: 20
})

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const isEditing = ref(false)
const editingId = ref('')
const formRef = ref<FormInstance>()

const form = reactive({
  companyId: '',
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  position: '',
  isPrimary: false
})

onMounted(() => {
  loadData()
  loadCompanies()
})

const loadData = async () => {
  loading.value = true
  try {
    const response = await api.contacts.getAll({
      search: filters.search || undefined,
      companyId: filters.companyId || undefined,
      page: filters.page,
      limit: filters.limit
    })
    tableData.value = response.data.data
    total.value = response.data.total
  } catch { ElMessage.error('Failed to load contacts') } finally { loading.value = false }
}

const loadCompanies = async () => {
  try {
    const response = await api.companies.getAll({ limit: 100 })
    companies.value = response.data.data
  } catch (e) { console.error(e) }
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

const openEditDrawer = (row: Contact) => {
  isEditing.value = true
  editingId.value = row.id
  Object.assign(form, {
    companyId: row.companyId,
    firstName: row.firstName,
    lastName: row.lastName,
    email: row.email,
    phone: row.phone || '',
    position: row.position || '',
    isPrimary: row.isPrimary
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
        await api.contacts.update(editingId.value, form)
        ElMessage.success('Contact updated')
      } else {
        await api.contacts.create(form)
        ElMessage.success('Contact created')
      }
      drawerVisible.value = false
      loadData()
    } catch { ElMessage.error('Failed to save contact') } finally { drawerLoading.value = false }
  })
}

const resetForm = () => {
  Object.assign(form, {
    companyId: '', firstName: '', lastName: '', email: '', phone: '', position: '', isPrimary: false
  })
  formRef.value?.resetFields()
}

const deleteContact = async (row: Contact) => {
  try {
    await api.contacts.delete(row.id)
    ElMessage.success('Contact deleted')
    loadData()
  } catch { ElMessage.error('Failed to delete contact') }
}
</script>

<template>
  <div class="contacts-view">
    <PageHeader title="Contacts" :breadcrumbs="[{ title: 'Contacts' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openAddDrawer">Add Contact</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8">
          <el-input v-model="filters.search" placeholder="Search..." clearable :prefix-icon="Search" @keyup.enter="handleSearch" />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.companyId" placeholder="Company" clearable filterable @change="handleSearch">
            <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :md="10" class="filter-actions">
          <el-button @click="() => { filters.search = ''; filters.companyId = ''; loadData() }">Reset</el-button>
          <el-button type="primary" @click="handleSearch">Search</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column label="Name" min-width="180">
          <template #default="{ row }">{{ row.firstName }} {{ row.lastName }}</template>
        </el-table-column>
        <el-table-column label="Company" min-width="180">
          <template #default="{ row }">
            <el-tag v-if="row.isPrimary" size="small" type="primary">Primary</el-tag>
            {{ row.company?.name || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="position" label="Position" min-width="150" />
        <el-table-column prop="email" label="Email" min-width="200" />
        <el-table-column prop="phone" label="Phone" min-width="140" />
        <el-table-column label="Status" width="100">
          <template #default="{ row }"><StatusBadge :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="Actions" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDrawer(row)">Edit</el-button>
            <el-button link type="danger" @click="deleteContact(row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="filters.page" :page-size="filters.limit" :total="total" @current-change="handlePageChange" />
    </el-card>

    <el-drawer v-model="drawerVisible" :title="isEditing ? 'Edit Contact' : 'Add Contact'" size="500px">
      <el-form ref="formRef" :model="form" label-position="top">
        <el-form-item label="Company" prop="companyId" required>
          <el-select v-model="form.companyId" placeholder="Select company" filterable style="width:100%">
            <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="First Name" prop="firstName" required><el-input v-model="form.firstName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Last Name" prop="lastName" required><el-input v-model="form.lastName" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Email" prop="email" required><el-input v-model="form.email" type="email" /></el-form-item>
        <el-form-item label="Phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="Position"><el-input v-model="form.position" /></el-form-item>
        <el-form-item><el-checkbox v-model="form.isPrimary">Primary Contact</el-checkbox></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="drawerLoading" @click="handleSubmit">{{ isEditing ? 'Update' : 'Create' }}</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.contacts-view { .filter-card { margin-bottom: 16px; .filter-actions { display: flex; justify-content: flex-end; gap: 8px; } } }
</style>
