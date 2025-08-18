import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/login/LoginView.vue'
import RegisterView from '@/views/register/RegisterView.vue' // 导入注册视图
import ProblemBank from '@/views/problembank/ProblemBank.vue'
import ProblemView from '@/views/problem/ProblemView.vue'
import { MainLayout } from '@/layout'
import DescriptionCard from '@/views/problem/components/QuestionCard/DescriptionCard.vue'
import SolutionCard from '@/views/problem/components/QuestionCard/SolutionCard.vue'
import SubmissionCard from '@/views/problem/components/QuestionCard/SubmissionCard.vue'
import SolutionEditOrAdd from '@/views/problem/components/QuestionCard/components/SolutionEditOrAdd.vue'
import SolutionView from '@/views/problem/components/QuestionCard/components/SolutionView.vue'
import SolutionTable from '@/views/problem/components/QuestionCard/components/SolutionTable.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register', // 添加注册路由
      name: 'register',
      component: RegisterView,
    },
    {
      path: '/problem/:id',
      name: 'problem',
      component: ProblemView,
      redirect: (to) => ({ name: 'problem-description', params: to.params }),
      children: [
        {
          path: 'description',
          name: 'problem-description',
          component: DescriptionCard,
        },
        {
          path: 'solution',
          name: 'problem-solution',
          component: SolutionCard,
          redirect: (to) => ({ name: 'solution-list', params: to.params }),
          children: [
            {
              path: 'list',
              name: 'solution-list',
              component: SolutionTable,
              props: true,
            },
            {
              path: 'add',
              name: 'solution-add',
              component: SolutionEditOrAdd,
              props: true,
            },
            {
              path: ':solutionId',
              name: 'solution-detail',
              component: SolutionView,
              props: (route) => ({
                solutionId: Number(route.params.solutionId),
              }),
            },
            {
              path: ':solutionId/edit',
              name: 'solution-edit',
              component: SolutionEditOrAdd,
              props: (route) => ({
                solutionId: Number(route.params.solutionId),
              }),
            },
          ],
        },
        {
          path: 'submissions',
          name: 'problem-submissions',
          component: SubmissionCard,
        },
      ],
    },
    {
      path: '/',
      component: MainLayout,
      meta: {
        requiresAuth: true,
      },
      children: [
        {
          path: '',
          name: 'problembank',
          component: ProblemBank,
        },
      ],
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/userinfo/UserInfoView.vue'),
    },
    {
      path: '/problems',
      redirect: '/',
      meta: {
        requiresAuth: true,
      },
    },
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.token) {
    next({ name: 'login' })
  } else {
    next()
  }
})

export default router
