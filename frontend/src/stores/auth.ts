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
  totpEnabled?: boolean
}

export interface LoginCredentials {
  username: string
  password: string
  rememberMe?: boolean
}

export interface TotpSetupData {
  secret: string
  qrDataUri: string
  issuer: string
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const totpChallengeToken = ref<string | null>(null)
  const totpRequired = ref(false)

  const isAuthenticated = computed(() => !!token.value)
  const mustChangePassword = computed(() => !!user.value?.mustChangePassword)

  const login = async (credentials: LoginCredentials): Promise<'ok' | 'totp' | false> => {
    try {
      const response = await api.auth.login({
        username: credentials.username,
        password: credentials.password
      })

      const data = response.data as any

      if (data.requiresTotp) {
        // Password correct but TOTP challenge required
        totpChallengeToken.value = data.totpChallengeToken
        totpRequired.value = true
        user.value = data.user
        return 'totp'
      }

      user.value = data.user
      token.value = data.token
      refreshToken.value = data.refreshToken
      totpChallengeToken.value = null
      totpRequired.value = false

      ElMessage.success('Login successful')
      return 'ok'
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

  const completeTotpChallenge = async (code: string): Promise<boolean> => {
    if (!totpChallengeToken.value) return false
    try {
      const response = await api.auth.totpChallenge(totpChallengeToken.value, code)
      const data = response.data as any
      user.value = data.user
      token.value = data.token
      refreshToken.value = data.refreshToken
      totpChallengeToken.value = null
      totpRequired.value = false
      ElMessage.success('Login successful')
      return true
    } catch (error) {
      const axiosError = error as AxiosError
      const errData = axiosError.response?.data as any
      const message = (errData && typeof errData === 'object' && 'message' in errData)
        ? errData.message
        : 'Invalid code'
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
      totpChallengeToken.value = null
      totpRequired.value = false
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
    totpChallengeToken,
    totpRequired,
    isAuthenticated,
    mustChangePassword,
    login,
    completeTotpChallenge,
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
