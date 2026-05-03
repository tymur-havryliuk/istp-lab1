import { httpClient } from './httpClient'

export function exportGradesReport() {
  return httpClient.get('/api/v1/reports/grades/export', {
    responseType: 'blob'
  })
}

export function importGradesReport(file) {
  const formData = new FormData()
  formData.append('file', file)
  return httpClient.post('/api/v1/reports/grades/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
