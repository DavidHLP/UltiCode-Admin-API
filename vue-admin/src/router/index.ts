import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/Layout.vue'
import UserManagement from '../views/user/UserManagement.vue'
import RoleManagement from '../views/role/RoleManagement.vue'
import ProblemManagement from '../views/problem/ProblemManagement.vue'
import Login from '../views/login/Login.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
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
          path: '',
          redirect: '/users',
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
