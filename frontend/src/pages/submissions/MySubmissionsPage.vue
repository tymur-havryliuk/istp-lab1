<script setup>
import { onMounted, ref } from 'vue'
import * as submissionsApi from '../../api/submissionsApi'
import * as filesApi from '../../api/filesApi'
import { normalizeError } from '../../utils/errorUtils'
import { downloadBlob, getFilenameFromHeaders } from '../../utils/downloadUtils'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'
import SubmissionCard from '../../components/SubmissionCard.vue'

const submissions = ref([])
const loading = ref(false)
const error = ref(null)

onMounted(loadSubmissions)

async function loadSubmissions() {
  loading.value = true
  error.value = null
  try {
    const { data } = await submissionsApi.getMySubmissions()
    submissions.value = data
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load submissions')
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
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">My submissions</h1>
        <p class="page-subtitle">Everything the current student has already submitted.</p>
      </div>
      <button class="button-secondary" type="button" @click="loadSubmissions">Refresh</button>
    </div>

    <ErrorAlert :error="error" />
    <LoadingState v-if="loading" />
    <EmptyState v-else-if="!submissions.length" title="No submissions yet" description="Submit an assignment to see it here." />
    <div v-else class="grid grid-2">
      <div v-for="submission in submissions" :key="submission.id" class="stack">
        <SubmissionCard :submission="submission" />
        <div class="actions">
          <button v-if="submission.fileId" class="button-ghost" type="button" @click="handleDownload(submission.fileId)">
            Download file
          </button>
        </div>
      </div>
    </div>
  </section>
</template>
