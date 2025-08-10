import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/LayoutComponent.vue'
import UserManagement from '@/views/user/UserManagement.vue'
import RoleManagement from '@/views/role/RoleManagement.vue'
import ProblemManagement from '@/views/problem/ProblemManagement.vue'
import ProblemEditor from '@/views/problem/ProblemEditor.vue'
import SolutionManagement from '@/views/solution/SolutionManagement.vue'
import ProfileCenter from '@/views/profile/ProfileCenter.vue'
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
          path: 'problems/new',
          name: 'problem-new',
          component: ProblemEditor,
          meta: { title: '新建题目' },
        },
        {
          path: 'problems/:id/edit',
          name: 'problem-edit',
          component: ProblemEditor,
          meta: { title: '编辑题目' },
        },
        {
          path: 'solutions',
          name: 'solutions',
          component: SolutionManagement,
        },
        {
          path: 'profile',
          name: 'profile',
          component: ProfileCenter,
          meta: { title: '个人中心' },
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
    // 如果要访问登录页面且已经登录，重定向到首页或原目标页
    if (to.name === 'login' && authStore.isAuthenticated) {
      const redirectTarget = (to.query?.redirect as string | undefined) || '/'
      next({ path: redirectTarget })
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

    // 角色权限校验（可选）：当路由声明了 meta.roles 时进行校验
    const requiredRoles = (to.meta.roles as string[] | undefined) || []
    if (requiredRoles.length > 0 && !requiredRoles.some((r) => authStore.hasRole(r))) {
      // 权限不足，回到首页或其它安全页面
      next({ path: '/' })
      return
    }

    // 如果有token，继续访问
    next()
  } catch (error) {
    console.error('路由守卫错误:', error)
    // 发生错误时清除token并重定向到登录页
    authStore.clearAuthData()
    next({
      name: 'login',
      query: { redirect: to.fullPath },
    })
  } finally {
    // 清除加载状态
    authStore.setLoading(false)
  }
})

// 路由后置守卫 - 清除加载状态并设置页面标题
router.afterEach((to) => {
  const authStore = useAuthStore()
  authStore.setLoading(false)
  const title = (to.meta?.title as string | undefined) || ''
  document.title = `SpringOJ Admin${title ? ' - ' + title : ''}`
})

export default router
