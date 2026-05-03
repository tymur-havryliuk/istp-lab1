<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import * as submissionsApi from '../../api/submissionsApi'
import * as filesApi from '../../api/filesApi'
import { normalizeError } from '../../utils/errorUtils'
import { downloadBlob, getFilenameFromHeaders } from '../../utils/downloadUtils'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'

const route = useRoute()
const submission = ref(null)
const loading = ref(false)
const error = ref(null)

onMounted(loadSubmission)

async function loadSubmission() {
  loading.value = true
  error.value = null
  try {
    const { data } = await submissionsApi.getSubmissionById(route.params.id)
    submission.value = data
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load submission details')
  } finally {
    loading.value = false
  }
}

async function handleDownload() {
  if (!submission.value?.fileId) {
    return
  }

  try {
    const response = await filesApi.downloadFile(submission.value.fileId)
    downloadBlob(response.data, getFilenameFromHeaders(response.headers, `submission-file-${submission.value.fileId}`))
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not download file')
  }
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Submission details</h1>
        <p class="page-subtitle">Authenticated page with backend ownership checks.</p>
      </div>
    </div>

    <ErrorAlert :error="error" />
    <LoadingState v-if="loading" />

    <div v-else-if="submission" class="panel stack">
      <div class="meta-list">
        <div class="meta-item">
          <span>Assignment ID</span>
          <strong>{{ submission.assignmentId }}</strong>
        </div>
        <div class="meta-item">
          <span>Student</span>
          <strong>{{ submission.studentName }}</strong>
        </div>
        <div class="meta-item">
          <span>Comment</span>
          <strong>{{ submission.comment || 'No comment provided.' }}</strong>
        </div>
        <div class="meta-item">
          <span>Submitted At</span>
          <strong>{{ submission.submittedAt }}</strong>
        </div>
        <div class="meta-item">
          <span>Grade</span>
          <strong>{{ submission.grade ?? 'Not graded yet' }}</strong>
        </div>
        <div class="meta-item">
          <span>Feedback</span>
          <strong>{{ submission.feedback || 'No feedback yet' }}</strong>
        </div>
      </div>

      <div class="actions">
        <button v-if="submission.fileId" class="button-secondary" type="button" @click="handleDownload">
          Download file
        </button>
      </div>
    </div>
  </section>
</template>
