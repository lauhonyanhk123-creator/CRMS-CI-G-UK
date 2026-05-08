<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import api from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import PageHeader from '@/components/common/PageHeader.vue'

const authStore = useAuthStore()
const loading = ref(false)
const showDialog = ref(false)
const editingId = ref<number | null>(null)
const searchQuery = ref('')
const formRef = ref()

const records = ref<any[]>([])
const clients = ref<any[]>([])

const form = reactive({
  notificationNumber: '',
  projectName: '',
  projectAddress: '',
  projectDescription: '',
  clientId: '',
  notificationDate: '',
  constructionStartDate: '',
  constructionEndDate: '',
  isNotifiable: true,
  hseNotificationRef: ''
})

const rules = {
  projectName: [{ required: true, message: 'Project name is required', trigger: 'blur' }],
  notificationDate: [{ required: true, message: 'Notification date is required', trigger: 'change' }]
}

const isAdminOrManager = () => {
  const roles = authStore.user?.roles ?? []
  return roles.some(r => ['ROLE_ADMIN', 'ROLE_MANAGER', 'admin', 'manager'].includes(r))
}

onMounted(() => {
  loadRecords()
  loadClients()
})

const loadRecords = async () => {
  loading.value = true
  try {
    const response = await api.cdmRegister.getAll({ search: searchQuery.value || undefined })
    records.value = response.data?.data ?? response.data ?? []
  } catch {
    ElMessage.error('Failed to load CDM register')
  } finally {
    loading.value = false
  }
}

const loadClients = async () => {
  try {
    const response = await api.companies.getAll({ type: 'client' })
    clients.value = response.data?.data ?? []
  } catch {
    // non-critical — client dropdown may be empty
  }
}

const openCreate = () => {
  editingId.value = null
  Object.assign(form, {
    notificationNumber: '',
    projectName: '',
    projectAddress: '',
    projectDescription: '',
    clientId: '',
    notificationDate: '',
    constructionStartDate: '',
    constructionEndDate: '',
    isNotifiable: true,
    hseNotificationRef: ''
  })
  showDialog.value = true
}

const openEdit = (row: any) => {
  editingId.value = row.id
  Object.assign(form, {
    notificationNumber: row.notificationNumber ?? '',
    projectName: row.projectName ?? '',
    projectAddress: row.projectAddress ?? '',
    projectDescription: row.projectDescription ?? '',
    clientId: row.clientId ?? '',
    notificationDate: row.notificationDate ?? '',
    constructionStartDate: row.constructionStartDate ?? '',
    constructionEndDate: row.constructionEndDate ?? '',
    isNotifiable: row.isNotifiable ?? true,
    hseNotificationRef: row.hseNotificationRef ?? ''
  })
  showDialog.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (editingId.value !== null) {
        await api.cdmRegister.update(editingId.value, { ...form })
        ElMessage.success('CDM notification updated')
      } else {
        await api.cdmRegister.create({ ...form })
        ElMessage.success('CDM notification created')
      }
      showDialog.value = false
      loadRecords()
    } catch {
      ElMessage.error('Failed to save CDM notification')
    }
  })
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `Delete CDM notification for "${row.projectName}"? This action cannot be undone.`,
      'Confirm Delete',
      { type: 'warning', confirmButtonText: 'Delete', cancelButtonText: 'Cancel' }
    )
    await api.cdmRegister.delete(row.id)
    ElMessage.success('CDM notification deleted')
    loadRecords()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('Failed to delete CDM notification')
  }
}

const handleSearch = () => {
  loadRecords()
}
</script>

<template>
  <div class="cdm-register-view">
    <PageHeader
      title="CDM Register"
      subtitle="Construction Design and Management notification records"
    >
      <template #actions>
        <el-button
          v-if="isAdminOrManager()"
          type="primary"
          :icon="Plus"
          @click="openCreate"
        >
          Add CDM Notification
        </el-button>
      </template>
    </PageHeader>

    <el-card class="content-card">
      <!-- Search bar -->
      <div class="toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="Search by project name or notification number..."
          :prefix-icon="Search"
          clearable
          style="width: 360px"
          @change="handleSearch"
          @clear="handleSearch"
        />
      </div>

      <!-- Table -->
      <el-table
        v-loading="loading"
        :data="records"
        stripe
        style="width: 100%"
        empty-text="No CDM notifications found"
      >
        <el-table-column prop="notificationNumber" label="Notification No." width="160" />
        <el-table-column prop="projectName" label="Project Name" min-width="200" />
        <el-table-column prop="clientName" label="Client" min-width="160" />
        <el-table-column prop="notificationDate" label="Notification Date" width="150">
          <template #default="{ row }">
            {{ row.notificationDate ? new Date(row.notificationDate).toLocaleDateString('en-GB') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="isNotifiable" label="Notifiable" width="110">
          <template #default="{ row }">
            <el-tag :type="row.isNotifiable ? 'danger' : 'info'" size="small">
              {{ row.isNotifiable ? 'Yes' : 'No' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hseNotificationRef" label="HSE Ref" width="140" />
        <el-table-column prop="status" label="Status" width="120">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'ACTIVE' ? 'success' : row.status === 'CLOSED' ? 'info' : 'warning'"
              size="small"
            >
              {{ row.status ?? 'ACTIVE' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isAdminOrManager()" label="Actions" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              :icon="Edit"
              size="small"
              circle
              @click="openEdit(row)"
            />
            <el-button
              type="danger"
              :icon="Delete"
              size="small"
              circle
              @click="handleDelete(row)"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create / Edit Dialog -->
    <el-dialog
      v-model="showDialog"
      :title="editingId !== null ? 'Edit CDM Notification' : 'Add CDM Notification'"
      width="680px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="180px"
        label-position="left"
      >
        <el-form-item label="Notification Number" prop="notificationNumber">
          <el-input v-model="form.notificationNumber" placeholder="e.g. CDM-2024-001" />
        </el-form-item>

        <el-form-item label="Project Name" prop="projectName">
          <el-input v-model="form.projectName" placeholder="Project name" />
        </el-form-item>

        <el-form-item label="Project Address" prop="projectAddress">
          <el-input
            v-model="form.projectAddress"
            type="textarea"
            :rows="2"
            placeholder="Site address"
          />
        </el-form-item>

        <el-form-item label="Project Description" prop="projectDescription">
          <el-input
            v-model="form.projectDescription"
            type="textarea"
            :rows="3"
            placeholder="Brief description of works"
          />
        </el-form-item>

        <el-form-item label="Client" prop="clientId">
          <el-select
            v-model="form.clientId"
            placeholder="Select client"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="client in clients"
              :key="client.id"
              :label="client.name"
              :value="client.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Notification Date" prop="notificationDate">
          <el-date-picker
            v-model="form.notificationDate"
            type="date"
            placeholder="Select date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="Construction Start" prop="constructionStartDate">
          <el-date-picker
            v-model="form.constructionStartDate"
            type="date"
            placeholder="Select date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="Construction End" prop="constructionEndDate">
          <el-date-picker
            v-model="form.constructionEndDate"
            type="date"
            placeholder="Select date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="Notifiable Project" prop="isNotifiable">
          <el-switch v-model="form.isNotifiable" />
        </el-form-item>

        <el-form-item label="HSE Notification Ref" prop="hseNotificationRef">
          <el-input v-model="form.hseNotificationRef" placeholder="HSE reference number" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">Cancel</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ editingId !== null ? 'Save Changes' : 'Create Notification' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.cdm-register-view {
  padding: 0;
}

.content-card {
  margin-top: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
