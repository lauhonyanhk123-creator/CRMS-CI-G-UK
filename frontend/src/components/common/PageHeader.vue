<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

interface Breadcrumb {
  title: string
  path?: string
}

interface Props {
  title: string
  breadcrumbs?: Breadcrumb[]
  showBack?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  breadcrumbs: () => [],
  showBack: false
})

const emit = defineEmits<{
  back: []
}>()

const route = useRoute()

const defaultBreadcrumbs = computed<Breadcrumb[]>(() => {
  const crumbs: Breadcrumb[] = [{ title: 'Home', path: '/' }]
  if (route.path !== '/dashboard') {
    const pathParts = route.path.split('/').filter(Boolean)
    if (pathParts.length > 0) {
      crumbs.push({
        title: pathParts[pathParts.length - 1].charAt(0).toUpperCase() + pathParts[pathParts.length - 1].slice(1)
      })
    }
  }
  return crumbs
})

const allBreadcrumbs = computed(() => 
  props.breadcrumbs.length > 0 ? props.breadcrumbs : defaultBreadcrumbs.value
)

const handleBack = () => {
  if (props.breadcrumbs.length > 0 && props.breadcrumbs[0].path) {
    window.location.href = props.breadcrumbs[0].path
  } else {
    window.history.back()
  }
  emit('back')
}
</script>

<template>
  <div class="page-header">
    <div class="header-content">
      <div class="header-left">
        <el-button v-if="showBack" :icon="ArrowLeft" text class="back-button" @click="handleBack" />
        <div class="breadcrumb-wrapper">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="crumb in allBreadcrumbs" :key="crumb.title">
              <router-link v-if="crumb.path" :to="crumb.path">{{ crumb.title }}</router-link>
              <span v-else>{{ crumb.title }}</span>
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1 class="page-title">{{ title }}</h1>
      </div>
      <div class="header-actions">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.page-header {
  margin-bottom: 20px;
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.back-button {
  margin-bottom: 8px;
  align-self: flex-start;
}
.breadcrumb-wrapper {
  :deep(.el-breadcrumb) {
    font-size: 12px;
  }
}
.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}
.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
