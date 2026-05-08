<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import api, { type Tender, type Company } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const loading = ref(false)
const tenders = ref<Tender[]>([])
const companies = ref<Company[]>([])

const stages = ['lead', 'qualified', 'pricing', 'submitted', 'negotiation', 'awarded', 'lost']
const stageLabels: Record<string, string> = {
  lead: 'Lead', qualified: 'Qualified', pricing: 'Pricing',
  submitted: 'Submitted', negotiation: 'Negotiation', awarded: 'Awarded', lost: 'Lost'
}
const stageColors: Record<string, string> = {
  lead: '#909399', qualified: '#1a73e8', pricing: '#e6a23c',
  submitted: '#3498db', negotiation: '#9b59b6', awarded: '#67c23a', lost: '#f56c6c'
}

const drawerVisible = ref(false)
const drawerLoading = ref(false)

const formRef = ref<FormInstance>()
const form = reactive({
  title: '', clientId: '', siteId: '', valueMin: undefined as number | undefined,
  valueMax: undefined as number | undefined, returnDate: '', winProbability: 50, stage: 'lead' as Tender['stage'], notes: ''
})

const pipelineStats = computed(() => {
  const active = tenders.value.filter(t => stages.slice(0, 5).includes(t.stage))
  return {
    totalLeads: active.length,
    totalValue: active.reduce((sum, t) => sum + (t.valueMax || 0), 0),
    conversionRate: tenders.value.length > 0
      ? Math.round((tenders.value.filter(t => t.stage === 'awarded').length / tenders.value.length) * 100) : 0
  }
})

const getTendersByStage = (stage: string) => tenders.value.filter(t => t.stage === stage)

onMounted(() => { loadData(); loadCompanies() })

const loadData = async () => {
  loading.value = true
  try {
    const response = await api.tenders.getAll({ limit: 100 })
    tenders.value = response.data.data
  } catch { ElMessage.error('Failed to load tenders') } finally { loading.value = false }
}

const loadCompanies = async () => {
  try {
    const response = await api.companies.getAll({ limit: 100 })
    companies.value = response.data.data
  } catch {}
}

const formatCurrency = (value?: number) => value ? `£${(value / 1000).toFixed(0)}k` : '—'
const formatDate = (date?: string) => date ? new Date(date).toLocaleDateString() : '—'

const openAddDrawer = () => {
  Object.assign(form, { title: '', clientId: '', siteId: '', valueMin: undefined, valueMax: undefined, returnDate: '', winProbability: 50, stage: 'lead', notes: '' })
  drawerVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    drawerLoading.value = true
    try {
      await api.tenders.create(form)
      ElMessage.success('Tender created')
      drawerVisible.value = false
      loadData()
    } catch { ElMessage.error('Failed to create tender') } finally { drawerLoading.value = false }
  })
}

const moveToStage = async (tender: Tender, newStage: string) => {
  try {
    await api.tenders.update(tender.id, { stage: newStage as Tender['stage'] })
    ElMessage.success(`Moved to ${stageLabels[newStage]}`)
    loadData()
  } catch { ElMessage.error('Failed to update tender') }
}

const winTender = async (tender: Tender) => {
  try {
    await api.tenders.win(tender.id)
    ElMessage.success('Tender won!')
    loadData()
  } catch { ElMessage.error('Failed to mark as won') }
}

const loseTender = async (tender: Tender) => {
  let reason: string
  try {
    const { value } = await ElMessageBox.prompt('Enter reason for losing:', 'Mark as Lost', {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      inputPattern: /.+/,
      inputErrorMessage: 'Please enter a reason'
    })
    reason = value
  } catch {
    return // cancelled
  }
  try {
    await api.tenders.lose(tender.id, reason)
    ElMessage.info('Tender marked as lost')
    loadData()
  } catch { ElMessage.error('Failed to mark as lost') }
}
</script>

<template>
  <div class="tenders-view">
    <PageHeader title="Tenders" :breadcrumbs="[{ title: 'Tenders' }]">
      <template #actions>
        <el-button :icon="Refresh" @click="loadData">Refresh</el-button>
        <el-button type="primary" :icon="Plus" @click="openAddDrawer">Add Tender</el-button>
      </template>
    </PageHeader>

    <el-card shadow="never" class="pipeline-stats-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="stat-item">
            <span class="stat-value">{{ pipelineStats.totalLeads }}</span>
            <span class="stat-label">Total Leads</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <span class="stat-value">{{ formatCurrency(pipelineStats.totalValue) }}</span>
            <span class="stat-label">Pipeline Value</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <span class="stat-value">{{ pipelineStats.conversionRate }}%</span>
            <span class="stat-label">Conversion Rate</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <div v-loading="loading" class="kanban-board">
      <div v-for="stage in stages" :key="stage" class="kanban-column">
        <div class="column-header" :style="{ borderTopColor: stageColors[stage] }">
          <span class="column-title">{{ stageLabels[stage] }}</span>
          <el-badge :value="getTendersByStage(stage).length" :type="stage === 'awarded' ? 'success' : 'primary'" />
        </div>
        <div class="column-content">
          <el-card
            v-for="tender in getTendersByStage(stage)"
            :key="tender.id"
            shadow="hover"
            class="tender-card"
          >
            <h4 class="tender-title">{{ tender.title }}</h4>
            <p class="tender-client">{{ tender.client?.name || '—' }}</p>
            <div class="tender-meta">
              <span>Value: {{ formatCurrency(tender.valueMax) }}</span>
              <span>Return: {{ formatDate(tender.returnDate) }}</span>
            </div>
            <div class="tender-probability">
              <el-progress :percentage="tender.winProbability || 50" :color="stageColors[stage]" size="small" />
            </div>
            <div class="tender-actions">
              <el-dropdown trigger="click" @command="(cmd: string) => cmd === 'win' ? winTender(tender) : cmd === 'lose' ? loseTender(tender) : moveToStage(tender, cmd)">
                <el-button size="small">Move <el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="s in stages" :key="s" :command="s" :disabled="s === tender.stage">{{ stageLabels[s] }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </el-card>
          <div v-if="getTendersByStage(stage).length === 0" class="empty-column">No tenders</div>
        </div>
      </div>
    </div>

    <el-drawer v-model="drawerVisible" title="Add Tender" size="500px">
      <el-form ref="formRef" :model="form" label-position="top">
        <el-form-item label="Title" prop="title" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="Client" prop="clientId">
          <el-select v-model="form.clientId" placeholder="Select client" filterable clearable>
            <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="Min Value"><el-input-number v-model="form.valueMin" :min="0" :step="1000" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="Max Value"><el-input-number v-model="form.valueMax" :min="0" :step="1000" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="Return Date"><el-date-picker v-model="form.returnDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="Win Probability"><el-input-number v-model="form.winProbability" :min="0" :max="100" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="Notes"><el-input v-model="form.notes" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="drawerLoading" @click="handleSubmit">Create</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.tenders-view { .pipeline-stats-card { margin-bottom: 16px; .stat-item { text-align: center; .stat-value { font-size: 24px; font-weight: 600; color: #1a73e8; } .stat-label { font-size: 12px; color: #909399; } } } }
.kanban-board { display: flex; gap: 12px; overflow-x: auto; padding-bottom: 20px; }
.kanban-column { flex: 0 0 280px; background: #f5f7fa; border-radius: 8px; .column-header { display: flex; justify-content: space-between; align-items: center; padding: 12px; border-top: 3px solid; border-radius: 8px 8px 0 0; background: #fff; .column-title { font-weight: 600; } } .column-content { padding: 8px; min-height: 200px; max-height: calc(100vh - 300px); overflow-y: auto; } }
.tender-card { margin-bottom: 8px; .tender-title { font-size: 14px; font-weight: 600; margin-bottom: 4px; } .tender-client { font-size: 12px; color: #909399; margin-bottom: 8px; } .tender-meta { display: flex; justify-content: space-between; font-size: 11px; color: #606266; margin-bottom: 8px; } .tender-probability { margin-bottom: 8px; } .tender-actions { display: flex; justify-content: flex-end; } }
.empty-column { text-align: center; padding: 20px; color: #909399; font-size: 12px; }
</style>
