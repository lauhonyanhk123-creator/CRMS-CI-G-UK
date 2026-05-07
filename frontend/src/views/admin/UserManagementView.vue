<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import api from '@/services/api'; import type { ElTagType } from '@/services/api'
import PageHeader from '@/components/common/PageHeader.vue'

interface AdminUser {
  id: string
  username: string
  firstName: string
  lastName: string
  email: string
  roles: string[]
  enabled: boolean
  mustChangePassword: boolean
}

const loading = ref(false)
const users = ref<AdminUser[]>([])
const total = ref(0)

const sortField = ref('username')
const currentPage = ref(1)
const pageSize = ref(20)

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const editingId = ref('')

const form = ref({
  username: '',
  firstName: '',
  lastName: '',
  email: '',
  role: 'ROLE_USER',
  password: '',
  newPassword: ''
})

const roles = [
  { label: 'Admin', value: 'ROLE_ADMIN' },
  { label: 'User', value: 'ROLE_USER' },
  { label: 'Ops Director', value: 'ROLE_OPS_DIRECTOR' },
  { label: 'Contracts Manager', value: 'ROLE_CONTRACTS_MANAGER' },
  { label: 'QS', value: 'ROLE_QS' },
  { label: 'Site Agent', value: 'ROLE_SITE_AGENT' },
  { label: 'Engineer', value: 'ROLE_ENGINEER' },
  { label: 'Plant Manager', value: 'ROLE_PLANT_MANAGER' },
  { label: 'Buyer', value: 'ROLE_BUYER' },
  { label: 'Finance', value: 'ROLE_FINANCE' },
  { label: 'Estimator', value: 'ROLE_ESTIMATOR' },
  { label: 'Bid Manager', value: 'ROLE_BID_MANAGER' },
  { label: 'IT Admin', value: 'ROLE_IT_ADMIN' }
]

const roleLabel = (roleValue: string) =>
  roles.find(r => r.value === roleValue)?.label ?? roleValue.replace('ROLE_', '').replace('_', ' ')

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await api.admin.users.getAll({
      page: currentPage.value - 1,  // backend is 0-based
      size: pageSize.value,
      sort: sortField.value
    })
    const page = response.data as any
    users.value = page.content ?? []
    total.value = page.totalElements ?? 0
  } catch {
    ElMessage.error('Failed to load users')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadUsers()
}

const handleAdd = () => {
  editingId.value = ''
  form.value = { username: '', firstName: '', lastName: '', email: '', role: 'ROLE_USER', password: '', newPassword: '' }
  dialogVisible.value = true
}

const handleEdit = (user: AdminUser) => {
  editingId.value = user.id
  form.value = {
    username: user.username,
    firstName: user.firstName ?? '',
    lastName: user.lastName ?? '',
    email: user.email,
    role: user.roles?.[0] ?? 'ROLE_USER',
    password: '',
    newPassword: ''
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  dialogLoading.value = true
  try {
    if (editingId.value) {
      const payload: Record<string, any> = {
        firstName: form.value.firstName,
        lastName: form.value.lastName,
        email: form.value.email,
        roles: [form.value.role]
      }
      if (form.value.newPassword) payload.newPassword = form.value.newPassword
      await api.admin.users.update(editingId.value, payload)
      ElMessage.success('User updated')
    } else {
      if (!form.value.username || !form.value.email || !form.value.password) {
        ElMessage.error('Username, email and password are required')
        return
      }
      await api.admin.users.create({
        username: form.value.username,
        email: form.value.email,
        password: form.value.password,
        firstName: form.value.firstName,
        lastName: form.value.lastName,
        role: form.value.role
      })
      ElMessage.success('User created — they must change their password on first login')
    }
    dialogVisible.value = false
    loadUsers()
  } catch (error: any) {
    // Error shown by interceptor
  } finally {
    dialogLoading.value = false
  }
}

const handleToggleEnabled = async (user: AdminUser) => {
  const action = user.enabled ? 'disable' : 'enable'
  try {
    await ElMessageBox.confirm(
      `${action.charAt(0).toUpperCase() + action.slice(1)} account for ${user.username}?`,
      'Confirm',
      { type: 'warning' }
    )
    if (user.enabled) {
      await api.admin.users.delete(user.id)
    } else {
      await api.admin.users.update(user.id, { enabled: true })
    }
    ElMessage.success(`User ${action}d`)
    loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`Failed to ${action} user`)
    }
  }
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getRoleTagType = (role: string): TagType | undefined => {
  const map: Record<string, TagType> = {
    ROLE_ADMIN: 'danger',
    ROLE_IT_ADMIN: 'danger',
    ROLE_OPS_DIRECTOR: 'warning',
    ROLE_CONTRACTS_MANAGER: 'warning'
  }
  return map[role]
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
      <el-table v-loading="loading" :data="users" stripe>
        <el-table-column prop="username" label="Username" width="150" />
        <el-table-column label="Name" min-width="160">
          <template #default="{ row }">
            {{ row.firstName }} {{ row.lastName }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="Email" min-width="200" />
        <el-table-column label="Role(s)" min-width="160">
          <template #default="{ row }">
            <el-tag
              v-for="r in row.roles"
              :key="r"
              :type="getRoleTagType(r)"
              size="small"
              style="margin-right: 4px"
            >
              {{ roleLabel(r) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Status" width="110">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? 'Active' : 'Disabled' }}
            </el-tag>
            <el-tag v-if="row.mustChangePassword" type="warning" size="small" style="margin-left: 4px">
              Pwd Reset
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">Edit</el-button>
            <el-button
              link
              :type="row.enabled ? 'danger' : 'success'"
              size="small"
              @click="handleToggleEnabled(row)"
            >
              {{ row.enabled ? 'Disable' : 'Enable' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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
        <el-form-item v-if="!editingId" label="Username" required>
          <el-input v-model="form.username" placeholder="Unique login name" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="First Name">
              <el-input v-model="form.firstName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Last Name">
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
        <el-form-item v-if="!editingId" label="Initial Password" required>
          <el-input v-model="form.password" type="password" show-password placeholder="Min 8 characters" />
        </el-form-item>
        <el-form-item v-if="editingId" label="Reset Password (leave blank to keep current)">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="New password (optional)" />
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
  .el-table {
    margin-top: 0;
  }
}
</style>
