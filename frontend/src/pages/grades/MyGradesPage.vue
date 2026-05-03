<script setup>
import { onMounted, ref } from 'vue'
import * as gradesApi from '../../api/gradesApi'
import { normalizeError } from '../../utils/errorUtils'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'

const grades = ref([])
const loading = ref(false)
const error = ref(null)

onMounted(loadGrades)

async function loadGrades() {
  loading.value = true
  error.value = null
  try {
    const { data } = await gradesApi.getMyGrades()
    grades.value = data
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load grades')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">My grades</h1>
        <p class="page-subtitle">Current student's gradebook.</p>
      </div>
      <button class="button-secondary" type="button" @click="loadGrades">Refresh</button>
    </div>

    <ErrorAlert :error="error" />
    <LoadingState v-if="loading" />
    <EmptyState v-else-if="!grades.length" title="No grades yet" description="Grades will appear here after teacher review." />
    <div v-else class="panel table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>Course</th>
            <th>Assignment</th>
            <th>Grade</th>
            <th>Feedback</th>
            <th>Graded At</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="grade in grades" :key="grade.submissionId">
            <td>{{ grade.courseTitle }}</td>
            <td>{{ grade.assignmentTitle }}</td>
            <td>{{ grade.grade }}</td>
            <td>{{ grade.feedback || '—' }}</td>
            <td>{{ grade.gradedAt || '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
