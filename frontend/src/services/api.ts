import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// Shared Element Plus tag type
export type ElTagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

// Base API instance
export const apiClient: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor - add JWT token
apiClient.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor - handle errors and 401
apiClient.interceptors.response.use(
  (response) => {
    // Unwrap ApiResponse envelope { success, message, data } → data
    if (response.data && typeof response.data === 'object' && 'success' in response.data) {
      const inner = response.data.data
      // Normalize Spring PageResponse { content, totalElements, ... } so that
      // existing view code using .data and .total still works alongside .content
      if (inner && typeof inner === 'object' && 'content' in inner && 'totalElements' in inner) {
        response.data = { ...inner, data: inner.content, total: inner.totalElements }
      } else {
        response.data = inner
      }
    }
    return response
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean }

    // Handle 403 MUST_CHANGE_PASSWORD before any retry logic
    if (error.response?.status === 403) {
      const data = error.response.data as any
      if (data?.code === 'MUST_CHANGE_PASSWORD') {
        router.push('/change-password')
        return Promise.reject(error)
      }
    }

    // Handle 401 - unauthorized
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      const authStore = useAuthStore()

      // Try to refresh token
      if (authStore.refreshToken) {
        try {
          const refreshed = await authStore.refreshTokenFn()
          if (refreshed && originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${authStore.token}`
            return apiClient(originalRequest)
          }
        } catch {
          // Refresh failed
        }
      }

      // Logout and redirect to login
      await authStore.logout()
      router.push('/login')
      ElMessage.error('Session expired. Please login again.')
    }

    // Handle other errors
    const message = (error.response?.data as any)?.message || error.message || 'An error occurred'

    if (error.response?.status !== 401) {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

// Types
export interface Company {
  id: string
  name: string
  companyType: 'client' | 'subcontractor' | 'supplier' | 'consultant' | 'other'
  registrationNumber?: string
  vatNumber?: string
  address: Address
  phone?: string
  email?: string
  cisStatus?: 'verified' | 'pending' | 'expired' | 'not_applicable'
  cisRate?: number
  subbieGateStatus?: 'ready' | 'gate_red'
  bankDetails?: BankDetails
  status: 'active' | 'inactive'
  createdAt: string
  updatedAt: string
}

export interface Address {
  addressLine1: string
  addressLine2?: string
  city: string
  county?: string
  postcode: string
  country?: string
}

export interface BankDetails {
  bankName: string
  sortCode: string
  accountNumber: string
  accountName: string
}

export interface Contact {
  id: string
  companyId: string
  firstName: string
  lastName: string
  email: string
  phone?: string
  position?: string
  isPrimary: boolean
  status: 'active' | 'inactive'
}

export interface Site {
  id: string
  name: string
  siteCode: string
  address: Address
  clientId: string
  client?: Company
  gridReference?: string
  status: 'planning' | 'active' | 'completed' | 'on_hold'
  startDate?: string
  estimatedCompletion?: string
  actualCompletion?: string
  createdAt: string
  updatedAt: string
}

export interface Tender {
  id: string
  title: string
  clientId: string
  client?: Company
  siteId?: string
  site?: Site
  valueMin?: number
  valueMax?: number
  returnDate?: string
  winProbability?: number
  stage: 'lead' | 'qualified' | 'pricing' | 'submitted' | 'negotiation' | 'awarded' | 'lost'
  lostReason?: string
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface Contract {
  id: string
  reference: string
  title: string
  clientId: string
  client?: Company
  siteId: string
  site?: Site
  contractValue: number
  retentionPercentage: number
  paymentTerms: string
  contractForm: string
  startDate: string
  endDate?: string
  status: 'draft' | 'active' | 'completed' | 'terminated'
  variations: Variation[]
  applications: Application[]
  createdAt: string
  updatedAt: string
}

export interface Variation {
  id: string
  contractId: string
  reference: string
  description: string
  value: number
  status: 'pending' | 'approved' | 'rejected'
  createdAt: string
}

export interface Application {
  id: string
  contractId: string
  applicationNumber: number
  periodStart: string
  periodEnd: string
  applicationValue: number
  retentionAmount: number
  status: 'draft' | 'submitted' | 'measured' | 'agreed' | 'paid'
  submittedAt?: string
  paidAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface ApplicationResponse {
  id: number
  applicationRef: string
  applicationNumber: number
  contractId: number
  contractRef?: string
  applicationPeriodStart: string
  applicationPeriodEnd: string
  dueDate?: string
  valueOfWorks: number
  retention: number
  grossValue: number
  status: string
  submittedDate?: string
  payerRef?: string
}

export interface ApplicationForPaymentRequest {
  applicationPeriodStart: string
  applicationPeriodEnd: string
  valueOfWorks: number
  retention?: number
  description?: string
}

export interface Subcontractor extends Company {
  cisVerificationStatus?: 'verified' | 'pending' | 'expired'
  cisVerificationDate?: string
  cis300Submitted?: boolean
  cis300SubmitDate?: string
  insuranceExpiry?: string
  notes?: string
}

export interface Operative {
  id: string
  firstName: string
  lastName: string
  employeeRef?: string
  employerId?: string
  employer?: Company
  niNumber?: string
  dateOfBirth?: string
  trade?: string
  email?: string
  phone?: string
  cscsCard?: CSCSCard
  qualifications: Qualification[]
  inductionStatus: 'pending' | 'complete' | 'expired'
  status: 'active' | 'inactive' | 'suspended'
  createdAt: string
  updatedAt: string
}

export interface CSCSCard {
  id?: string
  operativeId?: string
  cardNumber: string
  cardType: string
  expiryDate: string
  photoUrl?: string
  verified?: boolean
}

export interface Qualification {
  id: string
  name: string
  level?: string
  expiryDate?: string
}

export interface PlantItem {
  id: string
  plantRef: string
  description: string
  category: 'EXCAVATOR' | 'DUMPER' | 'ROLLER' | 'PLANT_MIXER' | 'CONCRETE_PUMP' | 'TELEHANDLER' | 'CRANE' | 'ACCESS_EQUIPMENT' | 'OTHER'
  make?: string
  model?: string
  serialNumber?: string
  hireRate?: number
  status: 'AVAILABLE' | 'HIRED_OUT' | 'MAINTENANCE' | 'DECOMMISSIONED'
  insuranceExpiry?: string
  nextLOLER?: string
  nextPUWER?: string
  notes?: string
  siteAllocations: SiteAllocation[]
  createdAt: string
  updatedAt: string
}

export interface SiteAllocation {
  id: string
  plantId: string
  siteId: string
  site?: Site
  startDate: string
  endDate?: string
  status: 'planned' | 'active' | 'completed'
}

export interface Requisition {
  id: string
  requisitionRef: string
  requestedBy: string
  siteId: string
  site?: Site
  status: 'draft' | 'submitted' | 'approved' | 'rejected' | 'converted'
  requiredDate: string
  notes?: string
  items: RequisitionItem[]
  total: number
  createdAt: string
}

export interface RequisitionItem {
  id?: string
  description: string
  quantity: number
  unit: string
  estimatedCost: number
}

export interface PurchaseOrder {
  id: string
  poNumber: string
  supplierId: string
  supplier?: Company
  siteId: string
  site?: Site
  orderDate?: string
  deliveryDate?: string
  deliveryAddress?: string
  notes?: string
  status: 'draft' | 'sent' | 'acknowledged' | 'partial' | 'received' | 'cancelled'
  items: POItem[]
  total: number
  createdAt: string
}

export interface POItem {
  id?: string
  description: string
  quantity: number
  unit: string
  unitCost: number
  delivered?: number
}

export interface DeliveryNote {
  id: string
  noteNumber: string
  poId?: string
  supplierId: string
  supplier?: Company
  siteId: string
  site?: Site
  type: 'standard' | 'concrete_ticket' | 'muckaway_ticket'
  items: DeliveryNoteItem[]
  total: number
  deliveredAt: string
}

export interface DeliveryNoteItem {
  description: string
  quantity: number
  unit: string
  notes?: string
}

export interface Document {
  id: string
  filename: string
  mimeType: string
  size: number
  uploadedBy: string
  category?: string
  metadata?: Record<string, any>
  downloadUrl: string
  versions: DocumentVersion[]
  createdAt: string
}

export interface DocumentVersion {
  version: number
  uploadedAt: string
  uploadedBy: string
  downloadUrl: string
}

export interface HealthSafetyRecord {
  id: string
  type: 'f10' | 'cpp' | 'rams' | 'permit' | 'incident'
  title: string
  siteId?: string
  site?: Site
  contractId?: string
  status: string
  data: Record<string, any>
  createdAt: string
  updatedAt: string
}

export interface RAMS extends HealthSafetyRecord {
  type: 'rams'
  data: {
    methodStatement: string
    riskAssessments: string[]
    ppeRequired: string[]
    signOnRequired: boolean
  }
}

export interface PermitToDig extends HealthSafetyRecord {
  type: 'permit'
  data: {
    location: string
    startDate: string
    endDate: string
    excavations: string[]
    services: string[]
    status: 'draft' | 'prechecked' | 'issued' | 'completed'
  }
}

export interface Incident extends HealthSafetyRecord {
  type: 'incident'
  data: {
    severity: 'near_miss' | 'minor' | 'major' | 'fatal'
    description: string
    location: string
    date: string
    personsInvolved: string[]
    witnesses: string[]
    actionTaken: string
    rootCause: string
    correctiveAction: string
  }
}

export interface AdoptionCase {
  id: string
  caseRef: string
  type: 's38' | 's278' | 's104'
  title: string
  clientId: string
  contractId?: string
  client?: Company
  laWaterAuthority?: string
  status: 'pre_application' | 'application' | 'technical_approval' | 'under_construction' | 'adopted' | 'rejected'
  bondValue?: number
  bondReleaseDate?: string
  stages: AdoptionStage[]
  createdAt: string
  updatedAt: string
}

export interface AdoptionStage {
  id: string
  name: string
  status: 'pending' | 'in_progress' | 'completed'
  completedAt?: string
  notes?: string
}

// Bond Types
export interface Bond {
  id: string
  caseId: string
  case?: AdoptionCase
  bondReference: string
  bondType: 'maintenance' | 'performance' | 'parent_company' | 'insurance'
  issuingBank?: string
  bondAmount: number
  effectiveDate: string
  expiryDate?: string
  releaseDate?: string
  status: 'pending' | 'issued' | 'called' | 'released' | 'expired' | 'reduced'
  reductionSchedule?: BondReduction[]
  documents?: BondDocument[]
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface BondReduction {
  id: string
  bondId: string
  reductionDate: string
  reductionAmount: number
  remainingAmount: number
  reason: string
  approvedBy?: string
  createdAt: string
}

export interface BondDocument {
  id: string
  bondId: string
  documentType: 'original' | 'amendment' | 'extension' | 'release_letter' | 'call_notice' | 'receipt'
  filename: string
  filePath: string
  uploadedAt: string
  uploadedBy?: string
}

// Commuted Sum Types
export interface CommutedSum {
  id: string
  caseId: string
  case?: AdoptionCase
  sumReference: string
  sumType: 'adoption' | 'maintenance' | 'lifecyle' | 'sinking_fund' | 'commuted'
  description: string
  calculatedAmount: number
  agreedAmount?: number
  paymentDate?: string
  paymentReference?: string
  status: 'calculated' | 'negotiated' | 'agreed' | 'paid' | 'challenged'
  validityPeriod?: number // months
  expiryDate?: string
  renewalDate?: string
  maintenanceSchedule?: MaintenanceSchedule[]
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface MaintenanceSchedule {
  id: string
  commutedSumId: string
  year: number
  maintenanceCost: number
  reviewed: boolean
  reviewedBy?: string
  reviewedAt?: string
  notes?: string
}

// Snagging Types
export interface SnaggingItem {
  id: string
  caseId: string
  case?: AdoptionCase
  itemReference: string
  category: 'road' | 'drainage' | 'footway' | 'verge' | 'signage' | 'lighting' | 'soft_landscape' | 'hard_landscape' | 'utility' | 'other'
  location: string
  description: string
  priority: 'low' | 'medium' | 'high' | 'critical'
  status: 'identified' | 'in_progress' | 'awaiting_materials' | 'completed' | 'passed' | 'failed'
  identifiedDate: string
  targetCompletionDate?: string
  actualCompletionDate?: string
  assignedTo?: string
  photos?: SnaggingPhoto[]
  inspections?: SnaggingInspection[]
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface SnaggingPhoto {
  id: string
  itemId: string
  filename: string
  filePath: string
  photoType: 'defect' | 'progress' | 'completion'
  takenDate: string
  uploadedBy?: string
}

export interface SnaggingInspection {
  id: string
  itemId: string
  inspectionDate: string
  inspectorName: string
  result: 'pass' | 'fail' | 'partial'
  comments?: string
  reinspectionRequired: boolean
  reinspectionDate?: string
}

// Quality Enums
export type TemplateStatus = 'draft' | 'active' | 'archived'
export type ScheduleStatus = 'pending' | 'in_progress' | 'completed' | 'failed'
export type InspectionType = 'witness' | 'hold' | 'monitor'
export type InspectionResult = 'pass' | 'fail' | 'nc' | 'conditional'
export type DefectPriority = 'low' | 'medium' | 'high' | 'critical'
export type DefectStatus = 'open' | 'in_progress' | 'resolved' | 'closed'
export type BuildingControlType = 'nhbc' | 'labc' | 'local_authority'
export type SignOffResult = 'pending' | 'approved' | 'conditions' | 'refused'

// ITP Item
export interface ITPItem {
  id: string
  sequence: number
  description: string
  inspectionType: InspectionType
  responsibleParty: string
  notes?: string
  frequency?: string
  requiredEvidence?: string
}

// ITP Template
export interface ITPTemplate {
  id: string
  name: string
  description?: string
  category?: string
  tradeCategory?: string
  status: TemplateStatus
  version: number
  items: ITPItem[]
  createdAt: string
  updatedAt: string
  createdBy?: string
  updatedBy?: string
}

// ITP Schedule Item
export interface ITPScheduleItem extends ITPItem {
  id: string
  dueDate?: string
  status: ScheduleStatus
  completedDate?: string
  completedBy?: string
  result?: string
}

// ITP Schedule
export interface ITPSchedule {
  id: string
  title: string
  contractId: string
  contractRef?: string
  templateId?: string
  templateName?: string
  startDate: string
  dueDate: string
  completedDate?: string
  status: ScheduleStatus
  assignedInspector?: string
  signOffBy?: string
  signOffDate?: string
  signOffSignature?: string
  notes?: string
  items: ITPScheduleItem[]
  createdAt: string
  updatedAt: string
}

// Inspection Record
export interface InspectionRecord {
  id: string
  scheduleItemId: string
  scheduleItemDescription?: string
  scheduleId?: string
  scheduleTitle?: string
  contractId?: string
  title: string
  inspectorName: string
  inspectorSignature?: string
  inspectionDate: string
  inspectionTime?: string
  result: InspectionResult
  notes?: string
  findings?: string
  nonConformanceRef?: string
  attachments?: InspectionAttachment[]
  createdAt: string
  updatedAt: string
}

export interface InspectionAttachment {
  id: string
  filename: string
  fileType?: string
  filePath: string
  fileSize?: number
  description?: string
  uploadedBy?: string
}

// Defect Photo
export interface DefectPhoto {
  id: string
  filename: string
  filePath: string
  fileSize?: number
  description?: string
  uploadedBy?: string
  takenDate?: string
}

// Defect
export interface Defect {
  id: string
  title: string
  contractId: string
  contractRef?: string
  description: string
  location: string
  priority: DefectPriority
  status: DefectStatus
  identifiedDate: string
  dueDate?: string
  resolvedDate?: string
  assignedOperative?: string
  assignedContractor?: string
  notes?: string
  rootCause?: string
  resolutionDetails?: string
  reinspectionRequired: boolean
  reinspectionDate?: string
  ncReference?: string
  photos?: DefectPhoto[]
  createdAt: string
  updatedAt: string
}

// Sign-off
export interface SignOff {
  id: string
  contractId: string
  contractRef?: string
  buildingControlType: BuildingControlType
  inspectionType: string
  referenceNumber?: string
  inspectorName?: string
  inspectorEmail?: string
  inspectorPhone?: string
  inspectionDate: string
  nextInspectionDate?: string
  result: SignOffResult
  conditionsOrNotes?: string
  reportPath?: string
  reportNumber?: string
  signOffSignature?: string
  signOffDate?: string
  notes?: string
  createdAt: string
  updatedAt: string
}

// Legacy QualityRecord (kept for compatibility)
export interface QualityRecord {
  id: string
  type: 'itp_template' | 'itp_schedule' | 'inspection' | 'defect'
  title: string
  contractId?: string
  siteId?: string
  status: string
  data: Record<string, any>
  createdAt: string
  updatedAt: string
}

export interface User {
  id: string
  name: string
  email: string
  role: 'admin' | 'manager' | 'user' | 'viewer'
  status: 'active' | 'inactive'
  lastLogin?: string
  createdAt: string
}

export interface CVRReport {
  contractId: string
  contractRef: string
  period: string
  value: number
  cost: number
  cvr: number
  forecast: number
}

export interface CashflowReport {
  month: string
  forecast: number
  actual?: number
  cumulative: number
}

export interface RetentionReport {
  applicationRef: string
  applicationValue: number
  retentionHeld: number
  retentionReleased: number
  balance: number
}

// Retention Ledger Entry
export interface RetentionLedgerEntry {
  id: string
  contractId: string
  applicationId: string
  application?: Application
  amount: number
  percentage: number
  releaseDate?: string
  status: 'held' | 'released' | 'proposed'
  createdAt: string
  updatedAt: string
}

// WIP Journal Entry
export interface WipEntry {
  id: string
  contractId: string
  contract?: Contract
  operativeId: string
  operative?: Operative
  entryDate: string
  description: string
  hours: number
  rate: number
  amount: number
  status: 'draft' | 'submitted' | 'reviewed' | 'approved'
  notes?: string
  createdAt: string
  updatedAt: string
}

// API namespaces
export const api = {
  auth: {
    login: (credentials: { username: string; password: string }) =>
      apiClient.post('/auth/login', credentials),
    logout: () => apiClient.post('/auth/logout'),
    refreshToken: (token: string) =>
      apiClient.post('/auth/refresh', { refreshToken: token }),
    getProfile: () => apiClient.get('/auth/profile'),
    changePassword: (data: { currentPassword: string; newPassword: string }) =>
      apiClient.post('/auth/change-password', data),
    totpSetup: () => apiClient.post('/auth/totp/setup'),
    totpEnable: (code: string) => apiClient.post('/auth/totp/enable', { code }),
    totpDisable: () => apiClient.delete('/auth/totp/disable'),
    totpChallenge: (challengeToken: string, code: string) =>
      apiClient.post('/auth/totp/challenge', { challengeToken, code })
  },

  companies: {
    getAll: (params?: { search?: string; type?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: Company[]; total: number }>('/companies', { params }),
    getById: (id: string) => apiClient.get<Company>(`/companies/${id}`),
    create: (data: Partial<Company>) => apiClient.post<Company>('/companies', data),
    update: (id: string, data: Partial<Company>) => apiClient.put<Company>(`/companies/${id}`, data),
    delete: (id: string) => apiClient.delete(`/companies/${id}`),
    refreshCompaniesHouse: (id: string) =>
      apiClient.post<Company>(`/companies/${id}/refresh-companies-house`),
    verifyCIS: (id: string) => apiClient.post<Company>(`/companies/${id}/verify-cis`)
  },

  contacts: {
    getAll: (params?: { companyId?: string; search?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: Contact[]; total: number }>('/contacts', { params }),
    getById: (id: string) => apiClient.get<Contact>(`/contacts/${id}`),
    create: (data: Partial<Contact>) => apiClient.post<Contact>('/contacts', data),
    update: (id: string, data: Partial<Contact>) => apiClient.put<Contact>(`/contacts/${id}`, data),
    delete: (id: string) => apiClient.delete(`/contacts/${id}`)
  },

  sites: {
    getAll: (params?: { search?: string; status?: string; clientId?: string; startDate?: string; endDate?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: Site[]; total: number }>('/sites', { params }),
    getById: (id: string) => apiClient.get<Site>(`/sites/${id}`),
    create: (data: Partial<Site>) => apiClient.post<Site>('/sites', data),
    update: (id: string, data: Partial<Site>) => apiClient.put<Site>(`/sites/${id}`, data),
    delete: (id: string) => apiClient.delete(`/sites/${id}`)
  },

  tenders: {
    getAll: (params?: { stage?: string; clientId?: string; siteId?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: Tender[]; total: number }>('/tenders', { params }),
    getById: (id: string) => apiClient.get<Tender>(`/tenders/${id}`),
    create: (data: Partial<Tender>) => apiClient.post<Tender>('/tenders', data),
    update: (id: string, data: Partial<Tender>) => apiClient.put<Tender>(`/tenders/${id}`, data),
    delete: (id: string) => apiClient.delete(`/tenders/${id}`),
    win: (id: string) => apiClient.post<Tender>(`/tenders/${id}/win`),
    lose: (id: string, reason: string) => apiClient.post<Tender>(`/tenders/${id}/lose`, { reason })
  },

  contracts: {
    getAll: (params?: { status?: string; clientId?: string; minValue?: number; maxValue?: number; page?: number; limit?: number }) =>
      apiClient.get<{ data: Contract[]; total: number }>('/contracts', { params }),
    getById: (id: string) => apiClient.get<Contract>(`/contracts/${id}`),
    create: (data: Partial<Contract>) => apiClient.post<Contract>('/contracts', data),
    update: (id: string, data: Partial<Contract>) => apiClient.put<Contract>(`/contracts/${id}`, data),
    delete: (id: string) => apiClient.delete(`/contracts/${id}`)
  },

  boqItems: {
    getByTender: (tenderId: string) =>
      apiClient.get(`/boq-items?tenderId=${tenderId}`),
    create: (data: any) => apiClient.post('/boq-items', data),
    update: (id: string, data: any) => apiClient.put(`/boq-items/${id}`, data),
    importCITE: (tenderId: string, file: File) => {
      const formData = new FormData()
      formData.append('file', file)
      return apiClient.post(`/boq-items/${tenderId}/import-cite`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    }
  },

  applications: {
    getByContract: (contractId: string) =>
      apiClient.get<Application[]>(`/contracts/${contractId}/applications`),
    create: (contractId: string, data: Partial<Application>) =>
      apiClient.post<Application>(`/contracts/${contractId}/applications`, data),
    submit: (contractId: string, applicationId: string) =>
      apiClient.post<Application>(`/contracts/${contractId}/applications/${applicationId}/submit`),
    addPaymentNotice: (contractId: string, applicationId: string, data: any) =>
      apiClient.post(`/contracts/${contractId}/applications/${applicationId}/payment-notice`, data),
    addPayLessNotice: (contractId: string, applicationId: string, data: any) =>
      apiClient.post(`/contracts/${contractId}/applications/${applicationId}/pay-less-notice`, data),
    addDefaultNotice: (contractId: string, applicationId: string) =>
      apiClient.post(`/contracts/${contractId}/applications/${applicationId}/default-notice`)
  },

  applicationsForPayment: {
    getAll: (params?: { status?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: ApplicationResponse[]; total: number }>('/applications', { params }),
    getById: (id: string) => apiClient.get<ApplicationResponse>(`/applications/${id}`),
    create: (contractId: string, data: Partial<ApplicationForPaymentRequest>) =>
      apiClient.post<ApplicationResponse>(`/applications?contractId=${contractId}`, data),
    submit: (id: string) =>
      apiClient.post<ApplicationResponse>(`/applications/${id}/submit`),
    measure: (id: string) =>
      apiClient.post<ApplicationResponse>(`/applications/${id}/measure`),
    agree: (id: string) =>
      apiClient.post<ApplicationResponse>(`/applications/${id}/agree`),
    approve: (id: string) =>
      apiClient.post<ApplicationResponse>(`/applications/${id}/approve`),
    reject: (id: string) =>
      apiClient.post<ApplicationResponse>(`/applications/${id}/reject`),
    markPaid: (id: string) =>
      apiClient.post<ApplicationResponse>(`/applications/${id}/mark-paid`)
  },

  subcontractors: {
    getAll: (params?: { status?: string; cisStatus?: string; search?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: Subcontractor[]; total: number }>('/subcontractors', { params }),
    getById: (id: string) => apiClient.get<Subcontractor>(`/subcontractors/${id}`),
    create: (data: Partial<Subcontractor>) => apiClient.post<Subcontractor>('/subcontractors', data),
    update: (id: string, data: Partial<Subcontractor>) => apiClient.put<Subcontractor>(`/subcontractors/${id}`, data),
    delete: (id: string) => apiClient.delete(`/subcontractors/${id}`),
    verify: (id: string) => apiClient.post<Subcontractor>(`/subcontractors/${id}/verify`)
  },

  cis: {
    submitCisReturn: (data: { subcontractorId: string }) =>
      apiClient.post('/cis/submit-cis-return', data)
  },

  cisReturns: {
    getAll: (params?: { month?: string; status?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: any[]; total: number }>('/cis-returns', { params }),
    submit: (id: string) => apiClient.post(`/cis-returns/${id}/submit`)
  },

  operatives: {
    getAll: (params?: { status?: string; employerId?: string; siteId?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: Operative[]; total: number }>('/operatives', { params }),
    getById: (id: string) => apiClient.get<Operative>(`/operatives/${id}`),
    create: (data: Partial<Operative>) => apiClient.post<Operative>('/operatives', data),
    update: (id: string, data: Partial<Operative>) => apiClient.put<Operative>(`/operatives/${id}`, data),
    delete: (id: string) => apiClient.delete(`/operatives/${id}`),
    smartCheckCard: (operativeId: string, cardId: string) =>
      apiClient.post(`/operatives/${operativeId}/cards/${cardId}/smart-check`)
  },

  plant: {
    getAll: (params?: { status?: string; category?: string; siteId?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: PlantItem[]; total: number }>('/plant', { params }),
    getById: (id: string) => apiClient.get<PlantItem>(`/plant/${id}`),
    create: (data: Partial<PlantItem>) => apiClient.post<PlantItem>('/plant', data),
    update: (id: string, data: Partial<PlantItem>) => apiClient.put<PlantItem>(`/plant/${id}`, data),
    delete: (id: string) => apiClient.delete(`/plant/${id}`),
    addLOLER: (id: string, data: { dueDate: string; certificateNumber?: string; notes?: string }) =>
      apiClient.post(`/plant/${id}/loler`, data),
    getPlantGantt: (params?: { startDate?: string; endDate?: string; siteId?: string }) =>
      apiClient.get<{ allocations: SiteAllocation[] }>('/plant/gantt', { params })
  },

  procurement: {
    getRequisitions: (params?: { status?: string; siteId?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: Requisition[]; total: number }>('/procurement/purchase-requisitions', { params }),
    createRequisition: (data: Partial<Requisition>) =>
      apiClient.post<Requisition>('/procurement/purchase-requisitions', data),
    approveRequisition: (id: string) =>
      apiClient.post<Requisition>(`/procurement/purchase-requisitions/${id}/approve`),
    createPO: (requisitionId: string, data: Partial<PurchaseOrder>) =>
      apiClient.post<PurchaseOrder>(`/procurement/purchase-requisitions/${requisitionId}/create-po`, data),
    getPO: (params?: { status?: string; supplierId?: string; siteId?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: PurchaseOrder[]; total: number }>('/procurement/purchase-orders', { params }),
    getDeliveryNotes: (params?: { type?: string; siteId?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: DeliveryNote[]; total: number }>('/procurement/delivery-notes', { params })
  },

  documents: {
    getAll: (params?: { entityId?: string; entityType?: string; category?: string }) =>
      apiClient.get<{ data: Document[]; total: number }>('/documents', { params }),
    upload: (file: File, metadata?: { category?: string; entityType?: string; entityId?: string }) => {
      const formData = new FormData()
      formData.append('file', file)
      if (metadata) {
        Object.entries(metadata).forEach(([key, value]) => {
          formData.append(key, value)
        })
      }
      return apiClient.post<Document>('/documents', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    },
    getVersions: (id: string) => apiClient.get<DocumentVersion[]>(`/documents/${id}/versions`),
    getDownloadUrl: (id: string) => apiClient.get<{ url: string }>(`/documents/${id}/download`)
  },

  reports: {
    getCVR: (contractId: string, period: string) =>
      apiClient.get<CVRReport>(`/reports/cvr?contractId=${contractId}&period=${period}`),
    getCashflow: (params: { startMonth: string; endMonth: string; siteId?: string }) =>
      apiClient.get<CashflowReport[]>('/reports/cashflow', { params }),
    getRetention: (contractId: string) =>
      apiClient.get<RetentionReport[]>(`/reports/retention?contractId=${contractId}`)
  },

  healthSafety: {
    // F10 Notifications
    getF10Notifications: (params?: { siteId?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: HealthSafetyRecord[]; total: number }>('/healthsafety/f10', { params }),
    getF10ById: (id: string) => apiClient.get<HealthSafetyRecord>(`/healthsafety/f10/${id}`),
    createF10: (data: Partial<HealthSafetyRecord>) => apiClient.post<HealthSafetyRecord>('/healthsafety/f10', data),
    updateF10: (id: string, data: Partial<HealthSafetyRecord>) => apiClient.put<HealthSafetyRecord>(`/healthsafety/f10/${id}`, data),
    deleteF10: (id: string) => apiClient.delete(`/healthsafety/f10/${id}`),

    // CPP (Construction Phase Plans)
    getCPPs: (params?: { siteId?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: HealthSafetyRecord[]; total: number }>('/healthsafety/cpp', { params }),
    getCPPById: (id: string) => apiClient.get<HealthSafetyRecord>(`/healthsafety/cpp/${id}`),
    createCPP: (data: Partial<HealthSafetyRecord>) => apiClient.post<HealthSafetyRecord>('/healthsafety/cpp', data),
    updateCPP: (id: string, data: Partial<HealthSafetyRecord>) => apiClient.put<HealthSafetyRecord>(`/healthsafety/cpp/${id}`, data),
    deleteCPP: (id: string) => apiClient.delete(`/healthsafety/cpp/${id}`),

    // RAMS Templates
    getRAMS: (params?: { contractId?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: RAMS[]; total: number }>('/healthsafety/rams', { params }),
    getRAMSById: (id: string) => apiClient.get<RAMS>(`/healthsafety/rams/${id}`),
    createRAMS: (data: Partial<RAMS>) => apiClient.post<RAMS>('/healthsafety/rams', data),
    updateRAMS: (id: string, data: Partial<RAMS>) => apiClient.put<RAMS>(`/healthsafety/rams/${id}`, data),
    deleteRAMS: (id: string) => apiClient.delete(`/healthsafety/rams/${id}`),

    // Permit to Dig Workflow
    getPermits: (params?: { siteId?: string; status?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: PermitToDig[]; total: number }>('/healthsafety/permits', { params }),
    getPermitById: (id: string) => apiClient.get<PermitToDig>(`/healthsafety/permits/${id}`),
    createPermit: (data: Partial<PermitToDig>) => apiClient.post<PermitToDig>('/healthsafety/permits', data),
    updatePermit: (id: string, data: Partial<PermitToDig>) => apiClient.put<PermitToDig>(`/healthsafety/permits/${id}`, data),
    deletePermit: (id: string) => apiClient.delete(`/healthsafety/permits/${id}`),
    updatePermitStatus: (id: string, status: string) =>
      apiClient.put<PermitToDig>(`/healthsafety/permits/${id}/status`, { status }),

    // Incident Reporting
    getIncidents: (params?: { siteId?: string; severity?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: Incident[]; total: number }>('/healthsafety/incidents', { params }),
    getIncidentById: (id: string) => apiClient.get<Incident>(`/healthsafety/incidents/${id}`),
    createIncident: (data: Partial<Incident>) => apiClient.post<Incident>('/healthsafety/incidents', data),
    updateIncident: (id: string, data: Partial<Incident>) => apiClient.put<Incident>(`/healthsafety/incidents/${id}`, data),
    deleteIncident: (id: string) => apiClient.delete(`/healthsafety/incidents/${id}`)
  },

  adoption: {
    // Adoption Cases
    getAll: (params?: { status?: string; type?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: AdoptionCase[]; total: number }>('/adoption', { params }),
    getById: (id: string) => apiClient.get<AdoptionCase>(`/adoption/${id}`),
    create: (data: Partial<AdoptionCase>) => apiClient.post<AdoptionCase>('/adoption', data),
    update: (id: string, data: Partial<AdoptionCase>) => apiClient.put<AdoptionCase>(`/adoption/${id}`, data),
    delete: (id: string) => apiClient.delete(`/adoption/${id}`),

    // Bonds
    getBonds: (params?: { caseId?: string; status?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: Bond[]; total: number }>('/adoption/bonds', { params }),
    getBondById: (id: string) => apiClient.get<Bond>(`/adoption/bonds/${id}`),
    createBond: (data: Partial<Bond>) => apiClient.post<Bond>('/adoption/bonds', data),
    updateBond: (id: string, data: Partial<Bond>) => apiClient.put<Bond>(`/adoption/bonds/${id}`, data),
    deleteBond: (id: string) => apiClient.delete(`/adoption/bonds/${id}`),
    addBondReduction: (bondId: string, data: { reductionDate: string; reductionAmount: number; reason: string; approvedBy?: string }) =>
      apiClient.post<BondReduction>(`/adoption/bonds/${bondId}/reductions`, data),
    releaseBond: (bondId: string, releaseDate: string) =>
      apiClient.post<Bond>(`/adoption/bonds/${bondId}/release`, { releaseDate }),
    callBond: (bondId: string, reason: string) =>
      apiClient.post<Bond>(`/adoption/bonds/${bondId}/call`, { reason }),

    // Commuted Sums
    getCommutedSums: (params?: { caseId?: string; status?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: CommutedSum[]; total: number }>('/adoption/commuted-sums', { params }),
    getCommutedSumById: (id: string) => apiClient.get<CommutedSum>(`/adoption/commuted-sums/${id}`),
    createCommutedSum: (data: Partial<CommutedSum>) => apiClient.post<CommutedSum>('/adoption/commuted-sums', data),
    updateCommutedSum: (id: string, data: Partial<CommutedSum>) => apiClient.put<CommutedSum>(`/adoption/commuted-sums/${id}`, data),
    deleteCommutedSum: (id: string) => apiClient.delete(`/adoption/commuted-sums/${id}`),
    addMaintenanceSchedule: (commutedSumId: string, data: { year: number; maintenanceCost: number; notes?: string }) =>
      apiClient.post<MaintenanceSchedule>(`/adoption/commuted-sums/${commutedSumId}/maintenance-schedule`, data),
    updateMaintenanceSchedule: (commutedSumId: string, scheduleId: string, data: Partial<MaintenanceSchedule>) =>
      apiClient.put<MaintenanceSchedule>(`/adoption/commuted-sums/${commutedSumId}/maintenance-schedule/${scheduleId}`, data),

    // Snagging
    getSnaggingItems: (params?: { caseId?: string; status?: string; category?: string; priority?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: SnaggingItem[]; total: number }>('/adoption/snagging', { params }),
    getSnaggingItemById: (id: string) => apiClient.get<SnaggingItem>(`/adoption/snagging/${id}`),
    createSnaggingItem: (data: Partial<SnaggingItem>) => apiClient.post<SnaggingItem>('/adoption/snagging', data),
    updateSnaggingItem: (id: string, data: Partial<SnaggingItem>) => apiClient.put<SnaggingItem>(`/adoption/snagging/${id}`, data),
    deleteSnaggingItem: (id: string) => apiClient.delete(`/adoption/snagging/${id}`),
    updateSnaggingStatus: (id: string, status: string) =>
      apiClient.put<SnaggingItem>(`/adoption/snagging/${id}/status`, { status }),
    addSnaggingInspection: (itemId: string, data: { inspectionDate: string; inspectorName: string; result: string; comments?: string; reinspectionRequired: boolean; reinspectionDate?: string }) =>
      apiClient.post<SnaggingInspection>(`/adoption/snagging/${itemId}/inspections`, data)
  },

  quality: {
    // ITP Templates
    getITPTemplates: (params?: { status?: string; category?: string; tradeCategory?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: ITPTemplate[]; total: number }>('/quality/templates', { params }),
    getITPTemplateById: (id: string) => apiClient.get<ITPTemplate>(`/quality/templates/${id}`),
    createITPTemplate: (data: Partial<ITPTemplate>) => apiClient.post<ITPTemplate>('/quality/templates', data),
    updateITPTemplate: (id: string, data: Partial<ITPTemplate>) => apiClient.put<ITPTemplate>(`/quality/templates/${id}`, data),
    deleteITPTemplate: (id: string) => apiClient.delete(`/quality/templates/${id}`),
    copyITPTemplate: (id: string) => apiClient.post<ITPTemplate>(`/quality/templates/${id}/copy`),

    // ITP Schedules
    getITPSchedules: (params?: { contractId?: string; status?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: ITPSchedule[]; total: number }>('/quality/schedules', { params }),
    getITPScheduleById: (id: string) => apiClient.get<ITPSchedule>(`/quality/schedules/${id}`),
    createITPSchedule: (data: Partial<ITPSchedule>) => apiClient.post<ITPSchedule>('/quality/schedules', data),
    updateITPSchedule: (id: string, data: Partial<ITPSchedule>) => apiClient.put<ITPSchedule>(`/quality/schedules/${id}`, data),
    deleteITPSchedule: (id: string) => apiClient.delete(`/quality/schedules/${id}`),
    createITPScheduleFromTemplate: (templateId: string, contractId: string) =>
      apiClient.post<ITPSchedule>('/quality/schedules/from-template', null, { params: { templateId, contractId } }),
    completeITPScheduleItem: (scheduleId: string, itemId: string, completedBy: string, result?: string) =>
      apiClient.post(`/quality/schedules/${scheduleId}/items/${itemId}/complete`, null, { params: { completedBy, result } }),

    // Inspection Records
    getInspections: (params?: { scheduleItemId?: string; result?: string; inspectorName?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: any[]; total: number }>('/quality/inspections', { params }),
    getInspectionById: (id: string) => apiClient.get<any>(`/quality/inspections/${id}`),
    createInspection: (data: any) => apiClient.post('/quality/inspections', data),
    updateInspection: (id: string, data: any) => apiClient.put(`/quality/inspections/${id}`, data),
    deleteInspection: (id: string) => apiClient.delete(`/quality/inspections/${id}`),

    // Defects
    getDefects: (params?: { contractId?: string; status?: string; priority?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: Defect[]; total: number }>('/quality/defects', { params }),
    getDefectById: (id: string) => apiClient.get<Defect>(`/quality/defects/${id}`),
    createDefect: (data: Partial<Defect>) => apiClient.post<Defect>('/quality/defects', data),
    updateDefect: (id: string, data: Partial<Defect>) => apiClient.put<Defect>(`/quality/defects/${id}`, data),
    deleteDefect: (id: string) => apiClient.delete(`/quality/defects/${id}`),
    updateDefectStatus: (id: string, status: string) => apiClient.patch<Defect>(`/quality/defects/${id}/status`, null, { params: { status } }),
    assignDefectOperative: (id: string, operative: string) => apiClient.patch<Defect>(`/quality/defects/${id}/operative`, null, { params: { operative } }),
    assignDefectContractor: (id: string, contractor: string) => apiClient.patch<Defect>(`/quality/defects/${id}/contractor`, null, { params: { contractor } }),

    // Sign-offs (NHBC/LABC)
    getSignOffs: (params?: { contractId?: string; buildingControlType?: string; result?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: any[]; total: number }>('/quality/signoffs', { params }),
    getSignOffById: (id: string) => apiClient.get<any>(`/quality/signoffs/${id}`),
    createSignOff: (data: any) => apiClient.post('/quality/signoffs', data),
    updateSignOff: (id: string, data: any) => apiClient.put(`/quality/signoffs/${id}`, data),
    deleteSignOff: (id: string) => apiClient.delete(`/quality/signoffs/${id}`),
    approveSignOff: (id: string, signature?: string) => apiClient.post(`/quality/signoffs/${id}/approve`, null, { params: { signature } }),
    refuseSignOff: (id: string, conditions: string) => apiClient.post(`/quality/signoffs/${id}/refuse`, null, { params: { conditions } })
  },

  admin: {
    getUsers: (params?: { role?: string; status?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: User[]; total: number }>('/admin/users', { params }),
    createUser: (data: Partial<User>) => apiClient.post<User>('/admin/users', data),
    updateUser: (id: string, data: Partial<User>) => apiClient.put<User>(`/admin/users/${id}`, data),
    deleteUser: (id: string) => apiClient.delete(`/admin/users/${id}`),
    getRoles: () => apiClient.get('/admin/roles'),
    getBackupStatus: () => apiClient.get('/admin/backup/status'),
    triggerBackup: () => apiClient.post('/admin/backup/trigger'),
    restoreBackup: (backupId: string) => apiClient.post(`/admin/backup/restore/${backupId}`),
    getIntegrations: () => apiClient.get('/admin/integrations'),
    getSettings: () => apiClient.get('/admin/settings'),
    updateSettings: (data: any) => apiClient.put('/admin/settings', data),

    users: {
      getAll: (params?: { page?: number; size?: number; sort?: string; limit?: number }) =>
        apiClient.get('/admin/users', { params }),
      create: (data: { username?: string; email: string; password: string; firstName?: string; lastName?: string; role?: string; status?: string }) =>
        apiClient.post('/admin/users', data),
      update: (id: string, data: { email?: string; firstName?: string; lastName?: string; roles?: string[]; enabled?: boolean; newPassword?: string }) =>
        apiClient.patch(`/admin/users/${id}`, data),
      delete: (id: string) => apiClient.delete(`/admin/users/${id}`)
    }
  },

  licence: {
    getStatus: () => apiClient.get('/licence')
  },

  auditLog: {
    getAll: (params?: {
      page?: number;
      size?: number;
      limit?: number;
      startDate?: string;
      endDate?: string;
      userId?: string;
      action?: string;
      entityType?: string
    }) => apiClient.get<{ data: any[]; total: number }>('/audit-logs', { params })
  },

  wip: {
    getAll: (params?: { startDate?: string; endDate?: string; contractId?: string; status?: string; operativeId?: string; page?: number; limit?: number }) =>
      apiClient.get<{ data: WipEntry[]; total: number }>('/wip', { params }),
    getById: (id: string) => apiClient.get<WipEntry>(`/wip/${id}`),
    create: (data: Partial<WipEntry>) => apiClient.post<WipEntry>('/wip', data),
    update: (id: string, data: Partial<WipEntry>) => apiClient.put<WipEntry>(`/wip/${id}`, data),
    delete: (id: string) => apiClient.delete(`/wip/${id}`)
  },

  retentionLedger: {
    getByContract: (contractId: string) =>
      apiClient.get<RetentionLedgerEntry[]>(`/contracts/${contractId}/retention-ledger`),
    create: (contractId: string, data: Partial<RetentionLedgerEntry>) =>
      apiClient.post<RetentionLedgerEntry>(`/contracts/${contractId}/retention-ledger`, data),
    update: (contractId: string, id: string, data: Partial<RetentionLedgerEntry>) =>
      apiClient.put<RetentionLedgerEntry>(`/contracts/${contractId}/retention-ledger/${id}`, data),
    delete: (contractId: string, id: string) =>
      apiClient.delete(`/contracts/${contractId}/retention-ledger/${id}`)
  },

}

export default api