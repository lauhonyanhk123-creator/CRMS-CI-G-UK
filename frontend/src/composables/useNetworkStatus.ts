import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useAppStore } from '@/stores/app'

export function useNetworkStatus() {
  const appStore = useAppStore()
  
  const isOnline = computed(() => appStore.isOnline)
  const isOffline = computed(() => !appStore.isOnline)
  const connectionQuality = ref<'good' | 'poor' | 'offline'>('good')
  const effectiveType = ref<string | null>(null)
  const rtt = ref<number | null>(null)
  const downlink = ref<number | null>(null)

  const updateNetworkInfo = () => {
    const connection = (navigator as any).connection || 
                       (navigator as any).mozConnection || 
                       (navigator as any).webkitConnection

    if (connection) {
      effectiveType.value = connection.effectiveType || null
      rtt.value = connection.rtt || null
      downlink.value = connection.downlink || null

      if (connection.effectiveType === 'slow-2g' || 
          connection.effectiveType === '2g' ||
          connection.rtt > 1000) {
        connectionQuality.value = 'poor'
      } else if (connection.effectiveType === '3g' || 
                 connection.rtt > 500) {
        connectionQuality.value = 'poor'
      } else {
        connectionQuality.value = 'good'
      }
    } else if (!navigator.onLine) {
      connectionQuality.value = 'offline'
    }
  }

  const handleOnline = () => {
    appStore.setOnlineStatus(true)
    updateNetworkInfo()
  }

  const handleOffline = () => {
    appStore.setOnlineStatus(false)
    connectionQuality.value = 'offline'
  }

  const checkConnection = async (): Promise<boolean> => {
    if (!navigator.onLine) {
      return false
    }

    try {
      const controller = new AbortController()
      const timeoutId = setTimeout(() => controller.abort(), 5000)
      
      const response = await fetch('/api/health', {
        method: 'HEAD',
        cache: 'no-cache',
        signal: controller.signal
      })
      
      clearTimeout(timeoutId)
      return response.ok
    } catch {
      return false
    }
  }

  const waitForConnection = (timeout: number = 30000): Promise<boolean> => {
    return new Promise((resolve) => {
      if (navigator.onLine) {
        resolve(true)
        return
      }

      const timeoutId = setTimeout(() => {
        window.removeEventListener('online', onlineHandler)
        resolve(false)
      }, timeout)

      const onlineHandler = () => {
        clearTimeout(timeoutId)
        window.removeEventListener('online', onlineHandler)
        resolve(true)
      }

      window.addEventListener('online', onlineHandler, { once: true })
    })
  }

  onMounted(() => {
    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)
    updateNetworkInfo()

    const connection = (navigator as any).connection
    if (connection) {
      connection.addEventListener('change', updateNetworkInfo)
    }
  })

  onUnmounted(() => {
    window.removeEventListener('online', handleOnline)
    window.removeEventListener('offline', handleOffline)

    const connection = (navigator as any).connection
    if (connection) {
      connection.removeEventListener('change', updateNetworkInfo)
    }
  })

  return {
    isOnline,
    isOffline,
    connectionQuality,
    effectiveType,
    rtt,
    downlink,
    checkConnection,
    waitForConnection,
    updateNetworkInfo
  }
}
