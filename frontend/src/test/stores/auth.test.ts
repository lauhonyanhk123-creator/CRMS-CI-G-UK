import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

// Mock the api service
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

// Mock vue-router (already done in setup, but explicit here for clarity)
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({ path: '/' })
}))

import api from '@/services/api'

const mockUser = {
  id: '1',
  username: 'testuser',
  email: 'test@example.com',
  roles: ['user'],
  firstName: 'Test',
  lastName: 'User'
}

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts unauthenticated', () => {
    const store = useAuthStore()
    expect(store.isAuthenticated).toBe(false)
    expect(store.user).toBeNull()
    expect(store.token).toBeNull()
  })

  it('sets auth state on successful login', async () => {
    const store = useAuthStore()
    ;(api.auth.login as any).mockResolvedValue({
      data: { user: mockUser, token: 'jwt-abc', refreshToken: 'refresh-xyz' }
    })

    const result = await store.login({ username: 'testuser', password: 'pass' })

    expect(result).toBe('ok')
    expect(store.isAuthenticated).toBe(true)
    expect(store.token).toBe('jwt-abc')
    expect(store.user?.username).toBe('testuser')
  })

  it('returns false and keeps state clear on failed login', async () => {
    const store = useAuthStore()
    ;(api.auth.login as any).mockRejectedValue({
      response: { data: { message: 'Invalid credentials' } }
    })

    const result = await store.login({ username: 'bad', password: 'wrong' })

    expect(result).toBe(false)
    expect(store.isAuthenticated).toBe(false)
    expect(store.token).toBeNull()
  })

  it('clears state on logout', async () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser, token: 'jwt-abc', refreshToken: 'refresh-xyz' })
    ;(api.auth.logout as any).mockResolvedValue({})

    await store.logout()

    expect(store.isAuthenticated).toBe(false)
    expect(store.user).toBeNull()
    expect(store.token).toBeNull()
    expect(store.refreshToken).toBeNull()
  })

  it('clears state on logout even if API call throws', async () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser, token: 'jwt-abc' })
    ;(api.auth.logout as any).mockRejectedValue(new Error('network error'))

    await store.logout()

    expect(store.isAuthenticated).toBe(false)
    expect(store.token).toBeNull()
  })

  it('refreshes token and updates credentials', async () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser, token: 'old-token', refreshToken: 'old-refresh' })
    ;(api.auth.refreshToken as any).mockResolvedValue({
      data: { token: 'new-token', refreshToken: 'new-refresh' }
    })

    const result = await store.refreshTokenFn()

    expect(result).toBe(true)
    expect(store.token).toBe('new-token')
  })

  it('logs out when token refresh fails', async () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser, token: 'old', refreshToken: 'old-refresh' })
    ;(api.auth.refreshToken as any).mockRejectedValue(new Error('expired'))
    ;(api.auth.logout as any).mockResolvedValue({})

    const result = await store.refreshTokenFn()

    expect(result).toBe(false)
    expect(store.isAuthenticated).toBe(false)
  })

  it('returns false immediately if no refresh token present', async () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser, token: 'tok' })

    const result = await store.refreshTokenFn()

    expect(result).toBe(false)
    expect(api.auth.refreshToken).not.toHaveBeenCalled()
  })

  it('reports mustChangePassword when flag is set on user', () => {
    const store = useAuthStore()
    store.setAuth({ user: { ...mockUser, mustChangePassword: true }, token: 'tok' })
    expect(store.mustChangePassword).toBe(true)
  })

  it('reports mustChangePassword as false by default', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser, token: 'tok' })
    expect(store.mustChangePassword).toBe(false)
  })
})
