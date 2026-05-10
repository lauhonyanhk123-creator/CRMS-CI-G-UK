<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import api, { 
  type Site, 
  type Contract, 
  type Tender, 
  type Document,
  type Company 
} from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const route = useRoute()
const router = useRouter()
const siteId = computed(() => route.params.id as string)

// Loading states
const loading = ref(false)
const saving = ref(false)

// Project/Site data
const project = ref<Site | null>(null)
const companies = ref<Company[]>([])

// Contracts
const contracts = ref<Contract[]>([])
const contractDrawerVisible = ref(false)
const editingContract = ref<Partial<Contract> | null>(null)
const contractForm = ref({
  reference: '',
  title: '',
  clientId: '',
  contractValue: 0,
  retentionPercentage: 0,
  paymentTerms: '',
  contractForm: '',
  startDate: '',
  endDate: '',
  status: 'draft' as Contract['status']
})

// Tenders
const tenders = ref<Tender[]>([])
const tenderDrawerVisible = ref(false)
const editingTender = ref<Partial<Tender> | null>(null)
const tenderForm = ref({
  title: '',
  clientId: '',
  siteId: '',
  valueMin: undefined as number | undefined,
  valueMax: undefined as number | undefined,
  returnDate: '',
  winProbability: undefined as number | undefined,
  stage: 'lead' as Tender['stage'],
  notes: ''
})

// Documents
const documents = ref<Document[]>([])
const uploading = ref(false)
const uploadProgress = ref(0)

onMounted(() => {
  loadProject()
  loadContracts()
  loadTenders()
  loadDocuments()
  loadCompanies()
})

const loadProject = async () => {
  if (!siteId.value) return
  loading.value = true
  try {
    const res = await api.sites.getById(siteId.value)
    project.value = res.data
  } catch {
    ElMessage.error('Failed to load project')
    router.push('/projects')
  } finally {
    loading.value = false
  }
}

const loadContracts = async () => {
  try {
    // Note: contracts API doesn't support siteId directly, filter client-side
    const res = await api.contracts.getAll({ clientId: project.value?.clientId })
    contracts.value = res.data.data?.filter((c: Contract) => c.siteId === siteId.value) || []
  } catch {
    ElMessage.error('Failed to load contracts')
  }
}

const loadTenders = async () => {
  try {
    const res = await api.tenders.getAll({ clientId: project.value?.clientId })
    tenders.value = res.data.data?.filter((t: Tender) => t.siteId === siteId.value) || []
  } catch {
    ElMessage.error('Failed to load tenders')
  }
}

const loadDocuments = async () => {
  try {
    const res = await api.documents.getAll({ entityId: siteId.value, entityType: 'site' })
    documents.value = res.data.data || []
  } catch {
    ElMessage.error('Failed to load documents')
  }
}

const loadCompanies = async () => {
  try {
    const res = await api.companies.getAll({ type: 'client', limit: 100 })
    companies.value = res.data.data || []
  } catch { /* ignore */ }
}

// Contract CRUD
const openAddContract = () => {
  editingContract.value = null
  contractForm.value = {
    reference: '',
    title: '',
    clientId: project.value?.clientId || '',
    contractValue: 0,
    retentionPercentage: 0,
    paymentTerms: '',
    contractForm: '',
    startDate: '',
    endDate: '',
    status: 'draft'
  }
  contractDrawerVisible.value = true
}

const openEditContract = (contract: Contract) => {
  editingContract.value = contract
  contractForm.value = {
    reference: contract.reference,
    title: contract.title,
    clientId: contract.clientId,
    contractValue: contract.contractValue,
    retentionPercentage: contract.retentionPercentage,
    paymentTerms: contract.paymentTerms,
    contractForm: contract.contractForm,
    startDate: contract.startDate,
    endDate: contract.endDate ?? '',
    status: contract.status
  }
  contractDrawerVisible.value = true
}

const saveContract = async () => {
  saving.value = true
  try {
    const data = {
      ...contractForm.value,
      siteId: siteId.value,
      clientId: contractForm.value.clientId
    }
    
    if (editingContract.value?.id) {
      await api.contracts.update(editingContract.value.id, data)
      ElMessage.success('Contract updated')
    } else {
      await api.contracts.create(data)
      ElMessage.success('Contract created')
    }
    contractDrawerVisible.value = false
    loadContracts()
  } catch {
    ElMessage.error('Failed to save contract')
  } finally {
    saving.value = false
  }
}

const deleteContract = async (contract: Contract) => {
  try {
    await ElMessageBox.confirm(
      `Delete contract "${contract.reference}"? This cannot be undone.`,
      'Confirm Delete',
      { type: 'warning' }
    )
    await api.contracts.delete(contract.id)
    ElMessage.success('Contract deleted')
    loadContracts()
  } catch { /* cancelled */ }
}

// Tender CRUD
const openAddTender = () => {
  editingTender.value = null
  tenderForm.value = {
    title: '',
    clientId: project.value?.clientId || '',
    siteId: siteId.value,
    valueMin: undefined,
    valueMax: undefined,
    returnDate: '',
    winProbability: undefined,
    stage: 'lead',
    notes: ''
  }
  tenderDrawerVisible.value = true
}

const openEditTender = (tender: Tender) => {
  editingTender.value = tender
  tenderForm.value = {
    title: tender.title,
    clientId: tender.clientId,
    siteId: tender.siteId || '',
    valueMin: tender.valueMin,
    valueMax: tender.valueMax,
    returnDate: tender.returnDate || '',
    winProbability: tender.winProbability,
    stage: tender.stage,
    notes: tender.notes || ''
  }
  tenderDrawerVisible.value = true
}

const saveTender = async () => {
  saving.value = true
  try {
    const data = { ...tenderForm.value, siteId: siteId.value }
    
    if (editingTender.value?.id) {
      await api.tenders.update(editingTender.value.id, data)
      ElMessage.success('Tender updated')
    } else {
      await api.tenders.create(data)
      ElMessage.success('Tender created')
    }
    tenderDrawerVisible.value = false
    loadTenders()
  } catch {
    ElMessage.error('Failed to save tender')
  } finally {
    saving.value = false
  }
}

const deleteTender = async (tender: Tender) => {
  try {
    await ElMessageBox.confirm(
      `Delete tender "${tender.title}"? This cannot be undone.`,
      'Confirm Delete',
      { type: 'warning' }
    )
    await api.tenders.delete(tender.id)
    ElMessage.success('Tender deleted')
    loadTenders()
  } catch { /* cancelled */ }
}

const winTender = async (tender: Tender) => {
  try {
    await ElMessageBox.confirm(
      `Mark tender "${tender.title}" as won? This will create a contract.`,
      'Confirm Win',
      { type: 'info' }
    )
    await api.tenders.win(tender.id)
    ElMessage.success('Tender marked as won')
    loadTenders()
    loadContracts()
  } catch { /* cancelled or failed */ }
}

const loseTender = async (tender: Tender) => {
  try {
    await ElMessageBox.prompt(
      `Enter reason for losing tender "${tender.title}":`,
      'Mark as Lost',
      { type: 'warning', inputPlaceholder: 'Reason...' }
    ).then(({ value }) => {
      api.tenders.lose(tender.id, value)
        .then(() => {
          ElMessage.success('Tender marked as lost')
          loadTenders()
        })
    })
  } catch { /* cancelled */ }
}

// Document upload
const handleFileUpload = async (file: File) => {
  uploading.value = true
  uploadProgress.value = 0
  try {
    await api.documents.upload(file, {
      entityType: 'site',
      entityId: siteId.value,
      category: 'project'
    })
    ElMessage.success('Document uploaded')
    loadDocuments()
  } catch {
    ElMessage.error('Failed to upload document')
  } finally {
    uploading.value = false
  }
}

const downloadDocument = async (doc: Document) => {
  try {
    const res = await api.documents.getDownloadUrl(doc.id)
    window.open(res.data.url, '_blank')
  } catch {
    ElMessage.error('Failed to download document')
  }
}

// Helpers
const formatCurrency = (value?: number) => value ? `£${value.toLocaleString()}` : '—'
const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getContractStatusType = (status: string): TagType => {
  const map: Record<string, TagType> = {
    draft: 'info',
    active: 'success',
    completed: 'primary',
    terminated: 'danger'
  }
  return map[status] ?? 'info'
}

const getTenderStageType = (stage: string): TagType => {
  const map: Record<string, TagType> = {
    lead: 'info',
    qualified: 'info',
    pricing: 'warning',
    submitted: 'primary',
    negotiation: 'warning',
    awarded: 'success',
    lost: 'danger'
  }
  return map[stage] ?? 'info'
}
</script>

<template>
  <div v-if="project" class="project-detail-view">
    <PageHeader 
      :title="project.name" 
      :breadcrumbs="[
        { title: 'Projects', path: '/projects' },
        { title: project.name }
      ]" 
    />

    <el-card shadow="never" class="mb-4">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="Project Code">{{ project.siteCode }}</el-descriptions-item>
        <el-descriptions-item label="Status">
          <StatusBadge :status="project.status" />
        </el-descriptions-item>
        <el-descriptions-item label="Client">{{ project.client?.name || '—' }}</el-descriptions-item>
        <el-descriptions-item label="Location">
          {{ project.address?.addressLine1 }}, {{ project.address?.city }}
        </el-descriptions-item>
        <el-descriptions-item label="Start Date">{{ formatDate(project.startDate) }}</el-descriptions-item>
        <el-descriptions-item label="Est. Completion">{{ formatDate(project.estimatedCompletion) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never">
      <el-tabs>
        <!-- Contracts Tab -->
        <el-tab-pane label="Contracts">
          <template #label>
            <span>Contracts <el-badge :value="contracts.length" :max="99" /></span>
          </template>
          
          <div class="mb-3">
            <el-button type="primary" @click="openAddContract">Add Contract</el-button>
          </div>

          <el-table v-loading="loading" :data="contracts" stripe>
            <el-table-column prop="reference" label="Reference" width="120" />
            <el-table-column prop="title" label="Title" min-width="200" />
            <el-table-column label="Client" width="150">
              <template #default="{ row }">{{ row.client?.name || row.clientId }}</template>
            </el-table-column>
            <el-table-column label="Value" width="140">
              <template #default="{ row }">{{ formatCurrency(row.contractValue) }}</template>
            </el-table-column>
            <el-table-column label="Status" width="100">
              <template #default="{ row }">
                <el-tag :type="getContractStatusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Start Date" width="120">
              <template #default="{ row }">{{ formatDate(row.startDate) }}</template>
            </el-table-column>
            <el-table-column label="Actions" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditContract(row)">Edit</el-button>
                <el-button type="primary" link size="small" @click="router.push(`/contracts/${row.id}`)">View</el-button>
                <el-button type="danger" link size="small" @click="deleteContract(row)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="contracts.length === 0" description="No contracts yet" />
        </el-tab-pane>

        <!-- Tenders Tab -->
        <el-tab-pane label="Tenders">
          <template #label>
            <span>Tenders <el-badge :value="tenders.length" :max="99" /></span>
          </template>
          
          <div class="mb-3">
            <el-button type="primary" @click="openAddTender">Add Tender</el-button>
          </div>

          <el-table v-loading="loading" :data="tenders" stripe>
            <el-table-column prop="title" label="Title" min-width="200" />
            <el-table-column label="Client" width="150">
              <template #default="{ row }">{{ row.client?.name || row.clientId }}</template>
            </el-table-column>
            <el-table-column label="Value Range" width="180">
              <template #default="{ row }">
                {{ formatCurrency(row.valueMin) }} - {{ formatCurrency(row.valueMax) }}
              </template>
            </el-table-column>
            <el-table-column label="Stage" width="120">
              <template #default="{ row }">
                <el-tag :type="getTenderStageType(row.stage)" size="small">{{ row.stage }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Return Date" width="120">
              <template #default="{ row }">{{ formatDate(row.returnDate) }}</template>
            </el-table-column>
            <el-table-column label="Win %" width="80">
              <template #default="{ row }">{{ row.winProbability || '—' }}</template>
            </el-table-column>
            <el-table-column label="Actions" width="220" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openEditTender(row)">Edit</el-button>
                <el-button v-if="row.stage !== 'awarded' && row.stage !== 'lost'" type="success" link size="small" @click="winTender(row)">Win</el-button>
                <el-button v-if="row.stage !== 'awarded' && row.stage !== 'lost'" type="warning" link size="small" @click="loseTender(row)">Lose</el-button>
                <el-button type="danger" link size="small" @click="deleteTender(row)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="tenders.length === 0" description="No tenders yet" />
        </el-tab-pane>

        <!-- Documents Tab -->
        <el-tab-pane label="Documents">
          <template #label>
            <span>Documents <el-badge :value="documents.length" :max="99" /></span>
          </template>
          
          <div class="mb-3">
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="(file: any) => handleFileUpload(file.raw)"
              accept="*"
            >
              <el-button type="primary" :loading="uploading">
                <el-icon class="el-icon--left"><Upload /></el-icon>
                Upload Document
              </el-button>
            </el-upload>
          </div>

          <el-table v-loading="loading" :data="documents" stripe>
            <el-table-column prop="filename" label="Filename" min-width="200" />
            <el-table-column label="Size" width="100">
              <template #default="{ row }">
                {{ (row.size / 1024).toFixed(1) }} KB
              </template>
            </el-table-column>
            <el-table-column prop="category" label="Category" width="120" />
            <el-table-column label="Uploaded" width="150">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="Actions" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="downloadDocument(row)">Download</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="documents.length === 0" description="No documents yet">
            <template #extra>
              <span class="text-gray-500 text-sm">Upload documents using the button above</span>
            </template>
          </el-empty>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Contract Drawer -->
    <el-drawer
      v-model="contractDrawerVisible"
      :title="editingContract?.id ? 'Edit Contract' : 'Add Contract'"
      size="600px"
    >
      <el-form :model="contractForm" label-position="top" :disabled="saving">
        <el-form-item label="Reference" required>
          <el-input v-model="contractForm.reference" placeholder="CON-001" />
        </el-form-item>
        <el-form-item label="Title" required>
          <el-input v-model="contractForm.title" placeholder="Contract title" />
        </el-form-item>
        <el-form-item label="Client">
          <el-select v-model="contractForm.clientId" placeholder="Select client" filterable style="width: 100%">
            <el-option
              v-for="company in companies"
              :key="company.id"
              :label="company.name"
              :value="company.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Contract Value">
              <el-input-number v-model="contractForm.contractValue" :min="0" :step="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Retention %">
              <el-input-number v-model="contractForm.retentionPercentage" :min="0" :max="100" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Payment Terms">
          <el-input v-model="contractForm.paymentTerms" placeholder="e.g., Monthly in arrears" />
        </el-form-item>
        <el-form-item label="Contract Form">
          <el-input v-model="contractForm.contractForm" placeholder="e.g., JCT Standard Building Contract" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Start Date">
              <el-date-picker v-model="contractForm.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="End Date">
              <el-date-picker v-model="contractForm.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Status">
          <el-select v-model="contractForm.status" style="width: 100%">
            <el-option label="Draft" value="draft" />
            <el-option label="Active" value="active" />
            <el-option label="Completed" value="completed" />
            <el-option label="Terminated" value="terminated" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contractDrawerVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="saveContract">
          {{ editingContract?.id ? 'Update' : 'Create' }}
        </el-button>
      </template>
    </el-drawer>

    <!-- Tender Drawer -->
    <el-drawer
      v-model="tenderDrawerVisible"
      :title="editingTender?.id ? 'Edit Tender' : 'Add Tender'"
      size="600px"
    >
      <el-form :model="tenderForm" label-position="top" :disabled="saving">
        <el-form-item label="Title" required>
          <el-input v-model="tenderForm.title" placeholder="Tender title" />
        </el-form-item>
        <el-form-item label="Client">
          <el-select v-model="tenderForm.clientId" placeholder="Select client" filterable style="width: 100%">
            <el-option
              v-for="company in companies"
              :key="company.id"
              :label="company.name"
              :value="company.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Min Value">
              <el-input-number v-model="tenderForm.valueMin" :min="0" :step="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Max Value">
              <el-input-number v-model="tenderForm.valueMax" :min="0" :step="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Return Date">
              <el-date-picker v-model="tenderForm.returnDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Win Probability %">
              <el-input-number v-model="tenderForm.winProbability" :min="0" :max="100" :step="5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Stage">
          <el-select v-model="tenderForm.stage" style="width: 100%">
            <el-option label="Lead" value="lead" />
            <el-option label="Qualified" value="qualified" />
            <el-option label="Pricing" value="pricing" />
            <el-option label="Submitted" value="submitted" />
            <el-option label="Negotiation" value="negotiation" />
            <el-option label="Awarded" value="awarded" />
            <el-option label="Lost" value="lost" />
          </el-select>
        </el-form-item>
        <el-form-item label="Notes">
          <el-input v-model="tenderForm.notes" type="textarea" :rows="3" placeholder="Additional notes..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tenderDrawerVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="saveTender">
          {{ editingTender?.id ? 'Update' : 'Create' }}
        </el-button>
      </template>
    </el-drawer>
  </div>

  <el-skeleton v-else v-loading="loading" :rows="10" animated />
</template>

<style lang="scss" scoped>
.project-detail-view {
  .mb-3 { margin-bottom: 16px; }
  .mb-4 { margin-bottom: 16px; }
  .text-gray-500 { color: #999; }
}
</style>
