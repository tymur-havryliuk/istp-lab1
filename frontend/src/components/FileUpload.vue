<script setup>
import { ref } from 'vue'
import * as filesApi from '../api/filesApi'
import { normalizeError } from '../utils/errorUtils'
import ErrorAlert from './ErrorAlert.vue'

const props = defineProps({
  multiple: {
    type: Boolean,
    default: true
  },
  selectable: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['uploaded', 'selected'])

const selectedFiles = ref([])
const uploadedFiles = ref([])
const selectedUploadedFile = ref(null)
const loading = ref(false)
const removingFileIds = ref([])
const error = ref(null)
const fileInput = ref(null)

async function handleUpload() {
  if (!selectedFiles.value.length) {
    return
  }

  loading.value = true
  error.value = null

  try {
    const uploadedBatch = []

    for (const file of selectedFiles.value) {
      const { data } = await filesApi.uploadFile(file)
      uploadedBatch.push(data)
    }

    uploadedFiles.value = [...uploadedFiles.value, ...uploadedBatch]
    selectedFiles.value = []
    resetFileInput()

    if (props.selectable && uploadedBatch.length) {
      selectUploadedFile(uploadedBatch[uploadedBatch.length - 1])
    }

    emit('uploaded', uploadedFiles.value)
  } catch (uploadError) {
    error.value = normalizeError(uploadError, 'Could not upload file')
  } finally {
    loading.value = false
  }
}

function handleFileChange(event) {
  selectedFiles.value = Array.from(event.target.files || [])
}

function selectUploadedFile(file) {
  selectedUploadedFile.value = file
  emit('selected', file)
}

async function removeUploadedFile(file) {
  error.value = null
  removingFileIds.value = [...removingFileIds.value, file.id]

  try {
    await filesApi.deleteFile(file.id)
    uploadedFiles.value = uploadedFiles.value.filter((uploadedFile) => uploadedFile.id !== file.id)

    if (selectedUploadedFile.value?.id === file.id) {
      const nextSelectedFile = props.selectable ? uploadedFiles.value.at(-1) ?? null : null
      selectedUploadedFile.value = nextSelectedFile
      emit('selected', nextSelectedFile)
    }

    emit('uploaded', uploadedFiles.value)
  } catch (removeError) {
    error.value = normalizeError(removeError, 'Could not remove uploaded file')
  } finally {
    removingFileIds.value = removingFileIds.value.filter((fileId) => fileId !== file.id)
  }
}

function resetFileInput() {
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}
</script>

<template>
  <div class="panel stack">
    <div class="form-row">
      <label for="file-upload">Choose file</label>
      <input id="file-upload" ref="fileInput" type="file" :multiple="multiple" @change="handleFileChange" />
    </div>
    <div class="actions">
      <button type="button" class="button" :disabled="!selectedFiles.length || loading" @click="handleUpload">
        {{ loading ? 'Uploading...' : multiple ? 'Upload files' : 'Upload file' }}
      </button>
    </div>
    <ErrorAlert :error="error" />
    <div v-if="uploadedFiles.length" class="stack">
      <div class="muted">Uploaded files</div>
      <div class="stack">
        <div v-for="file in uploadedFiles" :key="file.id" class="panel">
          <div class="page-header">
            <div>
              <strong>{{ file.fileName }}</strong>
              <div class="muted">ID {{ file.id }}</div>
            </div>
            <div class="actions">
              <button
                v-if="selectable"
                type="button"
                class="button-secondary"
                :disabled="selectedUploadedFile?.id === file.id || removingFileIds.includes(file.id)"
                @click="selectUploadedFile(file)"
              >
                {{ selectedUploadedFile?.id === file.id ? 'Selected' : 'Use for submission' }}
              </button>
              <button
                type="button"
                class="button-danger"
                :disabled="removingFileIds.includes(file.id)"
                @click="removeUploadedFile(file)"
              >
                {{ removingFileIds.includes(file.id) ? 'Removing...' : 'Remove' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
