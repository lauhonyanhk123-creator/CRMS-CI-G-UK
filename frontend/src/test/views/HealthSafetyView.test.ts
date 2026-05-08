import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import HealthSafetyView from '@/views/healthsafety/HealthSafetyView.vue'

vi.mock('@/services/api', () => ({
  default: {
    healthSafety: {
      getF10Notifications: vi.fn(),
      getCPPs: vi.fn(),
      getRAMS: vi.fn(),
      getPermits: vi.fn(),
      getIncidents: vi.fn(),
      createF10: vi.fn(),
      updateF10: vi.fn(),
      deleteF10: vi.fn(),
      createCPP: vi.fn(),
      updateCPP: vi.fn(),
      deleteCPP: vi.fn(),
      createRAMS: vi.fn(),
      updateRAMS: vi.fn(),
      deleteRAMS: vi.fn(),
      createPermit: vi.fn(),
      updatePermit: vi.fn(),
      deletePermit: vi.fn(),
      updatePermitStatus: vi.fn(),
      createIncident: vi.fn(),
      updateIncident: vi.fn(),
      deleteIncident: vi.fn()
    },
    sites: {
      getAll: vi.fn()
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
import { ElMessage, ElMessageBox } from 'element-plus'

const stubs = {
  PageHeader: { template: '<div><slot name="actions" /></div>' },
  StatusBadge: { template: '<span />', props: ['status'] },
  ElCard: { template: '<div><slot /></div>' },
  ElTabs: {
    template: '<div><slot /></div>',
    props: ['modelValue'],
    emits: ['update:modelValue', 'tab-change']
  },
  ElTabPane: { template: '<div><slot /></div>', props: ['label', 'name'] },
  ElTable: { template: '<div><slot /></div>', props: ['data', 'loading', 'stripe'] },
  ElTableColumn: { template: '<div><slot :row="{}" /></div>', props: ['prop', 'label', 'width', 'fixed', 'minWidth'] },
  ElButton: { template: '<button @click="$emit(\'click\')"><slot /></button>', props: ['type', 'size', 'loading', 'icon', 'link', 'circle'], emits: ['click'] },
  ElDialog: { template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>', props: ['modelValue', 'title', 'width'], emits: ['update:modelValue'] },
  ElDrawer: { template: '<div v-if="modelValue"><slot /></div>', props: ['modelValue', 'title'], emits: ['update:modelValue'] },
  ElForm: {
    template: '<form><slot /></form>',
    props: ['model', 'rules', 'labelWidth'],
    methods: { validate: vi.fn().mockResolvedValue(true) }
  },
  ElFormItem: { template: '<div><slot /></div>', props: ['label', 'prop'] },
  ElInput: { template: '<input />', props: ['modelValue', 'type', 'rows', 'placeholder'], emits: ['update:modelValue'] },
  ElSelect: { template: '<select><slot /></select>', props: ['modelValue', 'placeholder', 'multiple', 'style'], emits: ['update:modelValue'] },
  ElOption: { template: '<option />', props: ['label', 'value'] },
  ElTag: { template: '<span><slot /></span>', props: ['type', 'size'] },
  ElIcon: { template: '<span><slot /></span>' },
  ElCheckbox: { template: '<input type="checkbox" />', props: ['modelValue'], emits: ['update:modelValue'] },
  ElRow: { template: '<div><slot /></div>', props: ['gutter'] },
  ElCol: { template: '<div><slot /></div>', props: ['span'] },
  ElDatePicker: { template: '<input type="date" />', props: ['modelValue', 'type', 'style'], emits: ['update:modelValue'] },
  ElDropdown: { template: '<div><slot /><slot name="dropdown" /></div>', props: ['trigger'], emits: ['command'] },
  ElDropdownMenu: { template: '<div><slot /></div>' },
  ElDropdownItem: { template: '<div><slot /></div>', props: ['command'] },
  Plus: { template: '<span />' },
  Search: { template: '<span />' },
  Edit: { template: '<span />' },
  Delete: { template: '<span />' },
  View: { template: '<span />' },
  ArrowDown: { template: '<span />' }
}

const mockF10Response = {
  data: {
    data: [
      { id: 'f10-1', title: 'F10 Notification 1', siteId: 'site1', status: 'draft', data: { reference: 'REF-001' }, createdAt: '2024-01-01' },
      { id: 'f10-2', title: 'F10 Notification 2', siteId: 'site2', status: 'submitted', data: { reference: 'REF-002' }, createdAt: '2024-01-02' }
    ]
  }
}

const mockPermitsResponse = {
  data: { data: [] }
}

const mockIncidentsResponse = {
  data: { data: [] }
}

const mockSitesResponse = {
  data: { data: [{ id: 'site1', name: 'Site Alpha' }, { id: 'site2', name: 'Site Beta' }] }
}

const mockContractsResponse = {
  data: { data: [{ id: 'con1', reference: 'CON-001', title: 'Test Contract' }] }
}

function makeWrapper() {
  return mount(HealthSafetyView, { global: { stubs } })
}

describe('HealthSafetyView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    ;(api.healthSafety.getF10Notifications as any).mockResolvedValue(mockF10Response)
    ;(api.healthSafety.getPermits as any).mockResolvedValue(mockPermitsResponse)
    ;(api.healthSafety.getIncidents as any).mockResolvedValue(mockIncidentsResponse)
    ;(api.sites.getAll as any).mockResolvedValue(mockSitesResponse)
    ;(api.contracts.getAll as any).mockResolvedValue(mockContractsResponse)
  })

  it('renders without crashing', () => {
    const wrapper = makeWrapper()
    expect(wrapper.exists()).toBe(true)
  })

  it('loads F10 data on mount', async () => {
    makeWrapper()
    await flushPromises()
    expect(api.healthSafety.getF10Notifications).toHaveBeenCalled()
  })

  it('loads permits on mount', async () => {
    makeWrapper()
    await flushPromises()
    expect(api.healthSafety.getPermits).toHaveBeenCalled()
  })

  it('loads incidents on mount', async () => {
    makeWrapper()
    await flushPromises()
    expect(api.healthSafety.getIncidents).toHaveBeenCalled()
  })

  it('loads sites on mount', async () => {
    makeWrapper()
    await flushPromises()
    expect(api.sites.getAll).toHaveBeenCalled()
  })

  it('loads contracts on mount', async () => {
    makeWrapper()
    await flushPromises()
    expect(api.contracts.getAll).toHaveBeenCalled()
  })

  it('stores f10Records after successful load', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    expect((wrapper.vm as any).f10Records).toHaveLength(2)
  })

  it('sets loading to false after load', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    expect((wrapper.vm as any).loading).toBe(false)
  })

  it('shows error when loadF10 fails', async () => {
    ;(api.healthSafety.getF10Notifications as any).mockRejectedValue(new Error('fail'))
    makeWrapper()
    await flushPromises()
    expect(ElMessage.error).toHaveBeenCalledWith('Failed to load F10 notifications')
  })

  it('shows error when loadSites fails', async () => {
    ;(api.sites.getAll as any).mockRejectedValue(new Error('fail'))
    makeWrapper()
    await flushPromises()
    expect(ElMessage.error).toHaveBeenCalledWith('Failed to load sites')
  })

  it('deleteF10 shows confirmation dialog before calling API', async () => {
    ;(api.healthSafety.deleteF10 as any).mockResolvedValue({})
    const wrapper = makeWrapper()
    await flushPromises()
    await (wrapper.vm as any).deleteF10('f10-1')
    expect(ElMessageBox.confirm).toHaveBeenCalledWith('Delete this F10 notification?', 'Confirm')
    expect(api.healthSafety.deleteF10).toHaveBeenCalledWith('f10-1')
  })

  it('deleteF10 shows ElMessage.error on API failure', async () => {
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')
    ;(api.healthSafety.deleteF10 as any).mockRejectedValue(new Error('delete fail'))
    const wrapper = makeWrapper()
    await flushPromises()
    await (wrapper.vm as any).deleteF10('f10-1')
    expect(ElMessage.error).toHaveBeenCalledWith('Failed to delete F10 notification')
  })

  it('deleteF10 does not call API when cancel is thrown', async () => {
    ;(ElMessageBox.confirm as any).mockRejectedValue('cancel')
    const wrapper = makeWrapper()
    await flushPromises()
    await (wrapper.vm as any).deleteF10('f10-1')
    expect(api.healthSafety.deleteF10).not.toHaveBeenCalled()
  })

  it('deleteCPP shows error on API failure (not cancel)', async () => {
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')
    ;(api.healthSafety.deleteCPP as any).mockRejectedValue(new Error('fail'))
    const wrapper = makeWrapper()
    await flushPromises()
    await (wrapper.vm as any).deleteCPP('cpp-1')
    expect(ElMessage.error).toHaveBeenCalledWith('Failed to delete CPP')
  })

  it('deleteRAMS shows error on API failure', async () => {
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')
    ;(api.healthSafety.deleteRAMS as any).mockRejectedValue(new Error('fail'))
    const wrapper = makeWrapper()
    await flushPromises()
    await (wrapper.vm as any).deleteRAMS('rams-1')
    expect(ElMessage.error).toHaveBeenCalledWith('Failed to delete RAMS')
  })

  it('deletePermit shows error on API failure', async () => {
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')
    ;(api.healthSafety.deletePermit as any).mockRejectedValue(new Error('fail'))
    const wrapper = makeWrapper()
    await flushPromises()
    await (wrapper.vm as any).deletePermit('permit-1')
    expect(ElMessage.error).toHaveBeenCalledWith('Failed to delete permit')
  })

  it('deleteIncident shows error on API failure', async () => {
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')
    ;(api.healthSafety.deleteIncident as any).mockRejectedValue(new Error('fail'))
    const wrapper = makeWrapper()
    await flushPromises()
    await (wrapper.vm as any).deleteIncident('inc-1')
    expect(ElMessage.error).toHaveBeenCalledWith('Failed to delete incident')
  })

  it('openF10Create sets editingId to null and opens dialog', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    ;(wrapper.vm as any).openF10Create()
    expect((wrapper.vm as any).editingId).toBeNull()
    expect((wrapper.vm as any).showF10Dialog).toBe(true)
  })

  it('openF10Edit sets editingId and populates form', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    const row = { id: 'f10-1', title: 'Test F10', siteId: 'site1', status: 'draft', data: { description: '', submittedDate: '', reference: 'REF-X' } }
    ;(wrapper.vm as any).openF10Edit(row)
    expect((wrapper.vm as any).editingId).toBe('f10-1')
    expect((wrapper.vm as any).f10Form.title).toBe('Test F10')
    expect((wrapper.vm as any).showF10Dialog).toBe(true)
  })

  it('handleTabChange calls loadCPPs when tab is cpp', async () => {
    ;(api.healthSafety.getCPPs as any).mockResolvedValue({ data: { data: [] } })
    const wrapper = makeWrapper()
    await flushPromises()
    ;(wrapper.vm as any).handleTabChange('cpp')
    expect(api.healthSafety.getCPPs).toHaveBeenCalled()
  })

  it('handleTabChange calls loadRAMS when tab is rams', async () => {
    ;(api.healthSafety.getRAMS as any).mockResolvedValue({ data: { data: [] } })
    const wrapper = makeWrapper()
    await flushPromises()
    ;(wrapper.vm as any).handleTabChange('rams')
    expect(api.healthSafety.getRAMS).toHaveBeenCalled()
  })

  it('getSiteName returns site name from sites list', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    expect((wrapper.vm as any).getSiteName('site1')).toBe('Site Alpha')
  })

  it('getSiteName returns dash for unknown site', async () => {
    const wrapper = makeWrapper()
    await flushPromises()
    expect((wrapper.vm as any).getSiteName('unknown')).toBe('—')
  })
})
