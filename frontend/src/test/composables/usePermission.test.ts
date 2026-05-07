import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'

const mockUser = (role: string) => ({
  id: '1',
  username: 'u',
  email: 'u@example.com',
  roles: [role]
})

describe('usePermission', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('viewer can only read', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser('viewer'), token: 'tok' })
    const { can, canCreate, canDelete, canApprove } = usePermission()
    expect(can('read')).toBe(true)
    expect(canCreate.value).toBe(false)
    expect(canDelete.value).toBe(false)
    expect(canApprove.value).toBe(false)
  })

  it('user can create and update but not delete or approve', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser('user'), token: 'tok' })
    const { canCreate, canUpdate, canDelete, canApprove } = usePermission()
    expect(canCreate.value).toBe(true)
    expect(canUpdate.value).toBe(true)
    expect(canDelete.value).toBe(false)
    expect(canApprove.value).toBe(false)
  })

  it('manager can create, update, delete and approve', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser('manager'), token: 'tok' })
    const { canCreate, canUpdate, canDelete, canApprove, isManager } = usePermission()
    expect(canCreate.value).toBe(true)
    expect(canUpdate.value).toBe(true)
    expect(canDelete.value).toBe(true)
    expect(canApprove.value).toBe(true)
    expect(isManager.value).toBe(true)
  })

  it('admin has all permissions including admin permission', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser('admin'), token: 'tok' })
    const { isAdmin, canDelete, canApprove, can } = usePermission()
    expect(isAdmin.value).toBe(true)
    expect(canDelete.value).toBe(true)
    expect(canApprove.value).toBe(true)
    expect(can('admin')).toBe(true)
  })

  it('isManager is true for admin', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser('admin'), token: 'tok' })
    const { isManager } = usePermission()
    expect(isManager.value).toBe(true)
  })

  it('defaults to viewer when no user is set', () => {
    const { userRole, canCreate } = usePermission()
    expect(userRole.value).toBe('viewer')
    expect(canCreate.value).toBe(false)
  })

  it('hasAnyPermission returns true if at least one matches', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser('user'), token: 'tok' })
    const { hasAnyPermission } = usePermission()
    expect(hasAnyPermission(['read', 'approve'])).toBe(true)
    expect(hasAnyPermission(['delete', 'approve'])).toBe(false)
  })

  it('hasAllPermissions requires every permission to match', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser('manager'), token: 'tok' })
    const { hasAllPermissions } = usePermission()
    expect(hasAllPermissions(['read', 'create', 'approve'])).toBe(true)
    expect(hasAllPermissions(['read', 'create', 'admin'])).toBe(false)
  })

  it('can() accepts an array and uses OR logic', () => {
    const store = useAuthStore()
    store.setAuth({ user: mockUser('viewer'), token: 'tok' })
    const { can } = usePermission()
    expect(can(['read', 'create'])).toBe(true)
    expect(can(['create', 'delete'])).toBe(false)
  })
})
