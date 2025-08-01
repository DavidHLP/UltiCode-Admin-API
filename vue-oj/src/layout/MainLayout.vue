<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <!-- 左侧导航菜单 -->
        <nav class="nav-menu">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :class="{ active: $route.path === item.path }"
            :to="item.path"
            class="nav-item"
          >
            {{ item.name }}
          </router-link>
        </nav>

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

// 导航菜单项
const navItems = ref([
  { name: '题库', path: '/' },
  { name: '竞赛', path: '/contest' },
  { name: '论坛', path: '/forum' },
])

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
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 左侧导航菜单 */
.nav-menu {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-item {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 0;
  position: relative;
  transition: color 0.3s;
}

.nav-item:hover {
  color: #1890ff;
}

.nav-item.active {
  color: #1890ff;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: -17px;
  left: 0;
  right: 0;
  height: 2px;
  background: #1890ff;
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

:deep(.search-input .el-input__wrapper) {
  border-radius: 20px;
  border: 1px solid #d9d9d9;
  box-shadow: none;
  transition: all 0.3s;
}

:deep(.search-input .el-input__wrapper:hover) {
  border-color: #1890ff;
}

:deep(.search-input .el-input__wrapper.is-focus) {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
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

/* 响应式设计 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }

  .nav-menu {
    gap: 16px;
  }

  .nav-item {
    font-size: 13px;
  }

  .search-box {
    width: 180px;
  }

  .user-actions {
    gap: 8px;
  }
}

@media (max-width: 640px) {
  .nav-menu {
    display: none;
  }

  .search-box {
    width: 140px;
  }

  .username {
    display: none;
  }
}
</style>
