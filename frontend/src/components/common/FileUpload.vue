<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

interface Props {
  accept?: string
  maxSize?: number
  multiple?: boolean
  disabled?: boolean
  headers?: Record<string, string>
}

const props = withDefaults(defineProps<Props>(), {
  accept: '*',
  maxSize: 10,
  multiple: true,
  disabled: false,
  headers: () => ({})
})

const emit = defineEmits<{
  success: [response: any]
  error: [error: any]
  progress: [percent: number]
}>()

const uploading = ref(false)
const uploadProgress = ref(0)
const fileList = ref<any[]>([])

const beforeUpload = (file: File) => {
  const isLtMaxSize = file.size / 1024 / 1024 < props.maxSize
  if (!isLtMaxSize) {
    ElMessage.error(`File size must be less than ${props.maxSize}MB`)
    return false
  }
  return true
}

const handleChange = (uploadFile: any) => {
  fileList.value = [uploadFile]
}

const handleSuccess = (response: any) => {
  ElMessage.success('File uploaded successfully')
  emit('success', response.data)
  uploading.value = false
  uploadProgress.value = 0
}

const handleError = (error: any) => {
  ElMessage.error('Upload failed')
  emit('error', error)
  uploading.value = false
  uploadProgress.value = 0
}

const handleProgress = (event: any) => {
  uploadProgress.value = Math.round(event.percent)
  emit('progress', uploadProgress.value)
}

const uploadRef = ref()

const submit = async (metadata?: Record<string, any>) => {
  if (fileList.value.length === 0) {
    ElMessage.warning('Please select a file first')
    return
  }
  
  uploading.value = true
  uploadRef.value?.submit()
}

const clearFiles = () => {
  fileList.value = []
  uploadProgress.value = 0
}
</script>

<template>
  <div class="file-upload">
    <el-upload
      ref="uploadRef"
      :action="'/api/v1/documents'"
      :accept="accept"
      :multiple="multiple"
      :disabled="disabled || uploading"
      :auto-upload="false"
      :before-upload="beforeUpload"
      :on-change="handleChange"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="handleProgress"
      :headers="headers"
      :data="{ metadata: JSON.stringify({}) }"
      drag
      class="upload-component"
    >
      <el-icon class="upload-icon"><UploadFilled /></el-icon>
      <div class="upload-text">
        <span class="upload-title">Drop file here or <em>click to upload</em></span>
        <span class="upload-hint">Maximum file size: {{ maxSize }}MB</span>
      </div>
    </el-upload>

    <div v-if="uploading" class="upload-progress">
      <el-progress :percentage="uploadProgress" :stroke-width="8" />
      <span class="progress-text">{{ uploadProgress }}%</span>
    </div>

    <div class="upload-actions">
      <el-button type="primary" :loading="uploading" :disabled="fileList.length === 0" @click="submit">
        {{ uploading ? 'Uploading...' : 'Upload' }}
      </el-button>
      <el-button :disabled="fileList.length === 0" @click="clearFiles">Clear</el-button>
    </div>
  </div>
</template>

<script lang="ts">
import { UploadFilled } from '@element-plus/icons-vue'
export default {
  components: { UploadFilled }
}
</script>

<style lang="scss" scoped>
.file-upload {
  .upload-component {
    :deep(.el-upload) {
      width: 100%;
      border: 1px dashed #d9d9d9;
      border-radius: 8px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: border-color 0.2s;
      
      &:hover {
        border-color: #1a73e8;
      }
    }
    
    :deep(.el-upload-dragger) {
      padding: 40px 20px;
      background-color: #fafafa;
    }
    
    &.is-disabled {
      :deep(.el-upload) {
        cursor: not-allowed;
      }
    }
  }
  
  .upload-icon {
    font-size: 48px;
    color: #909399;
    margin-bottom: 16px;
  }
  
  .upload-text {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .upload-title {
      font-size: 14px;
      color: #606266;
      
      em {
        color: #1a73e8;
        font-style: normal;
      }
    }
    
    .upload-hint {
      font-size: 12px;
      color: #909399;
    }
  }
  
  .upload-progress {
    margin-top: 16px;
    display: flex;
    align-items: center;
    gap: 12px;
    
    .progress-text {
      font-size: 14px;
      color: #606266;
      min-width: 40px;
    }
  }
  
  .upload-actions {
    margin-top: 16px;
    display: flex;
    gap: 12px;
  }
}
</style>
