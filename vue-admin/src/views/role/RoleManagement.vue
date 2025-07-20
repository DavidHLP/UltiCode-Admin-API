<template>
  <div class="role-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2 class="page-title">
            <el-icon class="title-icon"><Lock /></el-icon>
            角色管理
          </h2>
        </div>
        <div class="header-actions">
          <el-button
            type="primary"
            @click="openAddDialog"
            :icon="Plus"
            class="add-btn"
            size="large"
          >
            添加角色
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <el-card class="search-card" shadow="never">
        <div class="search-form">
          <el-input
            v-model="searchQuery"
            placeholder="搜索角色名称或备注..."
            :prefix-icon="Search"
            class="search-input"
            clearable
            @input="handleSearch"
          />
          <el-select
            v-model="selectedStatus"
            placeholder="筛选状态"
            class="status-filter"
            clearable
            @change="handleStatusFilter"
          >
            <el-option label="激活" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button :icon="Refresh" @click="refreshData" class="refresh-btn"> 刷新 </el-button>
        </div>
      </el-card>
    </div>

    <!-- 角色表格 -->
    <div class="table-section">
      <el-card class="table-card" shadow="never">
        <el-table
          :data="filteredRoles"
          class="modern-table"
          stripe
          :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: '600' }"
          v-loading="loading"
        >
          <el-table-column prop="id" label="ID" width="80" align="center">
            <template #default="scope">
              <el-tag type="info" size="small" class="id-tag"> #{{ scope.row.id }} </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="roleName" label="角色名称" min-width="150">
            <template #default="scope">
              <div class="role-info">
                <el-icon class="role-icon" :class="getRoleIconClass(scope.row.roleName)">
                  <component :is="getRoleIcon(scope.row.roleName)" />
                </el-icon>
                <span class="role-name">{{ scope.row.roleName }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="remark" label="备注" min-width="200">
            <template #default="scope">
              <div class="remark-cell">
                <el-icon class="remark-icon"><Document /></el-icon>
                <span>{{ scope.row.remark || '暂无备注' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag
                :type="scope.row.status === 1 ? 'success' : 'danger'"
                size="small"
                class="status-tag"
              >
                {{ scope.row.status === 1 ? '激活' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <div class="action-buttons">
                <el-button
                  @click="openEditDialog(scope.row)"
                  :icon="Edit"
                  size="small"
                  type="primary"
                  plain
                  class="action-btn"
                >
                  编辑
                </el-button>
                <el-button
                  @click="handleDelete(scope.row.id)"
                  :icon="Delete"
                  size="small"
                  type="danger"
                  plain
                  class="action-btn"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="filteredRoles.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无角色数据" />
        </div>
      </el-card>
    </div>

    <!-- 现代化角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      class="role-dialog"
      :close-on-click-modal="false"
    >
      <el-form
        :model="currentRole"
        :rules="roleRules"
        ref="roleFormRef"
        label-width="80px"
        class="role-form"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input
            v-model="currentRole.roleName"
            placeholder="请输入角色名称"
            :prefix-icon="Lock"
            clearable
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="currentRole.remark"
            type="textarea"
            placeholder="请输入角色备注信息"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="currentRole.status"
            :active-value="1"
            :inactive-value="0"
            active-text="激活"
            inactive-text="禁用"
            :active-icon="Check"
            :inactive-icon="Close"
            class="status-switch"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn"> 取消 </el-button>
          <el-button type="primary" @click="saveRole" :loading="saving" class="save-btn">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { fetchRoles, createRole, updateRole, deleteRole } from '@/api/role.ts'
import type { Role } from '@/types/role.ts'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Lock,
  Search,
  Refresh,
  Document,
  Check,
  Close,
  User,
  Setting,
  Star,
  Tools,
} from '@element-plus/icons-vue'

// 响应式数据
const roles = ref<Role[]>([])
const loading = ref(false)
const saving = ref(false)

// 搜索和筛选
const searchQuery = ref('')
const selectedStatus = ref<number | undefined>()

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const currentRole = ref<Partial<Role>>({})
const roleFormRef = ref<FormInstance>()

// 表单验证规则
const roleRules: FormRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  remark: [{ max: 200, message: '备注信息不能超过 200 个字符', trigger: 'blur' }],
}

// 计算属性
const filteredRoles = computed(() => {
  let result = roles.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(
      (role) =>
        role.roleName.toLowerCase().includes(query) ||
        (role.remark && role.remark.toLowerCase().includes(query)),
    )
  }

  // 状态过滤
  if (selectedStatus.value !== undefined) {
    result = result.filter((role) => role.status === selectedStatus.value)
  }

  return result
})

// 工具方法
const getRoleIcon = (roleName: string) => {
  const roleIconMap: Record<string, any> = {
    ADMIN: Star,
    USER: User,
    MODERATOR: Tools,
    GUEST: Setting,
  }
  return roleIconMap[roleName] || Lock
}

const getRoleIconClass = (roleName: string) => {
  const roleClassMap: Record<string, string> = {
    ADMIN: 'admin-icon',
    USER: 'user-icon',
    MODERATOR: 'moderator-icon',
    GUEST: 'guest-icon',
  }
  return roleClassMap[roleName] || 'default-icon'
}

// 搜索和筛选方法
const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

const handleStatusFilter = () => {
  // 筛选逻辑已在计算属性中处理
}

const refreshData = async () => {
  loading.value = true
  try {
    await getRoles()
    ElMessage.success('数据刷新成功！')
  } catch (error) {
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

// 数据获取方法
const getRoles = async () => {
  try {
    roles.value = await fetchRoles()
  } catch (error) {
    console.error('Error fetching roles:', error)
    ElMessage.error('Failed to fetch roles.')
  }
}

const openAddDialog = () => {
  isEdit.value = false
  dialogTitle.value = '添加角色'
  currentRole.value = { status: 1 }
  dialogVisible.value = true
}

const openEditDialog = (role: Role) => {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  currentRole.value = { ...role }
  dialogVisible.value = true
}

const saveRole = async () => {
  try {
    if (isEdit.value) {
      await updateRole(currentRole.value.id!, currentRole.value as Role)
      ElMessage.success('Role updated successfully.')
    } else {
      await createRole(currentRole.value as Role)
      ElMessage.success('Role created successfully.')
    }
    dialogVisible.value = false
    getRoles()
  } catch (error) {
    console.error('Error saving role:', error)
    ElMessage.error('Failed to save role.')
  }
}

const handleDelete = async (roleId: number) => {
  try {
    await deleteRole(roleId)
    getRoles()
    ElMessage.success('Role deleted successfully.')
  } catch (error) {
    console.error('Error deleting role:', error)
    ElMessage.error('Failed to delete role.')
  }
}

onMounted(() => {
  getRoles()
})
</script>

<style>
@import './index.css';
</style>
