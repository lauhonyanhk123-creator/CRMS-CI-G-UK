<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import api from '@/services/api'; import type { ElTagType } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

interface AdminUser {
  id: string
  firstName: string
  lastName: string
  email: string
  role: string
  status: string
  lastLogin?: string
  phone?: string
}

const activeTab = ref('users')
const loading = ref(false)
const users = ref<AdminUser[]>([])
const backupStatus = ref<any>(null)
const integrations = ref<any[]>([])
// Note: backup and integration endpoints are not yet implemented server-side
const settings = ref<any>({})
const roles = ref<any[]>([])

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const editingId = ref('')
const form = ref({ 
  firstName: '', 
  lastName: '', 
  email: '', 
  role: 'SURVEYOR' as string, 
  password: '',
  status: 'ACTIVE' as string 
})

onMounted(() => {
  loadUsers();
  loadSettings()
  loadRoles()
})

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await api.admin.users.getAll({})
    users.value = response.data.data || []
  } catch { ElMessage.error('Failed to load users') } finally { loading.value = false }
}

const loadRoles = async () => {
  try {
    const response = await api.admin.getRoles()
    roles.value = response.data || []
  } catch {}
}

const loadBackupStatus = async () => {
  try {
    const response = await api.admin.getBackupStatus()
    backupStatus.value = response.data
  } catch {}
}

const loadIntegrations = async () => {
  try {
    const response = await api.admin.getIntegrations()
    integrations.value = response.data || []
  } catch {}
}

const loadSettings = async () => {
  try {
    const response = await api.admin.getSettings()
    settings.value = response.data || {}
  } catch {}
}

const handleTabChange = (tab: string | number) => {
  activeTab.value = String(tab)
}

const triggerBackup = async () => {
  try {
    await api.admin.triggerBackup()
    ElMessage.success('Backup started')
    loadBackupStatus()
  } catch { ElMessage.error('Failed to start backup') }
}

const saveUser = async () => {
  drawerLoading.value = true
  try {
    const userData = {
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      email: form.value.email,
      role: form.value.role,
      status: form.value.status
    }
    if (editingId.value) {
      await api.admin.users.update(editingId.value, userData)
    } else {
      await api.admin.users.create({ ...userData, password: form.value.password })
    }
    ElMessage.success('User saved')
    drawerVisible.value = false
    loadUsers()
  } catch { ElMessage.error('Failed to save user') } finally { drawerLoading.value = false }
}

const openAddUser = () => {
  editingId.value = ''
  form.value = { firstName: '', lastName: '', email: '', role: 'SURVEYOR', password: '', status: 'ACTIVE' }
  drawerVisible.value = true
}

const openEditUser = (user: AdminUser) => {
  editingId.value = user.id
  form.value = { 
    firstName: user.firstName, 
    lastName: user.lastName,
    email: user.email, 
    role: user.role, 
    password: '',
    status: user.status 
  }
  drawerVisible.value = true
}

const deleteUser = async (user: AdminUser) => {
  try {
    await api.admin.users.delete(user.id)
    ElMessage.success('User deleted')
    loadUsers()
  } catch { ElMessage.error('Failed to delete user') }
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getRoleType = (role: string): TagType | undefined => {
  const map: Record<string, TagType> = {
    ADMIN: 'danger',
    CONTRACT_MANAGER: 'warning',
    DIRECTOR: 'success'
  }
  return map[role]
}

const getUserName = (user: AdminUser) => `${user.firstName} ${user.lastName}`

const formatDate = (date?: string) => date ? new Date(date).toLocaleString() : '—'
</script>

<template>
  <div class="admin-view">
    <PageHeader title="Admin" :breadcrumbs="[{ title: 'Admin' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openAddUser">
          Add User
        </el-button>
      </template>
    </PageHeader>

    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="Users" name="users">
          <el-table v-loading="loading" :data="users" stripe>
            <el-table-column label="Name" min-width="150">
              <template #default="{ row }">{{ getUserName(row) }}</template>
            </el-table-column>
            <el-table-column prop="email" label="Email" min-width="200" />
            <el-table-column label="Role" width="160">
              <template #default="{ row }">
                <el-tag :type="getRoleType(row.role)" size="small">{{ row.role.replace('_', ' ') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Status" width="100">
              <template #default="{ row }"><StatusBadge :status="row.status" /></template>
            </el-table-column>
            <el-table-column label="Last Login" width="180">
              <template #default="{ row }">{{ formatDate(row.lastLogin) }}</template>
            </el-table-column>
            <el-table-column label="Actions" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openEditUser(row)">Edit</el-button>
                <el-button link type="danger" size="small" @click="deleteUser(row)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="Roles" name="roles">
          <el-card shadow="never">
            <template #header><span>Casbin Policies</span></template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="Admin">Full access to all resources</el-descriptions-item>
              <el-descriptions-item label="Manager">Create, Read, Update, Delete, Approve operations</el-descriptions-item>
              <el-descriptions-item label="User">Create, Read, Update operations</el-descriptions-item>
              <el-descriptions-item label="Viewer">Read-only access</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="Backup &amp; Integrations" name="backup">
          <el-card shadow="never">
            <el-empty description="Backup management and external integration status are not yet available in this release." />
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="Settings" name="settings">
          <el-form label-position="top">
            <el-form-item label="Company Name">
              <el-input v-model="settings.companyName" />
            </el-form-item>
            <el-form-item label="Default Retention Percentage">
              <el-input-number v-model="settings.defaultRetention" :min="0" :max="100" />
            </el-form-item>
            <el-form-item label="Auto-approve Requisitions Under">
              <el-input-number v-model="settings.autoApproveThreshold" :min="0" :step="100" prefix="£" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="async () => { await api.admin.updateSettings(settings); ElMessage.success('Settings saved') }">Save Settings</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-drawer v-model="drawerVisible" :title="editingId ? 'Edit User' : 'Add User'" size="500px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="First Name" required><el-input v-model="form.firstName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Last Name" required><el-input v-model="form.lastName" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Email" required><el-input v-model="form.email" type="email" /></el-form-item>
        <el-form-item label="Role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="Admin" value="ADMIN" />
            <el-option label="Contract Manager" value="CONTRACT_MANAGER" />
            <el-option label="Surveyor" value="SURVEYOR" />
            <el-option label="Director" value="DIRECTOR" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editingId" label="Password" required>
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="Status">
          <el-radio-group v-model="form.status">
            <el-radio label="ACTIVE">Active</el-radio>
            <el-radio label="INACTIVE">Inactive</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="drawerLoading" @click="saveUser">Save</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.admin-view { }
</style>
