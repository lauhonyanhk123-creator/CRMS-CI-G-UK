import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

export interface User {
  id: string
  name: string
  email: string
  role: string
  token: string
}

export function useAuth() {
  const authStore = useAuthStore()

  const isAuthenticated = computed(() => authStore.isAuthenticated)
  const currentUser = computed(() => authStore.user)

  const login = async (credentials: { username: string; password: string; rememberMe?: boolean }) => {
    return await authStore.login(credentials)
  }

  const logout = async () => {
    await authStore.logout()
  }

  const refreshToken = async () => {
    return await authStore.refreshTokenFn()
  }

  const hasRole = (roles: string | string[]) => {
    const userRole = authStore.user?.role
    if (!userRole) return false
    const roleArray = Array.isArray(roles) ? roles : [roles]
    return roleArray.includes(userRole)
  }

  const can = (permission: string) => {
    const role = authStore.user?.role
    if (!role) return false
    
    const permissions: Record<string, string[]> = {
      admin: ['*'],
      manager: ['read', 'write', 'update', 'delete', 'approve'],
      user: ['read', 'write', 'update'],
      viewer: ['read']
    }

    const userPermissions = permissions[role] || []
    return userPermissions.includes('*') || userPermissions.includes(permission)
  }

  return {
    isAuthenticated,
    currentUser,
    login,
    logout,
    refreshToken,
    hasRole,
    can
  }
}
