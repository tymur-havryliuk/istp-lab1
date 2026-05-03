import { httpClient } from './httpClient'

export function login(email, password) {
  return httpClient.post('/api/v1/auth/login', { email, password })
}
