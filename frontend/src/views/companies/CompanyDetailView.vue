<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Edit, Refresh, Upload, Plus, Delete } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import api, { type Company, type Contact, type Site, type Contract, type Document } from '@/services/api'; import type { ElTagType } from '@/services/api'
import StatusBadge from '@/components/common/StatusBadge.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import FileUpload from '@/components/common/FileUpload.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const companyId = computed(() => route.params.id as string)

const company = ref<Company | null>(null)
const contacts = ref<Contact[]>([])
const sites = ref<Site[]>([])
const contracts = ref<Contract[]>([])
const documents = ref<Document[]>([])

// CIS verification
const verifyingCIS = ref(false)
const cisVerificationResult = ref<any>(null)

// Contact form
const contactDrawerVisible = ref(false)
const contactFormRef = ref<FormInstance>()
const editingContactId = ref('')
const contactForm = ref({
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  position: '',
  isPrimary: false
})

onMounted(() => {
  loadCompany()
})

const loadCompany = async () => {
  if (!companyId.value) return
  
  loading.value = true
  try {
    const [companyRes, contactsRes, sitesRes, contractsRes, docsRes] = await Promise.all([
      api.companies.getById(companyId.value),
      api.contacts.getAll({ companyId: companyId.value }),
      api.sites.getAll({ clientId: companyId.value }),
      api.contracts.getAll({ clientId: companyId.value }),
      api.documents.getAll({ entityId: companyId.value, entityType: 'company' })
    ])
    
    company.value = companyRes.data
    contacts.value = contactsRes.data.data
    sites.value = sitesRes.data.data
    contracts.value = contractsRes.data.data
    documents.value = docsRes.data.data
  } catch (error) {
    ElMessage.error('Failed to load company details')
  } finally {
    loading.value = false
  }
}

const verifyCIS = async () => {
  if (!companyId.value) return
  
  verifyingCIS.value = true
  try {
    const response = await api.companies.verifyCIS(companyId.value)
    cisVerificationResult.value = response.data
    company.value = response.data
    ElMessage.success('CIS verification completed')
  } catch (error) {
    ElMessage.error('CIS verification failed')
  } finally {
    verifyingCIS.value = false
  }
}

const refreshCompaniesHouse = async () => {
  if (!companyId.value) return
  
  try {
    await api.companies.refreshCompaniesHouse(companyId.value)
    ElMessage.success('Companies House data refreshed')
    loadCompany()
  } catch (error) {
    ElMessage.error('Failed to refresh data')
  }
}

const openAddContact = () => {
  editingContactId.value = ''
  contactForm.value = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    position: '',
    isPrimary: false
  }
  contactDrawerVisible.value = true
}

const openEditContact = (contact: Contact) => {
  editingContactId.value = contact.id
  contactForm.value = {
    firstName: contact.firstName,
    lastName: contact.lastName,
    email: contact.email,
    phone: contact.phone || '',
    position: contact.position || '',
    isPrimary: contact.isPrimary
  }
  contactDrawerVisible.value = true
}

const saveContact = async () => {
  if (!contactFormRef.value) return
  
await contactFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    try {
      if (editingContactId.value) {
        await api.contacts.update(editingContactId.value, {
          ...contactForm.value,
          companyId: companyId.value
        })
      } else {
        await api.contacts.create({
          ...contactForm.value,
          companyId: companyId.value
        })
      }
      ElMessage.success('Contact saved')
      contactDrawerVisible.value = false
      loadCompany()
    } catch (error) {
      ElMessage.error('Failed to save contact')
    }
  })
}

const deleteContact = async (contact: Contact) => {
  try {
    await ElMessageBox.confirm(
      `Delete contact "${contact.firstName} ${contact.lastName}"?`,
      'Confirm Delete',
      { type: 'warning' }
    )
    await api.contacts.delete(contact.id)
    ElMessage.success('Contact deleted')
    loadCompany()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('Failed to delete contact')
    }
  }
}

const handleUploadSuccess = () => {
  ElMessage.success('Document uploaded')
  loadCompany()
}

const downloadDocument = async (doc: Document) => {
  try {
    const res = await api.documents.getDownloadUrl(doc.id)
    window.open(res.data.url, '_blank')
  } catch {
    ElMessage.error('Failed to download document')
  }
}

const formatDate = (date?: string) => {
  if (!date) return '—'
  return new Date(date).toLocaleDateString()
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const getCisStatusType = (status?: string): TagType => {
  const map: Record<string, TagType> = {
    verified: 'success',
    pending: 'warning',
    expired: 'danger'
  }
  return map[status || ''] ?? 'info'
}

// Edit company dialog
const editDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const editLoading = ref(false)
const editForm = ref({
  name: '',
  companyType: 'client' as Company['companyType'],
  registrationNumber: '',
  vatNumber: '',
  phone: '',
  email: '',
  address: {
    addressLine1: '',
    addressLine2: '',
    city: '',
    county: '',
    postcode: ''
  }
})

const openEditDialog = () => {
  if (!company.value) return
  editForm.value = {
    name: company.value.name,
    companyType: company.value.companyType,
    registrationNumber: company.value.registrationNumber || '',
    vatNumber: company.value.vatNumber || '',
    phone: company.value.phone || '',
    email: company.value.email || '',
    address: {
      addressLine1: company.value.address.addressLine1,
      addressLine2: company.value.address.addressLine2 || '',
      city: company.value.address.city,
      county: company.value.address.county || '',
      postcode: company.value.address.postcode
    }
  }
  editDialogVisible.value = true
}

const saveEdit = async () => {
  if (!editFormRef.value || !companyId.value) return
  await editFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    editLoading.value = true
    try {
      await api.companies.update(companyId.value, editForm.value)
      ElMessage.success('Company updated')
      editDialogVisible.value = false
      loadCompany()
    } catch { ElMessage.error('Failed to update company') } finally { editLoading.value = false }
  })
}
</script>

<template>
  <div class="company-detail-view">
    <PageHeader 
      :title="company?.name || 'Company Details'" 
      :breadcrumbs="[
        { title: 'Companies', path: '/companies' },
        { title: company?.name || '' }
      ]"
    >
      <template #actions>
        <el-button :icon="Refresh" @click="refreshCompaniesHouse">
          Refresh CH
        </el-button>
        <el-button :icon="Edit" @click="openEditDialog">Edit</el-button>
      </template>
    </PageHeader>

    <el-tabs v-if="company">
      <el-tab-pane label="Overview">
        <el-card shadow="never">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="Company Type">
              <el-tag>{{ company.companyType }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Status">
              <StatusBadge :status="company.status" />
            </el-descriptions-item>
            <el-descriptions-item label="Registration No.">
              {{ company.registrationNumber || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="VAT Number">
              {{ company.vatNumber || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="Phone">
              {{ company.phone || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="Email">
              {{ company.email || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="Address" :span="2">
              {{ company.address.addressLine1 }},
              {{ company.address.addressLine2 ? company.address.addressLine2 + ', ' : '' }}
              {{ company.address.city }},
              {{ company.address.county ? company.address.county + ', ' : '' }}
              {{ company.address.postcode }}
            </el-descriptions-item>
          </el-descriptions>

          <el-divider v-if="company.companyType === 'subcontractor'" content-position="left">
            CIS Verification
          </el-divider>
          
          <div v-if="company.companyType === 'subcontractor'" class="cis-section">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="cis-card">
                  <span class="label">CIS Status</span>
                  <el-tag :type="getCisStatusType(company.cisStatus)">
                    {{ company.cisStatus?.replace('_', ' ').toUpperCase() || 'N/A' }}
                  </el-tag>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="cis-card">
                  <span class="label">CIS Rate</span>
                  <span class="value">{{ company.cisRate ? `${company.cisRate}%` : '—' }}</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="cis-card">
                  <el-button type="primary" :loading="verifyingCIS" @click="verifyCIS">
                    Verify with HMRC
                  </el-button>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Contacts">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>Contacts ({{ contacts.length }})</span>
              <el-button type="primary" size="small" :icon="Plus" @click="openAddContact">
                Add Contact
              </el-button>
            </div>
          </template>
          
          <el-table :data="contacts" stripe>
            <el-table-column label="Name" min-width="180">
              <template #default="{ row }">
                {{ row.firstName }} {{ row.lastName }}
                <el-tag v-if="row.isPrimary" size="small" type="success">Primary</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="position" label="Position" width="150" />
            <el-table-column prop="email" label="Email" min-width="200" />
            <el-table-column prop="phone" label="Phone" width="140" />
            <el-table-column label="Actions" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openEditContact(row)">
                  Edit
                </el-button>
                <el-button link type="danger" size="small" @click="deleteContact(row)">
                  Delete
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Documents">
        <el-card shadow="never">
          <template #header>
            <span>Documents</span>
          </template>
          
          <FileUpload 
            :entity-type="'company'" 
            :entity-id="companyId"
            @success="handleUploadSuccess"
          />
          
          <el-divider />
          
          <el-table :data="documents" stripe>
            <el-table-column prop="filename" label="Filename" min-width="200" />
            <el-table-column prop="category" label="Category" width="120" />
            <el-table-column label="Size" width="100">
              <template #default="{ row }">
                {{ (row.size / 1024).toFixed(1) }} KB
              </template>
            </el-table-column>
            <el-table-column label="Uploaded" width="120">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="downloadDocument(row)">Download</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Related Sites">
        <el-card shadow="never">
          <el-table :data="sites" stripe>
            <el-table-column prop="name" label="Site Name" min-width="200" />
            <el-table-column prop="siteCode" label="Code" width="100" />
            <el-table-column label="Status" width="120">
              <template #default="{ row }">
                <StatusBadge :status="row.status" />
              </template>
            </el-table-column>
            <el-table-column label="Start Date" width="120">
              <template #default="{ row }">
                {{ formatDate(row.startDate) }}
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="router.push(`/sites/${row.id}`)">
                  View
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Related Contracts">
        <el-card shadow="never">
          <el-table :data="contracts" stripe>
            <el-table-column prop="reference" label="Ref" width="120" />
            <el-table-column prop="title" label="Title" min-width="200" />
            <el-table-column label="Value" width="120">
              <template #default="{ row }">
                £{{ row.contractValue.toLocaleString() }}
              </template>
            </el-table-column>
            <el-table-column label="Status" width="120">
              <template #default="{ row }">
                <StatusBadge :status="row.status" />
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="router.push(`/contracts/${row.id}`)">
                  View
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- Contact Drawer -->
    <el-drawer v-model="contactDrawerVisible" title="Contact" size="500px">
      <el-form ref="contactFormRef" :model="contactForm" label-position="top">
        <el-form-item label="First Name" required>
          <el-input v-model="contactForm.firstName" />
        </el-form-item>
        <el-form-item label="Last Name" required>
          <el-input v-model="contactForm.lastName" />
        </el-form-item>
        <el-form-item label="Position">
          <el-input v-model="contactForm.position" />
        </el-form-item>
        <el-form-item label="Email" required>
          <el-input v-model="contactForm.email" type="email" />
        </el-form-item>
        <el-form-item label="Phone">
          <el-input v-model="contactForm.phone" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="contactForm.isPrimary">Primary Contact</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contactDrawerVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveContact">Save</el-button>
      </template>
    </el-drawer>

    <!-- Edit Company Dialog -->
    <el-dialog v-model="editDialogVisible" title="Edit Company" width="600px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" label-position="top">
        <el-form-item label="Company Name" prop="name" required>
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="Company Type" prop="companyType" required>
          <el-select v-model="editForm.companyType" style="width: 100%">
            <el-option label="Client" value="client" />
            <el-option label="Subcontractor" value="subcontractor" />
            <el-option label="Supplier" value="supplier" />
            <el-option label="Consultant" value="consultant" />
            <el-option label="Other" value="other" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Registration Number">
              <el-input v-model="editForm.registrationNumber" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="VAT Number">
              <el-input v-model="editForm.vatNumber" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Phone">
              <el-input v-model="editForm.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Email">
              <el-input v-model="editForm.email" type="email" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">Address</el-divider>
        <el-form-item label="Address Line 1" prop="address.addressLine1" required>
          <el-input v-model="editForm.address.addressLine1" />
        </el-form-item>
        <el-form-item label="Address Line 2">
          <el-input v-model="editForm.address.addressLine2" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="City" prop="address.city" required>
              <el-input v-model="editForm.address.city" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="County">
              <el-input v-model="editForm.address.county" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Postcode" prop="address.postcode" required>
          <el-input v-model="editForm.address.postcode" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="editLoading" @click="saveEdit">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.company-detail-view {
  .cis-section {
    margin-top: 16px;
  }
  
  .cis-card {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 16px;
    background: #f9fafb;
    border-radius: 8px;
    
    .label {
      font-size: 12px;
      color: #909399;
    }
    
    .value {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
