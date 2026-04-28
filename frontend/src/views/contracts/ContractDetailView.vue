<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api, { apiClient, type Contract, type Variation, type Application } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const route = useRoute()
const contractId = computed(() => route.params.id as string)

const loading = ref(false)
const contract = ref<Contract | null>(null)
const applications = ref<Application[]>([])
const variations = ref<Variation[]>([])

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const editingId = ref('')
const form = ref({ description: '', value: 0, status: 'pending' as Variation['status'] })

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
  } catch { ElMessage.error('Failed to load contract') } finally { loading.value = false }
}

const formatCurrency = (value: number) => `£${value.toLocaleString()}`
const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'

const submitApplication = async (app: Application) => {
  try {
    await api.applications.submit(contractId.value, app.id)
    ElMessage.success('Application submitted')
    loadData()
  } catch { ElMessage.error('Failed to submit application') }
}

const getApplicationStatusType = (status: string) => {
  const map: Record<string, string> = { draft: 'info', submitted: 'warning', measured: 'primary', agreed: 'success', paid: 'success' }
  return map[status] || 'info'
}

const addVariation = () => { editingId.value = ''; form.value = { description: '', value: 0, status: 'pending' }; drawerVisible.value = true }
const editVariation = (v: Variation) => { editingId.value = v.id; form.value = { description: v.description, value: v.value, status: v.status }; drawerVisible.value = true }

const saveVariation = async () => {
  drawerLoading.value = true
  try {
    if (editingId.value) {
      await api.contracts.update(contractId.value, { variations: [...variations.value.map(v => v.id === editingId.value ? { ...v, ...form.value } : v)] } as any)
    }
    ElMessage.success('Variation saved')
    drawerVisible.value = false
    loadData()
  } catch { ElMessage.error('Failed to save variation') } finally { drawerLoading.value = false }
}

const retentionBalance = computed(() => {
  const held = applications.value.reduce((sum, a) => sum + a.retentionAmount, 0)
  const released = 0 // Would come from retention ledger
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
          <template #header><div class="card-header"><span>Variations ({{ variations.length }})</span><el-button type="primary" size="small" @click="addVariation">Add Variation</el-button></div></template>
          <el-table :data="variations" stripe>
            <el-table-column prop="reference" label="Ref" width="100" />
            <el-table-column prop="description" label="Description" min-width="200" />
            <el-table-column label="Value" width="140"><template #default="{ row }">{{ formatCurrency(row.value) }}</template></el-table-column>
            <el-table-column label="Status" width="120"><template #default="{ row }"><el-tag :type="row.status === 'approved' ? 'success' : row.status === 'rejected' ? 'danger' : 'warning'" size="small">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="Actions" width="100"><template #default="{ row }"><el-button link type="primary" size="small" @click="editVariation(row)">Edit</el-button></template></el-table-column>
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
          <el-descriptions :column="3" border>
            <el-descriptions-item label="Total Held">{{ formatCurrency(retentionBalance.held) }}</el-descriptions-item>
            <el-descriptions-item label="Total Released">{{ formatCurrency(retentionBalance.released) }}</el-descriptions-item>
            <el-descriptions-item label="Balance">{{ formatCurrency(retentionBalance.balance) }}</el-descriptions-item>
          </el-descriptions>
          <el-table :data="applications" stripe class="mt-4">
            <el-table-column label="Application" width="100"><template #default="{ row }">#{{ row.applicationNumber }}</template></el-table-column>
            <el-table-column label="Retention Held" width="150"><template #default="{ row }">{{ formatCurrency(row.retentionAmount) }}</template></el-table-column>
            <el-table-column label="Retention Released" width="150">{{ formatCurrency(0) }}</el-table-column>
            <el-table-column label="Running Balance" width="150"><template #default="{ row, $index }">{{ formatCurrency(applications.slice(0, $index + 1).reduce((s, a) => s + a.retentionAmount, 0)) }}</template></el-table-column>
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

      <el-tab-pane label="Documents">Documents tab content</el-tab-pane>
      <el-tab-pane label="Adoption Cases">Adoption cases tab content</el-tab-pane>
    </el-tabs>

    <el-drawer v-model="drawerVisible" title="Variation" size="500px">
      <el-form :model="form" label-position="top">
        <el-form-item label="Description"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="Value"><el-input-number v-model="form.value" :min="0" :step="100" style="width:100%" /></el-form-item>
        <el-form-item label="Status"><el-select v-model="form.status"><el-option label="Pending" value="pending" /><el-option label="Approved" value="approved" /><el-option label="Rejected" value="rejected" /></el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="drawerLoading" @click="saveVariation">Save</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.contract-detail-view { .card-header { display: flex; justify-content: space-between; align-items: center; } }
</style>
