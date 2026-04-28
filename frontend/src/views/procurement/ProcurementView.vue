<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import api, { type Requisition, type PurchaseOrder, type DeliveryNote, type Site } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const activeTab = ref('requisitions')
const loading = ref(false)

const requisitions = ref<Requisition[]>([])
const purchaseOrders = ref<PurchaseOrder[]>([])
const deliveryNotes = ref<DeliveryNote[]>([])
const sites = ref<Site[]>([])

const filters = reactive({
  status: '',
  siteId: '',
  page: 1,
  limit: 20
})

onMounted(() => { loadRequisitions(); loadSites() })

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
  } catch {}
}

const handleTabChange = (tab: string) => {
  activeTab.value = tab
  filters.page = 1
  if (tab === 'requisitions') loadRequisitions()
  else if (tab === 'orders') loadPurchaseOrders()
  else if (tab === 'deliveries') loadDeliveryNotes()
}

const formatCurrency = (value: number) => `£${value.toLocaleString()}`
const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'
</script>

<template>
  <div class="procurement-view">
    <PageHeader title="Procurement" :breadcrumbs="[{ title: 'Procurement' }]">
      <template #actions>
        <el-button type="primary" :icon="Plus">New Requisition</el-button>
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
          <el-table v-loading="loading" :data="requisitions" stripe>
            <el-table-column prop="requisitionRef" label="Req Ref" width="120" />
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
            <el-table-column label="Actions" width="100" fixed="right">
              <template #default><el-button link type="primary" size="small">View</el-button></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="Purchase Orders" name="orders">
          <el-table v-loading="loading" :data="purchaseOrders" stripe>
            <el-table-column prop="poNumber" label="PO Number" width="120" />
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
            <el-table-column label="Actions" width="100" fixed="right">
              <template #default><el-button link type="primary" size="small">View</el-button></template>
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
                <el-tag size="small" :type="row.type === 'concrete_ticket' ? 'primary' : row.type === 'muckaway_ticket' ? 'success' : ''">
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
  </div>
</template>

<style lang="scss" scoped>
.procurement-view { .filter-card { margin-bottom: 16px; } }
.delivery-details { padding: 12px 48px; h4 { margin-bottom: 8px; font-size: 14px; color: #606266; } }
</style>
