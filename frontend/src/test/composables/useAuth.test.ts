import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { useAuth } from '@/composables/useAuth'

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

const mockUser = (roles: string[]) => ({
  id: '1',
  username: 'testuser',
  email: 'test@crms.local',
  roles
})

describe('useAuth', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('isAuthenticated is false before login', () => {
    const { isAuthenticated } = useAuth()
    expect(isAuthenticated.value).toBe(false)
  })

  it('isAuthenticated is true after setAuth', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser(['user']), token: 'tok' })
    const { isAuthenticated } = useAuth()
    expect(isAuthenticated.value).toBe(true)
  })

  it('currentUser reflects store state', () => {
    const store = useAuthStore()
    const user = mockUser(['admin'])
    store.setAuth({ user, token: 'tok' })
    const { currentUser } = useAuth()
    expect(currentUser.value?.username).toBe('testuser')
  })

  it('hasRole returns true for matching role', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser(['manager', 'user']), token: 'tok' })
    const { hasRole } = useAuth()
    expect(hasRole('manager')).toBe(true)
    expect(hasRole('admin')).toBe(false)
  })

  it('hasRole accepts an array and uses OR logic', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser(['user']), token: 'tok' })
    const { hasRole } = useAuth()
    expect(hasRole(['admin', 'user'])).toBe(true)
    expect(hasRole(['admin', 'manager'])).toBe(false)
  })

  it('hasRole returns false when no user', () => {
    const { hasRole } = useAuth()
    expect(hasRole('admin')).toBe(false)
  })

  it('can() uses wildcard for admin role', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser(['admin']), token: 'tok' })
    const { can } = useAuth()
    expect(can('delete')).toBe(true)
    expect(can('approve')).toBe(true)
  })

  it('can() restricts non-admin roles', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser(['viewer']), token: 'tok' })
    const { can } = useAuth()
    expect(can('read')).toBe(true)
    expect(can('write')).toBe(false)
  })

  it('login delegates to auth store', async () => {
    ;(api.auth.login as any).mockResolvedValue({
      data: { user: mockUser(['user']), token: 'tok', refreshToken: 'ref' }
    })
    const { login, isAuthenticated } = useAuth()
    const result = await login({ username: 'testuser', password: 'pass' })
    expect(result).toBe('ok')
    expect(isAuthenticated.value).toBe(true)
  })

  it('logout delegates to auth store', async () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser(['user']), token: 'tok' })
    ;(api.auth.logout as any).mockResolvedValue({})

    const { logout, isAuthenticated } = useAuth()
    await logout()
    expect(isAuthenticated.value).toBe(false)
  })
})
