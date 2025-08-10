<template>
  <el-container class="layout-container">
    <!-- 现代化侧边栏 -->
    <el-aside :width="sidebarWidth" class="sidebar">
      <div class="sidebar-header">
        <div class="logo-section">
          <div class="logo-icon">
            <el-icon size="24">
              <Platform />
            </el-icon>
          </div>
          <transition name="fade">
            <div v-show="!isCollapsed" class="logo-text">
              <h3 class="app-title">CodeForge</h3>
              <h3 class="app-title">后台管理</h3>
            </div>
          </transition>
        </div>
        <el-button
          :icon="isCollapsed ? Expand : Fold"
          class="collapse-btn"
          text
          @click="toggleSidebar"
        />
      </div>

      <el-scrollbar class="sidebar-menu-container">
        <el-menu
          :collapse="isCollapsed"
          :default-active="$route.path"
          :router="true"
          active-text-color="#ffffff"
          background-color="transparent"
          class="sidebar-menu"
          text-color="#ffffff"
        >
          <el-menu-item class="menu-item" index="/users">
            <el-icon>
              <User />
            </el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item class="menu-item" index="/roles">
            <el-icon>
              <Lock />
            </el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item class="menu-item" index="/problems">
            <el-icon>
              <Memo />
            </el-icon>
            <span>题目管理</span>
          </el-menu-item>
          <el-menu-item class="menu-item" index="/solutions">
            <el-icon>
              <Memo />
            </el-icon>
            <span>题解管理</span>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="main-container">
      <!-- 现代化头部导航 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-breadcrumb class="breadcrumb" separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentPageName">{{ currentPageName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <div class="user-info">
            <span class="welcome-text">欢迎回来</span>
            <el-dropdown class="user-dropdown" trigger="click">
              <div class="user-avatar-section">
                <el-avatar :icon="UserFilled" class="user-avatar" />
                <el-icon class="dropdown-icon">
                  <ArrowDown />
                </el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-menu">
                  <el-dropdown-item class="user-menu-item" @click="goProfile">
                    <el-icon>
                      <User />
                    </el-icon>
                    <span>个人中心</span>
                  </el-dropdown-item>
                  <el-dropdown-item class="user-menu-item">
                    <el-icon>
                      <Setting />
                    </el-icon>
                    <span>系统设置</span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    class="user-menu-item logout-item"
                    divided
                    @click="handleLogout"
                  >
                    <el-icon>
                      <SwitchButton />
                    </el-icon>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="main-content">
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <transition mode="out-in" name="fade-slide">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  Expand,
  Fold,
  Lock,
  Memo,
  Platform,
  Setting,
  SwitchButton,
  User,
  UserFilled,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 侧边栏状态管理
const isCollapsed = ref(false)
const sidebarWidth = computed(() => (isCollapsed.value ? '64px' : '240px'))

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const goProfile = () => {
  router.push('/profile')
}

// 页面名称映射
const pageNameMap: Record<string, string> = {
  '/users': '用户管理',
  '/roles': '角色管理',
  '/problems': '题目管理',
  '/solutions': '题解管理',
  '/profile': '个人中心',
}

const currentPageName = computed(() => {
  return pageNameMap[route.path] || ''
})

// 退出登录处理
const handleLogout = async () => {
  try {
    await authStore.logout()
    ElMessage.success('退出成功！')
  } catch (error) {
    console.error('Logout failed:', error)
    ElMessage.error('退出失败，请重试')
  }
}
</script>

<style scoped>
@import './index.css';
</style>
