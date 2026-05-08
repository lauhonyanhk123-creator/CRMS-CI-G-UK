<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, Check, Close, Money } from '@element-plus/icons-vue'
import api, { type ApplicationResponse, type Contract } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import dayjs from 'dayjs'
import { exportCsv } from '@/utils/exportCsv'

const loading = ref(false)
const activeTab = ref('submitted')

// Tab data
const tabs = [
  { key: 'submitted', label: 'Submitted', status: 'SUBMITTED' },
  { key: 'measured', label: 'Measured', status: 'MEASURED' },
  { key: 'agreed', label: 'Agreed', status: 'MEASURED' },
  { key: 'approved', label: 'Approved', status: 'APPROVED' },
  { key: 'paid', label: 'Paid', status: 'PAID' },
  { key: 'rejected', label: 'Rejected', status: 'REJECTED' }
]

const tableData = ref<ApplicationResponse[]>([])
const contracts = ref<Contract[]>([])
const total = ref(0)

// Dialog state
const showDialog = ref(false)
const dialogLoading = ref(false)
const dialogMode = ref<'create' | 'view'>('create')
const currentApp = ref<ApplicationResponse | null>(null)

// Form state
const form = reactive({
  contractId: '',
  periodStart: '',
  periodEnd: '',
  description: '',
  valueOfWorks: 0,
  items: [] as Array<{
    description: string
    quantity: number
    unit: string
    rate: number
    amount: number
  }>
})

const statusOptions = [
  { label: 'Submitted', value: 'SUBMITTED' },
  { label: 'Measured', value: 'MEASURED' },
  { label: 'Agreed', value: 'AGREED' },
  { label: 'Approved', value: 'APPROVED' },
  { label: 'Paid', value: 'PAID' },
  { label: 'Rejected', value: 'REJECTED' }
]

onMounted(() => {
  loadContracts()
  loadData()
})

const loadContracts = async () => {
  try {
    const response = await api.contracts.getAll({ limit: 100 })
    contracts.value = response.data?.data || []
  } catch (error) {
    console.error('Failed to load contracts:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    // Map tab to status filter
    const statusMap: Record<string, string> = {
      submitted: 'SUBMITTED',
      measured: 'MEASURED',
      agreed: 'AGREED',
      approved: 'APPROVED',
      paid: 'PAID',
      rejected: 'REJECTED'
    }
    
    const status = statusMap[activeTab.value] || undefined
    const response = await api.applicationsForPayment.getAll({ status })
    
    tableData.value = response.data?.data || response.data || []
    total.value = response.data?.total || tableData.value.length
  } catch (error) {
    console.error('Failed to load applications:', error)
    ElMessage.error('Failed to load applications')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  loadData()
}

const formatCurrency = (value?: number) => {
  if (value == null) return '—'
  return `£${value.toLocaleString('en-GB', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const formatDate = (date?: string) => {
  if (!date) return '—'
  return dayjs(date).format('DD/MM/YYYY')
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getStatusType = (status?: string): TagType => {
  const map: Record<string, TagType> = {
    SUBMITTED: 'warning',
    MEASURED: 'info',
    AGREED: 'primary',
    APPROVED: 'success',
    PAID: 'success',
    REJECTED: 'danger'
  }
  return map[status || ''] ?? 'info'
}

const viewApplication = async (row: ApplicationResponse) => {
  try {
    const response = await api.applicationsForPayment.getById(String(row.id))
    currentApp.value = response.data
    dialogMode.value = 'view'
    showDialog.value = true
  } catch (error) {
    console.error('Failed to load application:', error)
    ElMessage.error('Failed to load application details')
  }
}

const openNewDialog = () => {
  dialogMode.value = 'create'
  resetForm()
  showDialog.value = true
}

const resetForm = () => {
  form.contractId = ''
  form.periodStart = ''
  form.periodEnd = ''
  form.description = ''
  form.valueOfWorks = 0
  form.items = []
}

const addLineItem = () => {
  form.items.push({
    description: '',
    quantity: 1,
    unit: 'item',
    rate: 0,
    amount: 0
  })
}

const removeLineItem = (index: number) => {
  form.items.splice(index, 1)
  calculateTotal()
}

const calculateLineAmount = (item: any) => {
  item.amount = item.quantity * item.rate
  calculateTotal()
}

const calculateTotal = () => {
  form.valueOfWorks = form.items.reduce((sum, item) => sum + (item.amount || 0), 0)
}

const submitApplication = async () => {
  if (!form.contractId) {
    ElMessage.warning('Please select a contract')
    return
  }
  if (!form.periodStart || !form.periodEnd) {
    ElMessage.warning('Please enter period start and end dates')
    return
  }
  if (form.valueOfWorks <= 0) {
    ElMessage.warning('Please enter at least one line item with a value')
    return
  }

  dialogLoading.value = true
  try {
    await api.applicationsForPayment.create(form.contractId, {
      applicationPeriodStart: form.periodStart,
      applicationPeriodEnd: form.periodEnd,
      valueOfWorks: form.valueOfWorks,
      description: form.description
    })
    ElMessage.success('Application created successfully')
    showDialog.value = false
    loadData()
  } catch (error) {
    console.error('Failed to create application:', error)
    ElMessage.error('Failed to create application')
  } finally {
    dialogLoading.value = false
  }
}

const submitForPayment = async (id: string) => {
  try {
    await ElMessageBox.confirm('Submit this application for payment?', 'Submit Application', {
      confirmButtonText: 'Submit',
      cancelButtonText: 'Cancel',
      type: 'warning'
    })
    await api.applicationsForPayment.submit(id)
    ElMessage.success('Application submitted successfully')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to submit application')
    }
  }
}

const approveApplication = async (id: string) => {
  try {
    await ElMessageBox.confirm('Approve this application?', 'Approve Application', {
      confirmButtonText: 'Approve',
      cancelButtonText: 'Cancel',
      type: 'success'
    })
    await api.applicationsForPayment.approve(id)
    ElMessage.success('Application approved successfully')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to approve application')
    }
  }
}

const rejectApplication = async (id: string) => {
  try {
    await ElMessageBox.confirm('Reject this application?', 'Reject Application', {
      confirmButtonText: 'Reject',
      cancelButtonText: 'Cancel',
      type: 'error'
    })
    await api.applicationsForPayment.reject(id)
    ElMessage.success('Application rejected')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to reject application')
    }
  }
}

const markAsPaid = async (id: string) => {
  try {
    await ElMessageBox.confirm('Mark this application as paid?', 'Mark as Paid', {
      confirmButtonText: 'Mark Paid',
      cancelButtonText: 'Cancel',
      type: 'success'
    })
    await api.applicationsForPayment.markPaid(id)
    ElMessage.success('Application marked as paid')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to mark as paid')
    }
  }
}

// Import axios for the mark-paid endpoint
// import apiClient from '@/services/api'; import type { ElTagType } from '@/services/api'

const handleExport = () => {
  const rows = tableData.value.map(a => ({
    applicationRef: a.applicationRef ?? `APP-${a.id}`,
    contractRef: a.contractRef ?? `Contract #${a.contractId}`,
    status: a.status ?? '',
    grossValue: a.grossValue ?? '',
    retention: a.retention ?? '',
    valueOfWorks: a.valueOfWorks ?? '',
    submittedDate: a.submittedDate ?? ''
  }))
  exportCsv('applications.csv', rows, [
    { label: 'Application No', key: 'applicationRef' },
    { label: 'Contract', key: 'contractRef' },
    { label: 'Status', key: 'status' },
    { label: 'Gross Amount', key: 'grossValue' },
    { label: 'Retention', key: 'retention' },
    { label: 'Net Amount', key: 'valueOfWorks' },
    { label: 'Submitted Date', key: 'submittedDate' }
  ])
}

const getRowActions = (row: ApplicationResponse) => {
  const actions: Array<{ label: string; action: Function; type?: string; icon?: any }> = [
    { label: 'View Details', action: () => viewApplication(row), type: 'primary' }
  ]
  
  // Only show workflow actions for specific statuses
  if (row.status === 'SUBMITTED') {
    actions.push({ 
      label: 'Submit', 
      action: () => submitForPayment(String(row.id)), 
      type: 'warning',
      icon: Money 
    })
  }
  
  if (row.status === 'AGREED') {
    actions.push({ 
      label: 'Approve', 
      action: () => approveApplication(String(row.id)), 
      type: 'success',
      icon: Check 
    })
    actions.push({ 
      label: 'Reject', 
      action: () => rejectApplication(String(row.id)), 
      type: 'danger',
      icon: Close 
    })
  }
  
  if (row.status === 'APPROVED') {
    actions.push({ 
      label: 'Mark Paid', 
      action: () => markAsPaid(String(row.id)), 
      type: 'success',
      icon: Money 
    })
  }
  
  return actions
}

</script>

<template>
  <div class="applications-view">
    <PageHeader 
      title="Applications for Payment" 
      :breadcrumbs="[{ title: 'Applications for Payment' }]"
    >
      <template #actions>
        <el-button size="small" @click="handleExport">Export CSV</el-button>
        <el-button type="primary" :icon="Plus" @click="openNewDialog">
          New Application
        </el-button>
      </template>
    </PageHeader>

    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane 
          v-for="tab in tabs" 
          :key="tab.key" 
          :label="tab.label" 
          :name="tab.key"
        >
          <el-table 
            v-loading="loading" 
            :data="tableData" 
            stripe 
            style="width: 100%"
          >
            <el-table-column prop="applicationRef" label="App No" width="140">
              <template #default="{ row }">
                <el-link type="primary" @click="viewApplication(row)">
                  {{ row.applicationRef || `APP-${row.id}` }}
                </el-link>
              </template>
            </el-table-column>
            
            <el-table-column label="Contract" min-width="150">
              <template #default="{ row }">
                {{ row.contractRef || `Contract #${row.contractId}` }}
              </template>
            </el-table-column>
            
            <el-table-column label="Period Start/End" width="200">
              <template #default="{ row }">
                {{ formatDate(row.applicationPeriodStart) }} - 
                {{ formatDate(row.applicationPeriodEnd) }}
              </template>
            </el-table-column>
            
            <el-table-column label="Gross Value" width="130" align="right">
              <template #default="{ row }">
                {{ formatCurrency(row.grossValue) }}
              </template>
            </el-table-column>
            
            <el-table-column label="Retention" width="110" align="right">
              <template #default="{ row }">
                {{ formatCurrency(row.retention) }}
              </template>
            </el-table-column>
            
            <el-table-column label="Net Value" width="130" align="right">
              <template #default="{ row }">
                <strong>{{ formatCurrency(row.valueOfWorks) }}</strong>
              </template>
            </el-table-column>
            
            <el-table-column label="Submitted Date" width="120">
              <template #default="{ row }">
                {{ formatDate(row.submittedDate) }}
              </template>
            </el-table-column>
            
            <el-table-column label="Status" width="110">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ row.status || 'DRAFT' }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="Actions" width="200" fixed="right">
              <template #default="{ row }">
                <template v-for="(action, idx) in getRowActions(row)" :key="idx">
                  <el-button 
                    :type="action.type as any" 
                    size="small" 
                    link
                    @click="action.action()"
                  >
                    {{ action.label }}
                  </el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
          
          <div v-if="tableData.length === 0 && !loading" class="empty-state">
            <el-empty description="No applications found" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- New Application Dialog -->
    <el-dialog
      v-model="showDialog"
      :title="dialogMode === 'create' ? 'New Application for Payment' : 'Application Details'"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="dialogMode === 'create'">
        <el-form :model="form" label-width="140px">
          <el-form-item label="Contract" required>
            <el-select 
              v-model="form.contractId" 
              placeholder="Select contract"
              filterable
              style="width: 100%"
            >
              <el-option 
                v-for="c in contracts" 
                :key="c.id" 
                :label="`${c.reference} - ${c.title}`" 
                :value="c.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="Period Start" required>
            <el-date-picker
              v-model="form.periodStart"
              type="date"
              placeholder="Select date"
              format="DD/MM/YYYY"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="Period End" required>
            <el-date-picker
              v-model="form.periodEnd"
              type="date"
              placeholder="Select date"
              format="DD/MM/YYYY"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="Description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="2"
              placeholder="Brief description of works..."
            />
          </el-form-item>
          
          <el-divider content-position="left">Line Items</el-divider>
          
          <div class="line-items">
            <div class="line-item-header">
              <span class="col-desc">Description</span>
              <span class="col-qty">Qty</span>
              <span class="col-unit">Unit</span>
              <span class="col-rate">Rate</span>
              <span class="col-amount">Amount</span>
              <span class="col-action"></span>
            </div>
            
            <div 
              v-for="(item, index) in form.items" 
              :key="index" 
              class="line-item-row"
            >
              <el-input 
                v-model="item.description" 
                placeholder="Description"
                class="col-desc"
              />
              <el-input-number 
                v-model="item.quantity" 
                :min="0" 
                :precision="2"
                class="col-qty"
                @change="calculateLineAmount(item)"
              />
              <el-input v-model="item.unit" placeholder="Unit" class="col-unit" />
              <el-input-number 
                v-model="item.rate" 
                :min="0" 
                :precision="2"
                :controls="false"
                class="col-rate"
                @change="calculateLineAmount(item)"
              />
              <span class="col-amount">{{ formatCurrency(item.amount) }}</span>
              <el-button 
                type="danger" 
                :icon="Close" 
                circle 
                size="small"
                @click="removeLineItem(index)"
              />
            </div>
            
            <el-button type="primary" link @click="addLineItem">
              <Plus /> Add Line Item
            </el-button>
          </div>
          
          <div class="total-section">
            <div class="total-row">
              <span>Total Value of Works:</span>
              <strong>{{ formatCurrency(form.valueOfWorks) }}</strong>
            </div>
          </div>
        </el-form>
      </div>
      
      <div v-else-if="currentApp" class="application-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="Application Ref">
            {{ currentApp.applicationRef }}
          </el-descriptions-item>
          <el-descriptions-item label="Contract">
            {{ currentApp.contractRef }}
          </el-descriptions-item>
          <el-descriptions-item label="Application #">
            {{ currentApp.applicationNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="Status">
            <el-tag :type="getStatusType(currentApp.status)" size="small">
              {{ currentApp.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Period Start">
            {{ formatDate(currentApp.applicationPeriodStart) }}
          </el-descriptions-item>
          <el-descriptions-item label="Period End">
            {{ formatDate(currentApp.applicationPeriodEnd) }}
          </el-descriptions-item>
          <el-descriptions-item label="Due Date">
            {{ formatDate(currentApp.dueDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="Submitted Date">
            {{ formatDate(currentApp.submittedDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="Value of Works">
            {{ formatCurrency(currentApp.valueOfWorks) }}
          </el-descriptions-item>
          <el-descriptions-item label="Retention">
            {{ formatCurrency(currentApp.retention) }}
          </el-descriptions-item>
          <el-descriptions-item label="Gross Value" :span="2">
            <strong>{{ formatCurrency(currentApp.grossValue) }}</strong>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentApp.payerRef" label="Payer Ref" :span="2">
            {{ currentApp.payerRef }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <div v-if="dialogMode === 'create'">
          <el-button @click="showDialog = false">Cancel</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="submitApplication">
            Create Application
          </el-button>
        </div>
        <div v-else>
          <el-button @click="showDialog = false">Close</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.applications-view {
  .empty-state {
    padding: 40px 0;
  }
}

.line-items {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
  
  .line-item-header {
    display: flex;
    gap: 8px;
    padding: 8px 0;
    font-weight: 600;
    font-size: 12px;
    color: #606266;
    
    .col-desc { flex: 2; }
    .col-qty { width: 80px; text-align: center; }
    .col-unit { width: 80px; text-align: center; }
    .col-rate { width: 100px; text-align: right; }
    .col-amount { width: 100px; text-align: right; }
    .col-action { width: 40px; }
  }
  
  .line-item-row {
    display: flex;
    gap: 8px;
    padding: 8px 0;
    align-items: center;
    
    .col-desc { flex: 2; }
    .col-qty { width: 80px; }
    .col-unit { width: 80px; }
    .col-rate { width: 100px; }
    .col-amount { width: 100px; text-align: right; font-weight: 600; }
    .col-action { width: 40px; }
  }
}

.total-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #dcdfe6;
  
  .total-row {
    display: flex;
    justify-content: space-between;
    font-size: 16px;
    
    strong {
      font-size: 18px;
      color: #409eff;
    }
  }
}

.application-details {
  padding: 8px 0;
}
</style>
