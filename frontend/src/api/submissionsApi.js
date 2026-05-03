import { httpClient } from './httpClient'

export function submitAssignment(assignmentId, payload) {
  return httpClient.post(`/api/v1/assignments/${assignmentId}/submissions`, payload)
}

export function getMySubmissions() {
  return httpClient.get('/api/v1/submissions/submitted')
}

export function getAssignmentSubmissions(assignmentId) {
  return httpClient.get(`/api/v1/assignments/${assignmentId}/submissions`)
}

export function getSubmissionById(submissionId) {
  return httpClient.get(`/api/v1/submissions/${submissionId}`)
}
