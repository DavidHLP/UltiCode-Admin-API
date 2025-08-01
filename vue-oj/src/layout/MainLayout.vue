<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <!-- 左侧导航菜单 -->
        <el-menu
          :default-active="activeIndex"
          class="nav-menu"
          mode="horizontal"
          :ellipsis="false"
          @select="handleSelect"
          background-color="transparent"
          text-color="#666"
          active-text-color="#1890ff"
        >
          <el-menu-item
            v-for="item in navItems"
            :key="item.path"
            :index="item.path"
            class="nav-item"
          >
            <router-link :to="item.path" class="menu-link">
              {{ item.name }}
            </router-link>
          </el-menu-item>
        </el-menu>

        <!-- 右侧操作区域 -->
        <div class="header-actions">
          <!-- 用户操作按钮 -->
          <div class="user-actions">
            <template v-if="!isLoggedIn">
              <el-button size="small" type="primary" @click="handleLogin"> 注册 </el-button>
              <el-button size="small" @click="handleRegister"> 登录 </el-button>
            </template>
            <template v-else>
              <el-dropdown @command="handleUserAction">
                <span class="user-info">
                  <el-avatar :size="32" :src="userAvatar" />
                  <span class="username">{{ username }}</span>
                  <el-icon>
                    <ArrowDown />
                  </el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                    <el-dropdown-item command="settings">设置</el-dropdown-item>
                    <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>

            <!-- 分享按钮 -->
            <el-button class="share-btn" size="small" text @click="handleShare">
              <el-icon>
                <Share />
              </el-icon>
              分享
            </el-button>
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Share } from '@element-plus/icons-vue'

const router = useRouter()

// 响应式数据
const isLoggedIn = ref(false) // 这里应该从用户状态管理中获取
const username = ref('用户名')
const userAvatar = ref('')
const activeIndex = ref('/')

// 导航菜单项
const navItems = ref([
  { name: '题库', path: '/' },
  { name: '竞赛', path: '/contest' },
  { name: '论坛', path: '/forum' },
])

// 处理菜单选择
const handleSelect = (key: string) => {
  activeIndex.value = key
}

const handleLogin = () => {
  router.push('/login')
}

const handleRegister = () => {
  router.push('/register')
}

const handleUserAction = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      // 实现登出逻辑
      isLoggedIn.value = false
      console.log('用户已登出')
      break
  }
}

const handleShare = () => {
  // 实现分享功能
  console.log('分享当前页面')
  if (navigator.share) {
    navigator.share({
      title: document.title,
      url: window.location.href,
    })
  } else {
    // 降级处理：复制链接到剪贴板
    navigator.clipboard.writeText(window.location.href)
    console.log('链接已复制到剪贴板')
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 左侧导航菜单 */
.nav-menu {
  border-bottom: none !important;
  height: 48px;
  display: flex;
  align-items: center;
}

:deep(.el-menu--horizontal) {
  border-bottom: none;
}

:deep(.el-menu--horizontal > .el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 0 8px;
  padding: 0 8px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  transition: color 0.3s;
  border-bottom: 2px solid transparent;
}

:deep(.el-menu--horizontal > .el-menu-item.is-active) {
  color: #1890ff;
  border-bottom-color: #1890ff;
  background: transparent !important;
}

:deep(.el-menu--horizontal > .el-menu-item:not(.is-disabled):hover) {
  color: #1890ff;
  background: transparent !important;
}

.menu-link {
  display: block;
  width: 100%;
  height: 100%;
  color: inherit;
  text-decoration: none;
}

/* 右侧操作区域 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  width: 240px;
}

.search-input {
  border-radius: 20px;
}

.search-icon {
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
}

.search-icon:hover {
  color: #1890ff;
}

/* 用户操作区域 */
.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.share-btn {
  color: #666;
  padding: 8px 12px;
}

.share-btn:hover {
  color: #1890ff;
  background-color: #f0f8ff;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  background: #f5f5f5;
}
</style>
