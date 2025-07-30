import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/login/LoginView.vue'
import ProblemBank from '@/views/problembank/ProblemBank.vue'
import ProblemView from '@/views/problem/ProblemView.vue'
import { MainLayout } from '@/layout'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/problem/:id',
      name: 'problem',
      component: ProblemView,
    },
    {
      path: '/',
      component: MainLayout,
      meta: {
        requiresAuth: true,
      },
      children: [
        {
          path: '/',
          name: 'problembank',
          component: ProblemBank,
        },
      ],
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
