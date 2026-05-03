import { httpClient } from './httpClient'

export function gradeSubmission(submissionId, payload) {
  return httpClient.post(`/api/v1/submissions/${submissionId}/grade`, payload)
}

export function getMyGrades() {
  return httpClient.get('/api/v1/grades/student')
}
