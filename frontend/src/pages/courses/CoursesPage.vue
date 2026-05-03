<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from '../../stores/authStore'
import * as coursesApi from '../../api/coursesApi'
import { normalizeError } from '../../utils/errorUtils'
import CourseCard from '../../components/CourseCard.vue'
import CourseForm from '../../components/CourseForm.vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'

const authStore = useAuthStore()

const statusOptions = ['ACTIVE', 'PLANNED', 'COMPLETED', 'CANCELLED']
const selectedStatuses = ref(['ACTIVE', 'PLANNED'])
const courses = ref([])
const enrolledCourseIds = ref([])
const loading = ref(false)
const saving = ref(false)
const showCreateForm = ref(false)
const error = ref(null)

onMounted(loadCourses)

async function loadCourses() {
  loading.value = true
  error.value = null

  try {
    const coursesPromise = coursesApi.getCourses(selectedStatuses.value)
    const enrolledPromise = authStore.hasRole('STUDENT')
      ? coursesApi.getEnrolledCourses()
      : Promise.resolve({ data: [] })

    const [coursesResponse, enrolledResponse] = await Promise.all([coursesPromise, enrolledPromise])
    courses.value = coursesResponse.data
    enrolledCourseIds.value = enrolledResponse.data.map((course) => course.id)
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load courses')
  } finally {
    loading.value = false
  }
}

async function handleEnroll(course) {
  try {
    await coursesApi.enroll(course.id)
    if (!enrolledCourseIds.value.includes(course.id)) {
      enrolledCourseIds.value = [...enrolledCourseIds.value, course.id]
    }
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not enroll in course')
  }
}

async function handleCreateCourse(payload) {
  saving.value = true
  error.value = null

  try {
    await coursesApi.createCourse(payload)
    showCreateForm.value = false
    await loadCourses()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not create course')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Courses</h1>
        <p class="page-subtitle">Browse the catalog and work only through the gateway.</p>
      </div>
      <div class="actions">
        <button class="button-secondary" type="button" @click="loadCourses">Refresh</button>
        <button
          v-if="authStore.hasRole('TEACHER')"
          class="button"
          type="button"
          @click="showCreateForm = !showCreateForm"
        >
          {{ showCreateForm ? 'Close form' : 'Create course' }}
        </button>
      </div>
    </div>

    <ErrorAlert :error="error" />

    <div class="panel stack">
      <strong>Statuses</strong>
      <div class="checkbox-grid">
        <label v-for="status in statusOptions" :key="status" class="checkbox-item">
          <input v-model="selectedStatuses" type="checkbox" :value="status" />
          <span>{{ status }}</span>
        </label>
      </div>
      <div class="actions">
        <button class="button" type="button" @click="loadCourses">Apply filters</button>
      </div>
    </div>

    <div v-if="showCreateForm" class="panel">
      <CourseForm submit-label="Create course" :loading="saving" @submit="handleCreateCourse" />
    </div>

    <LoadingState v-if="loading" />
    <EmptyState
      v-else-if="!courses.length"
      title="No courses found"
      description="Try another status filter or create a new course."
    />
    <div v-else class="grid grid-2">
      <CourseCard
        v-for="course in courses"
        :key="course.id"
        :course="course"
        :can-enroll="authStore.hasRole('STUDENT')"
        :is-enrolled="enrolledCourseIds.includes(course.id)"
        @enroll="handleEnroll"
      />
    </div>
  </section>
</template>
