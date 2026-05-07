import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useOfflineSync, type PendingOperation } from './useOfflineSync'
import router from '@/router'

// Cache TTL in milliseconds
const API_CACHE_TTL = 5 * 60 * 1000 // 5 minutes
const STALE_THRESHOLD = 60 * 1000 // 1 minute - consider cache stale if older than this

export interface CachedResponse {
  data: any
  timestamp: number
  status: number
  statusText: string
  headers: Record<string, string>
}

export interface OfflineApiOptions {
  cacheEnabled?: boolean
  staleWhileRevalidate?: boolean
  queueIfOffline?: boolean
  cacheKey?: string
}

const DB_NAME = 'crms-api-cache'
const DB_VERSION = 1

// IndexedDB for API caching
let cacheDb: IDBDatabase | null = null

const initCacheDb = async (): Promise<IDBDatabase> => {
  if (cacheDb) return cacheDb

  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION)
    
    request.onerror = () => reject(request.error)
    request.onsuccess = () => {
      cacheDb = request.result
      resolve(cacheDb)
    }
    
    request.onupgradeneeded = (event) => {
      const db = (event.target as IDBOpenDBRequest).result
      
      if (!db.objectStoreNames.contains('apiCache')) {
        const store = db.createObjectStore('apiCache', { keyPath: 'key' })
        store.createIndex('timestamp', 'timestamp')
      }
    }
  })
}

const getCachedResponse = async (cacheKey: string): Promise<CachedResponse | null> => {
  try {
    const db = await initCacheDb()
    return new Promise((resolve, reject) => {
      const transaction = db.transaction('apiCache', 'readonly')
      const store = transaction.objectStore('apiCache')
      const request = store.get(cacheKey)
      
      request.onerror = () => reject(request.error)
      request.onsuccess = () => {
        const result = request.result as CachedResponse | undefined
        if (result) {
          // Check if cache is too old
          const age = Date.now() - result.timestamp
          if (age > API_CACHE_TTL + STALE_THRESHOLD) {
            // Delete stale cache
            store.delete(cacheKey)
            resolve(null)
          } else {
            resolve(result)
          }
        } else {
          resolve(null)
        }
      }
    })
  } catch {
    return null
  }
}

const setCachedResponse = async (cacheKey: string, response: CachedResponse): Promise<void> => {
  try {
    const db = await initCacheDb()
    return new Promise((resolve, reject) => {
      const transaction = db.transaction('apiCache', 'readwrite')
      const store = transaction.objectStore('apiCache')
      const request = store.put({ ...response, key: cacheKey })
      
      request.onerror = () => reject(request.error)
      request.onsuccess = () => resolve()
    })
  } catch {
    // Silently fail cache writes
  }
}

const clearApiCache = async (): Promise<void> => {
  try {
    const db = await initCacheDb()
    return new Promise((resolve, reject) => {
      const transaction = db.transaction('apiCache', 'readwrite')
      const store = transaction.objectStore('apiCache')
      const request = store.clear()
      
      request.onerror = () => reject(request.error)
      request.onsuccess = () => resolve()
    })
  } catch {
    // Silently fail
  }
}

// Generate cache key from request config
const generateCacheKey = (config: AxiosRequestConfig): string => {
  const url = config.url || ''
  const params = config.params ? JSON.stringify(config.params) : ''
  const method = config.method || 'GET'
  return `${method}:${url}:${params}`
}

// Base API instance
const apiClient: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor - add JWT token
apiClient.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor - handle errors and 401
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean }
    
    // Handle 401 - unauthorized
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      
      const authStore = useAuthStore()
      
      // Try to refresh token
      if (authStore.refreshToken) {
        try {
          const refreshed = await authStore.refreshTokenFn()
          if (refreshed && originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${authStore.token}`
            return apiClient(originalRequest)
          }
        } catch {
          // Refresh failed
        }
      }
      
      // Logout and redirect to login
      await authStore.logout()
      router.push('/login')
      ElMessage.error('Session expired. Please login again.')
    }
    
    // Handle other errors
    const message = (error.response?.data as any)?.message || error.message || 'An error occurred'
    
    if (error.response?.status !== 401) {
      ElMessage.error(message)
    }
    
    return Promise.reject(error)
  }
)

// Offline-aware API wrapper
export class OfflineApiService {
  private cacheEnabled: boolean = true
  private queueIfOffline: boolean = true

  constructor(options: OfflineApiOptions = {}) {
    this.cacheEnabled = options.cacheEnabled ?? true
    this.queueIfOffline = options.queueIfOffline ?? true
  }

  private isOnline(): boolean {
    return navigator.onLine
  }

  private shouldCache(method: string): boolean {
    // Only cache GET requests
    return method.toUpperCase() === 'GET' && this.cacheEnabled
  }

  private async cacheResponse(config: AxiosRequestConfig, response: AxiosResponse): Promise<void> {
    if (!this.shouldCache(config.method || 'GET')) return

    const cacheKey = generateCacheKey(config)
    const cachedResponse: CachedResponse = {
      data: response.data,
      timestamp: Date.now(),
      status: response.status,
      statusText: response.statusText,
      headers: response.headers as Record<string, string>
    }

    await setCachedResponse(cacheKey, cachedResponse)
  }

  private async getCached<T = any>(config: AxiosRequestConfig): Promise<T | null> {
    if (!this.shouldCache(config.method || 'GET')) return null

    const cacheKey = generateCacheKey(config)
    const cached = await getCachedResponse(cacheKey)
    
    if (cached) {
      return cached.data as T
    }
    
    return null
  }

  private async queueOfflineOperation(
    method: string,
    url: string,
    data?: any,
    params?: any
  ): Promise<void> {
    const offlineSync = useOfflineSync()
    
    await offlineSync.queueOperation({
      type: method.toLowerCase() as 'create' | 'update' | 'delete',
      entity: url,
      entityId: '',
      data: { url, data, params }
    })
  }

  async request<T = any>(
    config: AxiosRequestConfig,
    options: OfflineApiOptions = {}
  ): Promise<{ data: T; source: 'network' | 'cache' | 'queued' }> {
    const cacheEnabled = options.cacheEnabled ?? this.cacheEnabled
    const staleWhileRevalidate = options.staleWhileRevalidate ?? false
    const queueIfOffline = options.queueIfOffline ?? this.queueIfOffline
    const method = config.method || 'GET'

    // If offline and this is a write operation, queue it
    if (!this.isOnline() && method.toUpperCase() !== 'GET') {
      if (queueIfOffline) {
        await this.queueOfflineOperation(method, config.url || '', config.data, config.params)
        return { 
          data: { ...config.data, _queued: true } as T, 
          source: 'queued' 
        }
      }
      throw new Error('You are offline and queuing is disabled')
    }

    // If online, make the request
    if (this.isOnline()) {
      try {
        const response = await apiClient<T>(config)
        await this.cacheResponse(config, response)
        return { data: response.data, source: 'network' }
      } catch (error) {
        // Network error - try cache
        if (axios.isAxiosError(error) && !error.response) {
          const cached = await this.getCached<T>(config)
          if (cached !== null) {
            return { data: cached, source: 'cache' }
          }
        }
        throw error
      }
    }

    // Offline - try cache
    const cached = await this.getCached<T>(config)
    if (cached !== null) {
      return { data: cached, source: 'cache' }
    }

    throw new Error('You are offline and no cached data is available')
  }

  async get<T = any>(
    url: string,
    config: AxiosRequestConfig = {},
    options: OfflineApiOptions = {}
  ): Promise<{ data: T; source: 'network' | 'cache' | 'queued' }> {
    return this.request<T>({ ...config, method: 'GET', url }, options)
  }

  async post<T = any>(
    url: string,
    data?: any,
    config: AxiosRequestConfig = {},
    options: OfflineApiOptions = {}
  ): Promise<{ data: T; source: 'network' | 'cache' | 'queued' }> {
    return this.request<T>({ ...config, method: 'POST', url, data }, options)
  }

  async put<T = any>(
    url: string,
    data?: any,
    config: AxiosRequestConfig = {},
    options: OfflineApiOptions = {}
  ): Promise<{ data: T; source: 'network' | 'cache' | 'queued' }> {
    return this.request<T>({ ...config, method: 'PUT', url, data }, options)
  }

  async delete<T = any>(
    url: string,
    config: AxiosRequestConfig = {},
    options: OfflineApiOptions = {}
  ): Promise<{ data: T; source: 'network' | 'cache' | 'queued' }> {
    return this.request<T>({ ...config, method: 'DELETE', url }, options)
  }

  async clearCache(): Promise<void> {
    await clearApiCache()
  }
}

// Singleton instance
export const offlineApi = new OfflineApiService()

// Export raw api client for non-offline-aware requests
export { apiClient }

// Re-export types from api.ts
export * from '@/services/api'
