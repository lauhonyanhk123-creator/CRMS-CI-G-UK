<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, View, ArrowDown } from '@element-plus/icons-vue'
import api, { type HealthSafetyRecord, type RAMS, type PermitToDig, type Incident } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const activeTab = ref('f10')
const loading = ref(false)

// Dialog states
const showF10Dialog = ref(false)
const showCPPDialog = ref(false)
const showRAMSDialog = ref(false)
const showPermitDialog = ref(false)
const showIncidentDialog = ref(false)
const showDetailDrawer = ref(false)
const editingId = ref<string | null>(null)
const detailRecord = ref<any>(null)

// Form refs
const f10FormRef = ref()
const cppFormRef = ref()
const ramsFormRef = ref()
const permitFormRef = ref()
const incidentFormRef = ref()

// Records
const f10Records = ref<HealthSafetyRecord[]>([])
const cppRecords = ref<HealthSafetyRecord[]>([])
const ramsRecords = ref<RAMS[]>([])
const permits = ref<PermitToDig[]>([])
const incidents = ref<Incident[]>([])

// Sites for dropdowns
const sites = ref<any[]>([])
const contracts = ref<any[]>([])

// Form models
const f10Form = reactive({
  title: '',
  siteId: '',
  status: 'draft',
  data: { description: '', submittedDate: '', reference: '' }
})

const cppForm = reactive({
  title: '',
  siteId: '',
  status: 'draft',
  data: { description: '', version: '1.0', reviewDate: '' }
})

const ramsForm = reactive({
  title: '',
  contractId: '',
  status: 'draft',
  data: {
    methodStatement: '',
    riskAssessments: [] as string[],
    ppeRequired: [] as string[],
    signOnRequired: false
  }
})

const permitForm = reactive({
  title: '',
  siteId: '',
  status: 'draft' as 'draft' | 'prechecked' | 'issued' | 'completed',
  data: {
    location: '',
    startDate: '',
    endDate: '',
    excavations: [] as string[],
    services: [] as string[]
  }
})

const incidentForm = reactive({
  title: '',
  siteId: '',
  status: 'draft',
  data: {
    severity: 'near_miss' as 'near_miss' | 'minor' | 'major' | 'fatal',
    description: '',
    location: '',
    date: '',
    personsInvolved: [] as string[],
    witnesses: [] as string[],
    actionTaken: '',
    rootCause: '',
    correctiveAction: ''
  }
})

// Validation rules
const rules = {
  title: [{ required: true, message: 'Title is required', trigger: 'blur' }],
  siteId: [{ required: true, message: 'Site is required', trigger: 'change' }],
  contractId: [{ required: true, message: 'Contract is required', trigger: 'change' }],
  status: [{ required: true, message: 'Status is required', trigger: 'change' }]
}

onMounted(() => {
  loadF10()
  loadPermits()
  loadIncidents()
  loadSites()
  loadContracts()
})

const loadSites = async () => {
  try {
    const response = await api.sites.getAll({})
    sites.value = response.data.data
  } catch { /* silent */ }
}

const loadContracts = async () => {
  try {
    const response = await api.contracts.getAll({})
    contracts.value = response.data.data
  } catch { /* silent */ }
}

const loadF10 = async () => {
  loading.value = true
  try {
    const response = await api.healthSafety.getF10Notifications({})
    f10Records.value = response.data.data
  } catch { ElMessage.error('Failed to load F10 notifications') } finally { loading.value = false }
}

const loadCPPs = async () => {
  loading.value = true
  try {
    const response = await api.healthSafety.getCPPs({})
    cppRecords.value = response.data.data
  } catch { ElMessage.error('Failed to load CPPs') } finally { loading.value = false }
}

const loadRAMS = async () => {
  loading.value = true
  try {
    const response = await api.healthSafety.getRAMS({})
    ramsRecords.value = response.data.data
  } catch { ElMessage.error('Failed to load RAMS') } finally { loading.value = false }
}

const loadPermits = async () => {
  loading.value = true
  try {
    const response = await api.healthSafety.getPermits({})
    permits.value = response.data.data
  } catch { ElMessage.error('Failed to load permits') } finally { loading.value = false }
}

const loadIncidents = async () => {
  loading.value = true
  try {
    const response = await api.healthSafety.getIncidents({})
    incidents.value = response.data.data
  } catch { ElMessage.error('Failed to load incidents') } finally { loading.value = false }
}

const handleTabChange = (tab: string | number) => {
  if (tab === 'f10') loadF10()
  else if (tab === 'cpp') loadCPPs()
  else if (tab === 'rams') loadRAMS()
  else if (tab === 'permits') loadPermits()
  else if (tab === 'incidents') loadIncidents()
}

// Open dialogs
const openF10Create = () => {
  editingId.value = null
  Object.assign(f10Form, { title: '', siteId: '', status: 'draft', data: { description: '', submittedDate: '', reference: '' } })
  showF10Dialog.value = true
}

const openF10Edit = (row: HealthSafetyRecord) => {
  editingId.value = row.id
  Object.assign(f10Form, { title: row.title, siteId: row.siteId || '', status: row.status, data: row.data })
  showF10Dialog.value = true
}

const openCPPCreate = () => {
  editingId.value = null
  Object.assign(cppForm, { title: '', siteId: '', status: 'draft', data: { description: '', version: '1.0', reviewDate: '' } })
  showCPPDialog.value = true
}

const openCPPEdit = (row: HealthSafetyRecord) => {
  editingId.value = row.id
  Object.assign(cppForm, { title: row.title, siteId: row.siteId || '', status: row.status, data: row.data })
  showCPPDialog.value = true
}

const openRAMSCreate = () => {
  editingId.value = null
  Object.assign(ramsForm, {
    title: '', contractId: '', status: 'draft',
    data: { methodStatement: '', riskAssessments: [], ppeRequired: [], signOnRequired: false }
  })
  showRAMSDialog.value = true
}

const openRAMSEdit = (row: RAMS) => {
  editingId.value = row.id
  Object.assign(ramsForm, { title: row.title, contractId: row.contractId || '', status: row.status, data: row.data })
  showRAMSDialog.value = true
}

const openPermitCreate = () => {
  editingId.value = null
  Object.assign(permitForm, {
    title: '', siteId: '', status: 'draft',
    data: { location: '', startDate: '', endDate: '', excavations: [], services: [] }
  })
  showPermitDialog.value = true
}

const openPermitEdit = (row: PermitToDig) => {
  editingId.value = row.id
  Object.assign(permitForm, { title: row.title, siteId: row.siteId || '', status: row.data.status || 'draft', data: row.data })
  showPermitDialog.value = true
}

const openIncidentCreate = () => {
  editingId.value = null
  Object.assign(incidentForm, {
    title: '', siteId: '', status: 'draft',
    data: {
      severity: 'near_miss', description: '', location: '', date: '',
      personsInvolved: [], witnesses: [], actionTaken: '', rootCause: '', correctiveAction: ''
    }
  })
  showIncidentDialog.value = true
}

const openIncidentEdit = (row: Incident) => {
  editingId.value = row.id
  Object.assign(incidentForm, { title: row.title, siteId: row.siteId || '', status: row.status, data: row.data })
  showIncidentDialog.value = true
}

// View detail
const viewDetail = async (row: any, type: string) => {
  detailRecord.value = row
  showDetailDrawer.value = true
}

// CRUD operations
const saveF10 = async () => {
  await f10FormRef.value?.validate()
  try {
    if (editingId.value) {
      await api.healthSafety.updateF10(editingId.value, f10Form as any)
      ElMessage.success('F10 notification updated')
    } else {
      await api.healthSafety.createF10(f10Form as any)
      ElMessage.success('F10 notification created')
    }
    showF10Dialog.value = false
    loadF10()
  } catch { ElMessage.error('Failed to save F10 notification') }
}

const saveCPP = async () => {
  await cppFormRef.value?.validate()
  try {
    if (editingId.value) {
      await api.healthSafety.updateCPP(editingId.value, cppForm as any)
      ElMessage.success('CPP updated')
    } else {
      await api.healthSafety.createCPP(cppForm as any)
      ElMessage.success('CPP created')
    }
    showCPPDialog.value = false
    loadCPPs()
  } catch { ElMessage.error('Failed to save CPP') }
}

const saveRAMS = async () => {
  await ramsFormRef.value?.validate()
  try {
    if (editingId.value) {
      await api.healthSafety.updateRAMS(editingId.value, ramsForm as any)
      ElMessage.success('RAMS updated')
    } else {
      await api.healthSafety.createRAMS(ramsForm as any)
      ElMessage.success('RAMS created')
    }
    showRAMSDialog.value = false
    loadRAMS()
  } catch { ElMessage.error('Failed to save RAMS') }
}

const savePermit = async () => {
  await permitFormRef.value?.validate()
  try {
    const payload = { ...permitForm, data: { ...permitForm.data, status: permitForm.status } }
    if (editingId.value) {
      await api.healthSafety.updatePermit(editingId.value, payload as any)
      ElMessage.success('Permit updated')
    } else {
      await api.healthSafety.createPermit(payload as any)
      ElMessage.success('Permit created')
    }
    showPermitDialog.value = false
    loadPermits()
  } catch { ElMessage.error('Failed to save permit') }
}

const saveIncident = async () => {
  await incidentFormRef.value?.validate()
  try {
    if (editingId.value) {
      await api.healthSafety.updateIncident(editingId.value, incidentForm as any)
      ElMessage.success('Incident updated')
    } else {
      await api.healthSafety.createIncident(incidentForm as any)
      ElMessage.success('Incident created')
    }
    showIncidentDialog.value = false
    loadIncidents()
  } catch { ElMessage.error('Failed to save incident') }
}

// Delete operations
const deleteF10 = async (id: string) => {
  await ElMessageBox.confirm('Delete this F10 notification?', 'Confirm')
  await api.healthSafety.deleteF10(id)
  ElMessage.success('Deleted')
  loadF10()
}

const deleteCPP = async (id: string) => {
  await ElMessageBox.confirm('Delete this CPP?', 'Confirm')
  await api.healthSafety.deleteCPP(id)
  ElMessage.success('Deleted')
  loadCPPs()
}

const deleteRAMS = async (id: string) => {
  await ElMessageBox.confirm('Delete this RAMS?', 'Confirm')
  await api.healthSafety.deleteRAMS(id)
  ElMessage.success('Deleted')
  loadRAMS()
}

const deletePermit = async (id: string) => {
  await ElMessageBox.confirm('Delete this permit?', 'Confirm')
  await api.healthSafety.deletePermit(id)
  ElMessage.success('Deleted')
  loadPermits()
}

const deleteIncident = async (id: string) => {
  await ElMessageBox.confirm('Delete this incident?', 'Confirm')
  await api.healthSafety.deleteIncident(id)
  ElMessage.success('Deleted')
  loadIncidents()
}

// Update permit status
const updatePermitStatus = async (id: string, status: string) => {
  try {
    await api.healthSafety.updatePermitStatus(id, status)
    ElMessage.success('Status updated')
    loadPermits()
  } catch { ElMessage.error('Failed to update status') }
}

// Helpers
type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getPermitStatusType = (status?: string): TagType => {
  const map: Record<string, TagType> = { draft: 'info', prechecked: 'warning', issued: 'success', completed: 'success' }
  return map[status || ''] ?? 'info'
}

const getIncidentSeverityType = (severity?: string): TagType => {
  const map: Record<string, TagType> = { near_miss: 'info', minor: 'warning', major: 'danger', fatal: 'danger' }
  return map[severity || ''] ?? 'info'
}

const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'

const getSiteName = (siteId?: string) => sites.value.find(s => s.id === siteId)?.name || '—'
const getContractRef = (contractId?: string) => contracts.value.find(c => c.id === contractId)?.reference || '—'
</script>

<template>
  <div class="healthsafety-view">
    <PageHeader title="Health & Safety" :breadcrumbs="[{ title: 'Health & Safety' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openIncidentCreate">Report Incident</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- F10 Notifications -->
        <el-tab-pane label="F10 Notifications" name="f10">
          <div class="mb-4">
            <el-button type="primary" :icon="Plus" @click="openF10Create">New F10</el-button>
          </div>
          <el-table v-loading="loading" :data="f10Records" stripe>
            <el-table-column prop="title" label="Title" min-width="200" />
            <el-table-column label="Site" min-width="150">
              <template #default="{ row }">{{ getSiteName(row.siteId) }}</template>
            </el-table-column>
            <el-table-column prop="data.reference" label="Reference" width="120" />
            <el-table-column label="Status" width="120">
              <template #default="{ row }"><StatusBadge :status="row.status" /></template>
            </el-table-column>
            <el-table-column label="Created" width="120">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="Actions" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" :icon="Edit" @click="openF10Edit(row)">Edit</el-button>
                <el-button link type="danger" size="small" :icon="Delete" @click="deleteF10(row.id)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- CPP -->
        <el-tab-pane label="Construction Phase Plans" name="cpp">
          <div class="mb-4">
            <el-button type="primary" :icon="Plus" @click="openCPPCreate">New CPP</el-button>
          </div>
          <el-table v-loading="loading" :data="cppRecords" stripe>
            <el-table-column prop="title" label="Title" min-width="200" />
            <el-table-column label="Site" min-width="150">
              <template #default="{ row }">{{ getSiteName(row.siteId) }}</template>
            </el-table-column>
            <el-table-column prop="data.version" label="Version" width="100" />
            <el-table-column label="Status" width="120">
              <template #default="{ row }"><StatusBadge :status="row.status" /></template>
            </el-table-column>
            <el-table-column label="Actions" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" :icon="Edit" @click="openCPPEdit(row)">Edit</el-button>
                <el-button link type="danger" size="small" :icon="Delete" @click="deleteCPP(row.id)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- RAMS -->
        <el-tab-pane label="RAMS" name="rams">
          <div class="mb-4">
            <el-button type="primary" :icon="Plus" @click="openRAMSCreate">New RAMS</el-button>
          </div>
          <el-table v-loading="loading" :data="ramsRecords" stripe>
            <el-table-column prop="title" label="Title" min-width="200" />
            <el-table-column label="Contract" min-width="150">
              <template #default="{ row }">{{ getContractRef(row.contractId) }}</template>
            </el-table-column>
            <el-table-column label="Sign-on Required" width="140">
              <template #default="{ row }">
                <el-tag :type="row.data?.signOnRequired ? 'warning' : 'success'" size="small">
                  {{ row.data?.signOnRequired ? 'Yes' : 'No' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Status" width="120">
              <template #default="{ row }"><StatusBadge :status="row.status" /></template>
            </el-table-column>
            <el-table-column label="Actions" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" :icon="Edit" @click="openRAMSEdit(row)">Edit</el-button>
                <el-button link type="danger" size="small" :icon="Delete" @click="deleteRAMS(row.id)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Permits to Dig -->
        <el-tab-pane label="Permits to Dig" name="permits">
          <div class="mb-4">
            <el-button type="primary" :icon="Plus" @click="openPermitCreate">New Permit</el-button>
          </div>
          <el-table v-loading="loading" :data="permits" stripe>
            <el-table-column prop="title" label="Title" min-width="200" />
            <el-table-column label="Site" min-width="150">
              <template #default="{ row }">{{ getSiteName(row.siteId) }}</template>
            </el-table-column>
            <el-table-column label="Location" min-width="150">
              <template #default="{ row }">{{ row.data?.location || '—' }}</template>
            </el-table-column>
            <el-table-column label="Status" width="140">
              <template #default="{ row }">
                <el-dropdown @command="(s: string) => updatePermitStatus(row.id, s)">
                  <el-tag :type="getPermitStatusType(row.data?.status)" size="small" class="cursor-pointer">
                    {{ row.data?.status || 'draft' }} <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-tag>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="draft">Draft</el-dropdown-item>
                      <el-dropdown-item command="prechecked">Prechecked</el-dropdown-item>
                      <el-dropdown-item command="issued">Issued</el-dropdown-item>
                      <el-dropdown-item command="completed">Completed</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
            <el-table-column label="Period" width="200">
              <template #default="{ row }">
                {{ formatDate(row.data?.startDate) }} - {{ formatDate(row.data?.endDate) }}
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" :icon="Edit" @click="openPermitEdit(row)">Edit</el-button>
                <el-button link type="danger" size="small" :icon="Delete" @click="deletePermit(row.id)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Incidents -->
        <el-tab-pane label="Incidents" name="incidents">
          <div class="mb-4">
            <el-button type="primary" :icon="Plus" @click="openIncidentCreate">Report Incident</el-button>
          </div>
          <el-table v-loading="loading" :data="incidents" stripe>
            <el-table-column prop="title" label="Title" min-width="200" />
            <el-table-column label="Site" min-width="150">
              <template #default="{ row }">{{ getSiteName(row.siteId) }}</template>
            </el-table-column>
            <el-table-column label="Severity" width="120">
              <template #default="{ row }">
                <el-tag :type="getIncidentSeverityType(row.data?.severity)" size="small">
                  {{ row.data?.severity?.replace('_', ' ').toUpperCase() || '—' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Date" width="120">
              <template #default="{ row }">{{ formatDate(row.data?.date) }}</template>
            </el-table-column>
            <el-table-column label="Status" width="120">
              <template #default="{ row }"><StatusBadge :status="row.status" /></template>
            </el-table-column>
            <el-table-column label="Actions" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" :icon="Edit" @click="openIncidentEdit(row)">Edit</el-button>
                <el-button link type="danger" size="small" :icon="Delete" @click="deleteIncident(row.id)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- F10 Dialog -->
    <el-dialog v-model="showF10Dialog" :title="editingId ? 'Edit F10 Notification' : 'New F10 Notification'" width="600px">
      <el-form ref="f10FormRef" :model="f10Form" :rules="rules" label-width="140px">
        <el-form-item label="Title" prop="title">
          <el-input v-model="f10Form.title" />
        </el-form-item>
        <el-form-item label="Site" prop="siteId">
          <el-select v-model="f10Form.siteId" placeholder="Select site" class="w-full">
            <el-option v-for="s in sites" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Reference" prop="data.reference">
          <el-input v-model="f10Form.data.reference" />
        </el-form-item>
        <el-form-item label="Submitted Date" prop="data.submittedDate">
          <el-date-picker v-model="f10Form.data.submittedDate" type="date" class="w-full" />
        </el-form-item>
        <el-form-item label="Description" prop="data.description">
          <el-input v-model="f10Form.data.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-select v-model="f10Form.status" class="w-full">
            <el-option label="Draft" value="draft" />
            <el-option label="Submitted" value="submitted" />
            <el-option label="Approved" value="approved" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showF10Dialog = false">Cancel</el-button>
        <el-button type="primary" @click="saveF10">Save</el-button>
      </template>
    </el-dialog>

    <!-- CPP Dialog -->
    <el-dialog v-model="showCPPDialog" :title="editingId ? 'Edit CPP' : 'New Construction Phase Plan'" width="600px">
      <el-form ref="cppFormRef" :model="cppForm" :rules="rules" label-width="140px">
        <el-form-item label="Title" prop="title">
          <el-input v-model="cppForm.title" />
        </el-form-item>
        <el-form-item label="Site" prop="siteId">
          <el-select v-model="cppForm.siteId" placeholder="Select site" class="w-full">
            <el-option v-for="s in sites" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Version" prop="data.version">
          <el-input v-model="cppForm.data.version" />
        </el-form-item>
        <el-form-item label="Review Date" prop="data.reviewDate">
          <el-date-picker v-model="cppForm.data.reviewDate" type="date" class="w-full" />
        </el-form-item>
        <el-form-item label="Description" prop="data.description">
          <el-input v-model="cppForm.data.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-select v-model="cppForm.status" class="w-full">
            <el-option label="Draft" value="draft" />
            <el-option label="In Review" value="in_review" />
            <el-option label="Approved" value="approved" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCPPDialog = false">Cancel</el-button>
        <el-button type="primary" @click="saveCPP">Save</el-button>
      </template>
    </el-dialog>

    <!-- RAMS Dialog -->
    <el-dialog v-model="showRAMSDialog" :title="editingId ? 'Edit RAMS' : 'New RAMS Template'" width="700px">
      <el-form ref="ramsFormRef" :model="ramsForm" :rules="rules" label-width="140px">
        <el-form-item label="Title" prop="title">
          <el-input v-model="ramsForm.title" />
        </el-form-item>
        <el-form-item label="Contract" prop="contractId">
          <el-select v-model="ramsForm.contractId" placeholder="Select contract" class="w-full">
            <el-option v-for="c in contracts" :key="c.id" :label="`${c.reference} - ${c.title}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Method Statement" prop="data.methodStatement">
          <el-input v-model="ramsForm.data.methodStatement" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item label="PPE Required">
          <el-select v-model="ramsForm.data.ppeRequired" multiple placeholder="Select PPE" class="w-full">
            <el-option label="Hard Hat" value="hard_hat" />
            <el-option label="Safety Boots" value="safety_boots" />
            <el-option label="High Vis Vest" value="high_vis" />
            <el-option label="Gloves" value="gloves" />
            <el-option label="Safety Glasses" value="safety_glasses" />
            <el-option label="Face Mask" value="face_mask" />
          </el-select>
        </el-form-item>
        <el-form-item label="Risk Assessments">
          <el-select v-model="ramsForm.data.riskAssessments" multiple placeholder="Select risk assessments" class="w-full">
            <el-option label="Working at Height" value="working_at_height" />
            <el-option label="Excavation" value="excavation" />
            <el-option label="Confined Spaces" value="confined_spaces" />
            <el-option label="Electrical" value="electrical" />
            <el-option label="Mechanical" value="mechanical" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="ramsForm.data.signOnRequired">Requires Sign-on</el-checkbox>
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-select v-model="ramsForm.status" class="w-full">
            <el-option label="Draft" value="draft" />
            <el-option label="In Review" value="in_review" />
            <el-option label="Approved" value="approved" />
            <el-option label="Active" value="active" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRAMSDialog = false">Cancel</el-button>
        <el-button type="primary" @click="saveRAMS">Save</el-button>
      </template>
    </el-dialog>

    <!-- Permit Dialog -->
    <el-dialog v-model="showPermitDialog" :title="editingId ? 'Edit Permit to Dig' : 'New Permit to Dig'" width="700px">
      <el-form ref="permitFormRef" :model="permitForm" :rules="rules" label-width="140px">
        <el-form-item label="Title" prop="title">
          <el-input v-model="permitForm.title" />
        </el-form-item>
        <el-form-item label="Site" prop="siteId">
          <el-select v-model="permitForm.siteId" placeholder="Select site" class="w-full">
            <el-option v-for="s in sites" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Location" prop="data.location">
          <el-input v-model="permitForm.data.location" placeholder="Exact location on site" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Start Date" prop="data.startDate">
              <el-date-picker v-model="permitForm.data.startDate" type="date" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="End Date" prop="data.endDate">
              <el-date-picker v-model="permitForm.data.endDate" type="date" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Excavations">
          <el-select v-model="permitForm.data.excavations" multiple placeholder="Select types" class="w-full">
            <el-option label="Trench" value="trench" />
            <el-option label="Pit" value="pit" />
            <el-option label="Basement" value="basement" />
            <el-option label="Piling" value="piling" />
          </el-select>
        </el-form-item>
        <el-form-item label="Services">
          <el-select v-model="permitForm.data.services" multiple placeholder="Select services" class="w-full">
            <el-option label="Electric" value="electric" />
            <el-option label="Gas" value="gas" />
            <el-option label="Water" value="water" />
            <el-option label="Sewerage" value="sewerage" />
            <el-option label="Telecom" value="telecom" />
          </el-select>
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-select v-model="permitForm.status" class="w-full">
            <el-option label="Draft" value="draft" />
            <el-option label="Prechecked" value="prechecked" />
            <el-option label="Issued" value="issued" />
            <el-option label="Completed" value="completed" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPermitDialog = false">Cancel</el-button>
        <el-button type="primary" @click="savePermit">Save</el-button>
      </template>
    </el-dialog>

    <!-- Incident Dialog -->
    <el-dialog v-model="showIncidentDialog" :title="editingId ? 'Edit Incident' : 'Report Incident'" width="700px">
      <el-form ref="incidentFormRef" :model="incidentForm" :rules="rules" label-width="140px">
        <el-form-item label="Title" prop="title">
          <el-input v-model="incidentForm.title" />
        </el-form-item>
        <el-form-item label="Site" prop="siteId">
          <el-select v-model="incidentForm.siteId" placeholder="Select site" class="w-full">
            <el-option v-for="s in sites" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Date" prop="data.date">
              <el-date-picker v-model="incidentForm.data.date" type="date" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Severity" prop="data.severity">
              <el-select v-model="incidentForm.data.severity" class="w-full">
                <el-option label="Near Miss" value="near_miss" />
                <el-option label="Minor" value="minor" />
                <el-option label="Major" value="major" />
                <el-option label="Fatal" value="fatal" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Location" prop="data.location">
          <el-input v-model="incidentForm.data.location" />
        </el-form-item>
        <el-form-item label="Description" prop="data.description">
          <el-input v-model="incidentForm.data.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="Persons Involved">
          <el-input :model-value="incidentForm.data.personsInvolved.join(', ')" placeholder="Names (comma separated)" @input="(v: string) => incidentForm.data.personsInvolved = v.split(',').map((s: string) => s.trim()).filter(Boolean)" />
        </el-form-item>
        <el-form-item label="Witnesses">
          <el-input :model-value="incidentForm.data.witnesses.join(', ')" placeholder="Names (comma separated)" @input="(v: string) => incidentForm.data.witnesses = v.split(',').map((s: string) => s.trim()).filter(Boolean)" />
        </el-form-item>
        <el-form-item label="Action Taken">
          <el-input v-model="incidentForm.data.actionTaken" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="Root Cause">
          <el-input v-model="incidentForm.data.rootCause" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="Corrective Action">
          <el-input v-model="incidentForm.data.correctiveAction" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-select v-model="incidentForm.status" class="w-full">
            <el-option label="Draft" value="draft" />
            <el-option label="Under Investigation" value="investigating" />
            <el-option label="Resolved" value="resolved" />
            <el-option label="Closed" value="closed" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showIncidentDialog = false">Cancel</el-button>
        <el-button type="primary" @click="saveIncident">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.healthsafety-view {
  .mb-4 { margin-bottom: 16px; }
  .w-full { width: 100%; }
  .cursor-pointer { cursor: pointer; }
}
</style>
