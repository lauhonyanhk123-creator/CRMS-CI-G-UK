<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import api, { type User } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const activeTab = ref('users')
const loading = ref(false)
const users = ref<User[]>([])
const backupStatus = ref<any>(null)
const integrations = ref<any[]>([])
const settings = ref<any>({})

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const editingId = ref('')
const form = ref({ name: '', email: '', role: 'user' as User['role'], status: 'active' as User['status'] })

onMounted(() => { loadUsers(); loadBackupStatus(); loadIntegrations(); loadSettings() })

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await api.admin.getUsers({})
    users.value = response.data.data
  } catch { ElMessage.error('Failed to load users') } finally { loading.value = false }
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
    integrations.value = response.data
  } catch {}
}

const loadSettings = async () => {
  try {
    const response = await api.admin.getSettings()
    settings.value = response.data
  } catch {}
}

const handleTabChange = (tab: string) => {
  activeTab.value = tab
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
    if (editingId.value) {
      await api.admin.updateUser(editingId.value, form.value)
    } else {
      await api.admin.createUser(form.value)
    }
    ElMessage.success('User saved')
    drawerVisible.value = false
    loadUsers()
  } catch { ElMessage.error('Failed to save user') } finally { drawerLoading.value = false }
}

const openEditUser = (user: User) => {
  editingId.value = user.id
  form.value = { name: user.name, email: user.email, role: user.role, status: user.status }
  drawerVisible.value = true
}

const deleteUser = async (user: User) => {
  try {
    await api.admin.deleteUser(user.id)
    ElMessage.success('User deleted')
    loadUsers()
  } catch { ElMessage.error('Failed to delete user') }
}

const getRoleType = (role: string) => {
  const map: Record<string, string> = { admin: 'danger', manager: 'warning', user: '', viewer: 'info' }
  return map[role] || ''
}

const formatDate = (date?: string) => date ? new Date(date).toLocaleString() : '—'
</script>

<template>
  <div class="admin-view">
    <PageHeader title="Admin" :breadcrumbs="[{ title: 'Admin' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="() => { editingId = ''; form = { name: '', email: '', role: 'user', status: 'active' }; drawerVisible = true }">
          Add User
        </el-button>
      </template>
    </PageHeader>

    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="Users" name="users">
          <el-table v-loading="loading" :data="users" stripe>
            <el-table-column prop="name" label="Name" min-width="150" />
            <el-table-column prop="email" label="Email" min-width="200" />
            <el-table-column label="Role" width="120">
              <template #default="{ row }">
                <el-tag :type="getRoleType(row.role)" size="small">{{ row.role.toUpperCase() }}</el-tag>
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

        <el-tab-pane label="Backup" name="backup">
          <el-card shadow="never">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="Status">{{ backupStatus?.status || '—' }}</el-descriptions-item>
              <el-descriptions-item label="Last Backup">{{ formatDate(backupStatus?.lastBackup) }}</el-descriptions-item>
              <el-descriptions-item label="Backup Location">{{ backupStatus?.location || '—' }}</el-descriptions-item>
              <el-descriptions-item label="Size">{{ backupStatus?.size || '—' }}</el-descriptions-item>
            </el-descriptions>
            <el-divider />
            <el-button type="primary" @click="triggerBackup">Trigger Backup</el-button>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="Integrations" name="integrations">
          <el-table :data="integrations" stripe>
            <el-table-column prop="name" label="Integration" min-width="200" />
            <el-table-column label="Status" width="120">
              <template #default="{ row }">
                <el-tag :type="row.connected ? 'success' : 'danger'" size="small">
                  {{ row.connected ? 'Connected' : 'Disconnected' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Last Sync" width="180">
              <template #default="{ row }">{{ formatDate(row.lastSync) }}</template>
            </el-table-column>
            <el-table-column label="Actions" width="100">
              <template #default="{ row }">
                <el-button link type="primary" size="small">{{ row.connected ? 'Configure' : 'Connect' }}</el-button>
              </template>
            </el-table-column>
          </el-table>
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
        <el-form-item label="Name" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="Email" required><el-input v-model="form.email" type="email" /></el-form-item>
        <el-form-item label="Role">
          <el-select v-model="form.role">
            <el-option label="Admin" value="admin" />
            <el-option label="Manager" value="manager" />
            <el-option label="User" value="user" />
            <el-option label="Viewer" value="viewer" />
          </el-select>
        </el-form-item>
        <el-form-item label="Status">
          <el-radio-group v-model="form.status">
            <el-radio label="active">Active</el-radio>
            <el-radio label="inactive">Inactive</el-radio>
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
