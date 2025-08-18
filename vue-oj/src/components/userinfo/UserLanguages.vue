<template>
  <div class="user-languages">
    <div class="section-title">语言</div>
    <div class="language-colors">
      <el-tooltip 
        v-for="language in languages" 
        :key="language.name" 
        :content="`${language.name} ${language.percentage}%`"
        placement="top"
      >
        <div 
          class="language-color" 
          :style="{ 
            background: language.color,
            width: `${Math.max(language.percentage / 5, 12)}px`
          }"
        />
      </el-tooltip>
    </div>
    <div class="language-list">
      <div v-for="language in languages" :key="language.name" class="language-item">
        <div class="language-dot" :style="{ background: language.color }"></div>
        <span class="language-name">{{ language.name }}</span>
        <span class="language-percentage">{{ language.percentage }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ProgrammingLanguage } from '@/types/userinfo'

defineProps<{
  languages: ProgrammingLanguage[]
}>()
</script>

<style scoped lang="scss">
.user-languages {
  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin: 24px 0 16px 0;
    padding-bottom: 8px;
    border-bottom: 1px solid #d0d7de;
    color: #24292f;
  }

  .language-colors {
    display: flex;
    gap: 2px;
    margin: 16px 0;
    height: 8px;

    .language-color {
      height: 8px;
      border-radius: 2px;
      min-width: 12px;
      cursor: pointer;
      transition: opacity 0.2s;

      &:hover {
        opacity: 0.8;
      }
    }
  }

  .language-list {
    .language-item {
      display: flex;
      align-items: center;
      margin: 6px 0;
      font-size: 12px;
      color: #656d76;

      .language-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        margin-right: 8px;
      }

      .language-name {
        flex: 1;
      }

      .language-percentage {
        font-weight: 500;
      }
    }
  }
}
</style>
