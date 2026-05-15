import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { AxiosError, AxiosRequestConfig } from 'axios'

export interface UseApiOptions {
  showSuccess?: boolean
  showError?: boolean
  errorTitle?: string
}

export interface UseApiReturn<T> {
  data: import('vue').Ref<T | null>
  loading: import('vue').Ref<boolean>
  error: import('vue').Ref<string | null>
  execute: (config?: AxiosRequestConfig) => Promise<T | null>
  refresh: () => Promise<T | null>
}

export function useApi<T = any>(
  request: (config?: AxiosRequestConfig) => Promise<{ data: T }>,
  options: UseApiOptions = {}
): UseApiReturn<T> {
  const { showSuccess = false, showError = true } = options

  const data = ref<T | null>(null) as import('vue').Ref<T | null>
  const loading = ref(false)
  const error = ref<string | null>(null)

  const execute = async (config?: AxiosRequestConfig): Promise<T | null> => {
    loading.value = true
    error.value = null

    try {
      const response = await request(config)
      data.value = response.data
      
      if (showSuccess) {
        ElMessage.success('Operation completed successfully')
      }
      
      return response.data
    } catch (err) {
      const axiosError = err as AxiosError
      
      if (showError) {
        const message = (axiosError.response?.data as any)?.message || axiosError.message || 'An error occurred'
        ElMessage.error(message)
      }
      
      error.value = (axiosError.response?.data as any)?.message || axiosError.message || 'An error occurred'
      return null
    } finally {
      loading.value = false
    }
  }

  const refresh = async (): Promise<T | null> => {
    return execute()
  }

  return {
    data,
    loading,
    error,
    execute,
    refresh
  }
}

// Helper for confirm dialogs
export async function confirm(
  message: string,
  title: string = 'Confirm',
  type: 'warning' | 'info' | 'error' | 'success' = 'warning'
): Promise<boolean> {
  try {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      type
    })
    return true
  } catch {
    return false
  }
}

// Helper for delete confirmation
export async function confirmDelete(itemName: string): Promise<boolean> {
  return confirm(
    `Are you sure you want to delete "${itemName}"? This action cannot be undone.`,
    'Confirm Delete',
    'warning'
  )
}
