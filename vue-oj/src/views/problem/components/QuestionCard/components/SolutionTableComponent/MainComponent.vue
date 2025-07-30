<!-- eslint-disable @typescript-eslint/no-explicit-any -->
<template>
  <div class="solution-list-container" ref="containerRef">
    <ul v-infinite-scroll="load" class="infinite-list" :infinite-scroll-disabled="disabled" :infinite-scroll-delay="200"
      :infinite-scroll-distance="20">

      <li v-for="solution in solutions" :key="solution.id" class="list-item-wrapper" @click="handleRowClick(solution)">

        <el-card shadow="hover" class="solution-card">
          <header class="card-header">
            <div class="author-info">
              <el-avatar :size="32" :src="solution.authorAvatar" />
              <span class="author-username">{{ solution.authorUsername }}</span>
            </div>
            <span class="post-time">{{ formatDate(solution.createdAt) }}</span>
          </header>

          <main class="card-content">
            <h3 class="solution-title">{{ solution.title }}</h3>
            <p class="solution-problem">{{ solution.problem }}</p>
            <div class="solution-tags">
              <el-space wrap>
                <el-tag v-for="tag in solution.tags" :key="tag" type="info" size="small">{{ tag }}</el-tag>
              </el-space>
            </div>
          </main>

          <footer class="card-footer">
            <el-space :size="24">
              <span class="footer-item">
                <el-icon>
                  <ElIconCaretTop />
                </el-icon>
                <span>{{ solution.upvotes }}</span>
              </span>
              <span class="footer-item">
                <el-icon>
                  <ElIconView />
                </el-icon>
                <span>{{ solution.views }}</span>
              </span>
            </el-space>
          </footer>
        </el-card>

      </li>
    </ul>

    <p v-if="loading" class="loading-text">加载中...</p>
    <p v-if="noMore" class="loading-text">没有更多了</p>
    <el-empty v-if="!loading && solutions.length === 0" description="暂无题解" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { CaretTop as ElIconCaretTop, View as ElIconView } from '@element-plus/icons-vue'
import { getSolutionsByProblemId } from '@/api/solution';
import type { SolutionCardVo } from '@/types/problem';

// 定义 props
interface Props {
  searchQuery: string
  currentSort: 'hot' | 'new'
}

const props = defineProps<Props>()

// 定义 emits
const emit = defineEmits<{
  (e: 'solution-click', solution: SolutionCardVo): void
}>()

const route = useRoute();
const containerRef = ref<HTMLElement | null>(null);
const solutions = ref<SolutionCardVo[]>([]);
const loading = ref(false);
const noMore = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);

const disabled = ref(false);

const fetchSolutions = async (isNewSearch = false) => {
  if (loading.value || noMore.value) return;

  loading.value = true;
  try {
    const problemId = Number(route.params.id);
    if (!problemId) {
      ElMessage.warning('无效的题目ID');
      return;
    }

    const res = await getSolutionsByProblemId({
      problemId,
      page: currentPage.value,
      size: pageSize.value,
      title: props.searchQuery,
      sort: props.currentSort,
    });

    if (res && res.records && res.records.length > 0) {
      if (isNewSearch) {
        solutions.value = res.records;
      } else {
        solutions.value = [...solutions.value, ...res.records];
      }
      currentPage.value++;
      noMore.value = solutions.value.length >= res.total;
    } else {
      noMore.value = true;
      if (isNewSearch) {
        solutions.value = [];
      }
    }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (error: any) {
    console.error('获取题解列表失败:', error);
    ElMessage.error(error.response?.data?.message || '获取题解列表失败');
  } finally {
    loading.value = false;
  }
};

const load = () => {
  if (!disabled.value) {
    fetchSolutions();
  }
};

const resetAndFetch = () => {
  currentPage.value = 1;
  noMore.value = false;
  solutions.value = [];
  fetchSolutions(true);
};

watch(() => [props.searchQuery, props.currentSort], () => {
  resetAndFetch();
});

watch(() => route.params.id, (newId) => {
  if (newId) {
    resetAndFetch();
  }
});

onMounted(() => {
  resetAndFetch();
});

const handleRowClick = (solution: SolutionCardVo) => {
  emit('solution-click', solution)
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}/${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
/* 容器样式保持不变 */
.solution-list-container {
  height: 650px;
  overflow: auto;
  background-color: #f7f7f7;
  /* 图片中的背景色 */
  padding: 8px;
  /* 给卡片留出一些边距 */
}

.infinite-list {
  padding: 0;
  margin: 0;
  list-style: none;
}

/* 移除旧的 item 样式，改为 card 的样式 */
.list-item-wrapper {
  margin-bottom: 12px;
  /* 卡片之间的垂直间距 */
  cursor: pointer;
}

.solution-card {
  /* 移除 el-card 默认的边框，使其更像图片中的样子 */
  border: none;
}

/* 使用 :deep() 或 ::v-deep 来修改 el-card 的内部 padding */
:deep(.el-card__body) {
  padding: 16px;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  /* 头像和用户名之间的间距 */
}

.author-username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.post-time {
  font-size: 13px;
  color: #999;
}

/* 卡片内容 */
.solution-title {
  font-size: 16px;
  font-weight: 600;
  color: #222;
  margin: 0 0 8px 0;
}

.solution-problem {
  font-size: 14px;
  color: #555;
  margin: 0 0 12px 0;
  line-height: 1.5;
}

/* 标签区域 */
.solution-tags {
  margin-bottom: 16px;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  align-items: center;
  color: #888;
  font-size: 14px;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.footer-item {
  display: flex;
  align-items: center;
  gap: 4px;
  /* 图标和数字之间的间距 */
}

.footer-item .el-icon {
  font-size: 16px;
}

/* 加载提示文字 */
.loading-text {
  text-align: center;
  color: #999;
  padding: 15px 0;
}
</style>
