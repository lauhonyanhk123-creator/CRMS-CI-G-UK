<script setup lang="ts">
import { computed } from 'vue'

type StatusValue = string

interface Props {
  status: StatusValue
  size?: 'small' | 'default' | 'large'
}

const props = withDefaults(defineProps<Props>(), {
  size: 'small'
})

const statusConfig: Record<string, { label: string; type: 'success' | 'warning' | 'danger' | 'info' | undefined }> = {
  // General statuses
  active: { label: 'Active', type: 'success' },
  inactive: { label: 'Inactive', type: 'info' },
  pending: { label: 'Pending', type: 'warning' },
  completed: { label: 'Completed', type: 'success' },
  draft: { label: 'Draft', type: 'info' },
  
  // Contract statuses
  terminated: { label: 'Terminated', type: 'danger' },
  on_hold: { label: 'On Hold', type: 'warning' },
  
  // CIS statuses
  verified: { label: 'VERIFIED', type: 'success' },
  expired: { label: 'EXPIRED', type: 'danger' },
  
  // Application statuses
  submitted: { label: 'Submitted', type: 'info' },
  measured: { label: 'Measured', type: 'info' },
  agreed: { label: 'Agreed', type: 'success' },
  paid: { label: 'Paid', type: 'success' },
  
  // Tender stages
  lead: { label: 'Lead', type: 'info' },
  qualified: { label: 'Qualified', type: 'info' },
  pricing: { label: 'Pricing', type: 'warning' },
  negotiation: { label: 'Negotiation', type: undefined },
  awarded: { label: 'Awarded', type: 'success' },
  lost: { label: 'Lost', type: 'danger' },
  
  // Site statuses
  planning: { label: 'Planning', type: 'info' },
  
  // Operative statuses
  employed: { label: 'Employed', type: 'success' },
  contractor: { label: 'Contractor', type: 'info' },
  
  // Plant statuses
  available: { label: 'Available', type: 'success' },
  allocated: { label: 'Allocated', type: 'info' },
  maintenance: { label: 'Maintenance', type: 'warning' },
  retired: { label: 'Retired', type: 'info' },
  
  // Approval statuses
  approved: { label: 'Approved', type: 'success' },
  rejected: { label: 'Rejected', type: 'danger' },
  
  // Record statuses
  open: { label: 'Open', type: 'danger' },
  closed: { label: 'Closed', type: 'info' },
  in_progress: { label: 'In Progress', type: 'warning' },
  resolved: { label: 'Resolved', type: 'success' },
  
  // Gate statuses
  ready: { label: 'READY', type: 'success' },
  gate_red: { label: 'GATE RED', type: 'danger' },
  
  // Default fallback
  default: { label: 'Unknown', type: undefined }
}

const config = computed(() => {
  const key = props.status?.toLowerCase() || 'default'
  return statusConfig[key] || statusConfig.default
})
</script>

<template>
  <el-tag :type="config.type" :size="size" class="status-badge">
    {{ config.label }}
  </el-tag>
</template>

<style lang="scss" scoped>
.status-badge {
  text-transform: uppercase;
  font-weight: 500;
  letter-spacing: 0.5px;
}
</style>
