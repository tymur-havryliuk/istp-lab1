<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import * as submissionsApi from '../../api/submissionsApi'
import * as gradesApi from '../../api/gradesApi'
import * as filesApi from '../../api/filesApi'
import { normalizeError } from '../../utils/errorUtils'
import { downloadBlob, getFilenameFromHeaders } from '../../utils/downloadUtils'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import GradeForm from '../../components/GradeForm.vue'
import LoadingState from '../../components/LoadingState.vue'

const route = useRoute()

const submissions = ref([])
const loading = ref(false)
const gradingId = ref(null)
const error = ref(null)
const gradeDrafts = reactive({})

onMounted(loadSubmissions)

async function loadSubmissions() {
  loading.value = true
  error.value = null
  try {
    const { data } = await submissionsApi.getAssignmentSubmissions(route.params.id)
    submissions.value = data
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load assignment submissions')
  } finally {
    loading.value = false
  }
}

async function handleDownload(fileId) {
  try {
    const response = await filesApi.downloadFile(fileId)
    downloadBlob(response.data, getFilenameFromHeaders(response.headers, `submission-file-${fileId}`))
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not download file')
  }
}

async function handleGrade(submissionId, payload) {
  gradingId.value = submissionId
  error.value = null
  try {
    await gradesApi.gradeSubmission(submissionId, payload)
    delete gradeDrafts[submissionId]
    await loadSubmissions()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not save grade')
  } finally {
    gradingId.value = null
  }
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Assignment submissions</h1>
        <p class="page-subtitle">Teacher view for assignment #{{ route.params.id }}.</p>
      </div>
      <button class="button-secondary" type="button" @click="loadSubmissions">Refresh</button>
    </div>

    <ErrorAlert :error="error" />
    <LoadingState v-if="loading" />
    <EmptyState v-else-if="!submissions.length" title="No submissions yet" description="Students have not submitted work for this assignment." />
    <div v-else class="stack">
      <article v-for="submission in submissions" :key="submission.id" class="panel stack">
        <div class="page-header">
          <div>
            <strong>{{ submission.studentName }}</strong>
            <div class="muted">Submitted at {{ submission.submittedAt }}</div>
          </div>
          <RouterLink class="button-secondary" :to="`/submissions/${submission.id}`">Details</RouterLink>
        </div>
        <div>{{ submission.comment || 'No comment provided.' }}</div>
        <div class="actions">
          <button v-if="submission.fileId" class="button-ghost" type="button" @click="handleDownload(submission.fileId)">
            Download file
          </button>
        </div>
        <div class="meta-list">
          <div class="meta-item">
            <span>Grade</span>
            <strong>{{ submission.grade ?? 'Not graded yet' }}</strong>
          </div>
          <div class="meta-item">
            <span>Feedback</span>
            <strong>{{ submission.feedback || 'No feedback yet' }}</strong>
          </div>
        </div>
        <GradeForm
          :model-value="gradeDrafts[submission.id] || { grade: submission.grade ?? '', feedback: submission.feedback || '' }"
          :loading="gradingId === submission.id"
          @submit="handleGrade(submission.id, $event)"
        />
      </article>
    </div>
  </section>
</template>
