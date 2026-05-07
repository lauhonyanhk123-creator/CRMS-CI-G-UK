import { config } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, vi } from 'vitest'

// Auto-create a fresh pinia before each test
beforeEach(() => {
  setActivePinia(createPinia())
})

// Stub Element Plus message helpers globally so tests don't throw
vi.mock('element-plus', async () => {
  const actual = await vi.importActual<typeof import('element-plus')>('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn()
    },
    ElMessageBox: {
      confirm: vi.fn().mockResolvedValue('confirm'),
      alert: vi.fn().mockResolvedValue(undefined)
    }
  }
})

// Stub vue-router
vi.mock('vue-router', async () => {
  const actual = await vi.importActual<typeof import('vue-router')>('vue-router')
  return {
    ...actual,
    useRouter: () => ({
      push: vi.fn(),
      replace: vi.fn(),
      back: vi.fn()
    }),
    useRoute: () => ({
      path: '/',
      params: {},
      query: {}
    })
  }
})

config.global.stubs = {
  teleport: true
}
