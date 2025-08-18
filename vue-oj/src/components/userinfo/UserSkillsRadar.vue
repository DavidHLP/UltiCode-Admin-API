<template>
  <div class="user-skills">
    <div class="section-title">技能</div>
    <div class="skills-radar" ref="radarRef"></div>
    <div class="skill-list">
      <div v-for="skill in skills" :key="skill.name" class="skill-item">
        <span class="skill-name">{{ skill.name }}</span>
        <span class="skill-level">{{ skill.level }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import type { SkillData } from '@/types/userinfo'

const props = defineProps<{
  skills: SkillData[]
}>()

const radarRef = ref<HTMLElement>()

onMounted(() => {
  if (radarRef.value) {
    const chart = echarts.init(radarRef.value)
    
    const option = {
      backgroundColor: 'transparent',
      radar: {
        indicator: props.skills.map(skill => ({
          name: skill.name,
          max: 100
        })),
        radius: '60%',
        center: ['50%', '50%'],
        splitArea: {
          areaStyle: {
            color: ['rgba(56, 178, 172, 0.05)', 'rgba(56, 178, 172, 0.1)']
          }
        },
        splitLine: {
          lineStyle: {
            color: 'rgba(208, 215, 222, 0.6)'
          }
        },
        axisLine: {
          lineStyle: {
            color: 'rgba(208, 215, 222, 0.8)'
          }
        },
        name: {
          textStyle: {
            color: '#656d76',
            fontSize: 10
          }
        }
      },
      series: [{
        type: 'radar',
        data: [{
          value: props.skills.map(skill => skill.value),
          areaStyle: {
            color: 'rgba(26, 127, 55, 0.2)'
          },
          lineStyle: {
            color: '#1a7f37',
            width: 2
          },
          itemStyle: {
            color: '#1a7f37'
          }
        }]
      }]
    }
    
    chart.setOption(option)
    
    // 响应式
    const resizeObserver = new ResizeObserver(() => {
      chart.resize()
    })
    resizeObserver.observe(radarRef.value)
  }
})
</script>

<style scoped lang="scss">
.user-skills {
  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin: 24px 0 16px 0;
    padding-bottom: 8px;
    border-bottom: 1px solid #d0d7de;
    color: #24292f;
  }

  .skills-radar {
    width: 200px;
    height: 200px;
    margin: 16px auto;
  }

  .skill-list {
    .skill-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 8px 0;
      color: #656d76;
      font-size: 14px;

      .skill-name {
        flex: 1;
      }

      .skill-level {
        font-style: italic;
      }
    }
  }
}
</style>
