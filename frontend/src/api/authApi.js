import { httpClient } from './httpClient'

export function login(email, password) {
  return httpClient.post('/api/v1/auth/login', { email, password })
}

export function register(payload) {
  return httpClient.post('/api/v1/auth/register', payload)
}
