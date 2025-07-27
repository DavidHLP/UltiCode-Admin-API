<template>
  <div class="card-header">
    <div class="header-left">
      <div class="header-item">
        <el-icon>
          <DocumentIcon />
        </el-icon>
        <span>代码</span>
      </div>
    </div>
    <div class="header-center">
      <el-select v-model="selectedLanguage" size="small" class="lang-select">
        <el-option v-for="lang in availableLanguages" :key="lang" :label="lang.charAt(0).toUpperCase() + lang.slice(1)"
          :value="lang" />
      </el-select>
    </div>
    <div class="header-right">
      <el-icon>
        <Setting />
      </el-icon>
      <el-icon>
        <CollectionTag />
      </el-icon>
      <el-icon @click="$emit('reset-code')">
        <Refresh />
      </el-icon>
      <el-icon>
        <FullScreen />
      </el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import {
  Document as DocumentIcon,
  Setting,
  CollectionTag,
  Refresh,
  FullScreen
} from '@element-plus/icons-vue';

interface Props {
  availableLanguages: string[];
  modelValue: string;
}

const props = withDefaults(defineProps<Props>(), {
  availableLanguages: () => []
});

const emit = defineEmits(['update:modelValue', 'reset-code']);

const selectedLanguage = ref<string>(props.modelValue);

// 监听语言选择变化
watch(selectedLanguage, (newVal: string) => {
  emit('update:modelValue', newVal);
});

// 监听props变化
watch(() => props.modelValue, (newVal: string) => {
  selectedLanguage.value = newVal;
});
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  height: 48px;
  flex-shrink: 0;
  color: #595959;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.header-left,
.header-center,
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-center {
  flex-grow: 1;
  padding-left: 24px;
}

.header-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.lang-select {
  width: 100px;
}

:deep(.lang-select .el-input__wrapper) {
  box-shadow: none !important;
  background-color: transparent;
}

.header-right .el-icon {
  cursor: pointer;
  font-size: 16px;
  color: #606266;
  transition: color 0.2s;
}

.header-right .el-icon:hover {
  color: #409eff;
}
</style>
