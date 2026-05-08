<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document, DataLine, Coin, Van, User, Tools,
  DataAnalysis, Files, List, Grid
} from '@element-plus/icons-vue'
import api from '@/services/api'
import PageHeader from '@/components/common/PageHeader.vue'

// Report data
const reportData = ref<any[]>([])
const loadingReport = ref(false)

interface ReportCard {
  id: string
  title: string
  icon: any
  description: string
  reportType: string
}

const financialReports = ref<ReportCard[]>([
  { id: 'afp', title: 'Applications for Payment Summary', icon: Document, description: 'Summary of all applications for payment by contract', reportType: 'financial_afp' },
  { id: 'ret', title: 'Retentions Report', icon: Coin, description: 'Retention held, released, and outstanding by contract', reportType: 'financial_retention' },
  { id: 'var', title: 'Variances Report', icon: DataLine, description: 'Contract variations and approved/rejected amounts', reportType: 'financial_variance' },
  { id: 'inv', title: 'Invoice Register', icon: Files, description: 'Complete register of all invoices issued', reportType: 'financial_invoice' }
])

const projectReports = ref<ReportCard[]>([
  { id: 'con', title: 'Contracts Summary', icon: List, description: 'All contracts with values and status', reportType: 'project_contracts' },
  { id: 'prog', title: 'Site Progress Report', icon: Grid, description: 'Progress of all active sites', reportType: 'project_progress' },
  { id: 'gantt', title: 'Programme/Gantt', icon: DataAnalysis, description: 'Project timeline and milestones', reportType: 'project_gantt' }
])

const cisReports = ref<ReportCard[]>([
  { id: 'cis300', title: 'CIS300 Submission Report', icon: Files, description: 'CIS300 returns submitted to HMRC', reportType: 'cis_cis300' },
  { id: 'subverify', title: 'Subcontractor Verification Report', icon: User, description: 'Subcontractor verification status', reportType: 'cis_verification' },
  { id: 'ded', title: 'CIS Deductions Summary', icon: Coin, description: 'Summary of CIS deductions made', reportType: 'cis_deductions' }
])

const hrReports = ref<ReportCard[]>([
  { id: 'onsite', title: 'Operatives on Site', icon: User, description: 'Operatives currently on each site', reportType: 'hr_onsite' },
  { id: 'training', title: 'Training Expiry Report', icon: Tools, description: 'Training qualifications expiry dates', reportType: 'hr_training' },
  { id: 'cscs', title: 'CSCS Card Expiry', icon: Grid, description: 'CSCS cards expiring soon', reportType: 'hr_cscs' }
])

const plantReports = ref<ReportCard[]>([
  { id: 'alloc', title: 'Plant Allocation Report', icon: Van, description: 'Plant allocation by site', reportType: 'plant_allocation' },
  { id: 'loler', title: 'LOLER/PUWER Expiry Report', icon: Tools, description: 'Plant certifications expiring', reportType: 'plant_certification' },
  { id: 'hire', title: 'Plant Hire Register', icon: List, description: 'Complete plant hire register', reportType: 'plant_hire' }
])

interface ColDef { prop: string; label: string; width?: number; currency?: boolean; tag?: boolean }

const REPORT_COLUMNS: Record<string, ColDef[]> = {
  financial_afp: [
    { prop: 'ref', label: 'Ref', width: 160 },
    { prop: 'contract', label: 'Contract', width: 140 },
    { prop: 'date', label: 'Due Date', width: 120 },
    { prop: 'value', label: 'Gross Value', currency: true },
    { prop: 'status', label: 'Status', width: 120, tag: true }
  ],
  financial_retention: [
    { prop: 'contractRef', label: 'Contract Ref', width: 150 },
    { prop: 'title', label: 'Title' },
    { prop: 'totalRetention', label: 'Total Held', currency: true },
    { prop: 'releasedAtPC', label: 'Released at PC', currency: true },
    { prop: 'releasedAtDefects', label: 'Released at Defects', currency: true },
    { prop: 'balance', label: 'Balance', currency: true }
  ],
  financial_variance: [
    { prop: 'variationRef', label: 'Ref', width: 140 },
    { prop: 'contractRef', label: 'Contract', width: 130 },
    { prop: 'description', label: 'Description' },
    { prop: 'originalValue', label: 'Original', currency: true },
    { prop: 'agreedValue', label: 'Agreed', currency: true },
    { prop: 'status', label: 'Status', width: 120, tag: true }
  ],
  financial_invoice: [
    { prop: 'ref', label: 'Invoice Ref', width: 160 },
    { prop: 'contract', label: 'Contract', width: 140 },
    { prop: 'paidDate', label: 'Paid Date', width: 120 },
    { prop: 'value', label: 'Amount', currency: true },
    { prop: 'status', label: 'Status', width: 120, tag: true }
  ],
  project_contracts: [
    { prop: 'contractRef', label: 'Ref', width: 130 },
    { prop: 'title', label: 'Title' },
    { prop: 'client', label: 'Client', width: 160 },
    { prop: 'startDate', label: 'Start', width: 110 },
    { prop: 'contractValue', label: 'Value', currency: true },
    { prop: 'status', label: 'Status', width: 120, tag: true }
  ],
  project_progress: [
    { prop: 'name', label: 'Site', width: 180 },
    { prop: 'client', label: 'Client', width: 160 },
    { prop: 'startDate', label: 'Start', width: 110 },
    { prop: 'endDate', label: 'End', width: 110 },
    { prop: 'status', label: 'Status', width: 120, tag: true }
  ],
  cis_cis300: [
    { prop: 'id', label: 'Return ID', width: 120 },
    { prop: 'month', label: 'Month', width: 110 },
    { prop: 'subcontractors', label: 'Subcontractors', width: 140 },
    { prop: 'gross', label: 'Gross', currency: true },
    { prop: 'deduction', label: 'Deduction', currency: true },
    { prop: 'status', label: 'Status', width: 120, tag: true }
  ],
  cis_verification: [
    { prop: 'name', label: 'Subcontractor' },
    { prop: 'utr', label: 'UTR', width: 130 },
    { prop: 'verificationNumber', label: 'Verification #', width: 150 },
    { prop: 'cisStatus', label: 'CIS Status', width: 130, tag: true },
    { prop: 'verificationDate', label: 'Verified', width: 120 }
  ],
  cis_deductions: [
    { prop: 'subcontractorName', label: 'Subcontractor' },
    { prop: 'totalGrossValue', label: 'Gross', currency: true },
    { prop: 'totalDeduction', label: 'Deduction', currency: true },
    { prop: 'totalNetValue', label: 'Net', currency: true }
  ],
  hr_onsite: [
    { prop: 'employeeRef', label: 'Ref', width: 110 },
    { prop: 'name', label: 'Name' },
    { prop: 'trade', label: 'Trade', width: 140 },
    { prop: 'employer', label: 'Employer', width: 160 },
    { prop: 'status', label: 'Status', width: 120, tag: true }
  ],
  hr_training: [
    { prop: 'employeeRef', label: 'Ref', width: 110 },
    { prop: 'name', label: 'Name' },
    { prop: 'qualificationName', label: 'Qualification' },
    { prop: 'issuingBody', label: 'Issuing Body', width: 140 },
    { prop: 'expiryDate', label: 'Expiry', width: 120 }
  ],
  hr_cscs: [
    { prop: 'employeeRef', label: 'Ref', width: 110 },
    { prop: 'name', label: 'Name' },
    { prop: 'cardType', label: 'Card Type', width: 160 },
    { prop: 'expiryDate', label: 'Expiry', width: 120 },
    { prop: 'status', label: 'Status', width: 120, tag: true }
  ],
  plant_allocation: [
    { prop: 'plantRef', label: 'Ref', width: 110 },
    { prop: 'description', label: 'Description' },
    { prop: 'category', label: 'Category', width: 140 },
    { prop: 'site', label: 'Current Site', width: 160 },
    { prop: 'status', label: 'Status', width: 120, tag: true }
  ],
  plant_certification: [
    { prop: 'plantRef', label: 'Ref', width: 110 },
    { prop: 'description', label: 'Description' },
    { prop: 'nextInspectionDate', label: 'Next Inspection', width: 140 },
    { prop: 'daysUntil', label: 'Days Left', width: 100 },
    { prop: 'status', label: 'Status', width: 120, tag: true }
  ],
  plant_hire: [
    { prop: 'plantRef', label: 'Ref', width: 110 },
    { prop: 'description', label: 'Description' },
    { prop: 'category', label: 'Category', width: 140 },
    { prop: 'hireRate', label: 'Hire Rate', currency: true },
    { prop: 'status', label: 'Status', width: 120, tag: true }
  ]
}

const currentColumns = computed<ColDef[]>(() => {
  if (!currentReport.value) return []
  return REPORT_COLUMNS[currentReport.value.reportType] ?? [
    { prop: 'id', label: 'ID', width: 120 },
    { prop: 'date', label: 'Date', width: 120 },
    { prop: 'value', label: 'Value', currency: true },
    { prop: 'status', label: 'Status', width: 120, tag: true }
  ]
})

const dialogVisible = ref(false)
const currentReport = ref<ReportCard | null>(null)

const openReport = async (report: ReportCard) => {
  currentReport.value = report
  dialogVisible.value = true
  loadingReport.value = true
  reportData.value = []

  try {
    if (report.reportType === 'financial_afp') {
      const res = await api.applicationsForPayment.getAll({ limit: 500 })
      reportData.value = (res.data.data ?? []).map((item: any) => ({
        ref: item.applicationRef ?? item.id,
        contract: item.contractRef ?? item.contractId,
        date: item.dueDate,
        value: item.grossValue ?? item.amount,
        status: item.status
      }))

    } else if (report.reportType === 'financial_retention') {
      const res = await api.reports.getRetentionSchedule()
      const d = res.data as any
      const rows: any[] = Array.isArray(d) ? d : Array.isArray(d?.data) ? d.data : []
      reportData.value = rows

    } else if (report.reportType === 'financial_variance') {
      const res = await api.variations.getAll({ limit: 500 })
      const d = res.data as any
      const rows: any[] = d?.content ?? d?.data ?? []
      reportData.value = rows.map((item: any) => ({
        variationRef: item.variationRef,
        contractRef: item.contractRef,
        description: item.description,
        originalValue: item.originalValue,
        agreedValue: item.agreedValue,
        status: item.status
      }))

    } else if (report.reportType === 'financial_invoice') {
      const res = await api.applicationsForPayment.getAll({ status: 'PAID', limit: 500 })
      reportData.value = (res.data.data ?? []).map((item: any) => ({
        ref: item.applicationRef ?? item.id,
        contract: item.contractRef ?? item.contractId,
        paidDate: item.paidDate ?? item.dueDate,
        value: item.grossValue ?? item.amount,
        status: item.status
      }))

    } else if (report.reportType === 'project_contracts') {
      const res = await api.contracts.getAll({ limit: 500 })
      reportData.value = (res.data.data ?? []).map((item: any) => ({
        contractRef: item.contractRef,
        title: item.title,
        client: item.clientName ?? item.client?.name,
        startDate: item.startDate,
        contractValue: item.contractValue,
        status: item.status
      }))

    } else if (report.reportType === 'project_progress') {
      const res = await api.sites.getAll({ limit: 500 })
      reportData.value = (res.data.data ?? []).map((item: any) => ({
        name: item.name,
        client: item.clientName ?? item.client?.name,
        startDate: item.startDate,
        endDate: item.endDate,
        status: item.status
      }))

    } else if (report.reportType === 'project_gantt') {
      ElMessage.info('Gantt view is available in the Sites module')
      loadingReport.value = false
      return

    } else if (report.reportType === 'cis_cis300') {
      const res = await api.cisReturns.getAll({ limit: 100 })
      reportData.value = (res.data.data ?? []).map((item: any) => ({
        id: item.id,
        month: item.taxMonth ?? item.month,
        subcontractors: item.cisReturnLines?.length ?? item.subcontractorCount ?? 0,
        gross: item.totalGross ?? item.grossValue ?? 0,
        deduction: item.totalDeduction ?? item.deductionAmount ?? 0,
        status: item.status
      }))

    } else if (report.reportType === 'cis_verification') {
      const res = await api.subcontractors.getAll({ limit: 500 })
      reportData.value = (res.data.data ?? []).map((item: any) => ({
        name: item.name,
        utr: item.utr,
        verificationNumber: item.verificationNumber,
        cisStatus: item.cisStatus ?? item.verificationStatus,
        verificationDate: item.verificationDate
      }))

    } else if (report.reportType === 'cis_deductions') {
      const res = await api.reports.getCISSummary()
      const d = res.data as any
      const rows: any[] = Array.isArray(d) ? d : Array.isArray(d?.data) ? d.data : []
      reportData.value = rows

    } else if (report.reportType === 'hr_onsite') {
      const res = await api.operatives.getAll({ status: 'ACTIVE', limit: 500 })
      reportData.value = (res.data.data ?? []).map((item: any) => ({
        employeeRef: item.employeeRef,
        name: `${item.firstName ?? ''} ${item.lastName ?? ''}`.trim(),
        trade: item.trade,
        employer: item.employer?.name ?? item.employerName,
        status: item.status
      }))

    } else if (report.reportType === 'hr_training') {
      const res = await api.operatives.getAll({ limit: 500 })
      const rows: any[] = []
      for (const op of (res.data.data ?? [])) {
        for (const q of ((op as any).qualifications ?? [])) {
          rows.push({
            employeeRef: op.employeeRef,
            name: `${op.firstName ?? ''} ${op.lastName ?? ''}`.trim(),
            qualificationName: (q as any).qualificationName ?? (q as any).name,
            issuingBody: (q as any).issuingBody,
            expiryDate: (q as any).expiryDate
          })
        }
      }
      reportData.value = rows

    } else if (report.reportType === 'hr_cscs') {
      const res = await api.operatives.getAll({ limit: 500 })
      const rows: any[] = []
      for (const op of (res.data.data ?? [])) {
        for (const c of ((op as any).cards ?? [])) {
          rows.push({
            employeeRef: op.employeeRef,
            name: `${op.firstName ?? ''} ${op.lastName ?? ''}`.trim(),
            cardType: (c as any).cardType,
            expiryDate: (c as any).expiryDate,
            status: (c as any).expiryDate && new Date((c as any).expiryDate) < new Date() ? 'EXPIRED' : 'VALID'
          })
        }
      }
      reportData.value = rows

    } else if (report.reportType === 'plant_allocation') {
      const res = await api.plant.getAll({ limit: 500 })
      reportData.value = (res.data.data ?? []).map((item: any) => ({
        plantRef: item.plantRef,
        description: item.description,
        category: item.category,
        site: item.siteName ?? item.currentSite?.name,
        status: item.status
      }))

    } else if (report.reportType === 'plant_certification') {
      const res = await api.plant.getAll({ limit: 500 })
      const today = new Date()
      reportData.value = (res.data.data ?? [])
        .filter((item: any) => item.nextInspectionDate)
        .map((item: any) => {
          const due = new Date(item.nextInspectionDate)
          const daysUntil = Math.ceil((due.getTime() - today.getTime()) / 86400000)
          return {
            plantRef: item.plantRef,
            description: item.description,
            nextInspectionDate: item.nextInspectionDate,
            daysUntil,
            status: daysUntil < 0 ? 'OVERDUE' : daysUntil <= 14 ? 'DUE_SOON' : 'OK'
          }
        })
        .sort((a: any, b: any) => a.daysUntil - b.daysUntil)

    } else if (report.reportType === 'plant_hire') {
      const res = await api.plant.getAll({ limit: 500 })
      reportData.value = (res.data.data ?? []).map((item: any) => ({
        plantRef: item.plantRef,
        description: item.description,
        category: item.category,
        hireRate: item.hireRate ?? item.dailyRate,
        status: item.status
      }))
    }

    ElMessage.success(`${report.title} loaded`)
  } catch (error) {
    console.error(`Failed to load ${report.reportType}:`, error)
    ElMessage.error('Failed to load report data')
    reportData.value = []
  } finally {
    loadingReport.value = false
  }
}

const downloadingPdf = ref(false)

const downloadReport = () => {
  if (!reportData.value.length) {
    ElMessage.warning('No data to download')
    return
  }
  const cols = currentColumns.value
  const header = cols.map(c => c.label).join(',')
  const rows = reportData.value.map(row =>
    cols.map(c => {
      const v = row[c.prop]
      if (v == null) return ''
      const s = String(v)
      return s.includes(',') ? `"${s}"` : s
    }).join(',')
  )
  const csv = [header, ...rows].join('\n')
  const blob = new Blob([csv], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${currentReport.value?.reportType ?? 'report'}-${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('CSV download started')
}

const downloadCisPdf = async (returnId: string | number) => {
  downloadingPdf.value = true
  try {
    const res = await api.cisReturns.downloadPdf(returnId)
    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `CIS300-return-${returnId}.pdf`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('PDF download started')
  } catch {
    ElMessage.error('Failed to download PDF')
  } finally {
    downloadingPdf.value = false
  }
}
</script>

<template>
  <div class="reports-view">
    <PageHeader title="Reports" :breadcrumbs="[{ title: 'Reports' }]" />

    <el-card shadow="never">
      <el-tabs>
        <!-- Financial Reports -->
        <el-tab-pane>
          <template #label>
            <el-icon><Coin /></el-icon> Financial Reports
          </template>
          <el-row :gutter="20">
            <el-col v-for="report in financialReports" :key="report.id" :xs="24" :sm="12" :md="8" :lg="6">
              <el-card class="report-card" shadow="hover" @click="openReport(report)">
                <el-icon :size="32" class="report-icon"><component :is="report.icon" /></el-icon>
                <h4 class="report-title">{{ report.title }}</h4>
                <p class="report-description">{{ report.description }}</p>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- Project Reports -->
        <el-tab-pane>
          <template #label>
            <el-icon><DataLine /></el-icon> Project Reports
          </template>
          <el-row :gutter="20">
            <el-col v-for="report in projectReports" :key="report.id" :xs="24" :sm="12" :md="8" :lg="6">
              <el-card class="report-card" shadow="hover" @click="openReport(report)">
                <el-icon :size="32" class="report-icon"><component :is="report.icon" /></el-icon>
                <h4 class="report-title">{{ report.title }}</h4>
                <p class="report-description">{{ report.description }}</p>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- CIS Reports -->
        <el-tab-pane>
          <template #label>
            <el-icon><Document /></el-icon> CIS Reports
          </template>
          <el-row :gutter="20">
            <el-col v-for="report in cisReports" :key="report.id" :xs="24" :sm="12" :md="8" :lg="6">
              <el-card class="report-card" shadow="hover" @click="openReport(report)">
                <el-icon :size="32" class="report-icon"><component :is="report.icon" /></el-icon>
                <h4 class="report-title">{{ report.title }}</h4>
                <p class="report-description">{{ report.description }}</p>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- HR Reports -->
        <el-tab-pane>
          <template #label>
            <el-icon><User /></el-icon> HR Reports
          </template>
          <el-row :gutter="20">
            <el-col v-for="report in hrReports" :key="report.id" :xs="24" :sm="12" :md="8" :lg="6">
              <el-card class="report-card" shadow="hover" @click="openReport(report)">
                <el-icon :size="32" class="report-icon"><component :is="report.icon" /></el-icon>
                <h4 class="report-title">{{ report.title }}</h4>
                <p class="report-description">{{ report.description }}</p>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- Plant Reports -->
        <el-tab-pane>
          <template #label>
            <el-icon><Van /></el-icon> Plant Reports
          </template>
          <el-row :gutter="20">
            <el-col v-for="report in plantReports" :key="report.id" :xs="24" :sm="12" :md="8" :lg="6">
              <el-card class="report-card" shadow="hover" @click="openReport(report)">
                <el-icon :size="32" class="report-icon"><component :is="report.icon" /></el-icon>
                <h4 class="report-title">{{ report.title }}</h4>
                <p class="report-description">{{ report.description }}</p>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Report Preview Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentReport?.title || 'Report'"
      width="80%"
      destroy-on-close
    >
      <div v-loading="loadingReport">
        <el-empty v-if="reportData.length === 0" description="No data available" />
        <el-table v-else :data="reportData" stripe max-height="400">
          <template v-for="col in currentColumns" :key="col.prop">
            <el-table-column
              v-if="col.currency"
              :prop="col.prop"
              :label="col.label"
              :width="col.width"
            >
              <template #default="{ row }">
                {{ row[col.prop] != null ? `£${Number(row[col.prop]).toLocaleString()}` : '—' }}
              </template>
            </el-table-column>
            <el-table-column
              v-else-if="col.tag"
              :prop="col.prop"
              :label="col.label"
              :width="col.width"
            >
              <template #default="{ row }">
                <el-tag
                  :type="['paid', 'active', 'approved', 'submitted'].includes(row[col.prop]) ? 'success' : 'warning'"
                  size="small"
                >
                  {{ row[col.prop] ?? '—' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              v-else
              :prop="col.prop"
              :label="col.label"
              :width="col.width"
            />
          </template>
          <!-- PDF download column — CIS300 only -->
          <el-table-column
            v-if="currentReport?.reportType === 'cis_cis300'"
            label="PDF"
            width="90"
            fixed="right"
          >
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                size="small"
                :loading="downloadingPdf"
                @click.stop="downloadCisPdf(row.id)"
              >
                PDF
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">Close</el-button>
        <el-button type="primary" plain @click="downloadReport">Download CSV</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.reports-view {
  .report-card {
    margin-bottom: 20px;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    .report-icon {
      color: var(--el-color-primary);
      margin-bottom: 12px;
    }
    
    .report-title {
      margin: 0 0 8px 0;
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
    
    .report-description {
      margin: 0;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}
</style>
