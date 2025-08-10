 <template>
  <div class="user-profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>

      <div v-if="user">
        <!-- Hero Header -->
        <div class="profile-hero">
          <div class="hero-left">
            <el-avatar :size="72" :src="userAvatar" />
            <div class="hero-meta">
              <div class="hero-name">{{ user.username }}</div>
              <div class="hero-sub">{{ user.email }} · ID: {{ user.userId }}</div>
            </div>
          </div>
          <div class="hero-actions">
            <el-button :loading="loading" @click="handleRefresh" plain>刷新信息</el-button>
            <el-button type="primary" @click="handleChangeAvatar">更换头像</el-button>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="tabs-container" :tab-position="tabPosition">
          <el-tab-pane label="概览" name="overview">
            <div class="profile-content">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-descriptions :column="2" border title="用户信息">
                    <el-descriptions-item label="用户名">
                      {{ user.username }}
                    </el-descriptions-item>
                    <el-descriptions-item label="邮箱">
                      {{ user.email }}
                    </el-descriptions-item>
                    <el-descriptions-item label="角色">
                      <el-tag>{{ user.role.roleName }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="用户ID">
                      {{ user.userId }}
                    </el-descriptions-item>
                  </el-descriptions>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>

          <el-tab-pane label="资料编辑" name="basic">
            <div class="profile-content">
              <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-width="90px">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="basicForm.username" placeholder="请输入用户名" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="basicForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <div class="form-actions">
                  <el-button type="primary" @click="submitBasic">保存</el-button>
                  <el-button @click="syncFormFromUser">重置</el-button>
                </div>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane label="安全设置" name="security">
            <div class="profile-content">
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input type="password" show-password v-model="pwdForm.oldPassword" placeholder="请输入当前密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input type="password" show-password v-model="pwdForm.newPassword" placeholder="请输入新密码" />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input type="password" show-password v-model="pwdForm.confirmPassword" placeholder="请再次输入新密码" />
                </el-form-item>
                <div class="form-actions">
                  <el-button type="primary" @click="submitPwd">修改密码</el-button>
                </div>
              </el-form>
            </div>
          </el-tab-pane>
          <el-tab-pane name="submissions">
            <template #label>
              <span>我的提交</span>
              <el-badge v-if="submissionCount" :value="submissionCount" class="tab-badge" />
            </template>
            <div class="profile-content">
              <el-form class="filters" inline size="small">
                <el-form-item label="题目ID">
                  <el-input v-model="submissionsFilter.problemId" placeholder="如 1001" style="width: 140px" />
                </el-form-item>
                <el-form-item label="状态">
                  <el-select v-model="submissionsFilter.status" placeholder="全部" style="width: 140px">
                    <el-option label="全部" value="" />
                    <el-option label="Accepted" value="AC" />
                    <el-option label="Wrong Answer" value="WA" />
                    <el-option label="Time Limit Exceeded" value="TLE" />
                    <el-option label="Memory Limit Exceeded" value="MLE" />
                    <el-option label="Runtime Error" value="RE" />
                    <el-option label="Compile Error" value="CE" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button @click="applySubmissionFilter" :loading="loadingSubmissions">应用筛选</el-button>
                  <el-button @click="resetSubmissionFilter" :disabled="loadingSubmissions">重置</el-button>
                </el-form-item>
              </el-form>
              <div class="list-actions">
                <el-button size="small" :loading="loadingSubmissions" @click="loadMySubmissions">刷新</el-button>
              </div>
              <el-skeleton v-if="loadingSubmissions" :rows="5" animated />
              <el-empty v-else-if="!mySubmissions.length" description="暂无提交记录" />
              <el-table v-else :data="mySubmissions" size="small" stripe>
                <el-table-column prop="id" label="提交ID" width="100" />
                <el-table-column prop="problemId" label="题目ID" width="100" />
                <el-table-column prop="status" label="状态" width="120" />
                <el-table-column prop="createdAt" label="提交时间" />
              </el-table>
            </div>
          </el-tab-pane>
          <el-tab-pane name="solutions">
            <template #label>
              <span>我的题解</span>
              <el-badge v-if="solutionCount" :value="solutionCount" class="tab-badge" />
            </template>
            <div class="profile-content">
              <el-form class="filters" inline size="small">
                <el-form-item label="题目ID">
                  <el-input v-model="solutionsFilter.problemId" placeholder="如 1001" style="width: 140px" />
                </el-form-item>
                <el-form-item label="标题关键词">
                  <el-input v-model="solutionsFilter.keyword" placeholder="如 二分、并查集" style="width: 240px" />
                </el-form-item>
                <el-form-item>
                  <el-button @click="applySolutionFilter" :loading="loadingSolutions">搜索</el-button>
                  <el-button @click="resetSolutionFilter" :disabled="loadingSolutions">重置</el-button>
                </el-form-item>
              </el-form>
              <div class="list-actions">
                <el-button size="small" :loading="loadingSolutions" @click="loadMySolutions">刷新</el-button>
              </div>
              <el-skeleton v-if="loadingSolutions" :rows="5" animated />
              <el-empty v-else-if="!mySolutions.length" description="暂无题解" />
              <el-table v-else :data="mySolutions" size="small" stripe>
                <el-table-column prop="id" label="题解ID" width="100" />
                <el-table-column prop="problemId" label="题目ID" width="100" />
                <el-table-column prop="title" label="标题" />
                <el-table-column prop="likes" label="点赞" width="100" />
              </el-table>
              <div v-if="solutionsTotal > 0" style="margin-top: 12px; display: flex; justify-content: flex-end;">
                <el-pagination
                  small
                  layout="prev, pager, next, sizes, total"
                  :total="solutionsTotal"
                  :page-sizes="[10, 20, 30, 50]"
                  v-model:current-page="solutionsPage"
                  v-model:page-size="solutionsSize"
                  @current-change="handleSolutionsPageChange"
                  @size-change="handleSolutionsSizeChange"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div v-else class="loading-state">
        <el-skeleton :rows="8" animated />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getSubmissionsByProblemId } from '@/api/submission'
import { getSolutionsByProblemId } from '@/api/solution'

const authStore = useAuthStore()

const user = computed(() => authStore.user)
const userAvatar = computed(() => authStore.userAvatar)
const isLoggedIn = computed(() => authStore.isLoggedIn)
const loading = computed(() => authStore.loading)

const activeTab = ref<'overview' | 'basic' | 'security' | 'submissions' | 'solutions'>('overview')

// 路由 & 标签同步
const route = useRoute()
const router = useRouter()
const allowedTabs = ['overview', 'basic', 'security', 'submissions', 'solutions'] as const
const isValidTab = (t: unknown): t is typeof allowedTabs[number] => typeof t === 'string' && (allowedTabs as readonly string[]).includes(t)

// Tabs 位置响应式（桌面左侧，移动顶部）
const tabPosition = ref<'top' | 'left'>('left')
const updateTabPosition = () => {
  tabPosition.value = window.innerWidth < 992 ? 'top' : 'left'
}

// 类型与数据（占位）
type MySubmission = { id: number; problemId: number; status: string; createdAt: string }
type MySolution = { id: number; problemId: number; title: string; likes: number }

const mySubmissions = ref<MySubmission[]>([])
const mySolutions = ref<MySolution[]>([])
const loadingSubmissions = ref(false)
const loadingSolutions = ref(false)
const submissionCount = computed(() => mySubmissions.value.length)
const solutionCount = computed(() => mySolutions.value.length)

// 提交/题解筛选与处理
type SubmissionsFilter = { problemId: string; status: string }
type SolutionsFilter = { problemId: string; keyword: string }

const submissionsFilter = reactive<SubmissionsFilter>({ problemId: '', status: '' })
const solutionsFilter = reactive<SolutionsFilter>({ problemId: '', keyword: '' })

// 题解分页
const solutionsPage = ref(1)
const solutionsSize = ref(10)
const solutionsTotal = ref(0)
const handleSolutionsPageChange = () => loadMySolutions()
const handleSolutionsSizeChange = () => {
  solutionsPage.value = 1
  loadMySolutions()
}

const isPositiveInt = (v: string) => /^\d+$/.test(v)

const applySubmissionFilter = () => {
  if (submissionsFilter.problemId && !isPositiveInt(submissionsFilter.problemId)) {
    ElMessage.error('题目ID需为数字')
    return
  }
  loadMySubmissions()
}

const resetSubmissionFilter = () => {
  submissionsFilter.problemId = ''
  submissionsFilter.status = ''
  loadMySubmissions()
}

const applySolutionFilter = () => {
  // 关键字暂不校验
  solutionsPage.value = 1
  loadMySolutions()
}

const resetSolutionFilter = () => {
  solutionsFilter.problemId = ''
  solutionsFilter.keyword = ''
  loadMySolutions()
}

// 基本资料表单
const basicFormRef = ref<FormInstance>()
const basicForm = reactive({
  username: '',
  email: '',
})
const basicRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度应为 3-20 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] },
  ],
}

// 密码表单
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const validateConfirmPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) return callback(new Error('请再次输入新密码'))
  if (value !== pwdForm.newPassword) return callback(new Error('两次输入的密码不一致'))
  callback()
}
const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码不少于 6 位', trigger: 'blur' },
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
}

// 同步用户信息到表单
const syncFormFromUser = () => {
  if (user.value) {
    basicForm.username = user.value.username || ''
    basicForm.email = user.value.email || ''
  }
}

watch(user, () => syncFormFromUser(), { immediate: true })

// 事件
const handleChangeAvatar = () => {
  ElMessage.info('更换头像功能正在开发中')
}

const handleRefresh = async () => {
  try {
    await authStore.fetchUserInfo()
    ElMessage.success('已刷新用户信息')
  } catch {
    // fetchUserInfo 内部已处理错误和 token 清理
  }
}

const submitBasic = async () => {
  const form = basicFormRef.value
  if (!form) return
  await form.validate(async (valid) => {
    if (!valid) return
    // TODO: 调用更新资料接口
    ElMessage.info('资料编辑功能正在接入后端，敬请期待')
  })
}

const submitPwd = async () => {
  const form = pwdFormRef.value
  if (!form) return
  await form.validate(async (valid) => {
    if (!valid) return
    // TODO: 调用修改密码接口
    ElMessage.info('密码修改功能正在接入后端，敬请期待')
  })
}

// 列表加载（占位）
const loadMySubmissions = async () => {
  if (!user.value) return
  try {
    loadingSubmissions.value = true
    if (!submissionsFilter.problemId) {
      mySubmissions.value = []
      ElMessage.warning('请先输入题目ID再查询提交记录')
      return
    }
    if (submissionsFilter.problemId && !isPositiveInt(submissionsFilter.problemId)) {
      ElMessage.error('题目ID需为数字')
      return
    }
    const list = await getSubmissionsByProblemId(Number(submissionsFilter.problemId))
    const mapped = list.map((s) => ({
      id: s.id,
      problemId: s.problemId,
      status: s.status,
      createdAt: s.createdAt,
    })) as MySubmission[]
    mySubmissions.value = submissionsFilter.status
      ? mapped.filter((x) => x.status === submissionsFilter.status)
      : mapped
  } catch (e) {
    console.error(e)
  } finally {
    loadingSubmissions.value = false
  }
}

const loadMySolutions = async () => {
  if (!user.value) return
  try {
    loadingSolutions.value = true
    if (!solutionsFilter.problemId) {
      mySolutions.value = []
      solutionsTotal.value = 0
      ElMessage.warning('请先输入题目ID再查询题解')
      return
    }
    if (solutionsFilter.problemId && !isPositiveInt(solutionsFilter.problemId)) {
      ElMessage.error('题目ID需为数字')
      return
    }
    const page = await getSolutionsByProblemId({
      problemId: Number(solutionsFilter.problemId),
      page: solutionsPage.value,
      size: solutionsSize.value,
      title: solutionsFilter.keyword || '',
      sort: 'hot',
    })
    solutionsTotal.value = page.total || 0
    mySolutions.value = (page.records || []).map((r) => ({
      id: r.id,
      problemId: Number(solutionsFilter.problemId),
      title: r.title,
      likes: r.upvotes ?? 0,
    })) as MySolution[]
  } catch (e) {
    console.error(e)
  } finally {
    loadingSolutions.value = false
  }
}

// 切换标签时按需加载
watch(activeTab, (t) => {
  if (t === 'submissions') loadMySubmissions()
  if (t === 'solutions') loadMySolutions()
})

// 标签变化时更新路由 query（避免历史堆栈膨胀使用 replace）
watch(activeTab, (t) => {
  const q = route.query.tab
  if (q !== t) {
    router.replace({ query: { ...route.query, tab: t } }).catch(() => void 0)
  }
})

// 监听路由变化，外部变更时同步到本地状态
watch(
  () => route.query.tab,
  (t) => {
    if (isValidTab(t) && t !== activeTab.value) activeTab.value = t
  }
)

onMounted(() => {
  updateTabPosition()
  window.addEventListener('resize', updateTabPosition)

  // 根据路由初始化 tab
  const t = route.query.tab
  if (isValidTab(t)) activeTab.value = t

  if (isLoggedIn.value && !user.value) {
    authStore.fetchUserInfo().catch(() => void 0)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateTabPosition)
})
</script>

<style scoped>
.user-profile-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 48px);
  /* 减去顶部导航栏高度 */
}

.profile-card {
  max-width: 980px;
  margin: 0 auto;
  border-radius: 8px;
}

.card-header {
  font-size: 18px;
  font-weight: 500;
}

.profile-content {
  padding: 20px;
}

.avatar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.mt-4 {
  margin-top: 16px;
}

.loading-state {
  padding: 20px;
}

.list-actions {
  margin-bottom: 12px;
  display: flex;
  justify-content: flex-end;
}

/* Hero header */
.profile-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 20px 12px;
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-meta {
  display: flex;
  flex-direction: column;
}

.hero-name {
  font-size: 18px;
  font-weight: 600;
}

.hero-sub {
  color: #909399;
  font-size: 13px;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.tab-badge {
  margin-left: 6px;
}

@media (max-width: 991px) {
  .profile-card {
    max-width: 100%;
  }
  .profile-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .hero-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
