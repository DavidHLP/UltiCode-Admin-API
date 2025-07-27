<template>
  <div class="management-container">
    <!-- 页面头部 -->
    <HeaderComponent :title="title" :icon="titleIcon" />

    <!-- 搜索和筛选区域 -->
    <SearchComponent v-model="searchQuery" :placeholder="searchPlaceholder" @search="handleSearch"
      @refresh="handleRefresh">
      <!-- 自定义筛选器插槽 -->
      <template #filters>
        <slot name="filters"></slot>
      </template>
    </SearchComponent>

    <!-- 表格区域 -->
    <TableComponent :data="tableData" :loading="loading" :empty-text="emptyText">
      <!-- 表格列插槽 -->
      <slot name="table-columns"></slot>
    </TableComponent>

    <!-- 通用对话框 -->
    <DialogComponent v-model="dialogVisible" :title="dialogTitle" :saving="saving"
      :confirm-button-text="isEdit ? '更新' : '创建'" :data="currentItem" :is-edit="isEdit" @confirm="handleDialogConfirm"
      @cancel="handleDialogCancel">
      <!-- 对话框表单插槽 -->
      <template #default="{ data, isEdit }">
        <slot name="dialog-form" :currentItem="data" :isEdit="isEdit"></slot>
      </template>
    </DialogComponent>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits, defineExpose, type Component } from 'vue'
import HeaderComponent from './components/HeaderComponent.vue'
import SearchComponent from './components/SearchComponent.vue'
import TableComponent from './components/TableComponent.vue'
import DialogComponent from './components/DialogComponent.vue'
import './components/styles/main.css'

// 定义 Props
interface Props {
  title: string
  titleIcon: Component
  searchPlaceholder: string
  emptyText: string
  tableData: Record<string, unknown>[]
  loading?: boolean
  saving?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
  saving: false
})

// 定义 Emits
const emit = defineEmits<{
  search: [query: string]
  refresh: []
  'dialog-confirm': []
  'dialog-cancel': []
}>()

// 响应式数据
const searchQuery = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentItem = ref<Record<string, unknown>>({})

// 方法
const handleSearch = () => {
  emit('search', searchQuery.value)
}

const handleRefresh = () => {
  emit('refresh')
}

const handleDialogConfirm = () => {
  emit('dialog-confirm')
}

const handleDialogCancel = () => {
  dialogVisible.value = false
  emit('dialog-cancel')
}

// 对外暴露的方法
const openDialog = (title: string, item: Record<string, unknown> = {}, edit: boolean = false) => {
  dialogTitle.value = title
  currentItem.value = item
  isEdit.value = edit
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
}

const updateSearchQuery = (query: string) => {
  searchQuery.value = query
}

// 暴露给父组件的方法和数据
defineExpose({
  openDialog,
  closeDialog,
  updateSearchQuery,
  dialogVisible,
  searchQuery,
  currentItem,
  isEdit
})
</script>

<style>
@import './index.css';
</style>
