<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'; import type { ElTagType } from '@/services/api'
import PageHeader from '@/components/common/PageHeader.vue'

interface AuditLogEntry {
  id: string
  timestamp: string
  userId: string
  userName: string
  action: 'CREATE' | 'UPDATE' | 'DELETE' | 'LOGIN' | 'LOGOUT'
  entityType: string
  entityId: string
  details: string
  ipAddress?: string
}

const loading = ref(false)
const auditLogs = ref<AuditLogEntry[]>([])
const total = ref(0)

const startDate = ref('')
const endDate = ref('')
const filterUser = ref('')
const filterAction = ref('')
const filterEntityType = ref('')

const users = ref<{ id: string; name: string }[]>([])
const currentPage = ref(1)
const pageSize = ref(20)

const entityTypes = [
  { label: 'Company', value: 'Company' },
  { label: 'Contact', value: 'Contact' },
  { label: 'Project', value: 'Project' },
  { label: 'Contract', value: 'Contract' },
  { label: 'Application', value: 'Application' },
  { label: 'Subcontractor', value: 'Subcontractor' },
  { label: 'Operative', value: 'Operative' },
  { label: 'Plant', value: 'Plant' },
  { label: 'User', value: 'User' },
  { label: 'Document', value: 'Document' }
]

const actionTypes = [
  { label: 'Create', value: 'CREATE' },
  { label: 'Update', value: 'UPDATE' },
  { label: 'Delete', value: 'DELETE' },
  { label: 'Login', value: 'LOGIN' },
  { label: 'Logout', value: 'LOGOUT' }
]

const loadAuditLogs = async () => {
  loading.value = true
  try {
    const response = await api.auditLog.getAll({
      page: currentPage.value,
      size: pageSize.value,
      startDate: startDate.value || undefined,
      endDate: endDate.value || undefined,
      userId: filterUser.value || undefined,
      action: filterAction.value || undefined,
      entityType: filterEntityType.value || undefined
    })
    auditLogs.value = response.data.data
    total.value = response.data.total
  } catch {
    ElMessage.error('Failed to load audit logs')
  } finally {
    loading.value = false
  }
}

const loadUsers = async () => {
  try {
    const response = await api.admin.users.getAll({ limit: 100 })
    users.value = response.data.data.map((u: any) => ({ id: u.id, name: `${u.firstName} ${u.lastName}` }))
  } catch {
    // Ignore errors for user loading
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadAuditLogs()
}

const handleReset = () => {
  startDate.value = ''
  endDate.value = ''
  filterUser.value = ''
  filterAction.value = ''
  filterEntityType.value = ''
  currentPage.value = 1
  loadAuditLogs()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadAuditLogs()
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleString()
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getActionType = (action: string): TagType | undefined => {
  const map: Record<string, TagType> = {
    CREATE: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    LOGIN: 'info',
    LOGOUT: 'info'
  }
  return map[action]
}

onMounted(() => {
  loadUsers()
  loadAuditLogs()
})
</script>

<template>
  <div class="audit-log-view">
    <PageHeader title="Audit Log" :breadcrumbs="[{ title: 'Admin', path: '/admin' }, { title: 'Audit Log' }]" />

    <el-card shadow="never">
      <!-- Filters -->
      <div class="filters">
        <el-date-picker
          v-model="startDate"
          type="date"
          placeholder="Start Date"
          style="width: 150px"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
        <el-date-picker
          v-model="endDate"
          type="date"
          placeholder="End Date"
          style="width: 150px"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
        <el-select v-model="filterUser" placeholder="All Users" clearable style="width: 180px">
          <el-option v-for="user in users" :key="user.id" :label="user.name" :value="user.id" />
        </el-select>
        <el-select v-model="filterAction" placeholder="All Actions" clearable style="width: 150px">
          <el-option v-for="action in actionTypes" :key="action.value" :label="action.label" :value="action.value" />
        </el-select>
        <el-select v-model="filterEntityType" placeholder="All Entity Types" clearable style="width: 180px">
          <el-option v-for="type in entityTypes" :key="type.value" :label="type.label" :value="type.value" />
        </el-select>
        <el-button type="primary" @click="handleSearch">Search</el-button>
        <el-button @click="handleReset">Reset</el-button>
      </div>

      <!-- Audit Log Table -->
      <el-table v-loading="loading" :data="auditLogs" stripe style="margin-top: 20px" max-height="500">
        <el-table-column label="Timestamp" width="180">
          <template #default="{ row }">
            {{ formatDate(row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="User" min-width="150" />
        <el-table-column label="Action" width="100">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)" size="small">
              {{ row.action }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="entityType" label="Entity Type" width="150" />
        <el-table-column prop="entityId" label="Entity ID" width="150" />
        <el-table-column prop="details" label="Details" min-width="250" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP Address" width="130" />
      </el-table>

      <!-- Pagination -->
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50, 100]"
        style="margin-top: 20px; justify-content: flex-end"
        @current-change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.audit-log-view {
  .filters {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    align-items: center;
  }
}
</style>
