<template>
  <div class="chart-container">
    <div ref="chartRef" class="chart"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import type { ContributionPoint } from '@/types/userinfo'

interface Props {
  chartData: ContributionPoint[]
}

const props = defineProps<Props>()

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value || !props.chartData?.length) return

  chartInstance = echarts.init(chartRef.value)

  const option = {
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: props.chartData.map(item => new Date(item.date).getFullYear()),
      axisLine: {
        lineStyle: { color: '#d0d7de' }
      },
      axisLabel: {
        color: '#656d76',
        fontSize: 10
      }
    },
    yAxis: {
      type: 'value',
      show: false
    },
    series: [
      {
        type: 'line',
        data: props.chartData.map(item => item.value),
        smooth: true,
        lineStyle: {
          color: '#0969da',
          width: 2
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(9, 105, 218, 0.3)' },
              { offset: 1, color: 'rgba(9, 105, 218, 0)' }
            ]
          }
        },
        symbol: 'none'
      }
    ],
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#24292f',
      borderColor: '#30363d',
      textStyle: { color: '#f0f6fc' }
    }
  }

  chartInstance.setOption(option)
}

onMounted(() => {
  initChart()
  
  const resizeObserver = new ResizeObserver(() => {
    chartInstance?.resize()
  })
  
  if (chartRef.value) {
    resizeObserver.observe(chartRef.value)
  }
})

watch(() => props.chartData, initChart, { deep: true })
</script>

<style scoped>
.chart-container {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1), rgba(78, 205, 196, 0.1));
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
  position: relative;
  height: 200px;
}

.chart {
  width: 100%;
  height: 100%;
}
</style>
