import { httpClient } from './httpClient'

export function getCourses(statuses) {
  const params = statuses?.length ? { statuses: statuses.join(',') } : undefined
  return httpClient.get('/api/v1/courses', { params })
}

export function getCourseById(courseId) {
  return httpClient.get(`/api/v1/courses/${courseId}`)
}

export function createCourse(payload) {
  return httpClient.post('/api/v1/courses', payload)
}

export function updateCourse(courseId, payload) {
  return httpClient.put(`/api/v1/courses/${courseId}`, payload)
}

export function deleteCourse(courseId) {
  return httpClient.delete(`/api/v1/courses/${courseId}`)
}

export function enroll(courseId) {
  return httpClient.post(`/api/v1/courses/${courseId}/enroll`)
}

export function getEnrolledCourses() {
  return httpClient.get('/api/v1/courses/enrolled')
}

export function getOwnedCourses() {
  return httpClient.get('/api/v1/courses/owned')
}

export function getCourseStudents(courseId) {
  return httpClient.get(`/api/v1/courses/${courseId}/students`)
}
