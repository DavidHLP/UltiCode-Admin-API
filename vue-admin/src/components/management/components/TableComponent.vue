<template>
  <div class="table-container">
    <el-table :data="data" v-loading="loading" stripe style="width: 100%"
      @selection-change="$emit('selectionChange', $event)">
      <slot></slot>
      <template #empty>
        <div v-if="emptyText" class="empty-state">
          <el-empty :description="emptyText" />
        </div>
        <slot v-else name="empty">
          <el-empty description="暂无数据" />
        </slot>
      </template>
    </el-table>
    <div class="pagination-container" v-if="showPagination">
      <el-pagination 
        v-model:current-page="currentPage" 
        v-model:page-size="pageSize" 
        :page-sizes="pageSizes"
        :total="total" 
        layout="total, sizes, prev, pager, next, jumper" 
        :pager-count="5"
        @size-change="$emit('sizeChange', $event)" 
        @current-change="$emit('currentChange', $event)" />
    </div>
  </div>
</template>

<script setup lang="ts" generic="T">
interface Props {
  data: T[]
  loading?: boolean
  showPagination?: boolean
  total?: number
  pageSizes?: number[]
  emptyText?: string
}

withDefaults(defineProps<Props>(), {
  loading: false,
  showPagination: true,
  total: 0,
  currentPage: 1,
  pageSize: 10,
  pageSizes: () => [10, 20, 50, 100]
})

defineEmits<{
  (e: 'selectionChange', value: T[]): void
  (e: 'sizeChange', value: number): void
  (e: 'currentChange', value: number): void
}>()

// 使用传入的值或默认值
const currentPage = defineModel('currentPage', { default: 1 })
const pageSize = defineModel('pageSize', { default: 10 })
</script>

<style>
.table-container {
  padding: var(--spacing-md);
  background-color: var(--bg-primary);
  border-radius: var(--border-radius-medium);
  box-shadow: var(--shadow-light);
}

.empty-state {
  padding: var(--spacing-xl) 0;
}

.pagination-container {
  margin-top: var(--spacing-lg);
  padding: var(--spacing-md);
  background-color: var(--bg-secondary);
  border-radius: var(--border-radius-medium);
  display: flex;
  justify-content: flex-end;
}
</style>
