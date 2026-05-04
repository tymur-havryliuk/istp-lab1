<script setup>
import { onMounted, ref } from 'vue'
import * as coursesApi from '../../api/coursesApi'
import { normalizeError } from '../../utils/errorUtils'
import CourseForm from '../../components/CourseForm.vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'
import { formatStatusLabel } from '../../utils/formatters'

const courses = ref([])
const loading = ref(false)
const saving = ref(false)
const editingCourse = ref(null)
const showCreateForm = ref(false)
const error = ref(null)

onMounted(loadCourses)

async function loadCourses() {
  loading.value = true
  error.value = null
  try {
    const { data } = await coursesApi.getOwnedCourses()
    courses.value = data
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load owned courses')
  } finally {
    loading.value = false
  }
}

async function handleCreate(payload) {
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

async function handleUpdate(payload) {
  if (!editingCourse.value) {
    return
  }

  saving.value = true
  error.value = null
  try {
    await coursesApi.updateCourse(editingCourse.value.id, payload)
    editingCourse.value = null
    await loadCourses()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not update course')
  } finally {
    saving.value = false
  }
}

async function handleDelete(courseId) {
  if (!window.confirm('Delete this course?')) {
    return
  }

  try {
    await coursesApi.deleteCourse(courseId)
    await loadCourses()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not delete course')
  }
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">My owned courses</h1>
        <p class="page-subtitle">Manage the courses attached to the current teacher account.</p>
      </div>
      <div class="actions">
        <button class="button-secondary" type="button" @click="loadCourses">Refresh</button>
        <button class="button" type="button" @click="showCreateForm = !showCreateForm">
          {{ showCreateForm ? 'Close form' : 'Create course' }}
        </button>
      </div>
    </div>

    <ErrorAlert :error="error" />

    <div v-if="showCreateForm" class="panel">
      <CourseForm submit-label="Create course" :loading="saving" @submit="handleCreate" />
    </div>

    <div v-if="editingCourse" class="panel">
      <CourseForm
        :model-value="editingCourse"
        submit-label="Update course"
        :loading="saving"
        @submit="handleUpdate"
      />
    </div>

    <LoadingState v-if="loading" />
    <EmptyState v-else-if="!courses.length" title="No owned courses found" description="Create the first course from this page." />
    <div v-else class="stack">
      <article v-for="course in courses" :key="course.id" class="panel stack">
        <div class="page-header">
          <div>
            <h3 style="margin: 0 0 6px;">{{ course.title }}</h3>
            <div class="muted">{{ formatStatusLabel(course.status) }}</div>
          </div>
          <div class="actions">
            <RouterLink class="button-secondary" :to="`/courses/${course.id}`">Details</RouterLink>
            <RouterLink class="button-secondary" :to="`/courses/${course.id}/students`">Students</RouterLink>
            <RouterLink class="button-secondary" :to="`/courses/${course.id}/assignments`">Assignments</RouterLink>
            <button class="button-ghost" type="button" @click="editingCourse = course">Edit</button>
            <button class="button-danger" type="button" @click="handleDelete(course.id)">Delete</button>
          </div>
        </div>
        <div>{{ course.description || 'No course description provided.' }}</div>
      </article>
    </div>
  </section>
</template>
