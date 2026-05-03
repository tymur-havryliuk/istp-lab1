<script setup>
import { ref } from 'vue'
import * as filesApi from '../api/filesApi'
import { normalizeError } from '../utils/errorUtils'
import ErrorAlert from './ErrorAlert.vue'

const emit = defineEmits(['uploaded'])

const selectedFile = ref(null)
const uploadedFile = ref(null)
const loading = ref(false)
const error = ref(null)

async function handleUpload() {
  if (!selectedFile.value) {
    return
  }

  loading.value = true
  error.value = null

  try {
    const { data } = await filesApi.uploadFile(selectedFile.value)
    uploadedFile.value = data
    emit('uploaded', data)
  } catch (uploadError) {
    error.value = normalizeError(uploadError, 'Could not upload file')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="panel stack">
    <div class="form-row">
      <label for="file-upload">Choose file</label>
      <input id="file-upload" type="file" @change="selectedFile = $event.target.files?.[0] || null" />
    </div>
    <div class="actions">
      <button type="button" class="button" :disabled="!selectedFile || loading" @click="handleUpload">
        {{ loading ? 'Uploading...' : 'Upload file' }}
      </button>
    </div>
    <ErrorAlert :error="error" />
    <div v-if="uploadedFile" class="meta-list">
      <div class="meta-item">
        <span>File ID</span>
        <strong>{{ uploadedFile.id }}</strong>
      </div>
      <div class="meta-item">
        <span>URL</span>
        <strong>{{ uploadedFile.url }}</strong>
      </div>
    </div>
  </div>
</template>
