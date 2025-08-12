<template>
  <ManageComponent title="角色管理" :title-icon="Lock" add-button-text="添加角色" search-placeholder="搜索角色名称或备注..."
    empty-text="暂无角色数据" :table-data="roles" :loading="loading" @add="handleAdd" @search="handleSearch"
    @refresh="handleRefresh" @dialog-confirm="handleDialogConfirm" @dialog-cancel="handleDialogCancel"
    :total="total" v-model:current-page="currentPage" v-model:page-size="pageSize"
    @size-change="handleSizeChange" @current-change="handleCurrentChange" ref="manageComponentRef">
    <!-- 筛选器插槽 -->
    <template #filters>
      <el-select v-model="selectedStatus" placeholder="筛选状态" class="status-filter" clearable
        @change="handleStatusFilter">
        <el-option label="激活" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleAdd" :icon="Plus" size="default" class="add-role-btn">添加角色</el-button>
    </template>

    <!-- 表格列插槽 -->
    <template #table-columns>
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
            <el-icon class="remark-icon">
              <Document />
            </el-icon>
            <span>{{ scope.row.remark || '暂无备注' }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small" class="status-tag">
            {{ scope.row.status === 1 ? '激活' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <div class="action-buttons">
            <el-button @click="openEditDialog(scope.row)" :icon="Edit" size="small" type="primary" plain
              class="action-btn">
              编辑
            </el-button>
            <el-button @click="handleDelete(scope.row.id)" :icon="Delete" size="small" type="danger" plain
              class="action-btn">
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </template>

    <!-- 对话框表单插槽 -->
    <template #dialog-form>
      <el-form :model="currentRole" :rules="roleRules" ref="roleFormRef" label-width="80px" class="management-form">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="currentRole.roleName" placeholder="请输入角色名称" :prefix-icon="Lock" clearable />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="currentRole.remark" type="textarea" placeholder="请输入角色备注信息" :rows="3" maxlength="200"
            show-word-limit />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch v-model="currentRole.status" :active-value="1" :inactive-value="0" active-text="激活"
            inactive-text="禁用" :active-icon="Check" :inactive-icon="Close" class="status-switch" />
        </el-form-item>
      </el-form>
    </template>
  </ManageComponent>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchRolePage, createRole, updateRole, deleteRole } from '@/api/role.ts'
import type { Role } from '@/types/role'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  Edit,
  Delete,
  Lock,
  Document,
  Check,
  Close,
  User,
  Setting,
  Star,
  Tools,
  Plus,
} from '@element-plus/icons-vue'
import ManageComponent from '@/components/management/ManageComponent.vue'

// 响应式数据
const roles = ref<Role[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索和筛选
const searchQuery = ref('')
const selectedStatus = ref<number | undefined>()

// 对话框相关
const currentRole = ref<Partial<Role>>({})
const roleFormRef = ref<FormInstance>()
const manageComponentRef = ref<InstanceType<typeof ManageComponent>>()

// 表单验证规则
const roleRules: FormRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  remark: [{ max: 200, message: '备注信息不能超过 200 个字符', trigger: 'blur' }],
}

// 计算属性移除：改为服务端分页

// 工具方法
const getRoleIcon = (roleName: string) => {
  const roleIconMap: Record<string, unknown> = {
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
const handleStatusFilter = () => {
  // 重置到第一页并重新加载
  currentPage.value = 1
  void getPagedRoles()
}

// ManageComponent 事件处理
const handleAdd = () => {
  manageComponentRef.value?.openDialog('添加角色', { status: 1 }, false)
  currentRole.value = { status: 1 }
}

const handleSearch = (query: string) => {
  searchQuery.value = query
  currentPage.value = 1
  void getPagedRoles()
}

const handleRefresh = async () => {
  loading.value = true
  try {
    await getPagedRoles()
    ElMessage.success('数据刷新成功！')
  } catch {
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

const handleDialogConfirm = async () => {
  try {
    if (manageComponentRef.value?.isEdit) {
      await updateRole(currentRole.value.id!, currentRole.value as Role)
      ElMessage.success('角色更新成功')
    } else {
      await createRole(currentRole.value as Role)
      ElMessage.success('角色创建成功')
    }
    manageComponentRef.value?.closeDialog()
    void getPagedRoles()
  } catch {
    ElMessage.error('保存角色失败')
  }
}

const handleDialogCancel = () => {
  // 对话框取消逻辑已在 ManageComponent 中处理
}

// 数据获取方法（服务端分页）
const getPagedRoles = async () => {
  try {
    loading.value = true
    const res = await fetchRolePage({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value || undefined,
      status: selectedStatus.value,
    })
    roles.value = res.records
    total.value = res.total
  } catch (error) {
    console.error('获取角色分页列表时出错:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const openEditDialog = (role: Role) => {
  manageComponentRef.value?.openDialog('编辑角色', role as unknown as Record<string, unknown>, true)
  currentRole.value = { ...role }
}

const handleDelete = async (roleId: number) => {
  try {
    await deleteRole(roleId)
    void getPagedRoles()
    ElMessage.success('角色删除成功')
  } catch (error) {
    console.error('删除角色时出错:', error)
    ElMessage.error('删除角色失败')
  }
}

onMounted(() => {
  void getPagedRoles()
})

// 分页事件
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  void getPagedRoles()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  void getPagedRoles()
}
</script>

<style scoped lang="css">
.status-filter {
  width: 120px;
}

.add-role-btn {
  margin-left: 10px;
}

.id-tag {
  font-weight: 600;
  border-radius: 6px;
}

.role-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  flex-shrink: 0;
}

.admin-icon {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
}

.user-icon {
  background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
}

.moderator-icon {
  background: linear-gradient(135deg, #feca57 0%, #ff9ff3 100%);
}

.guest-icon {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.default-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.role-name {
  font-weight: 500;
  color: #303133;
}

.remark-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.remark-icon {
  color: #909399;
  flex-shrink: 0;
}

.status-tag {
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

.status-switch {
  --el-switch-on-color: #67c23a;
  --el-switch-off-color: #f56c6c;
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
