<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Key } from '@element-plus/icons-vue'
import api from '@/services/api'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const step = ref<'info' | 'scan' | 'verify' | 'done'>('info')
const loading = ref(false)
const qrDataUri = ref('')
const secret = ref('')
const confirmCode = ref('')
const disableLoading = ref(false)

const totpEnabled = ref(authStore.user?.totpEnabled ?? false)

const startSetup = async () => {
  loading.value = true
  try {
    const response = await api.auth.totpSetup()
    const data = response.data as any
    qrDataUri.value = data.qrDataUri
    secret.value = data.secret
    step.value = 'scan'
  } catch {
    // error shown by interceptor
  } finally {
    loading.value = false
  }
}

const verifyAndEnable = async () => {
  loading.value = true
  try {
    await api.auth.totpEnable(confirmCode.value.replace(/\s/g, ''))
    step.value = 'done'
    totpEnabled.value = true
    if (authStore.user) {
      authStore.user = { ...authStore.user, totpEnabled: true } as any
    }
  } catch {
    // error shown by interceptor
  } finally {
    loading.value = false
  }
}

const disable2FA = async () => {
  disableLoading.value = true
  try {
    await api.auth.totpDisable()
    totpEnabled.value = false
    step.value = 'info'
    ElMessage.success('Two-factor authentication disabled')
    if (authStore.user) {
      authStore.user = { ...authStore.user, totpEnabled: false } as any
    }
  } catch {
    // error shown by interceptor
  } finally {
    disableLoading.value = false
  }
}
</script>

<template>
  <div class="totp-setup">
    <div class="totp-setup__header">
      <el-icon :size="32" color="#1a73e8"><Key /></el-icon>
      <h2>Two-Factor Authentication</h2>
      <p class="subtitle">
        Add an extra layer of security by requiring a time-based one-time password alongside your password.
      </p>
    </div>

    <!-- Already enabled -->
    <template v-if="totpEnabled && step !== 'done'">
      <el-alert type="success" :closable="false" show-icon style="margin-bottom: 20px">
        <template #title>2FA is active on your account</template>
        You will be asked for a verification code each time you sign in.
      </el-alert>
      <el-button type="danger" :loading="disableLoading" @click="disable2FA">
        Disable Two-Factor Authentication
      </el-button>
    </template>

    <!-- Step: info (not yet enrolled) -->
    <template v-else-if="step === 'info'">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 20px">
        <template #title>2FA is not enabled</template>
        Use Google Authenticator, Microsoft Authenticator, or any TOTP-compatible app.
      </el-alert>
      <el-button type="primary" :loading="loading" @click="startSetup">
        Enable Two-Factor Authentication
      </el-button>
    </template>

    <!-- Step: scan QR code -->
    <template v-else-if="step === 'scan'">
      <ol class="setup-steps">
        <li>Install an authenticator app (Google Authenticator, Authy, etc.) on your phone.</li>
        <li>Scan the QR code below or enter the secret key manually.</li>
        <li>Enter the 6-digit code shown in your app to confirm.</li>
      </ol>

      <div class="qr-wrapper">
        <img :src="qrDataUri" alt="TOTP QR code" width="220" height="220" />
      </div>

      <el-descriptions :column="1" border size="small" style="margin: 16px 0">
        <el-descriptions-item label="Manual key">
          <code class="secret-key">{{ secret }}</code>
        </el-descriptions-item>
        <el-descriptions-item label="Issuer">CRMS CI G UK</el-descriptions-item>
        <el-descriptions-item label="Algorithm">SHA-1 · 6 digits · 30 s</el-descriptions-item>
      </el-descriptions>

      <el-input
        v-model="confirmCode"
        placeholder="Enter 6-digit code to confirm"
        size="large"
        maxlength="7"
        style="margin-bottom: 12px"
        @keyup.enter="verifyAndEnable"
      />

      <div class="action-row">
        <el-button @click="step = 'info'">Cancel</el-button>
        <el-button
          type="primary"
          :loading="loading"
          :disabled="confirmCode.replace(/\s/g, '').length !== 6"
          @click="verifyAndEnable"
        >
          Confirm &amp; Enable
        </el-button>
      </div>
    </template>

    <!-- Step: done -->
    <template v-else-if="step === 'done'">
      <el-result
        icon="success"
        title="Two-Factor Authentication Enabled"
        sub-title="Your account is now protected with 2FA. You will need your authenticator app each time you sign in."
      >
        <template #extra>
          <el-button type="primary" @click="step = 'info'">Done</el-button>
        </template>
      </el-result>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.totp-setup {
  max-width: 520px;
  margin: 0 auto;
  padding: 24px;

  &__header {
    text-align: center;
    margin-bottom: 24px;

    h2 {
      margin: 12px 0 8px;
      font-size: 22px;
      font-weight: 600;
    }

    .subtitle {
      color: #606266;
      font-size: 14px;
    }
  }
}

.setup-steps {
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
  padding-left: 20px;
  margin-bottom: 20px;
}

.qr-wrapper {
  display: flex;
  justify-content: center;
  margin: 16px 0;

  img {
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 8px;
  }
}

.secret-key {
  font-family: monospace;
  font-size: 13px;
  word-break: break-all;
  color: #409eff;
}

.action-row {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
