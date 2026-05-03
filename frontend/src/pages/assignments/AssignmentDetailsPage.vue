<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'
import * as assignmentsApi from '../../api/assignmentsApi'
import * as submissionsApi from '../../api/submissionsApi'
import { normalizeError } from '../../utils/errorUtils'
import AssignmentForm from '../../components/AssignmentForm.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import FileUpload from '../../components/FileUpload.vue'
import LoadingState from '../../components/LoadingState.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const assignment = ref(null)
const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const error = ref(null)
const showEditForm = ref(false)
const uploadedFile = ref(null)
const submitForm = reactive({
  comment: ''
})

const canManage = computed(() => authStore.hasRole('TEACHER'))
const canSubmit = computed(() => authStore.hasRole('STUDENT'))

onMounted(loadAssignment)

async function loadAssignment() {
  loading.value = true
  error.value = null
  try {
    const { data } = await assignmentsApi.getAssignmentById(route.params.id)
    assignment.value = data
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load assignment')
  } finally {
    loading.value = false
  }
}

async function handleUpdate(payload) {
  saving.value = true
  try {
    await assignmentsApi.updateAssignment(route.params.id, normalizePayload(payload))
    showEditForm.value = false
    await loadAssignment()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not update assignment')
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  if (!window.confirm('Delete this assignment?')) {
    return
  }

  try {
    await assignmentsApi.deleteAssignment(route.params.id)
    await router.push(`/courses/${assignment.value.courseId}/assignments`)
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not delete assignment')
  }
}

async function handleSubmitAssignment() {
  if (!uploadedFile.value) {
    error.value = { status: 400, message: 'Upload a file before submitting the assignment' }
    return
  }

  submitting.value = true
  try {
    await submissionsApi.submitAssignment(route.params.id, {
      comment: submitForm.comment,
      fileId: uploadedFile.value.id
    })
    submitForm.comment = ''
    uploadedFile.value = null
    await router.push('/submissions/my')
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not submit assignment')
  } finally {
    submitting.value = false
  }
}

function normalizePayload(payload) {
  return {
    ...payload,
    deadline: payload.deadline?.length === 16 ? `${payload.deadline}:00` : payload.deadline
  }
}

function toDateTimeInput(value) {
  return value?.slice(0, 16) || ''
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Assignment details</h1>
        <p class="page-subtitle">Public read, role-based actions.</p>
      </div>
      <div class="actions" v-if="assignment">
        <RouterLink class="button-secondary" :to="`/courses/${assignment.courseId}/assignments`">Back to course</RouterLink>
        <RouterLink v-if="canManage" class="button-secondary" :to="`/assignments/${assignment.id}/submissions`">
          View submissions
        </RouterLink>
      </div>
    </div>

    <ErrorAlert :error="error" />
    <LoadingState v-if="loading" />

    <template v-else-if="assignment">
      <div class="split">
        <div class="panel stack">
          <div class="meta-list">
            <div class="meta-item">
              <span>Title</span>
              <strong>{{ assignment.title }}</strong>
            </div>
            <div class="meta-item">
              <span>Description</span>
              <strong>{{ assignment.description || 'No assignment description provided.' }}</strong>
            </div>
            <div class="meta-item">
              <span>Deadline</span>
              <strong>{{ assignment.deadline }}</strong>
            </div>
            <div class="meta-item">
              <span>Course ID</span>
              <strong>{{ assignment.courseId }}</strong>
            </div>
          </div>

          <div v-if="canManage" class="actions">
            <button class="button-secondary" type="button" @click="showEditForm = !showEditForm">
              {{ showEditForm ? 'Close edit' : 'Edit assignment' }}
            </button>
            <button class="button-danger" type="button" @click="handleDelete">Delete assignment</button>
          </div>

          <div v-if="showEditForm" class="panel">
            <AssignmentForm
              :model-value="{ ...assignment, deadline: toDateTimeInput(assignment.deadline) }"
              submit-label="Update assignment"
              :loading="saving"
              @submit="handleUpdate"
            />
          </div>
        </div>

        <div v-if="canSubmit" class="stack">
          <FileUpload @uploaded="uploadedFile = $event" />
          <div class="panel stack">
            <h3 style="margin: 0;">Submit assignment</h3>
            <div class="form-row">
              <label for="submission-comment">Comment</label>
              <textarea id="submission-comment" v-model="submitForm.comment" class="textarea" />
            </div>
            <div v-if="uploadedFile" class="meta-item">
              <span>Uploaded file</span>
              <strong>{{ uploadedFile.fileName }} (ID {{ uploadedFile.id }})</strong>
            </div>
            <button class="button" type="button" :disabled="submitting" @click="handleSubmitAssignment">
              {{ submitting ? 'Submitting...' : 'Submit assignment' }}
            </button>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>
