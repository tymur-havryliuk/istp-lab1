import { httpClient } from './httpClient'

export function getAverageGradesByCourse() {
  return httpClient.get('/api/v1/statistics/courses/average-grades')
}
