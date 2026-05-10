<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Edit, Refresh } from '@element-plus/icons-vue'
import api, { type Site, type Operative, type PlantItem, type Document } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import FileUpload from '@/components/common/FileUpload.vue'

const route = useRoute()
const siteId = computed(() => route.params.id as string)

const loading = ref(false)
const site = ref<Site | null>(null)
const operatives = ref<Operative[]>([])
const plantItems = ref<PlantItem[]>([])
const documents = ref<Document[]>([])
const activityLog = ref<any[]>([])
const activityLoading = ref(false)

onMounted(() => loadData())

const loadData = async () => {
  if (!siteId.value) return
  loading.value = true
  try {
    const [siteRes, operativesRes, plantRes, docsRes] = await Promise.all([
      api.sites.getById(siteId.value),
      api.operatives.getAll({ siteId: siteId.value }),
      api.plant.getAll({ siteId: siteId.value }),
      api.documents.getAll({ entityId: siteId.value, entityType: 'site' })
    ])
    site.value = siteRes.data
    operatives.value = operativesRes.data.data || []
    plantItems.value = plantRes.data.data || []
    documents.value = docsRes.data.data || []
  } catch { ElMessage.error('Failed to load site') } finally { loading.value = false }
  loadActivityLog()
}

const loadActivityLog = async () => {
  activityLoading.value = true
  try {
    const res = await api.auditLog.getAll({ entityType: 'SITE', entityId: siteId.value, limit: 50 })
    activityLog.value = res.data.data || []
  } catch {
    activityLog.value = []
  } finally {
    activityLoading.value = false
  }
}

const formatDateTime = (ts?: string) => ts ? new Date(ts).toLocaleString() : '—'

const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getOperativeStatusType = (status: string): TagType => {
  const map: Record<string, TagType> = { active: 'success', inactive: 'info', suspended: 'danger' }
  return map[status] ?? 'info'
}

const getPlantStatusType = (status: string): TagType => {
  const map: Record<string, TagType> = { AVAILABLE: 'success', HIRED_OUT: 'warning', MAINTENANCE: 'danger', DECOMMISSIONED: 'info' }
  return map[status] ?? 'info'
}

const handleUploadSuccess = () => {
  ElMessage.success('Document uploaded')
  loadData()
}

const downloadDocument = async (doc: Document) => {
  try {
    const res = await api.documents.getDownloadUrl(doc.id)
    window.open(res.data.url, '_blank')
  } catch {
    ElMessage.error('Failed to download document')
  }
}
</script>

<template>
  <div v-if="site" class="site-detail-view">
    <PageHeader 
      :title="site.name" 
      :breadcrumbs="[
        { title: 'Sites', path: '/sites' },
        { title: site.name }
      ]"
    >
      <template #actions>
        <el-button :icon="Refresh" @click="loadData">Refresh</el-button>
        <el-button :icon="Edit">Edit</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="site-info-card">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="Site Code">{{ site.siteCode }}</el-descriptions-item>
        <el-descriptions-item label="Status">
          <StatusBadge :status="site.status" />
        </el-descriptions-item>
        <el-descriptions-item label="Client">{{ site.client?.name }}</el-descriptions-item>
        <el-descriptions-item label="Grid Reference">{{ site.gridReference || '—' }}</el-descriptions-item>
        <el-descriptions-item label="Start Date">{{ formatDate(site.startDate) }}</el-descriptions-item>
        <el-descriptions-item label="Est. Completion">{{ formatDate(site.estimatedCompletion) }}</el-descriptions-item>
        <el-descriptions-item label="Address" :span="3">
          {{ site.address.addressLine1 }},
          {{ site.address.addressLine2 ? site.address.addressLine2 + ', ' : '' }}
          {{ site.address.city }}, {{ site.address.postcode }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />
      
      <div class="map-placeholder">
        <el-icon :size="48" color="#909399"><Location /></el-icon>
        <p>Map View - Grid Reference: {{ site.gridReference || 'Not set' }}</p>
      </div>
    </el-card>

    <el-tabs>
      <el-tab-pane label="Operatives">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>Operatives on Site ({{ operatives.length }})</span>
            </div>
          </template>
          <el-table :data="operatives" stripe>
            <el-table-column label="Name" min-width="180">
              <template #default="{ row }">{{ row.firstName }} {{ row.lastName }}</template>
            </el-table-column>
            <el-table-column prop="employeeRef" label="Employee Ref" width="120" />
            <el-table-column label="CSCS Card" width="150">
              <template #default="{ row }">
                <el-tag v-if="row.cscsCard" size="small" :type="row.cscsCard.verified ? 'success' : 'warning'">
                  {{ row.cscsCard.cardType }}
                </el-tag>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column label="Induction" width="100">
              <template #default="{ row }">
                <el-tag :type="row.inductionStatus === 'complete' ? 'success' : row.inductionStatus === 'expired' ? 'danger' : 'warning'" size="small">
                  {{ row.inductionStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Status" width="100">
              <template #default="{ row }">
                <el-tag :type="getOperativeStatusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="operatives.length === 0" description="No operatives assigned to this site" />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Plant">
        <el-card shadow="never">
          <template #header>
            <span>Plant on Site ({{ plantItems.length }})</span>
          </template>
          <el-table :data="plantItems" stripe>
            <el-table-column prop="plantRef" label="Ref" width="100" />
            <el-table-column prop="description" label="Description" min-width="200" />
            <el-table-column prop="category" label="Category" width="140" />
            <el-table-column label="Make/Model" width="140">
              <template #default="{ row }">{{ row.make }} {{ row.model }}</template>
            </el-table-column>
            <el-table-column prop="hireRate" label="Hire Rate" width="100">
              <template #default="{ row }">{{ row.hireRate ? '£' + row.hireRate + '/day' : '—' }}</template>
            </el-table-column>
            <el-table-column label="Status" width="120">
              <template #default="{ row }">
                <el-tag :type="getPlantStatusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="plantItems.length === 0" description="No plant allocated to this site" />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Documents">
        <el-card shadow="never">
          <template #header>
            <span>Documents</span>
          </template>
          <FileUpload 
            :entity-type="'site'" 
            :entity-id="siteId"
            @success="handleUploadSuccess"
          />
          <el-divider />
          <el-table :data="documents" stripe>
            <el-table-column prop="filename" label="Filename" min-width="200" />
            <el-table-column prop="category" label="Category" width="120" />
            <el-table-column label="Size" width="100">
              <template #default="{ row }">{{ (row.size / 1024).toFixed(1) }} KB</template>
            </el-table-column>
            <el-table-column label="Uploaded" width="120">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="Actions" width="100">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="downloadDocument(row)">Download</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="documents.length === 0" description="No documents" />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Activity Log">
        <el-card v-loading="activityLoading" shadow="never">
          <el-empty v-if="activityLog.length === 0 && !activityLoading" description="No activity recorded for this site" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="entry in activityLog"
              :key="entry.id"
              :timestamp="formatDateTime(entry.createdAt ?? entry.timestamp)"
              placement="top"
            >
              <strong>{{ entry.action }}</strong>
              <span v-if="entry.username" style="margin-left: 8px; color: #606266;">by {{ entry.username }}</span>
              <p v-if="entry.details" style="margin: 4px 0 0; color: #909399; font-size: 12px;">{{ entry.details }}</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style lang="scss" scoped>
.site-detail-view {
  .site-info-card {
    margin-bottom: 16px;
  }
  
  .map-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 200px;
    background: #f5f7fa;
    border-radius: 8px;
    border: 1px dashed #dcdfe6;
    
    p {
      margin-top: 12px;
      color: #909399;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>