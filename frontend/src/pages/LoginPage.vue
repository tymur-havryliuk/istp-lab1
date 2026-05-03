<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import { normalizeError } from '../utils/errorUtils'
import ErrorAlert from '../components/ErrorAlert.vue'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  email: '',
  password: ''
})

const error = ref(null)
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  error.value = null

  try {
    const user = await authStore.login(form.email, form.password)
    await router.push(user.role === 'TEACHER' ? '/courses/owned' : '/courses/enrolled')
  } catch (loginError) {
    error.value = normalizeError(loginError, 'Invalid email or password')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-shell">
    <div class="panel auth-card stack">
      <div>
        <h1 class="page-title" style="font-size: 32px; margin-bottom: 8px;">Sign in</h1>
        <p class="page-subtitle">Frontend talks only to the gateway on port 8080.</p>
      </div>

      <ErrorAlert :error="error" />

      <form class="form-grid" @submit.prevent="handleLogin">
        <div class="form-row">
          <label for="email">Email</label>
          <input id="email" v-model="form.email" class="input" type="email" required />
        </div>

        <div class="form-row">
          <label for="password">Password</label>
          <input id="password" v-model="form.password" class="input" type="password" required />
        </div>

        <button class="button" type="submit" :disabled="loading">
          {{ loading ? 'Signing in...' : 'Login' }}
        </button>
      </form>

      <div class="panel" style="background: #f8fbff;">
        <strong>Test credentials</strong>
        <div class="stack" style="margin-top: 10px;">
          <div>Teacher: <strong>teacher@example.com</strong> / <strong>password123</strong></div>
          <div>Student: <strong>student@example.com</strong> / <strong>password123</strong></div>
        </div>
      </div>
    </div>
  </div>
</template>
