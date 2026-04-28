import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface Breadcrumb {
  title: string
  path?: string
}

export interface Notification {
  id: string
  title: string
  message: string
  type: 'info' | 'success' | 'warning' | 'error'
  read: boolean
  createdAt: string
}

export type ThemeMode = 'light' | 'dark'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const activeMenu = ref('')
  const breadcrumbs = ref<Breadcrumb[]>([])
  const notifications = ref<Notification[]>([])
  const theme = ref<ThemeMode>('light')
  const isOnline = ref(navigator.onLine)
  const hasPendingSync = ref(false)

  const unreadNotifications = computed(() => 
    notifications.value.filter(n => !n.read).length
  )

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const setActiveMenu = (menu: string) => {
    activeMenu.value = menu
  }

  const setBreadcrumbs = (crumbs: Breadcrumb[]) => {
    breadcrumbs.value = crumbs
  }

  const addNotification = (notification: Omit<Notification, 'id' | 'createdAt' | 'read'>) => {
    notifications.value.unshift({
      ...notification,
      id: crypto.randomUUID(),
      read: false,
      createdAt: new Date().toISOString()
    })
  }

  const markNotificationRead = (id: string) => {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.read = true
    }
  }

  const clearNotifications = () => {
    notifications.value = []
  }

  const setTheme = (newTheme: ThemeMode) => {
    theme.value = newTheme
    document.documentElement.setAttribute('data-theme', newTheme)
  }

  const setOnlineStatus = (status: boolean) => {
    isOnline.value = status
  }

  const setPendingSync = (status: boolean) => {
    hasPendingSync.value = status
  }

  // Initialize online status listeners
  if (typeof window !== 'undefined') {
    window.addEventListener('online', () => setOnlineStatus(true))
    window.addEventListener('offline', () => setOnlineStatus(false))
  }

  return {
    sidebarCollapsed,
    activeMenu,
    breadcrumbs,
    notifications,
    theme,
    isOnline,
    hasPendingSync,
    unreadNotifications,
    toggleSidebar,
    setActiveMenu,
    setBreadcrumbs,
    addNotification,
    markNotificationRead,
    clearNotifications,
    setTheme,
    setOnlineStatus,
    setPendingSync
  }
}, {
  persist: {
    key: 'crms-app',
    storage: localStorage,
    paths: ['sidebarCollapsed', 'theme']
  }
})
