import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import LoginView from '@/views/login/LoginView.vue'
import { useAuthStore } from '@/stores/auth'

vi.mock('@/services/api', () => ({
  default: {
    auth: {
      login: vi.fn(),
      logout: vi.fn(),
      refreshToken: vi.fn(),
      getProfile: vi.fn()
    }
  }
}))

import api from '@/services/api'

// Stub Element Plus components so we don't need a full install
const stubs = {
  ElCard: { template: '<div><slot /></div>' },
  ElForm: {
    template: '<form @submit.prevent><slot /></form>',
    methods: {
      validate(cb: (v: boolean) => void) { cb(true) }
    }
  },
  ElFormItem: { template: '<div><slot /></div>' },
  ElInput: {
    template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
    props: ['modelValue'],
    emits: ['update:modelValue']
  },
  ElButton: { template: '<button @click="$emit(\'click\')"><slot /></button>', emits: ['click'] },
  ElCheckbox: { template: '<input type="checkbox" />' },
  ElLink: { template: '<a><slot /></a>' },
  ElDivider: { template: '<hr />' },
  ElIcon: { template: '<span />' },
  Document: { template: '<span />' },
  User: { template: '<span />' },
  Lock: { template: '<span />' }
}

function makeWrapper() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/login', component: LoginView },
      { path: '/dashboard', component: { template: '<div>Dashboard</div>' } }
    ]
  })
  router.push('/login')

  return { router }
}

describe('LoginView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('renders without crashing', async () => {
    const { router } = makeWrapper()
    const wrapper = mount(LoginView, {
      global: { plugins: [router], stubs }
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('disables sign-in button when form is empty', () => {
    const { router } = makeWrapper()
    const wrapper = mount(LoginView, {
      global: { plugins: [router], stubs }
    })
    // isFormValid is false with empty fields, button should have disabled attribute
    expect((wrapper.vm as any).isFormValid).toBe(false)
  })

  it('isFormValid becomes true with valid username and password', async () => {
    const { router } = makeWrapper()
    const wrapper = mount(LoginView, {
      global: { plugins: [router], stubs }
    })
    // Access the reactive form directly
    ;(wrapper.vm as any).form.username = 'admin@crms.local'
    ;(wrapper.vm as any).form.password = 'Admin123!'
    await wrapper.vm.$nextTick()
    expect((wrapper.vm as any).isFormValid).toBe(true)
  })

  it('calls authStore.login on handleLogin with correct credentials', async () => {
    const store = useAuthStore()
    ;(api.auth.login as any).mockResolvedValue({
      data: {
        user: { id: '1', username: 'admin', email: 'admin@crms.local', roles: ['admin'] },
        token: 'jwt-tok',
        refreshToken: 'ref'
      }
    })

    const { router } = makeWrapper()
    const wrapper = mount(LoginView, {
      global: { plugins: [router], stubs }
    })

    ;(wrapper.vm as any).form.username = 'admin@crms.local'
    ;(wrapper.vm as any).form.password = 'Admin123!'
    await (wrapper.vm as any).handleLogin()
    await flushPromises()

    expect(api.auth.login).toHaveBeenCalledWith({
      username: 'admin@crms.local',
      password: 'Admin123!'
    })
    expect(store.isAuthenticated).toBe(true)
  })

  it('stays on login page when authentication fails', async () => {
    ;(api.auth.login as any).mockRejectedValue({
      response: { data: { message: 'Invalid credentials' } }
    })

    const { router } = makeWrapper()
    const wrapper = mount(LoginView, {
      global: { plugins: [router], stubs }
    })

    ;(wrapper.vm as any).form.username = 'bad@crms.local'
    ;(wrapper.vm as any).form.password = 'wrongpass'
    await (wrapper.vm as any).handleLogin()
    await flushPromises()

    expect((wrapper.vm as any).loading).toBe(false)
    const store = useAuthStore()
    expect(store.isAuthenticated).toBe(false)
  })

  it('demoLogin populates credentials and calls login', async () => {
    ;(api.auth.login as any).mockResolvedValue({
      data: {
        user: { id: '1', username: 'admin@crms.com', email: 'admin@crms.com', roles: ['admin'] },
        token: 'tok',
        refreshToken: 'ref'
      }
    })

    const { router } = makeWrapper()
    const wrapper = mount(LoginView, {
      global: { plugins: [router], stubs }
    })

    await (wrapper.vm as any).demoLogin('admin')
    await flushPromises()

    expect((wrapper.vm as any).form.username).toBe('admin@crms.com')
    expect(api.auth.login).toHaveBeenCalled()
  })
})
