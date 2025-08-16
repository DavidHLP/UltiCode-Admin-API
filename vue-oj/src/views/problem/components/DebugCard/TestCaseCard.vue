<template>
  <div class="case-content">
    <div class="test-case-details" v-if="activeTestCase">
      <div class="case-tabs-container">
        <div class="case-selector">
          <el-button-group>
            <el-button v-for="(testCase, index) in localTestCases" :key="index"
              :type="activeTestCaseName === testCase.name ? 'primary' : 'default'" size="small"
              @click="activeTestCaseName = testCase.name">
              {{ testCase.name }}
            </el-button>
          </el-button-group>
          <div class="case-actions">
            <el-button v-if="localTestCases.length > 1" size="small" type="danger" @click="removeCurrentTestCase">
              <el-icon>
                <Delete />
              </el-icon>
              删除
            </el-button>
            <el-button size="small" type="success" @click="addNewTestCase">
              <el-icon>
                <Plus />
              </el-icon>
              添加
            </el-button>
          </div>
        </div>
      </div>
      <div class="input-section">
        <div class="section-title">输入参数</div>
        <div class="input-fields">
          <div v-for="(input, idx) in activeTestCase.inputs" :key="idx" class="input-field">
            <div class="input-name-disabled">{{ input.inputName || `参数 ${idx + 1}` }}</div>
            <el-input v-model="input.input" :autosize="{ minRows: 1, maxRows: 1 }"
              :placeholder="`请输入 ${input.inputName || '参数 ' + (idx + 1)} 的值`" class="input-textarea" />
          </div>
        </div>
      </div>

      <div class="output-section">
        <div class="section-title">期望输出</div>
        <el-input v-model="activeTestCase.output" :autosize="{ minRows: 1, maxRows: 1 }" class="output-textarea"
          placeholder="请输入期望输出结果" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { Plus, Delete } from '@element-plus/icons-vue';

const props = defineProps<{
  testCases: Array<{
    id: number;
    inputs: Array<{ inputName: string; input: string }>;
    output: string;
    sample: boolean;
    score: number;
  }>;
}>();

const activeTestCaseName = ref(`Case 1`);

// 创建本地测试用例副本
const localTestCases = ref(props.testCases.map((tc, index) => ({
  ...tc,
  name: `Case ${index + 1}`,
  // 确保 inputs 数组存在且不为空
  inputs: tc.inputs?.length ? [...tc.inputs] : [{ inputName: '', input: '' }]
})));

// 监听props变化
watch(() => props.testCases, (newVal) => {
  localTestCases.value = newVal.map((tc, index) => ({
    ...tc,
    name: `Case ${index + 1}`,
    // 确保 inputs 数组存在且不为空
    inputs: tc.inputs?.length ? [...tc.inputs] : [{ inputName: '', input: '' }]
  }));
  // 如果当前激活的测试用例不存在了，设置为第一个
  if (localTestCases.value.length > 0 &&
    !localTestCases.value.find(tc => tc.name === activeTestCaseName.value)) {
    activeTestCaseName.value = localTestCases.value[0].name;
  }
}, { deep: true });

// 计算当前激活的测试用例
const activeTestCase = computed(() => {
  const testCase = localTestCases.value.find(tc => tc.name === activeTestCaseName.value);
  // 确保testCase.inputs存在
  if (testCase && (!testCase.inputs || !Array.isArray(testCase.inputs))) {
    testCase.inputs = [{ inputName: '', input: '' }];
  }
  return testCase;
});

// 添加新测试用例
const addNewTestCase = () => {
  const newCaseName = `Case ${localTestCases.value.length + 1}`;

  // 获取当前激活测试用例的输入参数格式作为模板
  const currentCase = activeTestCase.value;
  const templateInputs = currentCase && currentCase.inputs && currentCase.inputs.length > 0
    ? currentCase.inputs.map(input => ({
      inputName: input.inputName,
      input: '' // 保留参数名，但清空输入值
    }))
    : [{ inputName: '', input: '' }]; // 如果没有当前用例，使用默认格式

  const newCase = {
    id: Date.now(), // 临时ID
    name: newCaseName,
    inputs: templateInputs,
    output: '',
    sample: true,
    score: 0
  };
  localTestCases.value.push(newCase);
  activeTestCaseName.value = newCaseName;
};

// 删除当前测试用例
const removeCurrentTestCase = () => {
  if (localTestCases.value.length <= 1) return;

  const currentIndex = localTestCases.value.findIndex(tc => tc.name === activeTestCaseName.value);
  if (currentIndex === -1) return;

  // 选择下一个激活的测试用例
  const nextCase = localTestCases.value[currentIndex + 1] || localTestCases.value[currentIndex - 1];
  if (nextCase) {
    activeTestCaseName.value = nextCase.name;
  }

  // 删除当前测试用例
  localTestCases.value.splice(currentIndex, 1);
};
</script>

<style scoped>
.case-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: transparent;
  overflow: hidden;
}

.case-tabs-container {
  margin-top: 12px;
}

.case-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.case-actions {
  display: flex;
  gap: 8px;
}

.test-case-details {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: transparent;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.input-fields {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-field {
  display: flex;
  gap: 12px;
  align-items: center;
}

.input-field .input-textarea {
  flex: 1;
}

.input-name-disabled {
  min-width: 80px;
  padding: 0 8px;
  height: 32px;
  line-height: 32px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 13px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  border: 1px solid #dcdfe6;
}


.input-textarea,
.output-textarea {
  width: 100%;
}

.output-section {
  margin-top: 8px;
}

/* 简化的滚动条样式 */
.test-case-details::-webkit-scrollbar {
  width: 4px;
}

.test-case-details::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 2px;
}

.test-case-details::-webkit-scrollbar-track {
  background: transparent;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .case-selector {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .case-actions {
    justify-content: center;
  }
}

.input-section,
.output-section {
  display: flex;
  flex-direction: column;
}

/* 移除动画，保持简洁 */
</style>
