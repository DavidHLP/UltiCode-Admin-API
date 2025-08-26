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
import UserInfoView from '@/views/userinfo/UserInfoView.vue'

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
        {
          path: '/profile',
          name: 'profile',
          component: UserInfoView,
        },
      ],
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

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    // 检查token是否存在且有效
    if (!authStore.token || !authStore.isTokenValid()) {
      // 如果token无效，清除认证状态并跳转到登录页
      if (authStore.token) {
        authStore.clearToken()
      }
      
      next({
        name: 'login',
        query: {
          redirect: to.fullPath // 保存原始路径，登录后可以跳转回来
        }
      })
      return
    }
    
    // 如果有token但没有用户信息，尝试获取用户信息
    if (!authStore.user) {
      try {
        await authStore.fetchUserInfo()
      } catch (error) {
        // 获取用户信息失败，可能是token无效，跳转到登录页
        console.error('路由守卫：获取用户信息失败', error)
        next({
          name: 'login',
          query: {
            redirect: to.fullPath
          }
        })
        return
      }
    }
  }
  
  // 如果已登录用户访问登录页，重定向到首页
  if (to.name === 'login' && authStore.token && authStore.isTokenValid()) {
    const redirectPath = (to.query.redirect as string) || '/'
    next(redirectPath)
    return
  }
  
  next()
})

export default router
