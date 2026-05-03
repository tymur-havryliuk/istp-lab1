<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import * as coursesApi from '../../api/coursesApi'
import { normalizeError } from '../../utils/errorUtils'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'

const route = useRoute()
const students = ref([])
const loading = ref(false)
const error = ref(null)

onMounted(loadStudents)

async function loadStudents() {
  loading.value = true
  error.value = null
  try {
    const { data } = await coursesApi.getCourseStudents(route.params.id)
    students.value = data
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load course students')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Course students</h1>
        <p class="page-subtitle">Roster for course #{{ route.params.id }}.</p>
      </div>
      <button class="button-secondary" type="button" @click="loadStudents">Refresh</button>
    </div>

    <ErrorAlert :error="error" />
    <LoadingState v-if="loading" />
    <EmptyState v-else-if="!students.length" title="No students found" description="This course has no active or completed enrollments." />
    <div v-else class="panel table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Full name</th>
            <th>Email</th>
            <th>Role</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="student in students" :key="student.id">
            <td>{{ student.id }}</td>
            <td>{{ student.fullName }}</td>
            <td>{{ student.email }}</td>
            <td>{{ student.role }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
