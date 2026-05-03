<script setup>
import { onMounted, ref } from 'vue'
import * as statisticsApi from '../../api/statisticsApi'
import { normalizeError } from '../../utils/errorUtils'
import AverageGradesChart from '../../components/AverageGradesChart.vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'

const items = ref([])
const loading = ref(false)
const error = ref(null)

onMounted(loadStatistics)

async function loadStatistics() {
  loading.value = true
  error.value = null
  try {
    const { data } = await statisticsApi.getAverageGradesByCourse()
    items.value = data
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load statistics')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Statistics</h1>
        <p class="page-subtitle">Average grade per course chart for the lab defense.</p>
      </div>
      <button class="button-secondary" type="button" @click="loadStatistics">Refresh</button>
    </div>

    <ErrorAlert :error="error" />
    <LoadingState v-if="loading" />
    <EmptyState v-else-if="!items.length" title="No statistics data" description="No graded submissions were found yet." />
    <AverageGradesChart v-else :items="items" />
  </section>
</template>
