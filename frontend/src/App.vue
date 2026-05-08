<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { usePWA } from '@/composables/usePWA'
import { useOfflineSync } from '@/composables/useOfflineSync'
import NetworkStatus from '@/components/common/NetworkStatus.vue'
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
            @click="toggleSidebar"
            size="20"
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

        <div class="header-right">
          <el-badge :value="appStore.notifications.length" :hidden="appStore.notifications.length === 0" class="notification-badge">
            <el-icon size="20" class="header-icon">
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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
