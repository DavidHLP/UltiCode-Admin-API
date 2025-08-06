<template>
  <AComment>
    <template #avatar>
      <AAvatar :src="userAvatar" alt="User Avatar" />
    </template>
    <template #content>
      <AFormItem>
        <ATextarea v-model:value="replyText" :placeholder="`回复 @${replyToUsername}`" :rows="rows" :maxlength="500"
          show-count />
      </AFormItem>
      <AFormItem>
        <div class="reply-actions">
          <AButton type="primary" @click="handleSubmit" :loading="submitting">
            提交回复
          </AButton>
          <AButton v-if="showCancel" @click="$emit('cancel')" class="cancel-btn">
            取消
          </AButton>
        </div>
      </AFormItem>
    </template>
  </AComment>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import {
  Comment as AComment,
  Avatar as AAvatar,
  FormItem as AFormItem,
  Textarea as ATextarea,
  Button as AButton,
  message
} from 'ant-design-vue';

withDefaults(defineProps<{
  replyToUsername: string;
  rows?: number;
  showCancel?: boolean;
}>(), {
  rows: 2,
  showCancel: true
});

const emit = defineEmits<{
  (e: 'submit', content: string): void;
  (e: 'cancel'): void;
}>();

const replyText = ref('');
const submitting = ref(false);

const userAvatar = 'https://prettyavatars.com/api/pixel-art/100';

const handleSubmit = async () => {
  if (!replyText.value.trim()) {
    message.warning('回复内容不能为空');
    return;
  }

  try {
    submitting.value = true;
    await emit('submit', replyText.value);
    replyText.value = '';
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
  } catch (error) {
    message.error('回复失败，请重试');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.reply-actions {
  display: flex;
  gap: 8px;
}

.cancel-btn {
  margin-left: 8px;
}
</style>
