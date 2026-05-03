<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'
import * as assignmentsApi from '../../api/assignmentsApi'
import { normalizeError } from '../../utils/errorUtils'
import AssignmentForm from '../../components/AssignmentForm.vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'

const route = useRoute()
const authStore = useAuthStore()

const assignments = ref([])
const loading = ref(false)
const saving = ref(false)
const error = ref(null)
const editingAssignment = ref(null)
const showCreateForm = ref(false)

onMounted(loadAssignments)

async function loadAssignments() {
  loading.value = true
  error.value = null
  try {
    const { data } = await assignmentsApi.getCourseAssignments(route.params.id)
    assignments.value = data
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load assignments')
  } finally {
    loading.value = false
  }
}

async function handleCreate(payload) {
  saving.value = true
  try {
    await assignmentsApi.createAssignment(route.params.id, normalizePayload(payload))
    showCreateForm.value = false
    await loadAssignments()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not create assignment')
  } finally {
    saving.value = false
  }
}

async function handleUpdate(payload) {
  if (!editingAssignment.value) {
    return
  }

  saving.value = true
  try {
    await assignmentsApi.updateAssignment(editingAssignment.value.id, normalizePayload(payload))
    editingAssignment.value = null
    await loadAssignments()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not update assignment')
  } finally {
    saving.value = false
  }
}

async function handleDelete(assignmentId) {
  if (!window.confirm('Delete this assignment?')) {
    return
  }

  try {
    await assignmentsApi.deleteAssignment(assignmentId)
    await loadAssignments()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not delete assignment')
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
        <h1 class="page-title">Course assignments</h1>
        <p class="page-subtitle">Assignments for course #{{ route.params.id }}.</p>
      </div>
      <button
        v-if="authStore.hasRole('TEACHER')"
        class="button"
        type="button"
        @click="showCreateForm = !showCreateForm"
      >
        {{ showCreateForm ? 'Close form' : 'Create assignment' }}
      </button>
    </div>

    <ErrorAlert :error="error" />

    <div v-if="showCreateForm" class="panel">
      <AssignmentForm submit-label="Create assignment" :loading="saving" @submit="handleCreate" />
    </div>

    <div v-if="editingAssignment" class="panel">
      <AssignmentForm
        :model-value="{ ...editingAssignment, deadline: toDateTimeInput(editingAssignment.deadline) }"
        submit-label="Update assignment"
        :loading="saving"
        @submit="handleUpdate"
      />
    </div>

    <LoadingState v-if="loading" />
    <EmptyState v-else-if="!assignments.length" title="No assignments found" description="Create the first assignment for this course." />
    <div v-else class="panel table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>Title</th>
            <th>Description</th>
            <th>Deadline</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="assignment in assignments" :key="assignment.id">
            <td>{{ assignment.title }}</td>
            <td>{{ assignment.description || '—' }}</td>
            <td>{{ assignment.deadline }}</td>
            <td>
              <div class="actions">
                <RouterLink class="button-secondary" :to="`/assignments/${assignment.id}`">Open</RouterLink>
                <template v-if="authStore.hasRole('TEACHER')">
                  <button class="button-ghost" type="button" @click="editingAssignment = assignment">Edit</button>
                  <button class="button-danger" type="button" @click="handleDelete(assignment.id)">Delete</button>
                </template>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
