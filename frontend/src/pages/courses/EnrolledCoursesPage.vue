<script setup>
import { onMounted, ref } from 'vue'
import * as coursesApi from '../../api/coursesApi'
import { normalizeError } from '../../utils/errorUtils'
import CourseCard from '../../components/CourseCard.vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'

const courses = ref([])
const loading = ref(false)
const error = ref(null)

onMounted(loadCourses)

async function loadCourses() {
  loading.value = true
  error.value = null

  try {
    const { data } = await coursesApi.getEnrolledCourses()
    courses.value = data
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load enrolled courses')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">My courses</h1>
        <p class="page-subtitle">Courses where the current student is enrolled.</p>
      </div>
      <button class="button-secondary" type="button" @click="loadCourses">Refresh</button>
    </div>

    <ErrorAlert :error="error" />
    <LoadingState v-if="loading" />
    <EmptyState v-else-if="!courses.length" title="No courses found" description="You are not enrolled in any course yet." />
    <div v-else class="grid grid-2">
      <CourseCard v-for="course in courses" :key="course.id" :course="course" />
    </div>
  </section>
</template>
