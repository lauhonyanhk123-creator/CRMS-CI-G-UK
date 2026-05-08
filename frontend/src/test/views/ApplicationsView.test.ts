import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import ApplicationsView from '@/views/applications/ApplicationsView.vue'

vi.mock('@/services/api', () => ({
  default: {
    applicationsForPayment: {
      getAll: vi.fn(),
      getById: vi.fn(),
      create: vi.fn(),
      submit: vi.fn(),
      approve: vi.fn(),
      reject: vi.fn(),
      markPaid: vi.fn()
    },
    contracts: {
      getAll: vi.fn()
    }
  }
}))

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn() },
  ElMessageBox: { confirm: vi.fn().mockResolvedValue('confirm') }
}))

import api from '@/services/api'
import { ElMessage } from 'element-plus'

const stubs = {
  PageHeader: { template: '<div><slot name="actions" /></div>' },
  StatusBadge: { template: '<span />', props: ['status'] },
  ElCard: { template: '<div><slot /></div>' },
  ElTabs: { template: '<div><slot /></div>', props: ['modelValue'], emits: ['update:modelValue', 'tab-change'] },
  ElTabPane: { template: '<div><slot /></div>', props: ['label', 'name'] },
  ElTable: { template: '<div><slot /></div>', props: ['data', 'loading', 'stripe'] },
  ElTableColumn: { template: '<div><slot :row="{}" /></div>', props: ['prop', 'label', 'width', 'fixed', 'align', 'minWidth'] },
  ElButton: { template: '<button @click="$emit(\'click\')"><slot /></button>', props: ['type', 'size', 'loading', 'icon', 'link', 'circle'], emits: ['click'] },
  ElDialog: { template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>', props: ['modelValue', 'title', 'width', 'closeOnClickModal'], emits: ['update:modelValue'] },
  ElForm: { template: '<form><slot /></form>', props: ['model', 'labelWidth'] },
  ElFormItem: { template: '<div><slot /></div>', props: ['label', 'required'] },
  ElInput: { template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />', props: ['modelValue', 'type', 'rows', 'placeholder'], emits: ['update:modelValue'] },
  ElSelect: { template: '<select><slot /></select>', props: ['modelValue', 'placeholder', 'filterable', 'style'], emits: ['update:modelValue'] },
  ElOption: { template: '<option />', props: ['label', 'value', 'key'] },
  ElTag: { template: '<span><slot /></span>', props: ['type', 'size'] },
  ElLink: { template: '<a @click="$emit(\'click\')"><slot /></a>', props: ['type'], emits: ['click'] },
  ElEmpty: { template: '<div />', props: ['description'] },
  ElDivider: { template: '<hr />', props: ['contentPosition'] },
  ElDescriptions: { template: '<div><slot /></div>', props: ['column', 'border'] },
  ElDescriptionsItem: { template: '<div><slot /></div>', props: ['label', 'span'] },
  ElInputNumber: { template: '<input type="number" />', props: ['modelValue', 'min', 'precision', 'controls'], emits: ['update:modelValue', 'change'] },
  ElDatePicker: { template: '<input type="date" />', props: ['modelValue', 'type', 'format', 'valueFormat', 'style'], emits: ['update:modelValue'] },
  Plus: { template: '<span />' },
  View: { template: '<span />' },
  Check: { template: '<span />' },
  Close: { template: '<span />' },
  Money: { template: '<span />' }
}

function makeWrapper() {
  return mount(ApplicationsView, {
    global: { stubs }
  })
}

const mockApplicationsResponse = {
  data: {
    data: [
      { id: '1', applicationRef: 'APP-001', contractId: 'c1', status: 'SUBMITTED', valueOfWorks: 10000 },
      { id: '2', applicationRef: 'APP-002', contractId: 'c2', status: 'APPROVED', valueOfWorks: 20000 }
    ],
    total: 2
  }
}

const mockContractsResponse = {
  data: {
    data: [
      { id: 'c1', reference: 'CON-001', title: 'Test Contract' }
    ]
  }
}

describe('ApplicationsView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    ;(api.applicationsForPayment.getAll as any).mockResolvedValue(mockApplicationsResponse)
    ;(api.contracts.getAll as any).mockResolvedValue(mockContractsResponse)
  })

  it('renders without crashing', () => {
    const wrapper = makeWrapper()
    expect(wrapper.exists()).toBe(true)
  })

  it('calls api.applicationsForPayment.getAll on mount', async () => {
    makeWrapper()
    await flushPromises()
    expect(api.applicationsForPayment.getAll).toHaveBeenCalled()
  })

  it('calls api.contracts.getAll on mount', async () => {
    makeWrapper()
    await flushPromises()
    expect(api.contracts.getAll).toHaveBeenCalledWith({ limit: 100 })
  })

  it('starts in loading state then resolves data', async () => {
    const wrapper = makeWrapper()
    expect((wrapper.vm as any).loading).toBe(true)
    await flushPromises()
    expect((wrapper.vm as any).loading).toBe(false)
    expect((wrapper.vm as any).tableData).toHaveLength(2)
  })

  it('stores applications in tableData after successful load', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    expect((wrapper.vm as any).tableData[0].applicationRef).toBe('APP-001')
    expect((wrapper.vm as any).total).toBe(2)
  })

  it('shows error message when API call fails', async () => {
    ;(api.applicationsForPayment.getAll as any).mockRejectedValue(new Error('Network error'))
    makeWrapper()
    await flushPromises()
    expect(ElMessage.error).toHaveBeenCalledWith('Failed to load applications')
  })

  it('sets loading to false after API failure', async () => {
    ;(api.applicationsForPayment.getAll as any).mockRejectedValue(new Error('fail'))
    const wrapper = makeWrapper()
    await flushPromises()
    expect((wrapper.vm as any).loading).toBe(false)
  })

  it('openNewDialog sets showDialog to true with create mode', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    ;(wrapper.vm as any).openNewDialog()
    expect((wrapper.vm as any).showDialog).toBe(true)
    expect((wrapper.vm as any).dialogMode).toBe('create')
  })

  it('handleTabChange reloads data', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    vi.clearAllMocks()
    ;(api.applicationsForPayment.getAll as any).mockResolvedValue(mockApplicationsResponse)
    ;(wrapper.vm as any).handleTabChange()
    await flushPromises()
    expect(api.applicationsForPayment.getAll).toHaveBeenCalled()
  })

  it('loadData passes correct status filter based on activeTab', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    vi.clearAllMocks()
    ;(api.applicationsForPayment.getAll as any).mockResolvedValue(mockApplicationsResponse)
    ;(wrapper.vm as any).activeTab = 'approved'
    await (wrapper.vm as any).loadData()
    expect(api.applicationsForPayment.getAll).toHaveBeenCalledWith({ status: 'APPROVED' })
  })

  it('submitApplication shows warning when contractId is empty', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    ;(wrapper.vm as any).form.contractId = ''
    await (wrapper.vm as any).submitApplication()
    expect(ElMessage.warning).toHaveBeenCalledWith('Please select a contract')
  })

  it('getRowActions returns View Details for any row', () => {
    const wrapper = makeWrapper() as any
    const actions = wrapper.vm.getRowActions({ id: '1', status: 'SUBMITTED' })
    expect(actions.some((a: any) => a.label === 'View Details')).toBe(true)
  })

  it('getRowActions returns Submit action for SUBMITTED status', () => {
    const wrapper = makeWrapper() as any
    const actions = wrapper.vm.getRowActions({ id: '1', status: 'SUBMITTED' })
    expect(actions.some((a: any) => a.label === 'Submit')).toBe(true)
  })

  it('getRowActions returns Approve and Reject for AGREED status', () => {
    const wrapper = makeWrapper() as any
    const actions = wrapper.vm.getRowActions({ id: '1', status: 'AGREED' })
    const labels = actions.map((a: any) => a.label)
    expect(labels).toContain('Approve')
    expect(labels).toContain('Reject')
  })
})
