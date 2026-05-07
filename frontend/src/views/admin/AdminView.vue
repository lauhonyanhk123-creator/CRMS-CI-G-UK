<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Key, WarningFilled, CircleCheckFilled, InfoFilled } from '@element-plus/icons-vue'
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

interface LicenceStatus {
  tier: string
  installationId: string
  maxUsers: number
  activeUsers: number
  availableSlots: number
  atCapacity: boolean
  maintenanceExpired: boolean
  expiryDate: string | null
  daysUntilExpiry: number
  summary: string
}

const activeTab = ref('users')
const loading = ref(false)
const users = ref<AdminUser[]>([])
const settings = ref<any>({})
const roles = ref<any[]>([])
const licence = ref<LicenceStatus | null>(null)

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

// Licence UI helpers
const licenceBannerType = computed((): 'danger' | 'warning' | 'success' | 'info' => {
  if (!licence.value) return 'info'
  if (licence.value.atCapacity) return 'danger'
  if (licence.value.maintenanceExpired) return 'warning'
  if (licence.value.daysUntilExpiry >= 0 && licence.value.daysUntilExpiry <= 30) return 'warning'
  return 'success'
})

const licenceBannerIcon = computed(() => {
  const t = licenceBannerType.value
  return t === 'danger' || t === 'warning' ? WarningFilled : CircleCheckFilled
})

const userCapPercent = computed(() => {
  if (!licence.value) return 0
  return Math.min(100, Math.round((licence.value.activeUsers / licence.value.maxUsers) * 100))
})

const userCapColor = computed(() => {
  const p = userCapPercent.value
  return p >= 100 ? '#f56c6c' : p >= 80 ? '#e6a23c' : '#67c23a'
})

onMounted(() => {
  loadUsers()
  loadSettings()
  loadRoles()
  loadLicence()
})

const loadLicence = async () => {
  try {
    const response = await api.licence.getStatus()
    licence.value = response.data as LicenceStatus
  } catch {
    // non-critical — silently ignore if endpoint fails in older installs
  }
}

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

const loadSettings = async () => {
  try {
    const response = await api.admin.getSettings()
    settings.value = response.data || {}
  } catch {}
}

const handleTabChange = (tab: string | number) => {
  activeTab.value = String(tab)
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
    loadLicence()
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
    loadLicence()
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

    <!-- Licence Status Banner -->
    <el-alert
      v-if="licence"
      :type="licenceBannerType"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    >
      <template #title>
        <span class="licence-title">
          <el-icon style="margin-right: 4px"><Key /></el-icon>
          Licence — {{ licence.tier }} tier &nbsp;·&nbsp; Installation: {{ licence.installationId }}
        </span>
      </template>
      <template #default>
        <div class="licence-detail">
          <span>{{ licence.summary }}</span>
          <div class="cap-bar">
            <el-progress
              :percentage="userCapPercent"
              :color="userCapColor"
              :stroke-width="8"
              :show-text="false"
              style="flex: 1"
            />
            <span class="cap-label">{{ licence.activeUsers }} / {{ licence.maxUsers }} users</span>
          </div>
        </div>
      </template>
    </el-alert>

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

        <el-tab-pane label="Licence" name="licence">
          <template v-if="licence">
            <el-descriptions title="Licence Details" :column="2" border>
              <el-descriptions-item label="Tier">
                <el-tag :type="licence.tier === 'GROUP' ? 'success' : licence.tier === 'SITE' ? 'warning' : 'info'">
                  {{ licence.tier }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="Installation ID">
                <code>{{ licence.installationId }}</code>
              </el-descriptions-item>
              <el-descriptions-item label="Active Users">
                {{ licence.activeUsers }} of {{ licence.maxUsers }}
              </el-descriptions-item>
              <el-descriptions-item label="Available Slots">
                <el-tag :type="licence.atCapacity ? 'danger' : 'success'">
                  {{ licence.atCapacity ? 'AT CAPACITY' : licence.availableSlots + ' free' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="Maintenance Expiry">
                <template v-if="licence.expiryDate">
                  <el-tag :type="licence.maintenanceExpired ? 'danger' : licence.daysUntilExpiry <= 30 ? 'warning' : 'success'">
                    {{ licence.expiryDate }}
                    <template v-if="!licence.maintenanceExpired"> ({{ licence.daysUntilExpiry }}d)</template>
                    <template v-else> EXPIRED</template>
                  </el-tag>
                </template>
                <span v-else class="text-muted">Perpetual (no expiry)</span>
              </el-descriptions-item>
              <el-descriptions-item label="Status">
                <el-tag type="success">Active — Perpetual Licence</el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <div style="margin-top: 20px">
              <p style="color: #606266; font-size: 13px;">
                To upgrade your tier or renew your support &amp; maintenance contract,
                contact <strong>support@crms-ci-g-uk.co.uk</strong> quoting installation ID
                <code>{{ licence.installationId }}</code>.
              </p>
            </div>
          </template>
          <el-empty v-else description="Licence information unavailable" />
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
.admin-view {}

.licence-title {
  display: flex;
  align-items: center;
  font-weight: 600;
}

.licence-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 4px;
}

.cap-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 400px;
}

.cap-label {
  font-size: 13px;
  white-space: nowrap;
  color: #606266;
}

.text-muted {
  color: #909399;
}
</style>
