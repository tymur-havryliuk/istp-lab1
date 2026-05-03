import { httpClient } from './httpClient'

export function getCourseAssignments(courseId) {
  return httpClient.get(`/api/v1/courses/${courseId}/assignments`)
}

export function getAssignmentById(assignmentId) {
  return httpClient.get(`/api/v1/assignments/${assignmentId}`)
}

export function createAssignment(courseId, payload) {
  return httpClient.post(`/api/v1/courses/${courseId}/assignments`, payload)
}

export function updateAssignment(assignmentId, payload) {
  return httpClient.put(`/api/v1/assignments/${assignmentId}`, payload)
}

export function deleteAssignment(assignmentId) {
  return httpClient.delete(`/api/v1/assignments/${assignmentId}`)
}
