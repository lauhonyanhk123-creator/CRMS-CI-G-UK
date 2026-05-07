<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { User, Lock, Document, Key } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const formRef = ref<FormInstance>()
const rememberMe = ref(false)
const step = ref<'credentials' | 'totp'>('credentials')
const totpCode = ref('')

const form = reactive({
  username: '',
  password: ''
})

const formRules = {
  username: [
    { required: true, message: 'Username is required', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Password is required', trigger: 'blur' },
    { min: 3, message: 'Password must be at least 3 characters', trigger: 'blur' }
  ]
}

const isFormValid = computed(() => {
  return form.username.length > 0 && form.password.length >= 3
})

const isTotpValid = computed(() => totpCode.value.replace(/\s/g, '').length === 6)

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    loading.value = true
    try {
      const result = await authStore.login({
        username: form.username,
        password: form.password,
        rememberMe: rememberMe.value
      })

      if (result === 'totp') {
        step.value = 'totp'
      } else if (result === 'ok') {
        router.push('/dashboard')
      }
    } catch (error) {
      ElMessage.error('Login failed. Please check your credentials.')
    } finally {
      loading.value = false
    }
  })
}

const handleTotpVerify = async () => {
  loading.value = true
  try {
    const success = await authStore.completeTotpChallenge(totpCode.value.replace(/\s/g, ''))
    if (success) {
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}

const backToCredentials = () => {
  step.value = 'credentials'
  totpCode.value = ''
}

const demoLogin = async (role: string) => {
  loading.value = true
  try {
    const credentials = {
      admin: { username: 'admin@crms.com', password: 'admin123' },
      manager: { username: 'manager@crms.com', password: 'manager123' },
      user: { username: 'user@crms.com', password: 'user123' }
    }

    const cred = credentials[role as keyof typeof credentials]
    form.username = cred.username
    form.password = cred.password

    const result = await authStore.login({
      username: cred.username,
      password: cred.password,
      rememberMe: false
    })

    if (result === 'totp') {
      step.value = 'totp'
    } else if (result === 'ok') {
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-view">
    <div class="login-container">
      <div class="login-header">
        <div class="logo">
          <el-icon :size="48" color="#1a73e8">
            <Document />
          </el-icon>
        </div>
        <h1 class="title">CRMS CI G UK</h1>
        <p class="subtitle">Construction Resource Management System</p>
      </div>

      <el-card shadow="hover" class="login-card">
        <!-- Step 1: Credentials -->
        <template v-if="step === 'credentials'">
          <h2 class="card-title">Sign In</h2>

          <el-form
            ref="formRef"
            :model="form"
            :rules="formRules"
            label-position="top"
            @submit.prevent="handleLogin"
          >
            <el-form-item label="Username" prop="username">
              <el-input
                v-model="form.username"
                placeholder="Enter your username"
                size="large"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item label="Password" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="Enter your password"
                size="large"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <div class="form-options">
                <el-checkbox v-model="rememberMe">Remember me</el-checkbox>
                <el-link type="primary" :underline="false">Forgot password?</el-link>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                :disabled="!isFormValid"
                class="login-button"
                @click="handleLogin"
              >
                Sign In
              </el-button>
            </el-form-item>
          </el-form>

          <el-divider>
            <span class="divider-text">or demo login</span>
          </el-divider>

          <div class="demo-buttons">
            <el-button size="small" @click="demoLogin('admin')">Admin</el-button>
            <el-button size="small" @click="demoLogin('manager')">Manager</el-button>
            <el-button size="small" @click="demoLogin('user')">User</el-button>
          </div>
        </template>

        <!-- Step 2: TOTP Challenge -->
        <template v-else>
          <div class="totp-header">
            <el-icon :size="40" color="#1a73e8"><Key /></el-icon>
            <h2 class="card-title">Two-Factor Authentication</h2>
            <p class="totp-hint">Enter the 6-digit code from your authenticator app.</p>
          </div>

          <el-input
            v-model="totpCode"
            placeholder="000 000"
            size="large"
            maxlength="7"
            class="totp-input"
            @keyup.enter="handleTotpVerify"
          />

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            :disabled="!isTotpValid"
            class="login-button"
            style="margin-top: 16px"
            @click="handleTotpVerify"
          >
            Verify
          </el-button>

          <el-button
            text
            size="small"
            style="margin-top: 8px; display: block; margin-left: auto; margin-right: auto"
            @click="backToCredentials"
          >
            Back to sign in
          </el-button>
        </template>
      </el-card>

      <div class="login-footer">
        <p class="footer-text">
          Powered by CRMS CI G UK &copy; {{ new Date().getFullYear() }}
        </p>
      </div>
    </div>

    <div class="login-background">
      <div class="bg-pattern"></div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .logo {
    margin-bottom: 16px;
  }

  .title {
    font-size: 28px;
    font-weight: 700;
    color: #fff;
    margin: 0 0 8px 0;
    letter-spacing: -0.5px;
  }

  .subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
    margin: 0;
  }
}

.login-card {
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);

  .card-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 24px 0;
    text-align: center;
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
}

.divider-text {
  font-size: 12px;
  color: #909399;
}

.demo-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.totp-header {
  text-align: center;
  margin-bottom: 24px;

  .totp-hint {
    font-size: 14px;
    color: #606266;
    margin: 8px 0 0 0;
  }
}

.totp-input {
  font-size: 24px;
  letter-spacing: 8px;
  text-align: center;

  :deep(.el-input__inner) {
    text-align: center;
    font-size: 24px;
    letter-spacing: 8px;
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;

  .footer-text {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.7);
    margin: 0;
  }
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
}

.bg-pattern {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}
</style>
