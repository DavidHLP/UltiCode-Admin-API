<template>
  <ManageComponent ref="manageComponentRef" title="用户管理" :title-icon="User" add-button-text="添加用户"
    search-placeholder="搜索用户名或邮箱..." empty-text="暂无用户数据" :table-data="filteredUsers" :loading="loading" :saving="saving"
    @add="openAddUserDialog" @search="handleSearch" @refresh="refreshData" @dialog-confirm="saveUser">
    <!-- 自定义筛选器：角色筛选 -->
    <template #filters>
      <el-select v-model="selectedRole" placeholder="筛选角色" class="role-filter" clearable @change="handleRoleFilter">
        <el-option v-for="role in allRoles" :key="role.id" :label="role.roleName" :value="role.id" />
      </el-select>
    </template>

    <!-- 表格列定义 -->
    <template #table-columns>
      <el-table-column prop="userId" label="ID" width="80" align="center">
        <template #default="scope">
          <el-tag type="info" size="small" class="id-tag"> #{{ scope.row.userId }} </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="username" label="用户名" min-width="120">
        <template #default="scope">
          <div class="user-info">
            <el-avatar :size="32" class="user-avatar">
              {{ scope.row.username.charAt(0).toUpperCase() }}
            </el-avatar>
            <span class="username">{{ scope.row.username }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="email" label="邮箱" min-width="200">
        <template #default="scope">
          <div class="email-cell">
            <el-icon class="email-icon">
              <Message />
            </el-icon>
            <span>{{ scope.row.email }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="个人简介" min-width="150" show-overflow-tooltip>
        <template #default="scope">
          <div class="introduction-cell">
            <el-icon class="intro-icon">
              <Document />
            </el-icon>
            <span>{{ scope.row.introduction || '暂无简介' }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="地址" min-width="120" show-overflow-tooltip>
        <template #default="scope">
          <div class="address-cell">
            <el-icon class="address-icon">
              <Location />
            </el-icon>
            <span>{{ scope.row.address || '未填写' }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small" class="status-tag">
            <el-icon class="status-icon">
              <CircleCheck v-if="scope.row.status === 1" />
              <CircleClose v-else />
            </el-icon>
            {{ scope.row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="角色" min-width="150">
        <template #default="scope">
          <div class="roles-cell">
            <el-tag v-for="role in scope.row.roles" :key="role.id" :type="getRoleTagType(role.roleName)" size="small"
              class="role-tag">
              {{ role.roleName }}
            </el-tag>
            <el-tag v-if="!scope.row.roles || scope.row.roles.length === 0" type="info" size="small">
              无角色
            </el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="最后登录" min-width="180">
        <template #default="scope">
          <div class="login-info">
            <div class="login-time">
              <el-icon class="time-icon">
                <Clock />
              </el-icon>
              <span>{{ formatDateTime(scope.row.lastLogin) || '从未登录' }}</span>
            </div>
            <div class="login-ip" v-if="scope.row.lastLoginIp">
              <el-icon class="ip-icon">
                <Monitor />
              </el-icon>
              <span>{{ scope.row.lastLoginIp }}</span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" min-width="120">
        <template #default="scope">
          <div class="create-time-cell">
            <el-icon class="create-icon">
              <Calendar />
            </el-icon>
            <span>{{ formatDateTime(scope.row.createTime) }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <div class="action-buttons">
            <el-button @click="openEditUserDialog(scope.row)" :icon="Edit" size="small" type="primary" plain
              class="action-btn">
              编辑
            </el-button>
            <el-button @click="handleDeleteUser(scope.row.userId)" :icon="Delete" size="small" type="danger" plain
              class="action-btn">
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </template>

    <!-- 对话框表单 -->
    <template #dialog-form="{ isEdit }">
      <el-form :model="currentUser" :rules="userRules" ref="userFormRef" label-width="80px" class="user-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="currentUser.username" placeholder="请输入用户名" :prefix-icon="UserIcon" clearable />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="currentUser.email" placeholder="请输入邮箱地址" :prefix-icon="Message" clearable />
        </el-form-item>

        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="currentUser.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password
            clearable />
        </el-form-item>

        <el-form-item label="角色" prop="roles">
          <el-select v-model="currentUserRoleIds" multiple placeholder="请选择用户角色" style="width: 100%" collapse-tags
            collapse-tags-tooltip>
            <el-option v-for="role in allRoles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
    </template>
  </ManageComponent>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import ManageComponent from '@/components/management/ManageComponent.vue'
import { fetchUsers, createUser, updateUser, deleteUser } from '@/api/user.ts'
import { fetchRoles } from '@/api/role.ts'
import type { User as UserData } from '@/types/user.ts'
import type { Role } from '@/types/role.ts'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  User,
  Edit,
  Delete,
  Message,
  UserFilled as UserIcon,
  Lock,
  Document,
  Location,
  CircleCheck,
  CircleClose,
  Clock,
  Monitor,
  Calendar
} from '@element-plus/icons-vue'

// ManageComponent 的引用
const manageComponentRef = ref<InstanceType<typeof ManageComponent> | null>(null)

// 响应式数据
const users = ref<UserData[]>([])
const allRoles = ref<Role[]>([])
const loading = ref(false)
const saving = ref(false)

// 搜索和筛选
const searchQuery = ref('')
const selectedRole = ref<number | undefined>()

// 对话框相关
const currentUser = ref<Partial<UserData>>({})
const currentUserRoleIds = ref<number[]>([])
const userFormRef = ref<FormInstance>()

// 表单验证规则
const userRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
}

// 计算属性
const filteredUsers = computed(() => {
  let result = users.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(
      (user) =>
        user.username.toLowerCase().includes(query) || user.email.toLowerCase().includes(query),
    )
  }

  // 角色过滤
  if (selectedRole.value) {
    result = result.filter(
      (user) => user.roles && user.roles.some((role) => role.id === selectedRole.value),
    )
  }

  return result
})

// 工具方法
const formatDateTime = (dateString: string | null) => {
  if (!dateString) return null

  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return null

    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    console.error('格式化日期时发生错误:', error)
    ElMessage.error('格式化日期时发生错误。')
    return null
  }
}

const getRoleTagType = (roleName: string) => {
  const roleTypeMap: Record<string, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    ADMIN: 'danger',
    USER: 'primary',
    MODERATOR: 'warning',
    GUEST: 'info',
  }
  return roleTypeMap[roleName] || 'primary'
}

// 搜索和筛选方法
const handleSearch = (query: string) => {
  searchQuery.value = query
}

const handleRoleFilter = () => {
  // 筛选逻辑已在计算属性中处理, 无需操作
}

const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([getUsers(), getRoles()])
    ElMessage.success('数据刷新成功！')
  } catch (error) {
    console.error('刷新用户数据时发生错误:', error)
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

// 数据获取方法
const getUsers = async () => {
  loading.value = true
  try {
    users.value = await fetchUsers()
  } catch (error) {
    console.error('获取用户数据时发生错误:', error)
    ElMessage.error('获取用户列表失败。')
  } finally {
    loading.value = false
  }
}

const getRoles = async () => {
  try {
    allRoles.value = await fetchRoles()
  } catch (error) {
    console.error('获取角色数据时发生错误:', error)
    ElMessage.error('获取角色列表失败。')
  }
}

const openAddUserDialog = () => {
  currentUser.value = {}
  currentUserRoleIds.value = []
  manageComponentRef.value?.openDialog('添加新用户', {}, false)
}

const openEditUserDialog = (user: UserData) => {
  currentUser.value = { ...user }
  currentUserRoleIds.value = user.roles ? user.roles.map((r) => r.id) : []
  manageComponentRef.value?.openDialog('编辑用户', user, true)
}

const saveUser = async () => {
  if (!userFormRef.value) return
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        currentUser.value.roles = allRoles.value.filter((role) =>
          currentUserRoleIds.value.includes(role.id),
        )

        if (manageComponentRef.value?.isEdit) {
          await updateUser(currentUser.value.userId!, currentUser.value as UserData)
          ElMessage.success('用户更新成功。')
        } else {
          await createUser(currentUser.value as UserData)
          ElMessage.success('用户创建成功。')
        }
        manageComponentRef.value?.closeDialog()
        await getUsers()
      } catch (error) {
        console.error('保存用户时发生错误:', error)
        ElMessage.error('保存用户失败。')
      } finally {
        saving.value = false
      }
    }
  })
}

const handleDeleteUser = async (userId: number) => {
  try {
    await deleteUser(userId)
    await getUsers()
    ElMessage.success('用户删除成功。')
  } catch (error) {
    console.error('删除用户时发生错误:', error)
    ElMessage.error('删除用户失败。')
  }
}

onMounted(() => {
  getUsers()
  getRoles()
})
</script>

<style scoped>
.role-filter {
  width: 150px;
}

.id-tag {
  font-weight: 600;
  border-radius: 6px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  flex-shrink: 0;
}

.username {
  font-weight: 500;
  color: #303133;
}

.email-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.email-icon {
  color: #909399;
  flex-shrink: 0;
}

.introduction-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.intro-icon {
  color: #909399;
  flex-shrink: 0;
}

.address-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.address-icon {
  color: #909399;
  flex-shrink: 0;
}

.status-tag {
  border-radius: 6px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-icon {
  font-size: 12px;
}

.login-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.login-time,
.login-ip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.time-icon,
.ip-icon {
  color: #909399;
  flex-shrink: 0;
  font-size: 12px;
}

.login-ip {
  color: #909399;
  font-size: 12px;
}

.create-time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 13px;
}

.create-icon {
  color: #909399;
  flex-shrink: 0;
  font-size: 12px;
}

.roles-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.role-tag {
  border-radius: 6px;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-btn {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }

  .action-btn {
    width: 100%;
  }
}
</style>
