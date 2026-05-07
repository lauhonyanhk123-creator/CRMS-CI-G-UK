<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const form = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const handleSubmit = async () => {
  if (!form.currentPassword || !form.newPassword || !form.confirmPassword) {
    ElMessage.error('All fields are required')
    return
  }
  if (form.newPassword.length < 8) {
    ElMessage.error('New password must be at least 8 characters')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.error('New passwords do not match')
    return
  }

  loading.value = true
  try {
    await api.auth.changePassword({
      currentPassword: form.currentPassword,
      newPassword: form.newPassword
    })
    if (authStore.user) {
      authStore.user.mustChangePassword = false
    }
    ElMessage.success('Password changed successfully')
    router.push('/dashboard')
  } catch {
    // Error already shown by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="change-password-view">
    <div class="change-password-container">
      <div class="change-password-header">
        <el-icon :size="48" color="#e6a817">
          <Lock />
        </el-icon>
        <h1 class="title">Password Change Required</h1>
        <p class="subtitle">
          Your account requires a password change before you can continue.
          Please set a new password now.
        </p>
      </div>

      <el-card shadow="never" class="change-password-card">
        <el-form label-position="top" @submit.prevent="handleSubmit">
          <el-form-item label="Current Password" required>
            <el-input
              v-model="form.currentPassword"
              type="password"
              show-password
              placeholder="Enter your current password"
            />
          </el-form-item>

          <el-form-item label="New Password" required>
            <el-input
              v-model="form.newPassword"
              type="password"
              show-password
              placeholder="At least 8 characters"
            />
          </el-form-item>

          <el-form-item label="Confirm New Password" required>
            <el-input
              v-model="form.confirmPassword"
              type="password"
              show-password
              placeholder="Repeat your new password"
            />
          </el-form-item>

          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            style="width: 100%; margin-top: 8px"
          >
            Change Password
          </el-button>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.change-password-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;

  .change-password-container {
    width: 100%;
    max-width: 420px;
    padding: 24px;

    .change-password-header {
      text-align: center;
      margin-bottom: 32px;

      .title {
        font-size: 24px;
        font-weight: 600;
        color: #1f2937;
        margin: 16px 0 8px;
      }

      .subtitle {
        color: #6b7280;
        font-size: 14px;
        line-height: 1.5;
      }
    }

    .change-password-card {
      border-radius: 12px;
    }
  }
}
</style>
