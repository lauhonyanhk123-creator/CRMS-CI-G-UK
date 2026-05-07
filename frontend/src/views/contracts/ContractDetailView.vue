<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api, { apiClient, type Contract, type Variation, type Application, type RetentionLedgerEntry, type Document, type AdoptionCase } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const route = useRoute()
const contractId = computed(() => route.params.id as string)

const loading = ref(false)
const contract = ref<Contract | null>(null)
const applications = ref<Application[]>([])
const variations = ref<Variation[]>([])

// Documents
const documents = ref<Document[]>([])
const loadingDocuments = ref(false)

// Adoption Cases
const adoptionCases = ref<AdoptionCase[]>([])
const loadingAdoptionCases = ref(false)

// Variations
const varDrawerVisible = ref(false)
const varDrawerLoading = ref(false)
const editingVarId = ref('')
const varForm = ref({ description: '', contractVariationOrder: '', value: 0, status: 'pending' as Variation['status'] })

// Retention Ledger
const retentionLedger = ref<RetentionLedgerEntry[]>([])
const retentionDialogVisible = ref(false)
const retentionDialogLoading = ref(false)
const editingRetentionId = ref('')
const retentionForm = ref({ applicationId: '', amount: 0, percentage: 0, releaseDate: '', status: 'held' as RetentionLedgerEntry['status'] })

onMounted(() => loadData())

const loadData = async () => {
  if (!contractId.value) return
  loading.value = true
  try {
    const [contractRes, appsRes] = await Promise.all([
      api.contracts.getById(contractId.value),
      api.applications.getByContract(contractId.value)
    ])
    contract.value = contractRes.data
    applications.value = appsRes.data
    variations.value = contractRes.data.variations || []
    await loadPayLessNotices()
    await loadRetentionLedger()
    await loadDocuments()
    await loadAdoptionCases()
  } catch { ElMessage.error('Failed to load contract') } finally { loading.value = false }
}

const loadDocuments = async () => {
  if (!contractId.value) return
  loadingDocuments.value = true
  try {
    const res = await api.documents.getAll({ entityId: contractId.value, entityType: 'contract' })
    documents.value = res.data.data
  } catch {
    ElMessage.error('Failed to load documents')
  } finally {
    loadingDocuments.value = false
  }
}

const loadAdoptionCases = async () => {
  if (!contractId.value) return
  loadingAdoptionCases.value = true
  try {
    const res = await api.adoption.getAll({})
    // Filter adoption cases related to this contract
    adoptionCases.value = res.data.data.filter((c: AdoptionCase) => c.contractId === contractId.value || c.title.toLowerCase().includes('contract'))
  } catch {
    // Silently fail - adoption cases may not be available
    adoptionCases.value = []
  } finally {
    loadingAdoptionCases.value = false
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

const formatCurrency = (value: number) => `£${value.toLocaleString()}`

const submitApplication = async (app: Application) => {
  try {
    await api.applications.submit(contractId.value, app.id)
    ElMessage.success('Application submitted')
    loadData()
  } catch { ElMessage.error('Failed to submit application') }
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'

const getApplicationStatusType = (status: string): TagType => {
  const map: Record<string, TagType> = { draft: 'info', submitted: 'warning', measured: 'primary', agreed: 'success', paid: 'success' }
  return map[status] ?? 'info'
}

const getAdoptionStatusType = (status?: string): TagType => {
  const map: Record<string, TagType> = {
    'pre_application': 'info',
    'application': 'warning',
    'technical_approval': 'primary',
    'under_construction': 'primary',
    'adopted': 'success',
    'rejected': 'danger'
  }
  return map[status || ''] ?? 'info'
}

// Variations handlers
const handleAddVar = () => { editingVarId.value = ''; varForm.value = { description: '', contractVariationOrder: '', value: 0, status: 'pending' }; varDrawerVisible.value = true }
const handleEditVar = (v: Variation) => { editingVarId.value = v.id; varForm.value = { description: v.description, contractVariationOrder: (v as any).contractVariationOrder || '', value: v.value, status: v.status }; varDrawerVisible.value = true }
const handleSaveVar = async () => {
  varDrawerLoading.value = true
  try {
    if (editingVarId.value) {
      await api.contracts.update(contractId.value, { variations: [...variations.value.map(v => v.id === editingVarId.value ? { ...v, ...varForm.value } : v)] } as any)
    } else {
      const newVariation = { id: crypto.randomUUID(), contractId: contractId.value, reference: `VAR-${variations.value.length + 1}`, ...varForm.value }
      await api.contracts.update(contractId.value, { variations: [...variations.value, newVariation] } as any)
    }
    ElMessage.success('Variation saved')
    varDrawerVisible.value = false
    loadData()
  } catch { ElMessage.error('Failed to save variation') } finally { varDrawerLoading.value = false }
}

// Retention Ledger
const loadRetentionLedger = async () => {
  try {
    const response = await api.retentionLedger.getByContract(contractId.value)
    retentionLedger.value = response.data
  } catch {
    retentionLedger.value = applications.value.map(app => ({
      id: app.id,
      contractId: contractId.value,
      applicationId: app.id,
      amount: app.retentionAmount,
      percentage: contract.value?.retentionPercentage || 0,
      status: 'held' as const,
      createdAt: app.createdAt || new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }))
  }
}

const handleAddRetention = () => { editingRetentionId.value = ''; retentionForm.value = { applicationId: '', amount: 0, percentage: contract.value?.retentionPercentage || 0, releaseDate: '', status: 'held' }; retentionDialogVisible.value = true }
const handleSaveRetention = async () => {
  retentionDialogLoading.value = true
  try {
    if (editingRetentionId.value) {
      await api.retentionLedger.update(contractId.value, editingRetentionId.value, retentionForm.value)
    } else {
      await api.retentionLedger.create(contractId.value, { ...retentionForm.value, contractId: contractId.value })
    }
    ElMessage.success('Retention entry saved')
    retentionDialogVisible.value = false
    loadRetentionLedger()
  } catch { ElMessage.error('Failed to save retention entry') } finally { retentionDialogLoading.value = false }
}

const retentionBalance = computed(() => {
  const held = retentionLedger.value.filter(r => r.status === 'held').reduce((sum, a) => sum + a.amount, 0)
  const released = retentionLedger.value.filter(r => r.status === 'released').reduce((sum, a) => sum + a.amount, 0)
  return { held, released, balance: held - released }
})

// Pay Less Notices
const payLessNotices = ref<any[]>([])

const loadPayLessNotices = async () => {
  if (!contractId.value) return
  try {
    const response = await apiClient.get(`/contracts/${contractId.value}/pay-less-notices`)
    payLessNotices.value = response.data
  } catch {}
}
</script>

<template>
  <div class="contract-detail-view" v-if="contract">
    <PageHeader :title="`${contract.reference} - ${contract.title}`" :breadcrumbs="[{ title: 'Contracts', path: '/contracts' }, { title: contract.reference }]" />

    <el-tabs>
      <el-tab-pane label="Overview">
        <el-card shadow="never">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="Reference">{{ contract.reference }}</el-descriptions-item>
            <el-descriptions-item label="Status"><StatusBadge :status="contract.status" /></el-descriptions-item>
            <el-descriptions-item label="Client">{{ contract.client?.name }}</el-descriptions-item>
            <el-descriptions-item label="Site">{{ contract.site?.name }}</el-descriptions-item>
            <el-descriptions-item label="Contract Value">{{ formatCurrency(contract.contractValue) }}</el-descriptions-item>
            <el-descriptions-item label="Retention %">{{ contract.retentionPercentage }}%</el-descriptions-item>
            <el-descriptions-item label="Contract Form">{{ contract.contractForm || '—' }}</el-descriptions-item>
            <el-descriptions-item label="Payment Terms">{{ contract.paymentTerms || '—' }}</el-descriptions-item>
            <el-descriptions-item label="Start Date">{{ formatDate(contract.startDate) }}</el-descriptions-item>
            <el-descriptions-item label="End Date">{{ formatDate(contract.endDate) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Variations">
        <el-card shadow="never">
          <template #header><div class="card-header"><span>Variations ({{ variations.length }})</span><el-button type="primary" size="small" @click="handleAddVar">Add Variation</el-button></div></template>
          <el-table :data="variations" stripe>
            <el-table-column prop="reference" label="Ref" width="100" />
            <el-table-column prop="description" label="Description" min-width="200" />
            <el-table-column prop="contractVariationOrder" label="CVO Reference" width="140" />
            <el-table-column label="Value" width="140"><template #default="{ row }">{{ formatCurrency(row.value) }}</template></el-table-column>
            <el-table-column label="Status" width="120"><template #default="{ row }"><el-tag :type="row.status === 'approved' ? 'success' : row.status === 'rejected' ? 'danger' : 'warning'" size="small">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="Actions" width="100"><template #default="{ row }"><el-button link type="primary" size="small" @click="handleEditVar(row)">Edit</el-button></template></el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Applications for Payment">
        <el-card shadow="never">
          <template #header><div class="card-header"><span>Applications ({{ applications.length }})</span><el-button type="primary" size="small">New Application</el-button></div></template>
          <el-table :data="applications" stripe>
            <el-table-column label="App #" width="80"><template #default="{ row }">#{{ row.applicationNumber }}</template></el-table-column>
            <el-table-column label="Period" width="200"><template #default="{ row }">{{ formatDate(row.periodStart) }} - {{ formatDate(row.periodEnd) }}</template></el-table-column>
            <el-table-column label="Value" width="140"><template #default="{ row }">{{ formatCurrency(row.applicationValue) }}</template></el-table-column>
            <el-table-column label="Retention" width="100"><template #default="{ row }">{{ formatCurrency(row.retentionAmount) }}</template></el-table-column>
            <el-table-column label="Status" width="120"><template #default="{ row }"><el-tag :type="getApplicationStatusType(row.status)" size="small">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="Actions" width="180">
              <template #default="{ row }">
                <el-button v-if="row.status === 'draft'" type="primary" size="small" @click="submitApplication(row)">Submit</el-button>
                <el-button size="small">Notices</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Retention Ledger">
        <el-card shadow="never">
          <template #header><div class="card-header"><span>Retention Ledger</span><el-button type="primary" size="small" @click="handleAddRetention">Add Retention</el-button></div></template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="Total Held">{{ formatCurrency(retentionBalance.held) }}</el-descriptions-item>
            <el-descriptions-item label="Total Released">{{ formatCurrency(retentionBalance.released) }}</el-descriptions-item>
            <el-descriptions-item label="Balance">{{ formatCurrency(retentionBalance.balance) }}</el-descriptions-item>
          </el-descriptions>
          <el-table :data="retentionLedger" stripe class="mt-4">
            <el-table-column label="Application" width="100"><template #default="{ row }">#{{ row.application?.applicationNumber || row.applicationId }}</template></el-table-column>
            <el-table-column label="Percentage" width="100"><template #default="{ row }">{{ row.percentage }}%</template></el-table-column>
            <el-table-column label="Amount" width="140"><template #default="{ row }">{{ formatCurrency(row.amount) }}</template></el-table-column>
            <el-table-column label="Release Date" width="120"><template #default="{ row }">{{ formatDate(row.releaseDate) }}</template></el-table-column>
            <el-table-column label="Status" width="120"><template #default="{ row }"><el-tag :type="row.status === 'held' ? 'warning' : row.status === 'released' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag></template></el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Pay Less Notices">
        <el-card shadow="never">
          <template #header><span>Pay Less Notices ({{ payLessNotices.length }})</span></template>
          <el-table :data="payLessNotices" stripe>
            <el-table-column prop="noticeNumber" label="Notice #" width="100" />
            <el-table-column label="Application" width="100"><template #default="{ row }">#{{ row.applicationNumber }}</template></el-table-column>
            <el-table-column label="Notice Date" width="120"><template #default="{ row }">{{ formatDate(row.noticeDate) }}</template></el-table-column>
            <el-table-column label="Sum Not Due" width="140"><template #default="{ row }">{{ formatCurrency(row.sumNotDue) }}</template></el-table-column>
            <el-table-column prop="reasons" label="Reasons" min-width="200" />
            <el-table-column label="Status" width="120"><template #default="{ row }"><el-tag :type="row.status === 'served' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="Served Date" width="120"><template #default="{ row }">{{ formatDate(row.servedDate) }}</template></el-table-column>
          </el-table>
          <el-empty v-if="payLessNotices.length === 0" description="No pay less notices" />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Documents">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>Documents ({{ documents.length }})</span>
            </div>
          </template>
          <el-table :data="documents" stripe v-loading="loadingDocuments">
            <el-table-column prop="filename" label="Filename" min-width="200" />
            <el-table-column prop="category" label="Category" width="120">
              <template #default="{ row }">
                {{ row.category || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="Size" width="100">
              <template #default="{ row }">
                {{ row.size ? (row.size / 1024).toFixed(1) + ' KB' : '—' }}
              </template>
            </el-table-column>
            <el-table-column label="Uploaded" width="120">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
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

      <el-tab-pane label="Adoption Cases">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>Adoption Cases ({{ adoptionCases.length }})</span>
            </div>
          </template>
          <el-table :data="adoptionCases" stripe v-loading="loadingAdoptionCases">
            <el-table-column prop="caseRef" label="Case Ref" width="120" />
            <el-table-column prop="title" label="Title" min-width="200" />
            <el-table-column prop="type" label="Type" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.type?.toUpperCase() || '—' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Bond Value" width="140">
              <template #default="{ row }">
                {{ row.bondValue ? formatCurrency(row.bondValue) : '—' }}
              </template>
            </el-table-column>
            <el-table-column label="Status" width="140">
              <template #default="{ row }">
                <el-tag :type="getAdoptionStatusType(row.status)" size="small">{{ row.status?.replace('_', ' ') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="100">
              <template #default="{ row }">
                <el-button link type="primary" size="small">View</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="adoptionCases.length === 0" description="No adoption cases" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-drawer v-model="varDrawerVisible" title="Variation" size="500px">
      <el-form :model="varForm" label-position="top">
        <el-form-item label="Description"><el-input v-model="varForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="Contract Variation Order Reference"><el-input v-model="varForm.contractVariationOrder" placeholder="CVO-XXX" /></el-form-item>
        <el-form-item label="Value"><el-input-number v-model="varForm.value" :min="0" :step="100" style="width:100%" /></el-form-item>
        <el-form-item label="Status"><el-select v-model="varForm.status"><el-option label="Pending" value="pending" /><el-option label="Approved" value="approved" /><el-option label="Rejected" value="rejected" /></el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="varDrawerVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="varDrawerLoading" @click="handleSaveVar">Save</el-button>
      </template>
    </el-drawer>

    <el-dialog v-model="retentionDialogVisible" title="Retention Entry" width="500px">
      <el-form :model="retentionForm" label-position="top">
        <el-form-item label="Application">
          <el-select v-model="retentionForm.applicationId" placeholder="Select application" style="width:100%">
            <el-option v-for="app in applications" :key="app.id" :label="`#${app.applicationNumber} - ${formatCurrency(app.applicationValue)}`" :value="app.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Amount"><el-input-number v-model="retentionForm.amount" :min="0" :step="100" style="width:100%" /></el-form-item>
        <el-form-item label="Percentage"><el-input-number v-model="retentionForm.percentage" :min="0" :max="100" :step="0.1" style="width:100%" /></el-form-item>
        <el-form-item label="Release Date"><el-date-picker v-model="retentionForm.releaseDate" type="date" style="width:100%" /></el-form-item>
        <el-form-item label="Status"><el-select v-model="retentionForm.status"><el-option label="Held" value="held" /><el-option label="Released" value="released" /><el-option label="Proposed" value="proposed" /></el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="retentionDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="retentionDialogLoading" @click="handleSaveRetention">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.contract-detail-view { .card-header { display: flex; justify-content: space-between; align-items: center; } }
</style>
