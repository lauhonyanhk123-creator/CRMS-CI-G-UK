import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElNotification } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useOfflineSync } from './useOfflineSync'

export interface PWAState {
  isOnline: boolean
  isUpdateAvailable: boolean
  updateHandler: (() => void) | null
}

export function usePWA() {
  const appStore = useAppStore()
  const offlineSync = useOfflineSync()
  
  const isOnline = ref(navigator.onLine)
  const isUpdateAvailable = ref(false)
  const updateHandler = ref<((() => void) | null)>(null)

  const showOfflineNotification = () => {
    ElNotification({
      title: 'You are offline',
      message: 'Some features may be limited. Changes will sync when you reconnect.',
      type: 'warning',
      duration: 0,
      position: 'bottom-right'
    })
  }

  const showOnlineNotification = () => {
    ElNotification({
      title: 'Back online',
      message: 'Your connection has been restored. Syncing pending changes...',
      type: 'success',
      duration: 3000,
      position: 'bottom-right'
    })
  }

  const handleOnline = async () => {
    isOnline.value = true
    appStore.setOnlineStatus(true)
    showOnlineNotification()
    
    // Sync pending operations when back online
    const hasPending = await offlineSync.hasPendingSync()
    if (hasPending) {
      // Trigger sync - the sync should be handled by the sync manager or API layer
      ElNotification({
        title: 'Sync in progress',
        message: 'Syncing your pending changes...',
        type: 'info',
        duration: 3000,
        position: 'bottom-right'
      })
    }
  }

  const handleOffline = () => {
    isOnline.value = false
    appStore.setOnlineStatus(false)
    showOfflineNotification()
  }

  const registerServiceWorker = async () => {
    if ('serviceWorker' in navigator) {
      try {
        const registration = await navigator.serviceWorker.register('/sw.js', {
          scope: '/'
        })

        registration.addEventListener('updatefound', () => {
          const newWorker = registration.installing
          
          if (newWorker) {
            newWorker.addEventListener('statechange', () => {
              if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
                isUpdateAvailable.value = true
                ElNotification({
                  title: 'Update available',
                  message: 'A new version of the app is available. Refresh to update.',
                  type: 'info',
                  duration: 0,
                  onClick: () => {
                    if (updateHandler.value) {
                      updateHandler.value()
                    }
                  }
                })
              }
            })
          }
        })
      } catch (error) {
        console.error('Service worker registration failed:', error)
      }
    }
  }

  const applyUpdate = () => {
    window.location.reload()
  }

  const setupPeriodicSync = async () => {
    if ('periodicSync' in ServiceWorkerRegistration.prototype) {
      const registration = await navigator.serviceWorker.ready
      
      try {
        await (registration as any).periodicSync.register('content-sync', {
          minInterval: 60 * 60 * 1000 // 1 hour
        })
      } catch (error) {
        console.error('Periodic sync registration failed:', error)
      }
    }
  }

  const requestNotificationPermission = async (): Promise<boolean> => {
    if (!('Notification' in window)) {
      return false
    }

    if (Notification.permission === 'granted') {
      return true
    }

    const permission = await Notification.requestPermission()
    return permission === 'granted'
  }

  const showNotification = (title: string, options?: NotificationOptions) => {
    if (Notification.permission === 'granted') {
      new Notification(title, options)
    }
  }

  onMounted(() => {
    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)
    
    updateHandler.value = applyUpdate
    
    // Register service worker
    registerServiceWorker()
  })

  onUnmounted(() => {
    window.removeEventListener('online', handleOnline)
    window.removeEventListener('offline', handleOffline)
  })

  return {
    isOnline,
    isUpdateAvailable,
    handleOnline,
    handleOffline,
    applyUpdate,
    setupPeriodicSync,
    requestNotificationPermission,
    showNotification
  }
}
