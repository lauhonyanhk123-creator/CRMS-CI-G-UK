import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'

export type Permission = 'create' | 'read' | 'update' | 'delete' | 'approve' | 'admin' | 'manager'

export interface RolePermissions {
  [key: string]: Permission[]
}

const rolePermissions: RolePermissions = {
  admin: ['create', 'read', 'update', 'delete', 'approve', 'admin'],
  manager: ['create', 'read', 'update', 'delete', 'approve'],
  user: ['create', 'read', 'update'],
  viewer: ['read']
}

export function usePermission() {
  const authStore = useAuthStore()
  const { user } = storeToRefs(authStore)

  const userRole = computed(() => user.value?.roles?.[0] || 'viewer')

  const permissions = computed(() => {
    return rolePermissions[userRole.value] || rolePermissions.viewer
  })

  const hasPermission = (permission: Permission): boolean => {
    return permissions.value.includes(permission)
  }

  const hasAnyPermission = (perms: Permission[]): boolean => {
    return perms.some(perm => hasPermission(perm))
  }

  const hasAllPermissions = (perms: Permission[]): boolean => {
    return perms.every(perm => hasPermission(perm))
  }

  const isAdmin = computed(() => hasPermission('admin'))
  const isManager = computed(() => userRole.value === 'manager' || isAdmin.value)
  const canCreate = computed(() => hasPermission('create'))
  const canUpdate = computed(() => hasPermission('update'))
  const canDelete = computed(() => hasPermission('delete'))
  const canApprove = computed(() => hasPermission('approve'))

  // Permission directive helper
  const can = (permission: Permission | Permission[]): boolean => {
    if (Array.isArray(permission)) {
      return hasAnyPermission(permission)
    }
    return hasPermission(permission)
  }

  return {
    userRole,
    permissions,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    isAdmin,
    isManager,
    canCreate,
    canUpdate,
    canDelete,
    canApprove,
    can
  }
}
