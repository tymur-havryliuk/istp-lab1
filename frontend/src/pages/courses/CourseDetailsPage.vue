<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'
import * as coursesApi from '../../api/coursesApi'
import * as assignmentsApi from '../../api/assignmentsApi'
import { normalizeError } from '../../utils/errorUtils'
import AssignmentForm from '../../components/AssignmentForm.vue'
import CourseForm from '../../components/CourseForm.vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const course = ref(null)
const assignments = ref([])
const loading = ref(false)
const courseSaving = ref(false)
const assignmentSaving = ref(false)
const error = ref(null)
const showCourseEdit = ref(false)
const showAssignmentCreate = ref(false)

const canManage = computed(() => authStore.hasRole('TEACHER'))

onMounted(loadPage)

async function loadPage() {
  loading.value = true
  error.value = null

  try {
    const [courseResponse, assignmentsResponse] = await Promise.all([
      coursesApi.getCourseById(route.params.id),
      assignmentsApi.getCourseAssignments(route.params.id)
    ])
    course.value = courseResponse.data
    assignments.value = assignmentsResponse.data
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load course details')
  } finally {
    loading.value = false
  }
}

async function handleUpdateCourse(payload) {
  courseSaving.value = true
  error.value = null
  try {
    await coursesApi.updateCourse(route.params.id, payload)
    showCourseEdit.value = false
    await loadPage()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not update course')
  } finally {
    courseSaving.value = false
  }
}

async function handleDeleteCourse() {
  if (!window.confirm('Delete this course?')) {
    return
  }

  try {
    await coursesApi.deleteCourse(route.params.id)
    await router.push('/courses/owned')
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not delete course')
  }
}

async function handleCreateAssignment(payload) {
  assignmentSaving.value = true
  error.value = null
  try {
    await assignmentsApi.createAssignment(route.params.id, normalizeAssignmentPayload(payload))
    showAssignmentCreate.value = false
    await loadPage()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not create assignment')
  } finally {
    assignmentSaving.value = false
  }
}

function normalizeAssignmentPayload(payload) {
  return {
    ...payload,
    deadline: payload.deadline?.length === 16 ? `${payload.deadline}:00` : payload.deadline
  }
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Course details</h1>
        <p class="page-subtitle">Course summary plus linked assignments.</p>
      </div>
      <div class="actions">
        <RouterLink class="button-secondary" :to="`/courses/${route.params.id}/assignments`">
          All assignments
        </RouterLink>
        <RouterLink
          v-if="canManage"
          class="button-secondary"
          :to="`/courses/${route.params.id}/students`"
        >
          View students
        </RouterLink>
      </div>
    </div>

    <ErrorAlert :error="error" />
    <LoadingState v-if="loading" />

    <template v-else-if="course">
      <div class="split">
        <div class="panel stack">
          <div class="page-header">
            <div>
              <h2 style="margin: 0 0 8px;">{{ course.title }}</h2>
              <p class="muted" style="margin: 0;">Teacher: {{ course.teacherName }}</p>
            </div>
            <span class="badge">{{ course.status }}</span>
          </div>
          <p style="margin: 0;">{{ course.description || 'No course description provided.' }}</p>
          <div v-if="canManage" class="actions">
            <button class="button-secondary" type="button" @click="showCourseEdit = !showCourseEdit">
              {{ showCourseEdit ? 'Close edit' : 'Edit course' }}
            </button>
            <button class="button-danger" type="button" @click="handleDeleteCourse">Delete course</button>
          </div>
          <div v-if="showCourseEdit" class="panel">
            <CourseForm
              :model-value="course"
              submit-label="Update course"
              :loading="courseSaving"
              @submit="handleUpdateCourse"
            />
          </div>
        </div>

        <div class="panel stack">
          <div class="page-header">
            <div>
              <h3 style="margin: 0 0 6px;">Assignments</h3>
              <p class="muted" style="margin: 0;">Latest work for this course.</p>
            </div>
            <button
              v-if="canManage"
              class="button"
              type="button"
              @click="showAssignmentCreate = !showAssignmentCreate"
            >
              {{ showAssignmentCreate ? 'Close form' : 'Create assignment' }}
            </button>
          </div>

          <div v-if="showAssignmentCreate" class="panel">
            <AssignmentForm
              submit-label="Create assignment"
              :loading="assignmentSaving"
              @submit="handleCreateAssignment"
            />
          </div>

          <EmptyState
            v-if="!assignments.length"
            title="No assignments found"
            description="This course does not have assignments yet."
          />
          <div v-else class="stack">
            <div v-for="assignment in assignments" :key="assignment.id" class="panel stack">
              <div class="page-header">
                <div>
                  <strong>{{ assignment.title }}</strong>
                  <div class="muted">{{ assignment.deadline }}</div>
                </div>
                <RouterLink class="button-secondary" :to="`/assignments/${assignment.id}`">
                  View assignment
                </RouterLink>
              </div>
              <div>{{ assignment.description || 'No assignment description provided.' }}</div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>
