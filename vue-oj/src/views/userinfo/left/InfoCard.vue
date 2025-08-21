<template>
  <div class="profile-card">
    <h3>个人简介</h3>
    <div class="bio">{{ profile.userProfile?.bio || '无' }}</div>
    
    <div class="profile-info">
      <div class="info-item">
        <span class="icon">📍</span>
        {{ profile.userProfile?.location }}
      </div>
      <div class="info-item">
        <span class="icon">👤</span>
        {{ profile.userProfile?.gender }}
      </div>
      <div class="info-item">
        <span class="icon">🏫</span>
        {{ profile.userProfile?.school }}
      </div>
      <div class="info-item" v-for="lang in profile.userProfile?.primaryLanguages" :key="lang">
        <span class="icon">{{ getLanguageIcon(lang) }}</span>
        {{ lang }}
      </div>
    </div>

    <h3>成就奖章</h3>
    <div class="achievements">
      <div 
        class="achievement-item" 
        v-for="achievement in profile.achievements" 
        :key="achievement.name"
      >
        <div class="achievement-name">
          <span :style="{ color: achievement.iconColor }">{{ achievement.icon }}</span>
          <span>{{ achievement.name }}</span>
        </div>
        <div class="achievement-count">{{ achievement.count }}</div>
      </div>
    </div>

    <h3>语言</h3>
    <div class="skills">
      <div 
        class="skill-item" 
        v-for="skill in profile.languageSkills" 
        :key="skill.name"
      >
        <span class="skill-name" :style="{ color: skill.color }">{{ skill.name }}</span>
        <span class="skill-count">解决数 {{ skill.solvedCount }}</span>
      </div>
      <div class="view-all">查看全部</div>
    </div>

    <h3>技能</h3>
    <div class="skill-tags">
      <a-tag 
        v-for="tag in profile.skillTags" 
        :key="tag.name"
        :color="tag.backgroundColor"
      >
        {{ tag.name }}
      </a-tag>
    </div>

    <div class="foundation-skills">
      <div class="foundation-header">
        <div class="foundation-title">基础实力</div>
        <div class="skill-indicators">
          <span 
            v-for="point in profile.foundationSkill?.skillPoints?.slice(0, 3)" 
            :key="point.name"
            class="skill-dot"
            :style="{ backgroundColor: point.color }"
          ></span>
        </div>
      </div>
      <div class="skill-categories">
        <div 
          v-for="(category, index) in profile.foundationSkill?.categories?.slice(0, 8)" 
          :key="category"
          class="skill-category"
          :class="{ 'category-row-1': index < 4, 'category-row-2': index >= 4 }"
        >
          {{ category }}
        </div>
      </div>
      <div class="view-all">查看全部</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { DeveloperProfile } from '@/types/userinfo'

interface Props {
  profile: DeveloperProfile
}

defineProps<Props>()

const getLanguageIcon = (language: string): string => {
  const icons: Record<string, string> = {
    'Java': '☕',
    'Python': '🐍',
    'JavaScript': '🟨',
    'TypeScript': '🔷',
    'C++': '⚡',
    'Go': '🐹'
  }
  return icons[language] || '💻'
}
</script>

<style scoped>
.profile-card {
  background: #ffffff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
}

.profile-card h3 {
  font-size: 14px;
  font-weight: 600;
  color: #24292f;
  margin: 16px 0 8px 0;
}

.profile-card h3:first-child {
  margin-top: 0;
}

.bio {
  color: #656d76;
  font-size: 14px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #24292f;
}

.icon {
  width: 16px;
  opacity: 0.7;
}

.achievements {
  margin-top: 8px;
}

.achievement-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #d0d7de;
}

.achievement-item:last-child {
  border-bottom: none;
}

.achievement-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.achievement-count {
  font-weight: 600;
  color: #24292f;
}

.skills {
  margin-top: 8px;
}

.skill-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  font-size: 14px;
}

.skill-name {
  font-weight: 500;
}

.skill-count {
  color: #656d76;
}

.view-all {
  text-align: center;
  margin-top: 8px;
  color: #0969da;
  font-size: 12px;
  cursor: pointer;
}

.view-all:hover {
  text-decoration: underline;
}

.skill-tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.foundation-skills {
  background: #f6f8fa;
  padding: 12px;
  border-radius: 6px;
  margin-top: 12px;
}

.foundation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.foundation-title {
  font-size: 12px;
  color: #656d76;
}

.skill-indicators {
  display: flex;
  gap: 4px;
}

.skill-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.skill-categories {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 16px;
  margin-bottom: 8px;
}

.skill-category {
  color: #656d76;
  font-size: 12px;
}
</style>
