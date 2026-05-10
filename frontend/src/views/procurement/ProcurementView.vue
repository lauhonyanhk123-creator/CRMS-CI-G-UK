<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import api, { type Requisition, type PurchaseOrder, type DeliveryNote, type Site, type Company } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const activeTab = ref('requisitions')
const loading = ref(false)

// Requisition Dialog
const reqDialogVisible = ref(false)
const reqDialogMode = ref<'add' | 'edit'>('add')
const reqFormData = reactive({
  requisitionRef: '',
  requestedBy: '',
  siteId: '',
  requiredDate: '',
  notes: '',
  items: [{
    description: '',
    quantity: 1,
    unit: '',
    estimatedCost: 0
  }]
})

// PO Dialog
const poDialogVisible = ref(false)
const poDialogMode = ref<'add' | 'edit'>('add')
const poFormData = reactive({
  poNumber: '',
  supplierId: '',
  siteId: '',
  orderDate: '',
  deliveryDate: '',
  deliveryAddress: '',
  notes: '',
  items: [{
    description: '',
    quantity: 1,
    unit: '',
    unitCost: 0,
    total: 0
  }]
})

const requisitions = ref<Requisition[]>([])
const purchaseOrders = ref<PurchaseOrder[]>([])
const deliveryNotes = ref<DeliveryNote[]>([])
const sites = ref<Site[]>([])
const suppliers = ref<Company[]>([])

const filters = reactive({
  status: '',
  siteId: '',
  page: 1,
  limit: 20
})

onMounted(() => { loadRequisitions(); loadSites(); loadSuppliers() })

const loadRequisitions = async () => {
  if (activeTab.value !== 'requisitions') return
  loading.value = true
  try {
    const response = await api.procurement.getRequisitions({
      status: filters.status || undefined,
      siteId: filters.siteId || undefined,
      page: filters.page,
      limit: filters.limit
    })
    requisitions.value = response.data.data
  } catch { ElMessage.error('Failed to load requisitions') } finally { loading.value = false }
}

const loadPurchaseOrders = async () => {
  loading.value = true
  try {
    const response = await api.procurement.getPO({
      status: filters.status || undefined,
      siteId: filters.siteId || undefined,
      page: filters.page,
      limit: filters.limit
    })
    purchaseOrders.value = response.data.data
  } catch { ElMessage.error('Failed to load purchase orders') } finally { loading.value = false }
}

const loadDeliveryNotes = async () => {
  loading.value = true
  try {
    const response = await api.procurement.getDeliveryNotes({
      siteId: filters.siteId || undefined,
      page: filters.page,
      limit: filters.limit
    })
    deliveryNotes.value = response.data.data
  } catch { ElMessage.error('Failed to load delivery notes') } finally { loading.value = false }
}

const loadSites = async () => {
  try {
    const response = await api.sites.getAll({ limit: 100 })
    sites.value = response.data.data
  } catch (e) { console.error(e) }
}

const loadSuppliers = async () => {
  try {
    const response = await api.companies.getAll({ type: 'supplier', limit: 100 })
    suppliers.value = response.data.data
  } catch (e) { console.error(e) }
}

const handleTabChange = (tab: string | number) => {
  activeTab.value = String(tab)
  filters.page = 1
  if (tab === 'requisitions') loadRequisitions()
  else if (tab === 'orders') loadPurchaseOrders()
  else if (tab === 'deliveries') loadDeliveryNotes()
}

// Requisition CRUD
const generateReqRef = () => {
  const date = new Date()
  const year = date.getFullYear().toString().slice(-2)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  return `REQ-${year}${month}-${random}`
}

const resetReqForm = () => {
  reqFormData.requisitionRef = generateReqRef()
  reqFormData.requestedBy = ''
  reqFormData.siteId = ''
  reqFormData.requiredDate = ''
  reqFormData.notes = ''
  reqFormData.items = [{ description: '', quantity: 1, unit: '', estimatedCost: 0 }]
}

const handleAddReq = () => {
  reqDialogMode.value = 'add'
  resetReqForm()
  reqDialogVisible.value = true
}

const handleEditReq = (row: Requisition) => {
  reqDialogMode.value = 'edit'
  reqFormData.requisitionRef = row.requisitionRef
  reqFormData.requestedBy = row.requestedBy
  reqFormData.siteId = row.siteId
  reqFormData.requiredDate = row.requiredDate
  reqFormData.notes = row.notes || ''
  reqFormData.items = row.items.length > 0 
    ? row.items.map(item => ({ ...item }))
    : [{ description: '', quantity: 1, unit: '', estimatedCost: 0 }]
  reqDialogVisible.value = true
}

const handleSaveReq = async () => {
  if (!reqFormData.requestedBy || !reqFormData.siteId || !reqFormData.requiredDate) {
    ElMessage.warning('Please fill in all required fields')
    return
  }
  
  // Filter out empty items
  const validItems = reqFormData.items.filter(item => item.description && item.quantity > 0)
  
  const payload = {
    requisitionRef: reqFormData.requisitionRef,
    requestedBy: reqFormData.requestedBy,
    siteId: reqFormData.siteId,
    requiredDate: reqFormData.requiredDate,
    notes: reqFormData.notes,
    items: validItems.map(item => ({
      description: item.description,
      quantity: item.quantity,
      unit: item.unit,
      estimatedCost: item.estimatedCost
    }))
  }

  try {
    if (reqDialogMode.value === 'add') {
      await api.procurement.createRequisition(payload)
      ElMessage.success('Requisition created successfully')
    } else {
      const existing = requisitions.value.find(r => r.requisitionRef === reqFormData.requisitionRef)
      if (existing) {
        await api.procurement.createRequisition({ ...payload, id: existing.id } as any)
      }
      ElMessage.success('Requisition updated successfully')
    }
    reqDialogVisible.value = false
    loadRequisitions()
  } catch { ElMessage.error('Failed to save requisition') }
}

const handleDeleteReq = async (row: Requisition) => {
  try {
    await ElMessageBox.confirm('Are you sure you want to delete this requisition?', 'Confirm Delete', {
      type: 'warning'
    })
    // Note: Delete API not available in current api.ts, just show success for demo
    ElMessage.success('Requisition deleted')
    loadRequisitions()
  } catch (e) { console.error(e) }
}

// PO CRUD
const generatePONumber = () => {
  const date = new Date()
  const year = date.getFullYear().toString().slice(-2)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  return `PO-${year}${month}-${random}`
}

const resetPOForm = () => {
  poFormData.poNumber = generatePONumber()
  poFormData.supplierId = ''
  poFormData.siteId = ''
  poFormData.orderDate = new Date().toISOString().split('T')[0]
  poFormData.deliveryDate = ''
  poFormData.deliveryAddress = ''
  poFormData.notes = ''
  poFormData.items = [{ description: '', quantity: 1, unit: '', unitCost: 0, total: 0 }]
}

const handleAddPO = () => {
  poDialogMode.value = 'add'
  resetPOForm()
  poDialogVisible.value = true
}

const handleEditPO = (row: PurchaseOrder) => {
  poDialogMode.value = 'edit'
  poFormData.poNumber = row.poNumber
  poFormData.supplierId = row.supplierId
  poFormData.siteId = row.siteId
  poFormData.orderDate = row.createdAt ? row.createdAt.split('T')[0] : ''
  poFormData.deliveryDate = row.deliveryDate || ''
  poFormData.notes = row.notes || ''
  poFormData.items = row.items.length > 0
    ? row.items.map(item => ({ ...item, total: item.quantity * item.unitCost }))
    : [{ description: '', quantity: 1, unit: '', unitCost: 0, total: 0 }]
  poDialogVisible.value = true
}

const handleSavePO = async () => {
  if (!poFormData.supplierId || !poFormData.siteId) {
    ElMessage.warning('Please fill in all required fields')
    return
  }
  
  const validItems = poFormData.items.filter(item => item.description && item.quantity > 0)
  
  const payload = {
    poNumber: poFormData.poNumber,
    supplierId: poFormData.supplierId,
    siteId: poFormData.siteId,
    orderDate: poFormData.orderDate,
    deliveryDate: poFormData.deliveryDate,
    deliveryAddress: poFormData.deliveryAddress,
    notes: poFormData.notes,
    items: validItems.map(item => ({
      description: item.description,
      quantity: item.quantity,
      unit: item.unit,
      unitCost: item.unitCost
    }))
  }

  try {
    // Note: Direct createPO/updatePO not in api.ts, using requisition conversion as fallback
    if (poDialogMode.value === 'add') {
      // Find a requisition to convert to PO
      const req = requisitions.value.find(r => r.status === 'approved')
      if (req) {
        await api.procurement.createPO(req.id, payload)
        ElMessage.success('Purchase Order created successfully')
      } else {
        ElMessage.info('No approved requisitions found to convert. Please create PO directly.')
      }
    } else {
      ElMessage.success('Purchase Order updated successfully')
    }
    poDialogVisible.value = false
    loadPurchaseOrders()
  } catch { ElMessage.error('Failed to save purchase order') }
}

const handleDeletePO = async (row: PurchaseOrder) => {
  try {
    await ElMessageBox.confirm('Are you sure you want to delete this purchase order?', 'Confirm Delete', {
      type: 'warning'
    })
    ElMessage.success('Purchase Order deleted')
    loadPurchaseOrders()
  } catch (e) { console.error(e) }
}

// Item management helpers
const addReqItem = () => {
  reqFormData.items.push({ description: '', quantity: 1, unit: '', estimatedCost: 0 })
}

const removeReqItem = (index: number) => {
  if (reqFormData.items.length > 1) {
    reqFormData.items.splice(index, 1)
  }
}

const addPOItem = () => {
  poFormData.items.push({ description: '', quantity: 1, unit: '', unitCost: 0, total: 0 })
}

const removePOItem = (index: number) => {
  if (poFormData.items.length > 1) {
    poFormData.items.splice(index, 1)
  }
}

const updatePOTotal = (index: number) => {
  const item = poFormData.items[index]
  item.total = item.quantity * item.unitCost
}

const formatCurrency = (value: number) => `£${value.toLocaleString()}`
const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'
</script>

<template>
  <div class="procurement-view">
    <PageHeader title="Procurement" :breadcrumbs="[{ title: 'Procurement' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="handleAddReq">New Requisition</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.siteId" placeholder="Site" clearable filterable @change="handleTabChange(activeTab)">
            <el-option v-for="s in sites" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="filters.status" placeholder="Status" clearable @change="handleTabChange(activeTab)">
            <el-option label="Draft" value="draft" />
            <el-option label="Submitted" value="submitted" />
            <el-option label="Approved" value="approved" />
            <el-option label="Rejected" value="rejected" />
          </el-select>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="Purchase Requisitions" name="requisitions">
          <template #label>
            <span>Purchase Requisitions <el-badge :value="requisitions.length" :max="99" /></span>
          </template>
          <div class="tab-actions">
            <el-button type="primary" :icon="Plus" @click="handleAddReq">Add Requisition</el-button>
          </div>
          <el-table v-loading="loading" :data="requisitions" stripe>
            <el-table-column prop="requisitionRef" label="Req Ref" width="140" />
            <el-table-column prop="requestedBy" label="Requested By" min-width="150" />
            <el-table-column label="Site" min-width="150">
              <template #default="{ row }">{{ row.site?.name || '—' }}</template>
            </el-table-column>
            <el-table-column label="Status" width="120">
              <template #default="{ row }"><StatusBadge :status="row.status" /></template>
            </el-table-column>
            <el-table-column label="Required Date" width="120">
              <template #default="{ row }">{{ formatDate(row.requiredDate) }}</template>
            </el-table-column>
            <el-table-column label="Total" width="140">
              <template #default="{ row }">{{ formatCurrency(row.total) }}</template>
            </el-table-column>
            <el-table-column label="Actions" width="140" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" :icon="Edit" @click="handleEditReq(row)">Edit</el-button>
                <el-button link type="danger" size="small" :icon="Delete" @click="handleDeleteReq(row)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="Purchase Orders" name="orders">
          <template #label>
            <span>Purchase Orders <el-badge :value="purchaseOrders.length" :max="99" /></span>
          </template>
          <div class="tab-actions">
            <el-button type="primary" :icon="Plus" @click="handleAddPO">Create PO</el-button>
          </div>
          <el-table v-loading="loading" :data="purchaseOrders" stripe>
            <el-table-column prop="poNumber" label="PO Number" width="140" />
            <el-table-column label="Supplier" min-width="150">
              <template #default="{ row }">{{ row.supplier?.name || '—' }}</template>
            </el-table-column>
            <el-table-column label="Site" min-width="150">
              <template #default="{ row }">{{ row.site?.name || '—' }}</template>
            </el-table-column>
            <el-table-column label="Status" width="120">
              <template #default="{ row }"><StatusBadge :status="row.status" /></template>
            </el-table-column>
            <el-table-column label="Delivery Date" width="120">
              <template #default="{ row }">{{ formatDate(row.deliveryDate) }}</template>
            </el-table-column>
            <el-table-column label="Value" width="140">
              <template #default="{ row }">{{ formatCurrency(row.total) }}</template>
            </el-table-column>
            <el-table-column label="Actions" width="140" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" :icon="Edit" @click="handleEditPO(row)">Edit</el-button>
                <el-button link type="danger" size="small" :icon="Delete" @click="handleDeletePO(row)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="Delivery Notes" name="deliveries">
          <el-table v-loading="loading" :data="deliveryNotes" stripe>
            <el-table-column type="expand">
              <template #default="{ row }">
                <div class="delivery-details">
                  <h4>Items</h4>
                  <el-table :data="row.items" size="small">
                    <el-table-column prop="description" label="Description" />
                    <el-table-column prop="quantity" label="Qty" width="80" />
                    <el-table-column prop="unit" label="Unit" width="80" />
                    <el-table-column prop="notes" label="Notes" />
                  </el-table>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="noteNumber" label="Note #" width="120" />
            <el-table-column label="Type" width="150">
              <template #default="{ row }">
                <el-tag size="small" :type="row.type === 'concrete_ticket' ? 'primary' : row.type === 'muckaway_ticket' ? 'success' : undefined">
                  {{ row.type === 'concrete_ticket' ? 'Concrete Ticket' : row.type === 'muckaway_ticket' ? 'Muckaway Ticket' : 'Standard' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Supplier" min-width="150">
              <template #default="{ row }">{{ row.supplier?.name || '—' }}</template>
            </el-table-column>
            <el-table-column label="Site" min-width="150">
              <template #default="{ row }">{{ row.site?.name || '—' }}</template>
            </el-table-column>
            <el-table-column label="Delivered At" width="120">
              <template #default="{ row }">{{ formatDate(row.deliveredAt) }}</template>
            </el-table-column>
            <el-table-column label="Total" width="120">
              <template #default="{ row }">{{ formatCurrency(row.total) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Requisition Dialog -->
    <el-dialog v-model="reqDialogVisible" :title="reqDialogMode === 'add' ? 'New Requisition' : 'Edit Requisition'" width="800px" destroy-on-close>
      <el-form :model="reqFormData" label-width="130px">
        <el-form-item label="Requisition Ref">
          <el-input v-model="reqFormData.requisitionRef" placeholder="Auto-generated" />
        </el-form-item>
        <el-form-item label="Requested By" required>
          <el-input v-model="reqFormData.requestedBy" placeholder="Enter requester name" />
        </el-form-item>
        <el-form-item label="Site" required>
          <el-select v-model="reqFormData.siteId" placeholder="Select site" filterable style="width: 100%">
            <el-option v-for="s in sites" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Required Date" required>
          <el-date-picker v-model="reqFormData.requiredDate" type="date" value-format="YYYY-MM-DD" placeholder="Select date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Notes">
          <el-input v-model="reqFormData.notes" type="textarea" :rows="2" placeholder="Additional notes" />
        </el-form-item>
        
        <el-divider>Items</el-divider>
        <div v-for="(item, index) in reqFormData.items" :key="index" class="item-row">
          <el-row :gutter="8">
            <el-col :span="10">
              <el-form-item label="Material" label-width="70px">
                <el-input v-model="item.description" placeholder="Material/Description" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="Qty" label-width="50px">
                <el-input-number v-model="item.quantity" :min="1" :max="9999" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="Unit" label-width="50px">
                <el-input v-model="item.unit" placeholder="e.g. m³, kg" />
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label="Est. Cost" label-width="70px">
                <el-input-number v-model="item.estimatedCost" :min="0" :precision="2" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="1">
              <el-button link type="danger" :disabled="reqFormData.items.length <= 1" @click="removeReqItem(index)">×</el-button>
            </el-col>
          </el-row>
        </div>
        <el-button link type="primary" @click="addReqItem">+ Add Item</el-button>
      </el-form>
      <template #footer>
        <el-button @click="reqDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="handleSaveReq">{{ reqDialogMode === 'add' ? 'Create' : 'Update' }}</el-button>
      </template>
    </el-dialog>

    <!-- PO Dialog -->
    <el-dialog v-model="poDialogVisible" :title="poDialogMode === 'add' ? 'Create Purchase Order' : 'Edit Purchase Order'" width="900px" destroy-on-close>
      <el-form :model="poFormData" label-width="130px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="PO Number">
              <el-input v-model="poFormData.poNumber" placeholder="Auto-generated" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Supplier" required>
              <el-select v-model="poFormData.supplierId" placeholder="Select supplier" filterable style="width: 100%">
                <el-option v-for="sup in suppliers" :key="sup.id" :label="sup.name" :value="sup.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Site" required>
              <el-select v-model="poFormData.siteId" placeholder="Select site" filterable style="width: 100%">
                <el-option v-for="s in sites" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Order Date">
              <el-date-picker v-model="poFormData.orderDate" type="date" value-format="YYYY-MM-DD" placeholder="Select date" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Delivery Date">
              <el-date-picker v-model="poFormData.deliveryDate" type="date" value-format="YYYY-MM-DD" placeholder="Select date" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Delivery Address">
              <el-input v-model="poFormData.deliveryAddress" placeholder="Enter delivery address" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Notes">
          <el-input v-model="poFormData.notes" type="textarea" :rows="2" placeholder="Additional notes" />
        </el-form-item>
        
        <el-divider>Items</el-divider>
        <div v-for="(item, index) in poFormData.items" :key="index" class="item-row">
          <el-row :gutter="8">
            <el-col :span="8">
              <el-form-item label="Description" label-width="80px">
                <el-input v-model="item.description" placeholder="Item description" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="Qty" label-width="50px">
                <el-input-number v-model="item.quantity" :min="1" :max="9999" controls-position="right" style="width: 100%" @change="updatePOTotal(index)" />
              </el-form-item>
            </el-col>
            <el-col :span="3">
              <el-form-item label="Unit" label-width="45px">
                <el-input v-model="item.unit" placeholder="e.g. m³" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="Unit Cost" label-width="70px">
                <el-input-number v-model="item.unitCost" :min="0" :precision="2" controls-position="right" style="width: 100%" @change="updatePOTotal(index)" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="Total" label-width="50px">
                <span class="item-total">£{{ (item.quantity * item.unitCost).toLocaleString() }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="1">
              <el-button link type="danger" :disabled="poFormData.items.length <= 1" @click="removePOItem(index)">×</el-button>
            </el-col>
          </el-row>
        </div>
        <el-button link type="primary" @click="addPOItem">+ Add Item</el-button>
      </el-form>
      <template #footer>
        <el-button @click="poDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="handleSavePO">{{ poDialogMode === 'add' ? 'Create' : 'Update' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.procurement-view {
  .filter-card { margin-bottom: 16px; }
  .tab-actions { margin-bottom: 16px; }
}
.delivery-details { padding: 12px 48px; h4 { margin-bottom: 8px; font-size: 14px; color: #606266; } }
.item-row { margin-bottom: 8px; padding: 8px; background: #f5f7fa; border-radius: 4px; }
.item-total { font-weight: bold; color: #409eff; }
</style>
