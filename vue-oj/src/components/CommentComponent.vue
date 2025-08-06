<template>
  <div class="comment-section">
    <template v-if="showCommentForm">
      <AComment>
        <template #avatar>
          <AAvatar :src="userAvatar" alt="User Avatar" />
        </template>
        <template #content>
          <AFormItem>
            <ATextarea v-model:value="commentText" placeholder="发表你的评论..." :rows="4" :maxlength="500" show-count />
          </AFormItem>
          <AFormItem>
            <AButton type="primary" @click="handleSubmitComment" :loading="submitting">
              发表评论
            </AButton>
          </AFormItem>
        </template>
      </AComment>
    </template>

    <AList v-if="comments.length" class="comment-list" :header="`${comments.length} 条评论`" item-layout="horizontal"
      :data-source="comments">
      <template #renderItem="{ item }">
        <AListItem>
          <AComment :author="item.username" :avatar="item.avatar || 'https://prettyavatars.com/api/pixel-art/100'"
            :content="item.content" :datetime="formatDate(item.createdAt)">
            <template #actions>
              <span @click="showReply(item.id)">回复</span>
            </template>
            <div v-if="replyingTo === item.id" class="reply-container">
              <ReplyComponent :reply-to-username="item.username"
                @submit="(content) => handleSubmitReply(item.id, item.userId, content)" @cancel="replyingTo = null" />
            </div>
            <!-- Recursively render children comments -->
            <div v-if="item.children && item.children.length">
              <CommentComponent :comments="item.children" :solutionId="solutionId" :show-comment-form="false"
                @comment-submitted="emit('comment-submitted')" />
            </div>
          </AComment>
        </AListItem>
      </template>
    </AList>
  </div>
</template>

<script lang="ts" setup>
import { ref, defineProps, defineEmits } from 'vue';
import {
  message,
  List as AList,
  ListItem as AListItem,
  Comment as AComment,
  Avatar as AAvatar,
  FormItem as AFormItem,
  Textarea as ATextarea,
  Button as AButton
} from 'ant-design-vue';
import ReplyComponent from './ReplyComponent.vue';
import type { SolutionCommentVo } from '@/types/comment';
import { createComment } from '@/api/comment';

// Define props and emits
const props = withDefaults(defineProps<{
  comments: SolutionCommentVo[];
  solutionId: number;
  showCommentForm?: boolean;
}>(), {
  showCommentForm: true
});
const emit = defineEmits(['comment-submitted']);

const commentText = ref('');
const submitting = ref(false);
const replyingTo = ref<number | null>(null);

const userAvatar = 'https://prettyavatars.com/api/pixel-art/100';

const formatDate = (dateString: string | null) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString();
};

const handleSubmitComment = async () => {
  if (!commentText.value) {
    message.warning('评论内容不能为空');
    return;
  }
  submitting.value = true;
  try {
    await createComment({
      solutionId: props.solutionId,
      content: commentText.value
    });
    commentText.value = '';
    message.success('评论成功');
    emit('comment-submitted');
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
  } catch (error) {
    message.error('评论失败');
  } finally {
    submitting.value = false;
  }
};

const showReply = (commentId: number) => {
  replyingTo.value = commentId;
};
const handleSubmitReply = async (parentId: number, replyToUserId: number, content: string) => {
  try {
    await createComment({
      solutionId: props.solutionId,
      content,
      parentId,
      replyToUserId
    });
    replyingTo.value = null;
    message.success('回复成功');
    emit('comment-submitted');
  } catch (error) {
    message.error('回复失败');
    throw error;
  }
};
</script>

<style scoped>
.comment-section {
  margin-top: 24px;
  background-color: #fff;
  padding: 16px;
  border-radius: 8px;
}

.comment-list .ant-list-item {
  border-bottom: 1px solid #f0f0f0 !important;
}

.comment-list .ant-list-header {
  font-size: 16px;
  font-weight: 500;
}

.reply-container {
  margin-top: 16px;
  padding-left: 16px;
  border-left: 2px solid #f0f0f0;
}
</style>
