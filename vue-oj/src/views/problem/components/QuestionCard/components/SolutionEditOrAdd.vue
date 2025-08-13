<template>
  <div class="solution-edit">
    <div class="solution-header">
      <div class="header-left">
        <el-icon class="back-icon" @click="handleBack">
          <ArrowLeft />
        </el-icon>
        {{ isEdit ? '编辑题解' : '新增题解' }}
      </div>
    </div>

    <el-form :model="form" ref="formRef" :rules="rules" class="solution-form">
      <el-form-item prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入题解标题"
          size="large"
          class="title-input"
        ></el-input>
      </el-form-item>

      <el-form-item prop="language">
        <el-select
          v-model="form.language"
          placeholder="请选择编程语言"
          class="language-select"
        >
          <el-option label="JavaScript" value="javascript"></el-option>
          <el-option label="TypeScript" value="typescript"></el-option>
          <el-option label="Python" value="python"></el-option>
          <el-option label="Java" value="java"></el-option>
          <el-option label="C++" value="cpp"></el-option>
          <el-option label="Go" value="go"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item prop="tags">
        <el-select
          v-model="form.tags"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="请选择或输入标签"
          class="tags-select"
        >
          <!-- 可选：预置常用标签；如无需预置，可留空 -->
        </el-select>
      </el-form-item>

      <el-form-item prop="content" class="editor-item">
        <MdEditor
          v-model="form.content"
          :preview="false"
          class="markdown-editor"
        />
      </el-form-item>

      <div class="form-actions">
        <el-button @click="handleBack" :disabled="loading">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="loading"
        >
          {{ loading ? '提交中...' : '发布题解' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';
import { ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElButton, ElMessage } from 'element-plus';
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import type { FormInstance, FormRules } from 'element-plus';
import type { SolutionEditVo } from '@/types/solution';
import { addSolution, updateSolution, getSolutionById } from '@/api/solution';

const route = useRoute();
const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);

// 判断是编辑模式还是添加模式
const isEdit = ref(false);
const solutionId = ref<number | null>(null);

const form = reactive({
  id: undefined as number | undefined,
  title: '',
  content: '',
  language: 'javascript',
  problemId: undefined as number | undefined,
  tags: [] as string[],
  status: 'PENDING' as 'PENDING' | 'APPROVED' | 'REJECTED',
});

const rules: FormRules = {
  title: [
    { required: true, message: '请输入题解标题', trigger: 'blur' },
    { max: 100, message: '长度在 100 个字符以内', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '请输入题解内容', trigger: 'blur' },
    { min: 10, message: '题解内容至少 10 个字符', trigger: 'blur' },
  ],
  language: [
    { required: true, message: '请选择编程语言', trigger: 'change' },
  ],
  tags: [
    {
      validator: (_rule, value: unknown, callback) => {
        if (!Array.isArray(value) || value.length === 0) {
          callback(new Error('请选择至少一个标签'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  status: [
    { required: true, message: '请选择题解状态', trigger: 'change' },
  ],
};

// 获取题解详情
const fetchSolution = async (id: number) => {
  try {
    loading.value = true;
    const data = await getSolutionById(id);
    if (data) {
      form.id = data.id;
      form.title = data.title;
      form.content = data.content;
      form.language = data.language || 'javascript';
      form.problemId = data.problemId;
      // 预填 tags/status（后端详情返回 tags 为 string[]）
      form.tags = Array.isArray(data.tags) ? data.tags : [];
      form.status = (data.status as typeof form.status) || 'PENDING';
    }
  } catch (error) {
    console.error('获取题解详情失败:', error);
    ElMessage.error('获取题解详情失败');
  } finally {
    loading.value = false;
  }
};

// 监听路由参数变化
watch(
  () => route.params,
  (params: Record<string, string | string[]>) => {
    const id = Array.isArray(params.id) ? params.id[0] : params.id;
    const sid = Array.isArray(params.solutionId) ? params.solutionId[0] : params.solutionId;
    form.problemId = Number(id) || undefined;

    if (sid) {
      // 编辑模式
      isEdit.value = true;
      solutionId.value = Number(sid);
      fetchSolution(Number(sid));
    } else {
      // 添加模式
      isEdit.value = false;
      solutionId.value = null;
      // 重置表单
      form.id = undefined;
      form.title = '';
      form.content = '';
      form.language = 'javascript';
      form.tags = [];
      form.status = 'PENDING';
    }
  },
  { immediate: true }
);

const handleBack = () => {
  router.push({
    name: 'solution-list',
    params: { id: route.params.id }
  });
};

const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    const valid = await formRef.value.validate();
    if (!valid) return;

    loading.value = true;

    if (isEdit.value && form.id) {
      // 调用更新题解的 API（后端 PUT /problems/api/view/solution，需要携带 id）
      await updateSolution({
        id: form.id as number,
        title: form.title,
        content: form.content,
        language: form.language,
        problemId: form.problemId,
        tags: form.tags,
        status: form.status,
      } as Partial<SolutionEditVo> & { id: number });
      ElMessage.success('题解更新成功');
    } else if (form.problemId) {
      // 调用添加题解的 API
      await addSolution({
        title: form.title,
        content: form.content,
        language: form.language,
        problemId: form.problemId,
        tags: form.tags,
        status: form.status,
      } as SolutionEditVo);
      ElMessage.success('题解添加成功');
    } else {
      throw new Error('缺少题目ID');
    }

    // 延迟返回，让用户看到成功提示
    setTimeout(() => {
      router.push({
        name: 'solution-list',
        params: { id: route.params.id }
      });
    }, 1000);
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error('操作失败，请重试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.solution-edit {
  padding: 24px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.solution-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.solution-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

.solution-form {
  max-width: 1000px;
  margin: 0 auto;
}

.title-input {
  font-size: 20px;
  font-weight: 500;
}

.title-input :deep(.el-input__wrapper) {
  padding: 8px 16px;
  box-shadow: none;
  border-bottom: 1px solid #dcdfe6;
  border-radius: 0;
}

.title-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: none !important;
  border-bottom-color: #409eff;
}

.language-select {
  width: 200px;
}

.editor-item {
  margin-top: 24px;
}

.markdown-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  min-height: 400px;
}

.form-actions {
  margin-top: 32px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}

.form-actions .el-button {
  min-width: 100px;
  margin-left: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .solution-edit {
    padding: 16px;
  }

  .header-left h2 {
    font-size: 18px;
  }

  .title-input {
    font-size: 18px;
  }

  .language-select {
    width: 100%;
  }
}
</style>
