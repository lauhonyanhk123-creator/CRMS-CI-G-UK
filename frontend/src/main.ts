import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './assets/styles/common.scss'

const app = createApp(App)

// Pinia with persistence for auth store
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)

// Vue Router
app.use(router)

// Element Plus
app.use(ElementPlus)

// Register all Element Plus icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')

// Register service worker for PWA with proper callbacks
if ('serviceWorker' in navigator) {
  import(/* @vite-ignore */ 'virtual:pwa-register').then(({ registerSW }) => {
    registerSW({
      immediate: false,
      onNeedRefresh() {
        console.log('New content available, refresh for updates')
      },
      onOfflineReady() {
        console.log('App ready to work offline')
      },
      onRegistered(registration) {
        if (registration) {
          console.log('Service Worker registered with scope:', registration.scope)
        }
      },
      onRegisterError(error) {
        console.error('Service Worker registration error:', error)
      }
    })
  }).catch(() => {
    // PWA plugin not available in dev mode without build
  })
}
