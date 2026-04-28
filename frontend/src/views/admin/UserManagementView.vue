<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import api, { type User } from '@/services/api'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

interface AdminUser {
  id: string
  firstName: string
  lastName: string
  email: string
  role: 'ADMIN' | 'SURVEYOR' | 'CONTRACT_MANAGER' | 'DIRECTOR'
  status: 'ACTIVE' | 'INACTIVE'
  lastLogin?: string
  phone?: string
  companyId?: string
}

const loading = ref(false)
const users = ref<AdminUser[]>([])
const total = ref(0)

const searchQuery = ref('')
const filterRole = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const editingId = ref('')

const form = ref({
  firstName: '',
  lastName: '',
  email: '',
  role: 'SURVEYOR' as AdminUser['role'],
  password: '',
  phone: '',
  companyId: ''
})

const roles = [
  { label: 'Admin', value: 'ADMIN' },
  { label: 'Surveyor', value: 'SURVEYOR' },
  { label: 'Contract Manager', value: 'CONTRACT_MANAGER' },
  { label: 'Director', value: 'DIRECTOR' }
]

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await api.admin.users.getAll({
      search: searchQuery.value || undefined,
      role: filterRole.value || undefined,
      status: filterStatus.value || undefined,
      page: currentPage.value,
      limit: pageSize.value
    })
    users.value = response.data.data
    total.value = response.data.total
  } catch {
    ElMessage.error('Failed to load users')
  } finally {
    loading.value = false }
}

const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadUsers()
}

const handleAdd = () => {
  editingId.value = ''
  form.value = {
    firstName: '',
    lastName: '',
    email: '',
    role: 'SURVEYOR',
    password: '',
    phone: '',
    companyId: ''
  }
  dialogVisible.value = true
}

const handleEdit = (user: AdminUser) => {
  editingId.value = user.id
  form.value = {
    firstName: user.firstName,
    lastName: user.lastName,
    email: user.email,
    role: user.role,
    password: '',
    phone: user.phone || '',
    companyId: user.companyId || ''
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  dialogLoading.value = true
  try {
    if (editingId.value) {
      await api.admin.users.update(editingId.value, {
        firstName: form.value.firstName,
        lastName: form.value.lastName,
        email: form.value.email,
        role: form.value.role,
        phone: form.value.phone,
        companyId: form.value.companyId
      })
      ElMessage.success('User updated successfully')
    } else {
      await api.admin.users.create({
        firstName: form.value.firstName,
        lastName: form.value.lastName,
        email: form.value.email,
        role: form.value.role,
        password: form.value.password,
        phone: form.value.phone,
        companyId: form.value.companyId
      })
      ElMessage.success('User created successfully')
    }
    dialogVisible.value = false
    loadUsers()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || 'Failed to save user')
  } finally {
    dialogLoading.value = false
  }
}

const handleDelete = async (user: AdminUser) => {
  try {
    await ElMessageBox.confirm(
      `Are you sure you want to delete user "${user.firstName} ${user.lastName}"?`,
      'Confirm Delete',
      { type: 'warning' }
    )
    await api.admin.users.delete(user.id)
    ElMessage.success('User deleted successfully')
    loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || 'Failed to delete user')
    }
  }
}

const getRoleType = (role: string) => {
  const map: Record<string, string> = {
    ADMIN: 'danger',
    SURVEYOR: '',
    CONTRACT_MANAGER: 'warning',
    DIRECTOR: 'success'
  }
  return map[role] || ''
}

const formatDate = (date?: string) => {
  return date ? new Date(date).toLocaleString() : '—'
}

onMounted(() => {
  loadUsers()
})
</script>

<template>
  <div class="user-management-view">
    <PageHeader title="User Management" :breadcrumbs="[{ title: 'Admin', path: '/admin' }, { title: 'Users' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          Add User
        </el-button>
      </template>
    </PageHeader>

    <el-card shadow="never">
      <!-- Filters -->
      <div class="filters">
        <el-input
          v-model="searchQuery"
          placeholder="Search by name or email..."
          :prefix-icon="Search"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="filterRole" placeholder="All Roles" clearable style="width: 180px" @change="handleSearch">
          <el-option v-for="role in roles" :key="role.value" :label="role.label" :value="role.value" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="All Statuses" clearable style="width: 150px" @change="handleSearch">
          <el-option label="Active" value="ACTIVE" />
          <el-option label="Inactive" value="INACTIVE" />
        </el-select>
        <el-button type="primary" @click="handleSearch">Search</el-button>
      </div>

      <!-- Users Table -->
      <el-table v-loading="loading" :data="users" stripe style="margin-top: 20px">
        <el-table-column label="Name" min-width="180">
          <template #default="{ row }">
            {{ row.firstName }} {{ row.lastName }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="Email" min-width="200" />
        <el-table-column label="Role" width="160">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">
              {{ row.role.replace('_', ' ') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Status" width="100">
          <template #default="{ row }">
            <StatusBadge :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="Last Login" width="160">
          <template #default="{ row }">
            {{ formatDate(row.lastLogin) }}
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">Edit</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? 'Edit User' : 'Add User'"
      width="500px"
      destroy-on-close
    >
      <el-form :model="form" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="First Name" required>
              <el-input v-model="form.firstName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Last Name" required>
              <el-input v-model="form.lastName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Email" required>
          <el-input v-model="form.email" type="email" />
        </el-form-item>
        <el-form-item label="Role" required>
          <el-select v-model="form.role" style="width: 100%">
            <el-option v-for="role in roles" :key="role.value" :label="role.label" :value="role.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editingId" label="Password" required>
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="Phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="Company ID">
          <el-input v-model="form.companyId" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="handleSave">
          {{ editingId ? 'Update' : 'Create' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.user-management-view {
  .filters {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }
}
</style>
