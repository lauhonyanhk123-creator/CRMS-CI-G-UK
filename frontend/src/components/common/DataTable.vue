<script setup lang="ts">
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'

interface Props {
  data: any[]
  columns: {
    key: string
    label: string
    width?: string | number
    minWidth?: string | number
    align?: 'left' | 'center' | 'right'
    slot?: string
    formatter?: (row: any) => string
  }[]
  loading?: boolean
  emptyText?: string
  showPagination?: boolean
  total?: number
  page?: number
  pageSize?: number
  searchable?: boolean
  searchPlaceholder?: string
}

withDefaults(defineProps<Props>(), {
  loading: false,
  emptyText: 'No data available',
  showPagination: true,
  page: 1,
  pageSize: 20,
  searchable: false,
  searchPlaceholder: 'Search...'
})

const emit = defineEmits<{
  'update:page': [page: number]
  'update:pageSize': [size: number]
  search: [query: string]
  rowClick: [row: any]
}>()

const searchQuery = ref('')

const handleSearch = () => {
  emit('search', searchQuery.value)
}

const handlePageChange = (page: number) => {
  emit('update:page', page)
}

const handleSizeChange = (size: number) => {
  emit('update:pageSize', size)
}

const handleRowClick = (row: any) => {
  emit('rowClick', row)
}

const getCellStyle = (column: Props['columns'][0]) => {
  return {
    textAlign: column.align || 'left',
    width: column.width ? (typeof column.width === 'number' ? `${column.width}px` : column.width) : undefined,
    minWidth: column.minWidth ? (typeof column.minWidth === 'number' ? `${column.minWidth}px` : column.minWidth) : undefined
  }
}
</script>

<template>
  <div class="data-table">
    <div v-if="searchable" class="table-toolbar">
      <el-input
        v-model="searchQuery"
        :placeholder="searchPlaceholder"
        clearable
        :prefix-icon="Search"
        class="search-input"
        @input="handleSearch"
      />
      <slot name="toolbar" />
    </div>

    <el-table
      v-loading="loading"
      :data="data"
      stripe
      class="table"
      @row-click="handleRowClick"
    >
      <el-table-column
        v-for="column in columns"
        :key="column.key"
        :prop="column.key"
        :label="column.label"
        :align="column.align || 'left'"
        :width="column.width"
        :min-width="column.minWidth"
        :style="getCellStyle(column)"
      >
        <template v-if="column.slot" #default="{ row }">
          <slot :name="column.slot" :row="row" />
        </template>
        <template v-else #default="{ row }">
          {{ column.formatter ? column.formatter(row) : row[column.key] }}
        </template>
      </el-table-column>

      <template #empty>
        <div class="empty-state">
          <el-icon :size="48" color="#c0c4cc"><DocumentDelete /></el-icon>
          <p>{{ emptyText }}</p>
        </div>
      </template>
    </el-table>

    <el-pagination
      v-if="showPagination && total"
      :current-page="page"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script lang="ts">
import { DocumentDelete } from '@element-plus/icons-vue'
export default {
  components: { DocumentDelete }
}
</script>

<style lang="scss" scoped>
.data-table {
  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    gap: 16px;
    
    .search-input {
      max-width: 300px;
    }
  }
  
  .table {
    cursor: pointer;
  }
  
  .empty-state {
    padding: 40px 0;
    text-align: center;
    color: #909399;
    
    p {
      margin: 12px 0 0 0;
    }
  }
  
  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
