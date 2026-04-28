import { openDB, type IDBPDatabase } from 'idb'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'

export interface PendingOperation {
  id: string
  type: 'create' | 'update' | 'delete'
  entity: string
  entityId: string
  data: any
  timestamp: number
  retryCount: number
}

export interface CachedData {
  key: string
  data: any
  cachedAt: number
}

const DB_NAME = 'crms-offline-db'
const DB_VERSION = 1

export function useOfflineSync() {
  let db: IDBPDatabase | null = null

  const initDB = async (): Promise<IDBPDatabase> => {
    if (db) return db

    db = await openDB(DB_NAME, DB_VERSION, {
      upgrade(database) {
        // Pending sync operations store
        if (!database.objectStoreNames.contains('pendingSync')) {
          const pendingStore = database.createObjectStore('pendingSync', { keyPath: 'id' })
          pendingStore.createIndex('timestamp', 'timestamp')
          pendingStore.createIndex('entity', 'entity')
        }

        // Cached data store
        if (!database.objectStoreNames.contains('cachedData')) {
          const cacheStore = database.createObjectStore('cachedData', { keyPath: 'key' })
          cacheStore.createIndex('cachedAt', 'cachedAt')
        }
      }
    })

    return db
  }

  const queueOperation = async (
    operation: Omit<PendingOperation, 'id' | 'timestamp' | 'retryCount'>
  ): Promise<void> => {
    const database = await initDB()

    const pendingOp: PendingOperation = {
      ...operation,
      id: crypto.randomUUID(),
      timestamp: Date.now(),
      retryCount: 0
    }

    await database.add('pendingSync', pendingOp)
    const appStore = useAppStore()
    appStore.setPendingSync(true)
    ElMessage.warning('Operation queued for sync when online')
  }

  const getPendingOperations = async (): Promise<PendingOperation[]> => {
    const database = await initDB()
    return await database.getAll('pendingSync')
  }

  const getPendingOperationsByEntity = async (entity: string): Promise<PendingOperation[]> => {
    const database = await initDB()
    const index = database.transaction('pendingSync').store.index('entity')
    return await index.getAll(entity)
  }

  const removePendingOperation = async (id: string): Promise<void> => {
    const database = await initDB()
    await database.delete('pendingSync', id)
  }

  const updatePendingOperation = async (operation: PendingOperation): Promise<void> => {
    const database = await initDB()
    await database.put('pendingSync', operation)
  }

  const syncPending = async (syncFn: (op: PendingOperation) => Promise<boolean>): Promise<{
    synced: number
    failed: number
  }> => {
    const operations = await getPendingOperations()
    let synced = 0
    let failed = 0

    for (const op of operations) {
      try {
        const success = await syncFn(op)
        
        if (success) {
          await removePendingOperation(op.id)
          synced++
        } else {
          op.retryCount++
          if (op.retryCount >= 3) {
            await removePendingOperation(op.id)
            failed++
          } else {
            await updatePendingOperation(op)
          }
        }
      } catch {
        op.retryCount++
        if (op.retryCount >= 3) {
          await removePendingOperation(op.id)
          failed++
        } else {
          await updatePendingOperation(op)
        }
      }
    }

    if (synced > 0) {
      ElMessage.success(`Synced ${synced} pending operation(s)`)
    }
    if (failed > 0) {
      ElMessage.error(`Failed to sync ${failed} operation(s)`)
    }

    return { synced, failed }
  }

  const cacheData = async (key: string, data: any, ttlMs: number = 3600000): Promise<void> => {
    const database = await initDB()

    const cachedData: CachedData = {
      key,
      data,
      cachedAt: Date.now()
    }

    await database.put('cachedData', cachedData)

    // Schedule cache invalidation
    setTimeout(() => {
      invalidateCache(key)
    }, ttlMs)
  }

  const getCachedData = async <T = any>(key: string): Promise<T | null> => {
    const database = await initDB()
    const cached = await database.get('cachedData', key)

    if (!cached) return null

    return cached.data as T
  }

  const invalidateCache = async (key: string): Promise<void> => {
    const database = await initDB()
    await database.delete('cachedData', key)
  }

  const clearAllCache = async (): Promise<void> => {
    const database = await initDB()
    await database.clear('cachedData')
  }

  const getCacheSize = async (): Promise<number> => {
    const database = await initDB()
    return await database.count('cachedData')
  }

  const hasPendingSync = async (): Promise<boolean> => {
    const database = await initDB()
    const count = await database.count('pendingSync')
    const appStore = useAppStore()
    appStore.setPendingSync(count > 0)
    return count > 0
  }

  return {
    queueOperation,
    getPendingOperations,
    getPendingOperationsByEntity,
    removePendingOperation,
    updatePendingOperation,
    syncPending,
    cacheData,
    getCachedData,
    invalidateCache,
    clearAllCache,
    getCacheSize,
    hasPendingSync
  }
}
