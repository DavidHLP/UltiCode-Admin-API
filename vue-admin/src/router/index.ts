import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/LayoutComponent.vue'
import UserManagement from '@/views/user/UserManagement.vue'
import RoleManagement from '@/views/role/RoleManagement.vue'
import ProblemManagement from '@/views/problem/ProblemManagement.vue'
import SolutionManagement from '@/views/solution/SolutionManagement.vue'
import Login from '@/views/login/Login.vue'
import { useAuthStore } from '@/stores/auth'

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login,
    },
    {
      path: '/',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'users',
          name: 'users',
          component: UserManagement,
        },
        {
          path: 'roles',
          name: 'roles',
          component: RoleManagement,
        },
        {
          path: 'problems',
          name: 'problems',
          component: ProblemManagement,
        },
        {
          path: 'solutions',
          name: 'solutions',
          component: SolutionManagement,
        },
        {
          path: '',
          redirect: '/users',
        },
      ],
    },
  ],
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 设置加载状态
  authStore.setLoading(true)

  try {
    // 如果要访问登录页面且已经登录，重定向到首页
    if (to.name === 'login' && authStore.isAuthenticated) {
      next({ path: '/' })
      return
    }

    // 如果路由不需要认证，直接通过
    if (!to.meta.requiresAuth) {
      next()
      return
    }

    // 如果需要认证但没有token，重定向到登录页
    if (!authStore.isAuthenticated) {
      next({
        name: 'login',
        query: { redirect: to.fullPath }, // 保存原始访问路径，登录后可以重定向回来
      })
      return
    }

    // 如果有token，继续访问
    next()
  } catch (error) {
    console.error('路由守卫错误:', error)
    // 发生错误时清除token并重定向到登录页
    authStore.clearToken()
    next({
      name: 'login',
      query: { redirect: to.fullPath },
    })
  } finally {
    // 清除加载状态
    authStore.setLoading(false)
  }
})

// 路由后置守卫 - 清除加载状态
router.afterEach(() => {
  const authStore = useAuthStore()
  authStore.setLoading(false)
})

export default router
