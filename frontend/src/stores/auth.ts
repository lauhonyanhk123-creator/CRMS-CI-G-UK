import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import api from '@/services/api'

export interface User {
  id: string
  username: string
  email: string
  firstName?: string
  lastName?: string
  roles: string[]
  enabled?: boolean
  mustChangePassword?: boolean
}

export interface LoginCredentials {
  username: string
  password: string
  rememberMe?: boolean
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  const mustChangePassword = computed(() => !!user.value?.mustChangePassword)

  const login = async (credentials: LoginCredentials): Promise<boolean> => {
    try {
      const response = await api.auth.login({
        username: credentials.username,
        password: credentials.password
      })

      const data = response.data as any
      user.value = data.user
      token.value = data.token
      refreshToken.value = data.refreshToken

      ElMessage.success('Login successful')
      return true
    } catch (error) {
      const axiosError = error as AxiosError
      const errData = axiosError.response?.data as any
      const message = (errData && typeof errData === 'object' && 'message' in errData)
        ? errData.message
        : 'Login failed'
      ElMessage.error(message)
      return false
    }
  }

  const logout = async (): Promise<void> => {
    try {
      if (token.value) {
        await api.auth.logout()
      }
    } catch {
      // Ignore logout API errors
    } finally {
      user.value = null
      token.value = null
      refreshToken.value = null
    }
  }

  const refreshTokenFn = async (): Promise<boolean> => {
    if (!refreshToken.value) return false

    try {
      const response = await api.auth.refreshToken(refreshToken.value)
      const data = response.data as any
      token.value = data.token
      refreshToken.value = data.refreshToken
      return true
    } catch {
      await logout()
      return false
    }
  }

  const getProfile = async (): Promise<void> => {
    try {
      const response = await api.auth.getProfile()
      user.value = response.data
    } catch (error) {
      console.error('Failed to fetch profile:', error)
    }
  }

  const setAuth = (authData: { user: User; token: string; refreshToken?: string }) => {
    user.value = authData.user
    token.value = authData.token
    refreshToken.value = authData.refreshToken || null
  }

  return {
    user,
    token,
    refreshToken,
    isAuthenticated,
    mustChangePassword,
    login,
    logout,
    refreshTokenFn,
    getProfile,
    setAuth
  }
}, {
  persist: {
    key: 'crms-auth',
    storage: localStorage,
    pick: ['user', 'token', 'refreshToken']
  }
})
