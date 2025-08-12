<template>
  <ManageComponent
    ref="manageComponentRef"
    v-model:current-page="currentPage"
    v-model:page-size="pageSize"
    :loading="loading"
    :saving="saving"
    :table-data="filteredUsers"
    :title-icon="User"
    :total="total"
    add-button-text="添加用户"
    empty-text="暂无用户数据"
    search-placeholder="搜索用户名或邮箱..."
    title="用户管理"
    @add="openAddUserDialog"
    @refresh="refreshData"
    @search="handleSearch"
    @size-change="handlePageSizeChange"
    @current-change="handleCurrentPageChange"
    @dialog-confirm="saveUser"
  >
    <!-- 自定义筛选器：角色筛选, 添加用户按钮 -->
    <template #filters>
      <el-select
        v-model="selectedRole"
        class="role-filter"
        clearable
        placeholder="筛选角色"
        @change="handleRoleFilter"
      >
        <el-option
          v-for="role in allRoles"
          :key="role.id"
          :label="role.roleName"
          :value="role.id"
        />
      </el-select>
      <el-button
        :icon="Plus"
        class="add-user-btn"
        size="default"
        type="primary"
        @click="openAddUserDialog"
        >添加用户</el-button
      >
    </template>

    <!-- 表格列定义 -->
    <template #table-columns>
      <el-table-column align="center" label="ID" prop="userId" width="80">
        <template #default="scope">
          <el-tag class="id-tag" size="small" type="info"> #{{ scope.row.userId }} </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="用户名" min-width="120" prop="username">
        <template #default="scope">
          <div class="user-info">
            <el-avatar :size="32" class="user-avatar">
              {{ scope.row.username.charAt(0).toUpperCase() }}
            </el-avatar>
            <span class="username">{{ scope.row.username }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="邮箱" min-width="200" prop="email">
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

      <el-table-column align="center" label="状态" width="100">
        <template #default="scope">
          <el-tag
            :type="scope.row.status === 1 ? 'success' : 'danger'"
            class="status-tag"
            size="small"
          >
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
            <el-tag
              v-for="role in scope.row.roles"
              :key="role.id"
              :type="getRoleTagType(role.roleName)"
              class="role-tag"
              size="small"
            >
              {{ role.roleName }}
            </el-tag>
            <el-tag
              v-if="!scope.row.roles || scope.row.roles.length === 0"
              size="small"
              type="info"
            >
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
            <div v-if="scope.row.lastLoginIp" class="login-ip">
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

      <el-table-column align="center" fixed="right" label="操作" width="200">
        <template #default="scope">
          <div class="action-buttons">
            <el-button
              :icon="Edit"
              class="action-btn"
              plain
              size="small"
              type="primary"
              @click="openEditUserDialog(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              :icon="Delete"
              class="action-btn"
              plain
              size="small"
              type="danger"
              @click="handleDeleteUser(scope.row.userId)"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </template>

    <!-- 对话框表单 -->
    <template #dialog-form="{ isEdit }">
      <el-form
        ref="userFormRef"
        :model="currentUser"
        :rules="userRules"
        class="user-form"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="currentUser.username"
            :prefix-icon="UserIcon"
            clearable
            placeholder="请输入用户名"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="currentUser.email"
            :prefix-icon="Message"
            clearable
            placeholder="请输入邮箱地址"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="currentUser.password"
            :prefix-icon="Lock"
            clearable
            placeholder="请输入密码"
            show-password
            type="password"
          />
          <div v-if="isEdit" class="password-tip">留空则不修改密码</div>
        </el-form-item>

        <el-form-item label="角色" prop="roles">
          <el-select
            v-model="currentUserRoleIds"
            collapse-tags
            collapse-tags-tooltip
            multiple
            placeholder="请选择用户角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in allRoles"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="currentUser.status"
            :active-value="1"
            :inactive-value="0"
            active-text="正常"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
    </template>
  </ManageComponent>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import ManageComponent from '@/components/management/ManageComponent.vue'
import { createUser, deleteUser, fetchUsersPage, updateUser } from '@/api/user.ts'
import type { PageResult } from '@/types/commons'
import { fetchRoles } from '@/api/role.ts'
import type { User as UserData } from '@/types/user'
import type { Role } from '@/types/role'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  Calendar,
  CircleCheck,
  CircleClose,
  Clock,
  Delete,
  Document,
  Edit,
  Location,
  Lock,
  Message,
  Monitor,
  Plus,
  User,
  UserFilled as UserIcon,
} from '@element-plus/icons-vue'

// ManageComponent 的引用
const manageComponentRef = ref<InstanceType<typeof ManageComponent> | null>(null)

// 响应式数据
const users = ref<UserData[]>([])
const allRoles = ref<Role[]>([])
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索和筛选
const searchQuery = ref('')
const selectedRole = ref<number | undefined>()

// 对话框相关
const currentUser = ref<Partial<UserData>>({})
const currentUserRoleIds = ref<number[]>([])
const userFormRef = ref<FormInstance>()

// 表单验证规则
// 注意：在 saveUser 方法中会根据 isEdit 状态动态调整 password 的 required 属性
const userRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  password: [{ min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// 计算属性
// 仅在当前页内应用角色筛选，搜索交由后端分页处理，保持总数一致
const filteredUsers = computed(() => {
  let result = users.value
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
      minute: '2-digit',
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
  currentPage.value = 1
  getUsers()
}

const handleRoleFilter = () => {
  // 服务端按角色筛选：切换角色时重置到第1页并重新拉取
  currentPage.value = 1
  getUsers()
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
    const res: PageResult<UserData> = await fetchUsersPage({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value || undefined,
      roleId: selectedRole.value || undefined,
    })
    users.value = res.records
    total.value = res.total
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
  currentUser.value = { status: 1 } // 默认激活状态
  currentUserRoleIds.value = []
  manageComponentRef.value?.openDialog('添加新用户', { status: 1 }, false)
}

const openEditUserDialog = (user: UserData) => {
  currentUser.value = { ...user }
  currentUserRoleIds.value = user.roles ? user.roles.map((r) => r.id) : []
  manageComponentRef.value?.openDialog('编辑用户', user, true)
}

const saveUser = async () => {
  if (!userFormRef.value) return

  // 动态设置密码字段的验证规则
  const isEdit = manageComponentRef.value?.isEdit
  if (!isEdit) {
    // 添加用户时密码必填
    userRules.password = [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    ]
  } else {
    // 编辑用户时密码非必填
    userRules.password = [
      { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    ]

    // 如果密码为空，则从提交数据中移除密码字段
    if (!currentUser.value.password || currentUser.value.password.trim() === '') {
      delete currentUser.value.password
    }
  }

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        currentUser.value.roles = allRoles.value.filter((role) =>
          currentUserRoleIds.value.includes(role.id),
        )

        if (isEdit) {
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

const handlePageSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  getUsers()
}

const handleCurrentPageChange = (val: number) => {
  currentPage.value = val
  getUsers()
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

.add-user-btn {
  margin-left: 10px;
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

.password-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
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
