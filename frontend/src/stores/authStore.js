import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from '../api/authApi'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(readUserFromStorage())

  const isAuthenticated = computed(() => Boolean(token.value))
  const role = computed(() => user.value?.role || '')

  function persistAuth(authToken, authUser) {
    token.value = authToken
    user.value = authUser
    localStorage.setItem('token', authToken)
    localStorage.setItem('user', JSON.stringify(authUser))
  }

  async function login(email, password) {
    const { data } = await authApi.login(email, password)
    persistAuth(data.token, data.user)
    return data.user
  }

  async function register(payload) {
    const { data } = await authApi.register(payload)
    persistAuth(data.token, data.user)
    return data.user
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function loadFromStorage() {
    token.value = localStorage.getItem('token') || ''
    user.value = readUserFromStorage()
  }

  function hasRole(expectedRole) {
    return role.value === expectedRole
  }

  return {
    token,
    user,
    isAuthenticated,
    role,
    login,
    register,
    logout,
    loadFromStorage,
    hasRole
  }
})

function readUserFromStorage() {
  const rawUser = localStorage.getItem('user')
  if (!rawUser) {
    return null
  }

  try {
    return JSON.parse(rawUser)
  } catch {
    localStorage.removeItem('user')
    return null
  }
}
