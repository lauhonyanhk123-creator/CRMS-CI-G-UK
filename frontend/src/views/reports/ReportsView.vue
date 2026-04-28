<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document, TrendCharts, Money, Truck, User, Tools,
  DataAnalysis, FileText, List, Grid
} from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'

// Mock report data
const mockReportData = ref<any[]>([])

interface ReportCard {
  id: string
  title: string
  icon: any
  description: string
  reportType: string
}

const financialReports = ref<ReportCard[]>([
  { id: 'afp', title: 'Applications for Payment Summary', icon: Document, description: 'Summary of all applications for payment by contract', reportType: 'financial_afp' },
  { id: 'ret', title: 'Retentions Report', icon: Money, description: 'Retention held, released, and outstanding by contract', reportType: 'financial_retention' },
  { id: 'var', title: 'Variances Report', icon: TrendCharts, description: 'Contract variations and approved/rejected amounts', reportType: 'financial_variance' },
  { id: 'inv', title: 'Invoice Register', icon: FileText, description: 'Complete register of all invoices issued', reportType: 'financial_invoice' }
])

const projectReports = ref<ReportCard[]>([
  { id: 'con', title: 'Contracts Summary', icon: List, description: 'All contracts with values and status', reportType: 'project_contracts' },
  { id: 'prog', title: 'Site Progress Report', icon: Grid, description: 'Progress of all active sites', reportType: 'project_progress' },
  { id: 'gantt', title: 'Programme/Gantt', icon: DataAnalysis, description: 'Project timeline and milestones', reportType: 'project_gantt' }
])

const cisReports = ref<ReportCard[]>([
  { id: 'cis300', title: 'CIS300 Submission Report', icon: FileText, description: 'CIS300 returns submitted to HMRC', reportType: 'cis_cis300' },
  { id: 'subverify', title: 'Subcontractor Verification Report', icon: User, description: 'Subcontractor verification status', reportType: 'cis_verification' },
  { id: 'ded', title: 'CIS Deductions Summary', icon: Money, description: 'Summary of CIS deductions made', reportType: 'cis_deductions' }
])

const hrReports = ref<ReportCard[]>([
  { id: 'onsite', title: 'Operatives on Site', icon: User, description: 'Operatives currently on each site', reportType: 'hr_onsite' },
  { id: 'training', title: 'Training Expiry Report', icon: Tools, description: 'Training qualifications expiry dates', reportType: 'hr_training' },
  { id: 'cscs', title: 'CSCS Card Expiry', icon: Grid, description: 'CSCS cards expiring soon', reportType: 'hr_cscs' }
])

const plantReports = ref<ReportCard[]>([
  { id: 'alloc', title: 'Plant Allocation Report', icon: Truck, description: 'Plant allocation by site', reportType: 'plant_allocation' },
  { id: 'loler', title: 'LOLER/PUWER Expiry Report', icon: Tools, description: 'Plant certifications expiring', reportType: 'plant_certification' },
  { id: 'hire', title: 'Plant Hire Register', icon: List, description: 'Complete plant hire register', reportType: 'plant_hire' }
])

const dialogVisible = ref(false)
const currentReport = ref<ReportCard | null>(null)
const loadingReport = ref(false)

const openReport = async (report: ReportCard) => {
  currentReport.value = report
  dialogVisible.value = true
  loadingReport.value = true
  
  // Mock API call - would be replaced with actual API
  try {
    // Simulate loading
    await new Promise(resolve => setTimeout(resolve, 500))
    mockReportData.value = generateMockData(report.reportType)
    ElMessage.info(`Loading ${report.title}...`)
  } catch {
    ElMessage.error('Failed to load report')
  } finally {
    loadingReport.value = false
  }
}

const generateMockData = (reportType: string) => {
  const now = new Date()
  const results = []
  const count = Math.floor(Math.random() * 10) + 5
  
  for (let i = 0; i < count; i++) {
    results.push({
      id: `${reportType}-${i + 1}`,
      date: new Date(now.getTime() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      value: Math.floor(Math.random() * 100000),
      status: ['draft', 'submitted', 'approved', 'paid'][Math.floor(Math.random() * 4)]
    })
  }
  return results
}

const downloadReport = () => {
  ElMessage.success('Report download started')
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
            <el-icon><Money /></el-icon> Financial Reports
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
            <el-icon><TrendCharts /></el-icon> Project Reports
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
            <el-icon><Truck /></el-icon> Plant Reports
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
        <el-empty v-if="mockReportData.length === 0" description="No data available" />
        <el-table v-else :data="mockReportData" stripe max-height="400">
          <el-table-column prop="id" label="ID" width="120" />
          <el-table-column prop="date" label="Date" width="120" />
          <el-table-column prop="value" label="Value">
            <template #default="{ row }">
              {{ row.value ? `£${row.value.toLocaleString()}` : '—' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="Status" width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === 'paid' ? 'success' : row.status === 'approved' ? 'success' : 'warning'" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">Close</el-button>
        <el-button type="primary" @click="downloadReport">Download CSV</el-button>
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
