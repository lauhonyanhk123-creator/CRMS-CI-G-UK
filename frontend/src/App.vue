<script setup lang="ts">
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { usePWA } from '@/composables/usePWA'
import { useOfflineSync } from '@/composables/useOfflineSync'
import NetworkStatus from '@/components/common/NetworkStatus.vue'
import api from '@/services/api'
import {
  Menu,
  Fold,
  Expand,
  User,
  Setting,
  Bell,
  SwitchButton,
  Document,
  HomeFilled,
  OfficeBuilding,
  Connection,
  Location,
  Ticket,
  DocumentCopy,
  UserFilled,
  Tools,
  ShoppingCart,
  FirstAidKit,
  CircleCheck,
  Medal,
  ArrowDown,
  Search,
  Setting as AdminSetting
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()

// Initialize PWA and sync
const { isOnline } = usePWA()
const offlineSync = useOfflineSync()

// Sync pending operations when coming back online
watch(isOnline, async (online) => {
  if (online) {
    await offlineSync.hasPendingSync()
  }
}, { immediate: true })

const isCollapsed = computed(() => appStore.sidebarCollapsed)

const user = computed(() => authStore.user)
const isAuthenticated = computed(() => authStore.isAuthenticated)

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}

const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const activeMenu = computed(() => route.path)

const menuItems = [
  { path: '/dashboard', title: 'Dashboard', icon: HomeFilled },
  { path: '/companies', title: 'Companies', icon: OfficeBuilding },
  { path: '/contacts', title: 'Contacts', icon: Connection },
  { path: '/projects', title: 'Sites / Projects', icon: Location },
  { path: '/sites', title: 'Sites', icon: Location },
  { path: '/tenders', title: 'Tenders', icon: Ticket },
  { path: '/contracts', title: 'Contracts', icon: DocumentCopy },
  { path: '/applications-for-payment', title: 'Applications', icon: DocumentCopy },
  { path: '/wip-journal', title: 'WIP Journal', icon: DocumentCopy },
  { path: '/subcontractors', title: 'Subcontractors', icon: UserFilled },
  { path: '/operatives', title: 'Operatives', icon: User },
  { path: '/plant', title: 'Plant', icon: Tools },
  { path: '/procurement', title: 'Procurement', icon: ShoppingCart },
  { path: '/healthsafety', title: 'Health & Safety', icon: FirstAidKit },
  { path: '/health-safety/cdm-register', title: 'CDM Register', icon: FirstAidKit },
  { path: '/adoption', title: 'Adoption', icon: CircleCheck },
  { path: '/quality', title: 'Quality', icon: Medal },
  { path: '/reports', title: 'Reports', icon: Document },
  { path: '/admin', title: 'Admin', icon: AdminSetting }
]

const handleMenuSelect = (path: string) => {
  router.push(path)
}

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    await authStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/admin')
  } else if (command === 'settings') {
    router.push('/admin')
  }
}

// ── Global Search ────────────────────────────────────────────────────────────
const searchQuery = ref('')
const searchResults = ref<Record<string, Array<{ id: string | number; label: string }>>>({})
const searchVisible = ref(false)
const searchLoading = ref(false)
let searchTimer: ReturnType<typeof setTimeout> | null = null

const groupLabels: Record<string, string> = {
  contracts: 'Contracts',
  operatives: 'Operatives',
  sites: 'Sites',
  companies: 'Companies',
  plant: 'Plant'
}

const routeForType: Record<string, string> = {
  contracts: '/contracts',
  operatives: '/operatives',
  sites: '/sites',
  companies: '/companies',
  plant: '/plant'
}

const hasResults = computed(() =>
  Object.values(searchResults.value).some((arr) => arr.length > 0)
)

const handleSearchInput = () => {
  if (searchTimer) clearTimeout(searchTimer)
  if (searchQuery.value.length < 3) {
    searchResults.value = {}
    searchVisible.value = false
    return
  }
  searchTimer = setTimeout(() => doSearch(), 500)
}

const handleSearchEnter = () => {
  if (searchQuery.value.length >= 3) {
    if (searchTimer) clearTimeout(searchTimer)
    doSearch()
  }
}

const doSearch = async () => {
  searchLoading.value = true
  try {
    const res = await api.search.global(searchQuery.value)
    searchResults.value = res.data as any
    searchVisible.value = true
  } catch {
    searchResults.value = {}
  } finally {
    searchLoading.value = false
  }
}

const navigateToResult = (type: string, id: string | number) => {
  searchVisible.value = false
  searchQuery.value = ''
  searchResults.value = {}
  router.push(`${routeForType[type]}/${id}`)
}

const closeSearch = () => {
  searchVisible.value = false
}

// ── Notifications ────────────────────────────────────────────────────────────
interface NotificationItem {
  type: string
  message: string
  severity: string
  link: string
}

const notificationCount = ref(0)
const notificationItems = ref<NotificationItem[]>([])
const notificationDrawerVisible = ref(false)
let notificationInterval: ReturnType<typeof setInterval> | null = null

const loadNotificationCount = async () => {
  if (!authStore.isAuthenticated) return
  try {
    const res = await api.notifications.getCount()
    const data = res.data as any
    notificationCount.value = data.count ?? 0
    notificationItems.value = data.items ?? []
  } catch {
    // silently ignore – non-critical
  }
}

const openNotifications = () => {
  notificationDrawerVisible.value = true
}

const navigateFromNotification = (link: string) => {
  notificationDrawerVisible.value = false
  router.push(link)
}

onMounted(() => {
  if (authStore.isAuthenticated) {
    loadNotificationCount()
    notificationInterval = setInterval(loadNotificationCount, 5 * 60 * 1000)
  }
})

onUnmounted(() => {
  if (notificationInterval) clearInterval(notificationInterval)
  if (searchTimer) clearTimeout(searchTimer)
})
</script>

<template>
  <el-container v-if="isAuthenticated" class="app-container">
    <el-aside 
      class="app-aside"
      :class="{ 'is-collapsed': isCollapsed }"
      :width="isCollapsed ? '64px' : '220px'"
    >
      <div class="logo-container">
        <el-icon :size="28" color="#1a73e8">
          <Document />
        </el-icon>
        <span v-show="!isCollapsed" class="logo-text">CRMS CI G UK</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        class="app-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item 
          v-for="item in menuItems" 
          :key="item.path" 
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-icon 
            class="collapse-btn" 
            size="20"
            @click="toggleSidebar"
          >
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item 
              v-for="(crumb, index) in appStore.breadcrumbs" 
              :key="index"
              :to="crumb.path"
            >
              {{ crumb.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <!-- Global Search -->
        <div v-click-outside="closeSearch" class="global-search">
          <el-input
            v-model="searchQuery"
            placeholder="Search contracts, operatives, sites..."
            :prefix-icon="Search"
            clearable
            class="search-input"
            :loading="searchLoading"
            @input="handleSearchInput"
            @keyup.enter="handleSearchEnter"
            @clear="() => { searchVisible = false; searchResults = {} }"
          />
          <div v-if="searchVisible && hasResults" class="search-dropdown">
            <template v-for="(items, type) in searchResults" :key="type">
              <template v-if="items.length > 0">
                <div class="search-group-label">{{ groupLabels[type] || type }}</div>
                <div
                  v-for="item in items"
                  :key="item.id"
                  class="search-result-item"
                  @click="navigateToResult(type as string, item.id)"
                >
                  {{ item.label }}
                </div>
              </template>
            </template>
          </div>
          <div v-if="searchVisible && !hasResults && !searchLoading && searchQuery.length >= 3" class="search-dropdown search-no-results">
            No results found
          </div>
        </div>

        <div class="header-right">
          <el-badge :value="notificationCount" :hidden="notificationCount === 0" class="notification-badge" type="danger">
            <el-icon size="20" class="header-icon" @click="openNotifications">
              <Bell />
            </el-icon>
          </el-badge>

          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ user?.firstName || user?.username || 'User' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  Profile
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  Settings
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  Logout
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Notification Drawer -->
      <el-drawer
        v-model="notificationDrawerVisible"
        title="Compliance Alerts"
        direction="rtl"
        size="380px"
      >
        <div v-if="notificationItems.length === 0" class="no-notifications">
          <el-empty description="No active alerts" :image-size="80" />
        </div>
        <div v-else class="notification-list">
          <div
            v-for="(item, idx) in notificationItems"
            :key="idx"
            class="notification-item"
            :class="`notification-${item.severity}`"
            @click="navigateFromNotification(item.link)"
          >
            <el-tag :type="item.severity === 'danger' ? 'danger' : 'warning'" size="small" class="notif-tag">
              {{ item.type.toUpperCase() }}
            </el-tag>
            <span class="notif-message">{{ item.message }}</span>
          </div>
        </div>
      </el-drawer>

      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>

      <NetworkStatus />
    </el-container>
  </el-container>
</template>
<style lang="scss" scoped>
.app-container {
  height: 100vh;
  overflow: hidden;
}

.app-aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  transition: width 0.3s;
  overflow-x: hidden;
  
  &.is-collapsed {
    .logo-container {
      justify-content: center;
    }
  }
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  height: 56px;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #1a73e8;
  white-space: nowrap;
}

.app-menu {
  border-right: none;
  
  &:not(.el-menu--collapse) {
    width: 220px;
  }
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  height: 56px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
  
  &:hover {
    background: #f5f7fa;
  }
}

.global-search {
  position: relative;
  flex: 1;
  max-width: 420px;
  margin: 0 16px;

  .search-input {
    width: 100%;
  }

  .search-dropdown {
    position: absolute;
    top: calc(100% + 4px);
    left: 0;
    right: 0;
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 6px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    z-index: 9999;
    max-height: 400px;
    overflow-y: auto;
  }

  .search-group-label {
    padding: 6px 12px 4px;
    font-size: 11px;
    font-weight: 600;
    color: #909399;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    background: #f5f7fa;
  }

  .search-result-item {
    padding: 8px 12px;
    font-size: 13px;
    color: #303133;
    cursor: pointer;
    transition: background 0.15s;

    &:hover {
      background: #ecf5ff;
      color: #1a73e8;
    }
  }

  .search-no-results {
    padding: 16px;
    text-align: center;
    color: #909399;
    font-size: 13px;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-badge {
  cursor: pointer;
}

.header-icon {
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  
  &:hover {
    background: #f5f7fa;
  }
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
  
  &:hover {
    background: #f5f7fa;
  }
}

.username {
  font-size: 14px;
  color: #303133;
}

.app-main {
  background: #f5f7fa;
  overflow-y: auto;
  padding: 20px;
}

.no-notifications {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;

  &.notification-danger {
    background: #fef0f0;
    border-left: 3px solid #f56c6c;
  }

  &.notification-warning {
    background: #fdf6ec;
    border-left: 3px solid #e6a23c;
  }

  &:hover {
    filter: brightness(0.97);
  }
}

.notif-tag {
  flex-shrink: 0;
  font-size: 10px;
}

.notif-message {
  font-size: 13px;
  color: #303133;
  line-height: 1.4;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
