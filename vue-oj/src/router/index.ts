import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/login/LoginView.vue'
import QuestionBank from '@/views/questionbank/QuestionBank.vue'
import { MainLayout } from '@/layout'
import QuizPage from '@/views/quizpage/QuizPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/quizpage/',
      name: 'quizpage',
      component: QuizPage,
    },
    {
      path: '/',
      component: MainLayout,
      meta: {
        requiresAuth: true,
      },
      children: [
        {
          path: 'questionbank',
          name: 'questionbank',
          component: QuestionBank,
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
