<template>
  <div class="submission-card">
    <SubmissionTable v-if="!currentSubmission" :submissions="submissions" @row-click="handleRowClick" />
    <SubmissionView v-else :submission="currentSubmission" @back="currentSubmission = null" />
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { getSubmissionsByProblemId } from '@/api/submission'
import type { Submission } from '@/types/problem'
import SubmissionTable from './components/SubmissionTable.vue'
import SubmissionView from './components/SubmissionView.vue'

const props = defineProps<{
  problemId: number
}>()

const submissions = ref<Submission[]>([])
const currentSubmission = ref<Submission | null>(null)

const fetchSubmissions = async () => {
  try {
    submissions.value = await getSubmissionsByProblemId(props.problemId)
    submissions.value.forEach(submission => {
      submission.memoryUsed = Number((submission.memoryUsed / 1024).toFixed(2))
    })
  } catch (error) {
    console.error('Failed to fetch submissions:', error)
  }
}



const handleRowClick = (submission: Submission) => {
  currentSubmission.value = submission
}

onMounted(() => {
  fetchSubmissions()
})
</script>

<style scoped>
.submission-card {
  padding: 16px;
}
</style>
