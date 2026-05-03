<script setup>
import { ref } from 'vue'
import * as reportsApi from '../../api/reportsApi'
import { normalizeError } from '../../utils/errorUtils'
import { downloadBlob, getFilenameFromHeaders } from '../../utils/downloadUtils'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'

const error = ref(null)
const exporting = ref(false)
const importing = ref(false)
const reportFile = ref(null)
const analysis = ref(null)

async function handleExport() {
  exporting.value = true
  error.value = null
  try {
    const response = await reportsApi.exportGradesReport()
    downloadBlob(response.data, getFilenameFromHeaders(response.headers, 'grades-report.xlsx'))
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not export report')
  } finally {
    exporting.value = false
  }
}

async function handleImport() {
  if (!reportFile.value) {
    return
  }

  importing.value = true
  error.value = null
  try {
    const { data } = await reportsApi.importGradesReport(reportFile.value)
    analysis.value = data
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not import report')
  } finally {
    importing.value = false
  }
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Reports</h1>
        <p class="page-subtitle">Teacher-only Excel export and import analysis.</p>
      </div>
    </div>

    <ErrorAlert :error="error" />

    <div class="grid grid-2">
      <div class="panel stack">
        <h3 style="margin: 0;">Export grades</h3>
        <p class="muted" style="margin: 0;">Download the owned-course grade report as XLSX.</p>
        <button class="button" type="button" :disabled="exporting" @click="handleExport">
          {{ exporting ? 'Exporting...' : 'Export Grades Report' }}
        </button>
      </div>

      <div class="panel stack">
        <h3 style="margin: 0;">Import grades</h3>
        <p class="muted" style="margin: 0;">Upload an XLSX and inspect the grade summary.</p>
        <input type="file" accept=".xlsx" @change="reportFile = $event.target.files?.[0] || null" />
        <button class="button" type="button" :disabled="!reportFile || importing" @click="handleImport">
          {{ importing ? 'Importing...' : 'Import Grades Report' }}
        </button>
      </div>
    </div>

    <EmptyState
      v-if="!analysis"
      title="No report analysis yet"
      description="Upload a grades workbook to see totals and score metrics."
    />

    <div v-else class="panel table-wrap">
      <table class="table">
        <tbody>
          <tr>
            <th>Total rows</th>
            <td>{{ analysis.totalRows }}</td>
          </tr>
          <tr>
            <th>Average grade</th>
            <td>{{ analysis.averageGrade ?? '—' }}</td>
          </tr>
          <tr>
            <th>Min grade</th>
            <td>{{ analysis.minGrade ?? '—' }}</td>
          </tr>
          <tr>
            <th>Max grade</th>
            <td>{{ analysis.maxGrade ?? '—' }}</td>
          </tr>
          <tr>
            <th>Course count</th>
            <td>{{ analysis.courseCount }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
