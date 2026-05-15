import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElNotification } from 'element-plus'
import { useOfflineSync, type PendingOperation } from './useOfflineSync'

export interface SyncManagerOptions {
  autoSync?: boolean
  syncInterval?: number
  maxRetries?: number
}

export interface SyncStatus {
  isSyncing: boolean
  lastSyncTime: number | null
  pendingCount: number
  lastError: string | null
}

export function useSyncManager(options: SyncManagerOptions = {}) {
  const {
    autoSync = true,
    syncInterval = 30000, // 30 seconds
    maxRetries = 3
  } = options

  const offlineSync = useOfflineSync()

  const isSyncing = ref(false)
  const lastSyncTime = ref<number | null>(null)
  const lastError = ref<string | null>(null)
  const pendingOperations = ref<PendingOperation[]>([])

  let syncIntervalId: ReturnType<typeof setInterval> | null = null

  const syncStatus = computed<SyncStatus>(() => ({
    isSyncing: isSyncing.value,
    lastSyncTime: lastSyncTime.value,
    pendingCount: pendingOperations.value.length,
    lastError: lastError.value
  }))

  const syncOperation = async (operation: PendingOperation): Promise<boolean> => {
    const { url, data, params } = operation.data
    
    try {
      // Import the api client dynamically to avoid circular deps
      const { apiClient } = await import('@/services/api')
      
      let response
      switch (operation.type) {
        case 'create':
          response = await apiClient.post(url, data)
          break
        case 'update':
          response = await apiClient.put(`${url}/${operation.entityId}`, data, { params })
          break
        case 'delete':
          response = await apiClient.delete(`${url}/${operation.entityId}`, { params })
          break
        default:
          throw new Error(`Unknown operation type: ${operation.type}`)
      }
      
      return response.status >= 200 && response.status < 300
    } catch (error: any) {
      // Network errors indicate we should retry
      if (!error.response) {
        throw error
      }
      // Server errors (5xx) should retry
      if (error.response?.status >= 500) {
        throw error
      }
      // Client errors (4xx) except 409 (conflict) should not retry
      if (error.response?.status >= 400 && error.response?.status !== 409) {
        console.error(`Operation failed with client error: ${error.response?.status}`, operation)
        return true // Remove from queue - won't be retried
      }
      throw error
    }
  }

  const syncAll = async (): Promise<{ success: number; failed: number }> => {
    if (isSyncing.value || !navigator.onLine) {
      return { success: 0, failed: 0 }
    }

    isSyncing.value = true
    lastError.value = null

    try {
      const pending = await offlineSync.getPendingOperations()
      pendingOperations.value = pending

      if (pending.length === 0) {
        isSyncing.value = false
        return { success: 0, failed: 0 }
      }

      const result = await offlineSync.syncPending(syncOperation)

      lastSyncTime.value = Date.now()
      lastError.value = result.failed > 0 ? `${result.failed} operations failed to sync` : null

      // Update app store sync status
      await offlineSync.hasPendingSync()

      if (result.synced > 0) {
        ElNotification({
          title: 'Sync complete',
          message: `Successfully synced ${result.synced} operation(s)`,
          type: 'success',
          duration: 3000
        })
      }

      if (result.failed > 0) {
        ElNotification({
          title: 'Sync issues',
          message: `${result.failed} operation(s) failed after ${maxRetries} retries`,
          type: 'warning',
          duration: 5000
        })
      }

      return { success: result.synced, failed: result.failed }
    } catch (error: any) {
      lastError.value = error.message || 'Sync failed'
      console.error('Sync error:', error)
      return { success: 0, failed: pendingOperations.value.length }
    } finally {
      isSyncing.value = false
      // Refresh pending count
      pendingOperations.value = await offlineSync.getPendingOperations()
    }
  }

  const refreshPending = async () => {
    pendingOperations.value = await offlineSync.getPendingOperations()
    await offlineSync.hasPendingSync()
  }

  const clearPending = async (operationId: string) => {
    await offlineSync.removePendingOperation(operationId)
    await refreshPending()
  }

  const clearAllPending = async () => {
    const ops = await offlineSync.getPendingOperations()
    for (const op of ops) {
      await offlineSync.removePendingOperation(op.id)
    }
    await refreshPending()
  }

  const startAutoSync = () => {
    if (syncIntervalId) return

    syncIntervalId = setInterval(async () => {
      if (navigator.onLine && !isSyncing.value) {
        const hasPending = await offlineSync.hasPendingSync()
        if (hasPending) {
          await syncAll()
        }
      }
    }, syncInterval)
  }

  const stopAutoSync = () => {
    if (syncIntervalId) {
      clearInterval(syncIntervalId)
      syncIntervalId = null
    }
  }

  const registerBackgroundSync = async () => {
    if ('serviceWorker' in navigator && 'sync' in ServiceWorkerRegistration.prototype) {
      try {
        const registration = await navigator.serviceWorker.ready
        await (registration as any).sync.register('sync-pending-operations')
        console.log('Background sync registered')
      } catch (error) {
        console.error('Background sync registration failed:', error)
      }
    }
  }

  onMounted(async () => {
    await refreshPending()
    
    if (autoSync) {
      startAutoSync()
    }

    // Listen for online event to trigger sync
    window.addEventListener('online', handleOnline)
  })

  onUnmounted(() => {
    stopAutoSync()
    window.removeEventListener('online', handleOnline)
  })

  const handleOnline = async () => {
    // Sync when coming back online
    setTimeout(async () => {
      await refreshPending()
      if (pendingOperations.value.length > 0) {
        await syncAll()
      }
    }, 1000) // Small delay to ensure connection is stable
  }

  return {
    syncStatus,
    syncAll,
    syncOperation,
    refreshPending,
    clearPending,
    clearAllPending,
    startAutoSync,
    stopAutoSync,
    registerBackgroundSync,
    pendingOperations
  }
}
