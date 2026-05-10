<template>
  <div class="quality-view">
    <div class="page-header">
      <h1>Quality & ITP Management</h1>
      <div class="header-actions">
        <select v-model="activeTab" class="tab-select">
          <option value="templates">ITP Templates</option>
          <option value="schedules">ITP Schedules</option>
          <option value="inspections">Inspection Records</option>
          <option value="defects">Defects</option>
          <option value="signoffs">NHBC/LABC Sign-offs</option>
        </select>
        <button class="btn-primary" @click="openCreateDialog">
          <Plus class="icon" />
          New {{ tabLabels[activeTab as keyof typeof tabLabels] }}
        </button>
      </div>
    </div>

    <!-- ITP Templates Tab -->
    <div v-if="activeTab === 'templates'" class="tab-content">
      <div class="filters-bar">
        <select v-model="templateFilters.status" @change="loadTemplates">
          <option value="">All Status</option>
          <option value="draft">Draft</option>
          <option value="active">Active</option>
          <option value="archived">Archived</option>
        </select>
        <select v-model="templateFilters.tradeCategory" @change="loadTemplates">
          <option value="">All Trade Categories</option>
          <option value="concrete">Concrete</option>
          <option value="drainage">Drainage</option>
          <option value="brickwork">Brickwork</option>
          <option value="structure">Structure</option>
          <option value="finishes">Finishes</option>
        </select>
        <input v-model="templateFilters.search" type="text" placeholder="Search templates..." class="search-input" @input="debouncedLoadTemplates">
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Category</th>
            <th>Trade Category</th>
            <th>Items</th>
            <th>Version</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="template in templates" :key="template.id">
            <td>{{ template.name }}</td>
            <td>{{ template.category || '-' }}</td>
            <td>{{ template.tradeCategory || '-' }}</td>
            <td>{{ template.items?.length || 0 }}</td>
            <td>{{ template.version }}</td>
            <td><span :class="['status-badge', template.status]">{{ template.status }}</span></td>
            <td class="actions-cell">
              <button class="btn-icon" title="View" @click="viewTemplate(template)"><View /></button>
              <button class="btn-icon" title="Edit" @click="editTemplate(template)"><Edit /></button>
              <button class="btn-icon" title="Copy" @click="copyTemplate(template.id)"><CopyDocument /></button>
              <button class="btn-icon btn-danger" title="Delete" @click="deleteTemplate(template.id)"><Delete /></button>
            </td>
          </tr>
          <tr v-if="templates.length === 0">
            <td colspan="7" class="empty-row">No templates found</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button :disabled="templatePage === 0" @click="templatePage--">Previous</button>
        <span>Page {{ templatePage + 1 }}</span>
        <button @click="templatePage++">Next</button>
      </div>
    </div>

    <!-- ITP Schedules Tab -->
    <div v-if="activeTab === 'schedules'" class="tab-content">
      <div class="filters-bar">
        <select v-model="scheduleFilters.contractId" @change="loadSchedules">
          <option value="">All Contracts</option>
          <option v-for="c in contracts" :key="c.id" :value="c.id">{{ c.contractRef }}</option>
        </select>
        <select v-model="scheduleFilters.status" @change="loadSchedules">
          <option value="">All Status</option>
          <option value="pending">Pending</option>
          <option value="in_progress">In Progress</option>
          <option value="completed">Completed</option>
          <option value="failed">Failed</option>
        </select>
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>Title</th>
            <th>Contract</th>
            <th>Start Date</th>
            <th>Due Date</th>
            <th>Items</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="schedule in schedules" :key="schedule.id">
            <td>{{ schedule.title }}</td>
            <td>{{ schedule.contractRef || schedule.contractId }}</td>
            <td>{{ schedule.startDate }}</td>
            <td>{{ schedule.dueDate }}</td>
            <td>{{ schedule.items?.length || 0 }}</td>
            <td><span :class="['status-badge', schedule.status]">{{ schedule.status }}</span></td>
            <td class="actions-cell">
              <button class="btn-icon" title="View" @click="viewSchedule(schedule)"><View /></button>
              <button class="btn-icon" title="Edit" @click="editSchedule(schedule)"><Edit /></button>
              <button class="btn-icon" title="Duplicate" @click="createScheduleFromTemplate(schedule.templateId, schedule.contractId)"><CopyDocument /></button>
              <button class="btn-icon btn-danger" title="Delete" @click="deleteSchedule(schedule.id)"><Delete /></button>
            </td>
          </tr>
          <tr v-if="schedules.length === 0">
            <td colspan="7" class="empty-row">No schedules found</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Inspection Records Tab -->
    <div v-if="activeTab === 'inspections'" class="tab-content">
      <div class="filters-bar">
        <select v-model="inspectionFilters.result" @change="loadInspections">
          <option value="">All Results</option>
          <option value="pass">Pass</option>
          <option value="fail">Fail</option>
          <option value="nc">NC</option>
          <option value="conditional">Conditional</option>
        </select>
        <input v-model="inspectionFilters.inspectorName" type="text" placeholder="Inspector name..." class="search-input" @input="debouncedLoadInspections">
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>Title</th>
            <th>Schedule</th>
            <th>Inspector</th>
            <th>Date</th>
            <th>Result</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="inspection in inspections" :key="inspection.id">
            <td>{{ inspection.title }}</td>
            <td>{{ inspection.scheduleTitle || inspection.scheduleItemId }}</td>
            <td>{{ inspection.inspectorName }}</td>
            <td>{{ inspection.inspectionDate }}</td>
            <td><span :class="['status-badge', inspection.result]">{{ inspection.result }}</span></td>
            <td class="actions-cell">
              <button class="btn-icon" title="View" @click="viewInspection(inspection)"><View /></button>
              <button class="btn-icon" title="Edit" @click="editInspection(inspection)"><Edit /></button>
              <button class="btn-icon btn-danger" title="Delete" @click="deleteInspection(inspection.id)"><Delete /></button>
            </td>
          </tr>
          <tr v-if="inspections.length === 0">
            <td colspan="6" class="empty-row">No inspections found</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Defects Tab -->
    <div v-if="activeTab === 'defects'" class="tab-content">
      <div class="filters-bar">
        <select v-model="defectFilters.contractId" @change="loadDefects">
          <option value="">All Contracts</option>
          <option v-for="c in contracts" :key="c.id" :value="c.id">{{ c.contractRef }}</option>
        </select>
        <select v-model="defectFilters.status" @change="loadDefects">
          <option value="">All Status</option>
          <option value="open">Open</option>
          <option value="in_progress">In Progress</option>
          <option value="resolved">Resolved</option>
          <option value="closed">Closed</option>
        </select>
        <select v-model="defectFilters.priority" @change="loadDefects">
          <option value="">All Priority</option>
          <option value="low">Low</option>
          <option value="medium">Medium</option>
          <option value="high">High</option>
          <option value="critical">Critical</option>
        </select>
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>Title</th>
            <th>Contract</th>
            <th>Location</th>
            <th>Priority</th>
            <th>Status</th>
            <th>Assigned To</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="defect in defects" :key="defect.id">
            <td>{{ defect.title }}</td>
            <td>{{ defect.contractRef || defect.contractId }}</td>
            <td>{{ defect.location }}</td>
            <td><span :class="['priority-badge', defect.priority]">{{ defect.priority }}</span></td>
            <td><span :class="['status-badge', defect.status]">{{ defect.status }}</span></td>
            <td>{{ defect.assignedOperative || defect.assignedContractor || '-' }}</td>
            <td class="actions-cell">
              <button class="btn-icon" title="View" @click="viewDefect(defect)"><View /></button>
              <button class="btn-icon" title="Edit" @click="editDefect(defect)"><Edit /></button>
              <button class="btn-icon" title="Update Status" @click="updateDefectStatus(defect)"><Refresh /></button>
              <button class="btn-icon btn-danger" title="Delete" @click="deleteDefect(defect.id)"><Delete /></button>
            </td>
          </tr>
          <tr v-if="defects.length === 0">
            <td colspan="7" class="empty-row">No defects found</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- NHBC/LABC Sign-offs Tab -->
    <div v-if="activeTab === 'signoffs'" class="tab-content">
      <div class="filters-bar">
        <select v-model="signOffFilters.contractId" @change="loadSignOffs">
          <option value="">All Contracts</option>
          <option v-for="c in contracts" :key="c.id" :value="c.id">{{ c.contractRef }}</option>
        </select>
        <select v-model="signOffFilters.buildingControlType" @change="loadSignOffs">
          <option value="">All Types</option>
          <option value="nhbc">NHBC</option>
          <option value="labc">LABC</option>
          <option value="local_authority">Local Authority</option>
        </select>
        <select v-model="signOffFilters.result" @change="loadSignOffs">
          <option value="">All Results</option>
          <option value="pending">Pending</option>
          <option value="approved">Approved</option>
          <option value="conditions">Conditions</option>
          <option value="refused">Refused</option>
        </select>
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>Reference</th>
            <th>Contract</th>
            <th>Type</th>
            <th>Inspection</th>
            <th>Inspector</th>
            <th>Date</th>
            <th>Result</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="signOff in signOffs" :key="signOff.id">
            <td>{{ signOff.referenceNumber || signOff.id }}</td>
            <td>{{ signOff.contractRef || signOff.contractId }}</td>
            <td>{{ signOff.buildingControlType?.toUpperCase() }}</td>
            <td>{{ signOff.inspectionType }}</td>
            <td>{{ signOff.inspectorName || '-' }}</td>
            <td>{{ signOff.inspectionDate }}</td>
            <td><span :class="['status-badge', signOff.result]">{{ signOff.result }}</span></td>
            <td class="actions-cell">
              <button class="btn-icon" title="View" @click="viewSignOff(signOff)"><View /></button>
              <button class="btn-icon" title="Edit" @click="editSignOff(signOff)"><Edit /></button>
              <button v-if="signOff.result === 'pending'" class="btn-icon btn-success" title="Approve" @click="approveSignOff(signOff)"><Check /></button>
              <button v-if="signOff.result === 'pending'" class="btn-icon btn-warning" title="Refuse" @click="refuseSignOff(signOff)"><Close /></button>
              <button class="btn-icon btn-danger" title="Delete" @click="deleteSignOff(signOff.id)"><Delete /></button>
            </td>
          </tr>
          <tr v-if="signOffs.length === 0">
            <td colspan="8" class="empty-row">No sign-offs found</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create/Edit Dialogs -->
    <div v-if="showDialog" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h2>{{ dialogMode === 'create' ? 'Create' : 'Edit' }} {{ tabLabels[activeTab as keyof typeof tabLabels] }}</h2>
          <button class="dialog-close" @click="closeDialog">&times;</button>
        </div>

        <!-- Template Dialog -->
        <div v-if="activeTab === 'templates'" class="dialog-content">
          <div class="form-group">
            <label>Name *</label>
            <input v-model="templateForm.name" type="text" required />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Category</label>
              <input v-model="templateForm.category" type="text" />
            </div>
            <div class="form-group">
              <label>Trade Category</label>
              <select v-model="templateForm.tradeCategory">
                <option value="">Select...</option>
                <option value="concrete">Concrete</option>
                <option value="drainage">Drainage</option>
                <option value="brickwork">Brickwork</option>
                <option value="structure">Structure</option>
                <option value="finishes">Finishes</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea v-model="templateForm.description" rows="3"></textarea>
          </div>

          <div class="items-section">
            <h3>Inspection Items</h3>
            <div v-for="(item, index) in templateForm.items" :key="index" class="item-row">
              <input v-model="item.description" type="text" placeholder="Description" class="item-desc" />
              <select v-model="item.inspectionType" class="item-type">
                <option value="witness">Witness</option>
                <option value="hold">Hold</option>
                <option value="monitor">Monitor</option>
              </select>
              <input v-model="item.responsibleParty" type="text" placeholder="Responsible Party" class="item-responsible" />
              <button class="btn-remove" @click="removeTemplateItem(index)">-</button>
            </div>
            <button class="btn-add-item" @click="addTemplateItem">+ Add Item</button>
          </div>
        </div>

        <!-- Schedule Dialog -->
        <div v-if="activeTab === 'schedules'" class="dialog-content">
          <div class="form-group">
            <label>Title *</label>
            <input v-model="scheduleForm.title" type="text" required />
          </div>
          <div class="form-group">
            <label>Contract *</label>
            <select v-model="scheduleForm.contractId" required>
              <option value="">Select contract...</option>
              <option v-for="c in contracts" :key="c.id" :value="c.id">{{ c.contractRef }}</option>
            </select>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Start Date</label>
              <input v-model="scheduleForm.startDate" type="date" />
            </div>
            <div class="form-group">
              <label>Due Date</label>
              <input v-model="scheduleForm.dueDate" type="date" />
            </div>
          </div>
          <div class="form-group">
            <label>Assigned Inspector</label>
            <input v-model="scheduleForm.assignedInspector" type="text" />
          </div>
          <div class="form-group">
            <label>From Template</label>
            <select v-model="scheduleForm.templateId">
              <option value="">Select template (optional)</option>
              <option v-for="t in templates" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Notes</label>
            <textarea v-model="scheduleForm.notes" rows="3"></textarea>
          </div>
        </div>

        <!-- Inspection Record Dialog -->
        <div v-if="activeTab === 'inspections'" class="dialog-content">
          <div class="form-group">
            <label>Title *</label>
            <input v-model="inspectionForm.title" type="text" required />
          </div>
          <div class="form-group">
            <label>Schedule Item *</label>
            <select v-model="inspectionForm.scheduleItemId" required>
              <option value="">Select schedule item...</option>
              <option v-for="s in schedules" :key="s.id" :value="s.id">{{ s.title }}</option>
            </select>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Inspector Name *</label>
              <input v-model="inspectionForm.inspectorName" type="text" required />
            </div>
            <div class="form-group">
              <label>Inspection Date *</label>
              <input v-model="inspectionForm.inspectionDate" type="date" required />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Result *</label>
              <select v-model="inspectionForm.result" required>
                <option value="">Select result...</option>
                <option value="pass">Pass</option>
                <option value="fail">Fail</option>
                <option value="nc">NC (Non-Conformance)</option>
                <option value="conditional">Conditional</option>
              </select>
            </div>
            <div class="form-group">
              <label>Time</label>
              <input v-model="inspectionForm.inspectionTime" type="time" />
            </div>
          </div>
          <div class="form-group">
            <label>Findings</label>
            <textarea v-model="inspectionForm.findings" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>Notes</label>
            <textarea v-model="inspectionForm.notes" rows="2"></textarea>
          </div>
        </div>

        <!-- Defect Dialog -->
        <div v-if="activeTab === 'defects'" class="dialog-content">
          <div class="form-group">
            <label>Title *</label>
            <input v-model="defectForm.title" type="text" required />
          </div>
          <div class="form-group">
            <label>Contract *</label>
            <select v-model="defectForm.contractId" required>
              <option value="">Select contract...</option>
              <option v-for="c in contracts" :key="c.id" :value="c.id">{{ c.contractRef }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Location</label>
            <input v-model="defectForm.location" type="text" placeholder="e.g., Plot 5 - Kitchen Wall" />
          </div>
          <div class="form-group">
            <label>Description *</label>
            <textarea v-model="defectForm.description" rows="3" required></textarea>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Priority</label>
              <select v-model="defectForm.priority">
                <option value="low">Low</option>
                <option value="medium">Medium</option>
                <option value="high">High</option>
                <option value="critical">Critical</option>
              </select>
            </div>
            <div class="form-group">
              <label>Status</label>
              <select v-model="defectForm.status">
                <option value="open">Open</option>
                <option value="in_progress">In Progress</option>
                <option value="resolved">Resolved</option>
                <option value="closed">Closed</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Identified Date</label>
              <input v-model="defectForm.identifiedDate" type="date" />
            </div>
            <div class="form-group">
              <label>Due Date</label>
              <input v-model="defectForm.dueDate" type="date" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Assigned Operative</label>
              <input v-model="defectForm.assignedOperative" type="text" />
            </div>
            <div class="form-group">
              <label>Assigned Contractor</label>
              <input v-model="defectForm.assignedContractor" type="text" />
            </div>
          </div>
          <div class="form-group">
            <label>Root Cause</label>
            <textarea v-model="defectForm.rootCause" rows="2"></textarea>
          </div>
          <div class="form-group">
            <label>Resolution Details</label>
            <textarea v-model="defectForm.resolutionDetails" rows="2"></textarea>
          </div>
          <div class="form-row">
            <div class="form-group checkbox-group">
              <label><input v-model="defectForm.reinspectionRequired" type="checkbox" /> Re-inspection Required</label>
            </div>
            <div v-if="defectForm.reinspectionRequired" class="form-group">
              <label>Re-inspection Date</label>
              <input v-model="defectForm.reinspectionDate" type="date" />
            </div>
          </div>
          <div class="form-group">
            <label>NC Reference</label>
            <input v-model="defectForm.ncReference" type="text" />
          </div>
        </div>

        <!-- Sign-off Dialog -->
        <div v-if="activeTab === 'signoffs'" class="dialog-content">
          <div class="form-group">
            <label>Contract *</label>
            <select v-model="signOffForm.contractId" required>
              <option value="">Select contract...</option>
              <option v-for="c in contracts" :key="c.id" :value="c.id">{{ c.contractRef }}</option>
            </select>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Building Control Type *</label>
              <select v-model="signOffForm.buildingControlType" required>
                <option value="">Select type...</option>
                <option value="nhbc">NHBC</option>
                <option value="labc">LABC</option>
                <option value="local_authority">Local Authority</option>
              </select>
            </div>
            <div class="form-group">
              <label>Inspection Type *</label>
              <select v-model="signOffForm.inspectionType" required>
                <option value="">Select...</option>
                <option value="foundation">Foundation</option>
                <option value="structure">Structure</option>
                <option value="first_fix">First Fix</option>
                <option value="second_fix">Second Fix</option>
                <option value="completion">Completion</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>Reference Number</label>
            <input v-model="signOffForm.referenceNumber" type="text" />
          </div>
          <div class="form-group">
            <label>Inspector Name</label>
            <input v-model="signOffForm.inspectorName" type="text" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Inspector Email</label>
              <input v-model="signOffForm.inspectorEmail" type="email" />
            </div>
            <div class="form-group">
              <label>Inspector Phone</label>
              <input v-model="signOffForm.inspectorPhone" type="tel" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Inspection Date</label>
              <input v-model="signOffForm.inspectionDate" type="date" />
            </div>
            <div class="form-group">
              <label>Next Inspection Date</label>
              <input v-model="signOffForm.nextInspectionDate" type="date" />
            </div>
          </div>
          <div class="form-group">
            <label>Result</label>
            <select v-model="signOffForm.result">
              <option value="pending">Pending</option>
              <option value="approved">Approved</option>
              <option value="conditions">Conditions</option>
              <option value="refused">Refused</option>
            </select>
          </div>
          <div class="form-group">
            <label>Conditions/Notes</label>
            <textarea v-model="signOffForm.conditionsOrNotes" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>Report Number</label>
            <input v-model="signOffForm.reportNumber" type="text" />
          </div>
          <div class="form-group">
            <label>Notes</label>
            <textarea v-model="signOffForm.notes" rows="2"></textarea>
          </div>
        </div>

        <div class="dialog-footer">
          <button class="btn-secondary" @click="closeDialog">Cancel</button>
          <button class="btn-primary" @click="saveDialog">{{ dialogMode === 'create' ? 'Create' : 'Update' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, View, CopyDocument, Refresh, Check, Close } from '@element-plus/icons-vue'
import api, { type TemplateStatus, type DefectPriority, type DefectStatus } from '@/services/api'

// State
const activeTab = ref('templates')
const tabLabels = {
  templates: 'ITP Template',
  schedules: 'ITP Schedule',
  inspections: 'Inspection Record',
  defects: 'Defect',
  signoffs: 'Sign-off'
}

const showDialog = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const currentId = ref<string | null>(null)

// Data
const templates = ref<any[]>([])
const schedules = ref<any[]>([])
const inspections = ref<any[]>([])
const defects = ref<any[]>([])
const signOffs = ref<any[]>([])
const contracts = ref<any[]>([])

// Pagination
const templatePage = ref(0)

// Filters
const templateFilters = reactive({ status: '', tradeCategory: '', search: '' })
const scheduleFilters = reactive({ contractId: '', status: '' })
const inspectionFilters = reactive({ result: '', inspectorName: '' })
const defectFilters = reactive({ contractId: '', status: '', priority: '' })
const signOffFilters = reactive({ contractId: '', buildingControlType: '', result: '' })

// Forms
const templateForm = reactive({
  name: '',
  description: '',
  category: '',
  tradeCategory: '',
  status: 'draft' as TemplateStatus,
  items: [] as any[]
})

const scheduleForm = reactive({
  title: '',
  contractId: '',
  templateId: '',
  startDate: '',
  dueDate: '',
  assignedInspector: '',
  notes: ''
})

const inspectionForm = reactive({
  title: '',
  scheduleItemId: '',
  inspectorName: '',
  inspectionDate: '',
  inspectionTime: '',
  result: '',
  findings: '',
  notes: ''
})

const defectForm = reactive({
  title: '',
  contractId: '',
  description: '',
  location: '',
  priority: 'medium' as DefectPriority,
  status: 'open' as DefectStatus,
  identifiedDate: '',
  dueDate: '',
  assignedOperative: '',
  assignedContractor: '',
  rootCause: '',
  resolutionDetails: '',
  reinspectionRequired: false,
  reinspectionDate: '',
  ncReference: ''
})

const signOffForm = reactive({
  contractId: '',
  buildingControlType: '',
  inspectionType: '',
  referenceNumber: '',
  inspectorName: '',
  inspectorEmail: '',
  inspectorPhone: '',
  inspectionDate: '',
  nextInspectionDate: '',
  result: 'pending',
  conditionsOrNotes: '',
  reportNumber: '',
  notes: ''
})

// Methods
const loadTemplates = async () => {
  try {
    const params: any = { page: templatePage.value, limit: 20 }
    if (templateFilters.status) params.status = templateFilters.status
    if (templateFilters.tradeCategory) params.tradeCategory = templateFilters.tradeCategory
    const response = await api.quality.getITPTemplates(params)
    templates.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('Failed to load templates:', error)
  }
}

const loadSchedules = async () => {
  try {
    const params: any = {}
    if (scheduleFilters.contractId) params.contractId = scheduleFilters.contractId
    if (scheduleFilters.status) params.status = scheduleFilters.status
    const response = await api.quality.getITPSchedules(params)
    schedules.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('Failed to load schedules:', error)
  }
}

const loadInspections = async () => {
  try {
    const params: any = {}
    if (inspectionFilters.result) params.result = inspectionFilters.result
    if (inspectionFilters.inspectorName) params.inspectorName = inspectionFilters.inspectorName
    const response = await api.quality.getInspections(params)
    inspections.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('Failed to load inspections:', error)
  }
}

const loadDefects = async () => {
  try {
    const params: any = {}
    if (defectFilters.contractId) params.contractId = defectFilters.contractId
    if (defectFilters.status) params.status = defectFilters.status
    if (defectFilters.priority) params.priority = defectFilters.priority
    const response = await api.quality.getDefects(params)
    defects.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('Failed to load defects:', error)
  }
}

const loadSignOffs = async () => {
  try {
    const params: any = {}
    if (signOffFilters.contractId) params.contractId = signOffFilters.contractId
    if (signOffFilters.buildingControlType) params.buildingControlType = signOffFilters.buildingControlType
    if (signOffFilters.result) params.result = signOffFilters.result
    const response = await api.quality.getSignOffs(params)
    signOffs.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('Failed to load sign-offs:', error)
  }
}

const loadContracts = async () => {
  try {
    const response = await api.contracts.getAll({ limit: 100 })
    contracts.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('Failed to load contracts:', error)
  }
}

const openCreateDialog = () => {
  dialogMode.value = 'create'
  currentId.value = null
  resetForms()
  showDialog.value = true
}

const closeDialog = () => {
  showDialog.value = false
}

const resetForms = () => {
  Object.assign(templateForm, { name: '', description: '', category: '', tradeCategory: '', status: 'draft', items: [] })
  Object.assign(scheduleForm, { title: '', contractId: '', templateId: '', startDate: '', dueDate: '', assignedInspector: '', notes: '' })
  Object.assign(inspectionForm, { title: '', scheduleItemId: '', inspectorName: '', inspectionDate: '', inspectionTime: '', result: '', findings: '', notes: '' })
  Object.assign(defectForm, { title: '', contractId: '', description: '', location: '', priority: 'medium', status: 'open', identifiedDate: '', dueDate: '', assignedOperative: '', assignedContractor: '', rootCause: '', resolutionDetails: '', reinspectionRequired: false, reinspectionDate: '', ncReference: '' })
  Object.assign(signOffForm, { contractId: '', buildingControlType: '', inspectionType: '', referenceNumber: '', inspectorName: '', inspectorEmail: '', inspectorPhone: '', inspectionDate: '', nextInspectionDate: '', result: 'pending', conditionsOrNotes: '', reportNumber: '', notes: '' })
}

const addTemplateItem = () => {
  templateForm.items.push({
    sequence: templateForm.items.length + 1,
    description: '',
    inspectionType: 'witness',
    responsibleParty: '',
    notes: '',
    frequency: '',
    requiredEvidence: ''
  })
}

const removeTemplateItem = (index: number) => {
  templateForm.items.splice(index, 1)
}

const saveDialog = async () => {
  try {
    if (activeTab.value === 'templates') {
      if (dialogMode.value === 'create') {
        await api.quality.createITPTemplate(templateForm)
      } else {
        await api.quality.updateITPTemplate(currentId.value!, templateForm)
      }
      await loadTemplates()
    } else if (activeTab.value === 'schedules') {
      if (dialogMode.value === 'create') {
        await api.quality.createITPSchedule(scheduleForm)
      } else {
        await api.quality.updateITPSchedule(currentId.value!, scheduleForm)
      }
      await loadSchedules()
    } else if (activeTab.value === 'inspections') {
      if (dialogMode.value === 'create') {
        await api.quality.createInspection(inspectionForm)
      } else {
        await api.quality.updateInspection(currentId.value!, inspectionForm)
      }
      await loadInspections()
    } else if (activeTab.value === 'defects') {
      if (dialogMode.value === 'create') {
        await api.quality.createDefect(defectForm)
      } else {
        await api.quality.updateDefect(currentId.value!, defectForm)
      }
      await loadDefects()
    } else if (activeTab.value === 'signoffs') {
      if (dialogMode.value === 'create') {
        await api.quality.createSignOff(signOffForm)
      } else {
        await api.quality.updateSignOff(currentId.value!, signOffForm)
      }
      await loadSignOffs()
    }
    closeDialog()
  } catch (error) {
    console.error('Failed to save:', error)
    ElMessage.error('Failed to save. Please try again.')
  }
}

// Edit methods
const editTemplate = (template: any) => {
  dialogMode.value = 'edit'
  currentId.value = template.id
  Object.assign(templateForm, {
    name: template.name,
    description: template.description,
    category: template.category,
    tradeCategory: template.tradeCategory,
    status: template.status,
    items: template.items || []
  })
  showDialog.value = true
}

const editSchedule = (schedule: any) => {
  dialogMode.value = 'edit'
  currentId.value = schedule.id
  Object.assign(scheduleForm, {
    title: schedule.title,
    contractId: schedule.contractId,
    templateId: schedule.templateId,
    startDate: schedule.startDate,
    dueDate: schedule.dueDate,
    assignedInspector: schedule.assignedInspector,
    notes: schedule.notes
  })
  showDialog.value = true
}

const editInspection = (inspection: any) => {
  dialogMode.value = 'edit'
  currentId.value = inspection.id
  Object.assign(inspectionForm, {
    title: inspection.title,
    scheduleItemId: inspection.scheduleItemId,
    inspectorName: inspection.inspectorName,
    inspectionDate: inspection.inspectionDate,
    inspectionTime: inspection.inspectionTime,
    result: inspection.result,
    findings: inspection.findings,
    notes: inspection.notes
  })
  showDialog.value = true
}

const editDefect = (defect: any) => {
  dialogMode.value = 'edit'
  currentId.value = defect.id
  Object.assign(defectForm, {
    title: defect.title,
    contractId: defect.contractId,
    description: defect.description,
    location: defect.location,
    priority: defect.priority,
    status: defect.status,
    identifiedDate: defect.identifiedDate,
    dueDate: defect.dueDate,
    assignedOperative: defect.assignedOperative,
    assignedContractor: defect.assignedContractor,
    rootCause: defect.rootCause,
    resolutionDetails: defect.resolutionDetails,
    reinspectionRequired: defect.reinspectionRequired,
    reinspectionDate: defect.reinspectionDate,
    ncReference: defect.ncReference
  })
  showDialog.value = true
}

const editSignOff = (signOff: any) => {
  dialogMode.value = 'edit'
  currentId.value = signOff.id
  Object.assign(signOffForm, {
    contractId: signOff.contractId,
    buildingControlType: signOff.buildingControlType,
    inspectionType: signOff.inspectionType,
    referenceNumber: signOff.referenceNumber,
    inspectorName: signOff.inspectorName,
    inspectorEmail: signOff.inspectorEmail,
    inspectorPhone: signOff.inspectorPhone,
    inspectionDate: signOff.inspectionDate,
    nextInspectionDate: signOff.nextInspectionDate,
    result: signOff.result,
    conditionsOrNotes: signOff.conditionsOrNotes,
    reportNumber: signOff.reportNumber,
    notes: signOff.notes
  })
  showDialog.value = true
}

// View methods (simplified - just edit for now)
const viewTemplate = (template: any) => editTemplate(template)
const viewSchedule = (schedule: any) => editSchedule(schedule)
const viewInspection = (inspection: any) => editInspection(inspection)
const viewDefect = (defect: any) => editDefect(defect)
const viewSignOff = (signOff: any) => editSignOff(signOff)

// Delete methods
const deleteTemplate = async (id: string) => {
  try {
    await ElMessageBox.confirm('Delete this template?', 'Confirm', { type: 'warning' })
    await api.quality.deleteITPTemplate(id)
    await loadTemplates()
  } catch { /* cancelled */ }
}

const deleteSchedule = async (id: string) => {
  try {
    await ElMessageBox.confirm('Delete this schedule?', 'Confirm', { type: 'warning' })
    await api.quality.deleteITPSchedule(id)
    await loadSchedules()
  } catch { /* cancelled */ }
}

const deleteInspection = async (id: string) => {
  try {
    await ElMessageBox.confirm('Delete this inspection?', 'Confirm', { type: 'warning' })
    await api.quality.deleteInspection(id)
    await loadInspections()
  } catch { /* cancelled */ }
}

const deleteDefect = async (id: string) => {
  try {
    await ElMessageBox.confirm('Delete this defect?', 'Confirm', { type: 'warning' })
    await api.quality.deleteDefect(id)
    await loadDefects()
  } catch { /* cancelled */ }
}

const deleteSignOff = async (id: string) => {
  try {
    await ElMessageBox.confirm('Delete this sign-off?', 'Confirm', { type: 'warning' })
    await api.quality.deleteSignOff(id)
    await loadSignOffs()
  } catch { /* cancelled */ }
}

// Copy template
const copyTemplate = async (id: string) => {
  try {
    await api.quality.copyITPTemplate(id)
    await loadTemplates()
  } catch (error) {
    console.error('Failed to copy template:', error)
  }
}

// Create schedule from template
const createScheduleFromTemplate = async (templateId: string, contractId: string) => {
  try {
    await api.quality.createITPScheduleFromTemplate(templateId, contractId)
    await loadSchedules()
  } catch (error) {
    console.error('Failed to create schedule from template:', error)
  }
}

// Update defect status
const updateDefectStatus = async (defect: any) => {
  try {
    const { value: newStatus } = await ElMessageBox.prompt(
      'Enter new status (open/in_progress/resolved/closed):',
      'Update Status',
      { inputPattern: /^(open|in_progress|resolved|closed)$/, inputErrorMessage: 'Invalid status' }
    )
    if (newStatus) {
      await api.quality.updateDefectStatus(defect.id, newStatus)
      await loadDefects()
    }
  } catch { /* cancelled */ }
}

// Approve/Refuse sign-off
const approveSignOff = async (signOff: any) => {
  try {
    await ElMessageBox.confirm('Approve this sign-off?', 'Confirm', { type: 'success' })
    const { value: signature } = await ElMessageBox.prompt('Enter signature (or leave empty):', 'Signature', { showCancelButton: true, inputValue: '' }).catch(() => ({ value: '' }))
    await api.quality.approveSignOff(signOff.id, signature || '')
    await loadSignOffs()
  } catch { /* cancelled */ }
}

const refuseSignOff = async (signOff: any) => {
  try {
    const { value: conditions } = await ElMessageBox.prompt('Enter refusal conditions:', 'Refuse Sign-off', { inputPattern: /.+/, inputErrorMessage: 'Reason required' })
    if (conditions) {
      await api.quality.refuseSignOff(signOff.id, conditions)
      await loadSignOffs()
    }
  } catch { /* cancelled */ }
}

// Debounced search
let debounceTimer: any = null
const debouncedLoadTemplates = () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(loadTemplates, 500)
}

const debouncedLoadInspections = () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(loadInspections, 500)
}

// Lifecycle
onMounted(async () => {
  await loadContracts()
  await loadTemplates()
})
</script>

<style scoped>
.quality-view {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.tab-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.filters-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filters-bar select,
.filters-bar input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.search-input {
  min-width: 200px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.data-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.data-table tbody tr:hover {
  background: #f9fafb;
}

.empty-row {
  text-align: center;
  color: #6b7280;
  padding: 40px !important;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.draft { background: #fef3c7; color: #92400e; }
.status-badge.active { background: #d1fae5; color: #065f46; }
.status-badge.archived { background: #e5e7eb; color: #6b7280; }
.status-badge.pending { background: #fef3c7; color: #92400e; }
.status-badge.in_progress { background: #dbeafe; color: #1e40af; }
.status-badge.completed { background: #d1fae5; color: #065f46; }
.status-badge.failed { background: #fee2e2; color: #991b1b; }
.status-badge.pass { background: #d1fae5; color: #065f46; }
.status-badge.fail { background: #fee2e2; color: #991b1b; }
.status-badge.nc { background: #fee2e2; color: #991b1b; }
.status-badge.conditional { background: #fef3c7; color: #92400e; }
.status-badge.approved { background: #d1fae5; color: #065f46; }
.status-badge.conditions { background: #fef3c7; color: #92400e; }
.status-badge.refused { background: #fee2e2; color: #991b1b; }

.priority-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.priority-badge.low { background: #d1fae5; color: #065f46; }
.priority-badge.medium { background: #fef3c7; color: #92400e; }
.priority-badge.high { background: #fed7aa; color: #c2410c; }
.priority-badge.critical { background: #fee2e2; color: #991b1b; }

.actions-cell {
  display: flex;
  gap: 4px;
}

.btn-icon {
  padding: 6px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: #6b7280;
  border-radius: 4px;
}

.btn-icon:hover {
  background: #f3f4f6;
  color: #374151;
}

.btn-icon.btn-danger:hover {
  background: #fee2e2;
  color: #991b1b;
}

.btn-icon.btn-success:hover {
  background: #d1fae5;
  color: #065f46;
}

.btn-icon.btn-warning:hover {
  background: #fef3c7;
  color: #92400e;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
}

.pagination button {
  padding: 8px 16px;
  background: #f9fafb;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Dialog styles */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.dialog-header h2 {
  font-size: 18px;
  font-weight: 600;
}

.dialog-close {
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
  color: #6b7280;
}

.dialog-content {
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #e5e7eb;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.items-section {
  margin-top: 16px;
}

.items-section h3 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.item-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.item-row input,
.item-row select {
  padding: 8px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.item-desc {
  flex: 2;
}

.item-type {
  width: 120px;
}

.item-responsible {
  flex: 1;
}

.btn-remove {
  width: 32px;
  background: #fee2e2;
  border: none;
  border-radius: 6px;
  color: #991b1b;
  cursor: pointer;
  font-weight: bold;
}

.btn-add-item {
  padding: 8px 16px;
  background: #f3f4f6;
  border: 1px dashed #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  color: #374151;
  width: 100%;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.btn-primary:hover {
  background: #1d4ed8;
}

.btn-secondary {
  padding: 8px 16px;
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.btn-secondary:hover {
  background: #f9fafb;
}

.icon {
  width: 20px;
  height: 20px;
}

.checkbox-group {
  display: flex;
  align-items: center;
}

.checkbox-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-group input[type="checkbox"] {
  width: auto;
}
</style>