import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import AdoptionView from '@/views/adoption/AdoptionView.vue'

vi.mock('@/services/api', () => ({
  default: {
    adoption: {
      getAll: vi.fn(),
      getBonds: vi.fn(),
      getCommutedSums: vi.fn(),
      getSnaggingItems: vi.fn(),
      create: vi.fn(),
      update: vi.fn(),
      delete: vi.fn(),
      createBond: vi.fn(),
      updateBond: vi.fn(),
      deleteBond: vi.fn(),
      releaseBond: vi.fn(),
      callBond: vi.fn(),
      createCommutedSum: vi.fn(),
      updateCommutedSum: vi.fn(),
      deleteCommutedSum: vi.fn(),
      createSnaggingItem: vi.fn(),
      updateSnaggingItem: vi.fn(),
      deleteSnaggingItem: vi.fn(),
      updateSnaggingStatus: vi.fn()
    },
    companies: {
      getAll: vi.fn()
    }
  }
}))

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn() },
  ElMessageBox: { confirm: vi.fn().mockResolvedValue('confirm') }
}))

import api from '@/services/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const stubs = {
  PageHeader: { template: '<div><slot name="actions" /></div>' },
  StatusBadge: { template: '<span />', props: ['status'] },
  ElCard: { template: '<div><slot /></div>' },
  ElRow: { template: '<div><slot /></div>', props: ['gutter'] },
  ElCol: { template: '<div><slot /></div>', props: ['span'] },
  ElTabs: { template: '<div><slot /></div>', props: ['modelValue'], emits: ['update:modelValue'] },
  ElTabPane: { template: '<div><slot /></div>', props: ['label', 'name'] },
  ElTable: { template: '<div><slot /></div>', props: ['data', 'loading', 'stripe', 'size', 'border'] },
  ElTableColumn: { template: '<div />', props: ['prop', 'label', 'width', 'fixed', 'type', 'minWidth', 'showOverflowTooltip'] },
  ElButton: { template: '<button @click="$emit(\'click\')"><slot /></button>', props: ['type', 'size', 'loading', 'icon', 'link', 'circle'], emits: ['click'] },
  ElDialog: { template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>', props: ['modelValue', 'title', 'width'], emits: ['update:modelValue'] },
  ElForm: { template: '<form><slot /></form>', props: ['model', 'labelWidth'] },
  ElFormItem: { template: '<div><slot /></div>', props: ['label', 'required'] },
  ElInput: { template: '<input />', props: ['modelValue', 'type', 'rows'], emits: ['update:modelValue'] },
  ElSelect: { template: '<select><slot /></select>', props: ['modelValue', 'filterable', 'style'], emits: ['update:modelValue', 'change'] },
  ElOption: { template: '<option />', props: ['label', 'value'] },
  ElTag: { template: '<span><slot /></span>', props: ['type', 'size'] },
  ElIcon: { template: '<span />' },
  ElTimeline: { template: '<div><slot /></div>' },
  ElTimelineItem: { template: '<div><slot /></div>', props: ['type', 'timestamp'] },
  ElDescriptions: { template: '<div><slot /></div>', props: ['column', 'border', 'size'] },
  ElDescriptionsItem: { template: '<div><slot /></div>', props: ['label', 'span'] },
  ElPagination: { template: '<div />', props: ['currentPage', 'pageSize', 'total', 'layout'], emits: ['update:currentPage', 'current-change'] },
  ElInputNumber: { template: '<input type="number" />', props: ['modelValue', 'min', 'precision', 'style'], emits: ['update:modelValue'] },
  ElDatePicker: { template: '<input type="date" />', props: ['modelValue', 'type', 'valueFormat', 'style'], emits: ['update:modelValue'] },
  ElDropdown: { template: '<div><slot /><slot name="dropdown" /></div>', props: ['trigger'], emits: ['command'] },
  ElDropdownMenu: { template: '<div><slot /></div>' },
  ElDropdownItem: { template: '<div @click="$emit(\'click\')"><slot /></div>', emits: ['click'] },
  Plus: { template: '<span />' },
  Search: { template: '<span />' },
  WarningFilled: { template: '<span />' },
  Edit: { template: '<span />' },
  Delete: { template: '<span />' },
  View: { template: '<span />' }
}

const mockCasesResponse = {
  data: {
    data: [
      { id: 'case1', caseRef: 'ADO-001', type: 's38', title: 'Test Adoption', clientId: 'comp1', status: 'application' }
    ],
    total: 1
  }
}

const mockBondsResponse = {
  data: { data: [], total: 0 }
}

const mockCommutedSumsResponse = {
  data: { data: [], total: 0 }
}

const mockSnaggingResponse = {
  data: { data: [], total: 0 }
}

const mockCompaniesResponse = {
  data: { data: [{ id: 'comp1', name: 'Test Company' }] }
}

function makeWrapper() {
  return mount(AdoptionView, { global: { stubs } })
}

describe('AdoptionView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    ;(api.adoption.getAll as any).mockResolvedValue(mockCasesResponse)
    ;(api.adoption.getBonds as any).mockResolvedValue(mockBondsResponse)
    ;(api.adoption.getCommutedSums as any).mockResolvedValue(mockCommutedSumsResponse)
    ;(api.adoption.getSnaggingItems as any).mockResolvedValue(mockSnaggingResponse)
    ;(api.companies.getAll as any).mockResolvedValue(mockCompaniesResponse)
  })

  it('renders without crashing', () => {
    const wrapper = makeWrapper()
    expect(wrapper.exists()).toBe(true)
  })

  it('calls api.adoption.getAll on mount', async () => {
    makeWrapper()
    await flushPromises()
    expect(api.adoption.getAll).toHaveBeenCalled()
  })

  it('calls api.adoption.getBonds on mount', async () => {
    makeWrapper()
    await flushPromises()
    expect(api.adoption.getBonds).toHaveBeenCalled()
  })

  it('calls api.adoption.getCommutedSums on mount', async () => {
    makeWrapper()
    await flushPromises()
    expect(api.adoption.getCommutedSums).toHaveBeenCalled()
  })

  it('calls api.adoption.getSnaggingItems on mount', async () => {
    makeWrapper()
    await flushPromises()
    expect(api.adoption.getSnaggingItems).toHaveBeenCalled()
  })

  it('calls api.companies.getAll on mount', async () => {
    makeWrapper()
    await flushPromises()
    expect(api.companies.getAll).toHaveBeenCalledWith({ limit: 500 })
  })

  it('displays adoption cases after successful load', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    expect((wrapper.vm as any).cases).toHaveLength(1)
    expect((wrapper.vm as any).cases[0].caseRef).toBe('ADO-001')
  })

  it('sets loading to false after data loads', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    expect((wrapper.vm as any).loading).toBe(false)
  })

  it('shows error message when loadCases fails', async () => {
    ;(api.adoption.getAll as any).mockRejectedValue(new Error('Network fail'))
    makeWrapper()
    await flushPromises()
    expect(ElMessage.error).toHaveBeenCalledWith('Failed to load adoption cases')
  })

  it('shows error message when loadBonds fails', async () => {
    ;(api.adoption.getBonds as any).mockRejectedValue(new Error('fail'))
    makeWrapper()
    await flushPromises()
    expect(ElMessage.error).toHaveBeenCalledWith('Failed to load bonds')
  })

  it('openCaseDialog sets caseDialogVisible to true', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    ;(wrapper.vm as any).openCaseDialog()
    expect((wrapper.vm as any).caseDialogVisible).toBe(true)
    expect((wrapper.vm as any).editingId).toBeNull()
  })

  it('openCaseDialog with existing row populates form and sets editingId', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    const row = { id: 'case1', caseRef: 'ADO-001', type: 's38', title: 'Test', clientId: 'comp1', status: 'application' }
    ;(wrapper.vm as any).openCaseDialog(row)
    expect((wrapper.vm as any).editingId).toBe('case1')
    expect((wrapper.vm as any).caseForm.caseRef).toBe('ADO-001')
  })

  it('deleteCase calls ElMessageBox.confirm then api.adoption.delete', async () => {
    ;(api.adoption.delete as any).mockResolvedValue({})
    const wrapper = makeWrapper()
    await flushPromises()
    await (wrapper.vm as any).deleteCase('case1')
    expect(ElMessageBox.confirm).toHaveBeenCalled()
    expect(api.adoption.delete).toHaveBeenCalledWith('case1')
  })

  it('deleteCase calls loadCases after successful delete', async () => {
    ;(api.adoption.delete as any).mockResolvedValue({})
    const wrapper = makeWrapper()
    await flushPromises()
    vi.clearAllMocks()
    ;(api.adoption.getAll as any).mockResolvedValue(mockCasesResponse)
    ;(api.adoption.delete as any).mockResolvedValue({})
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')
    await (wrapper.vm as any).deleteCase('case1')
    await flushPromises()
    expect(api.adoption.getAll).toHaveBeenCalled()
  })

  it('deleteCase does not call api.adoption.delete when cancel is thrown', async () => {
    ;(ElMessageBox.confirm as any).mockRejectedValue('cancel')
    const wrapper = makeWrapper()
    await flushPromises()
    await (wrapper.vm as any).deleteCase('case1')
    expect(api.adoption.delete).not.toHaveBeenCalled()
  })

  it('saveCase calls api.adoption.create when editingId is null', async () => {
    ;(api.adoption.create as any).mockResolvedValue({})
    const wrapper = makeWrapper()
    await flushPromises()
    ;(wrapper.vm as any).editingId = null
    ;(wrapper.vm as any).caseForm.title = 'New Case'
    await (wrapper.vm as any).saveCase()
    expect(api.adoption.create).toHaveBeenCalled()
  })

  it('saveCase calls api.adoption.update when editingId is set', async () => {
    ;(api.adoption.update as any).mockResolvedValue({})
    const wrapper = makeWrapper()
    await flushPromises()
    ;(wrapper.vm as any).editingId = 'case1'
    await (wrapper.vm as any).saveCase()
    expect(api.adoption.update).toHaveBeenCalledWith('case1', expect.anything())
  })

  it('getClientName returns company name from companies list', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    expect((wrapper.vm as any).getClientName('comp1')).toBe('Test Company')
  })

  it('getClientName returns dash for unknown client', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    expect((wrapper.vm as any).getClientName('unknown')).toBe('—')
  })
})
