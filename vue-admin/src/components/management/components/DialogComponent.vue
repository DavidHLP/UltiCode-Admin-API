<template>
  <el-dialog v-model="visible" :title="title" :width="width" :before-close="handleBeforeClose">
    <div class="dialog-content">
      <slot :data="data" :isEdit="isEdit"></slot>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleConfirm">
          {{ confirmButtonText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts" generic="T">
type DialogData = T | null

interface Props {
  modelValue: boolean
  title?: string
  width?: string
  saving?: boolean
  confirmButtonText?: string
  data?: DialogData
  isEdit?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  title: '操作',
  width: '500px',
  saving: false,
  confirmButtonText: '确定',
  data: null,
  isEdit: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', data: DialogData): void
  (e: 'cancel'): void
}>()

const visible = defineModel<boolean>('modelValue', { default: false })

const handleConfirm = () => {
  emit('confirm', props.data)
}

const handleCancel = () => {
  emit('cancel')
  visible.value = false
}

const handleBeforeClose = (done: () => void) => {
  if (props.saving) {
    return
  }
  emit('cancel')
  done()
}
</script>

<style>
.dialog-content {
  padding: var(--spacing-md);
}

.dialog-footer {
  padding: var(--spacing-md);
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
}
</style>
