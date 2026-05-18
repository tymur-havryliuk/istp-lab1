<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import { normalizeError } from '../utils/errorUtils'
import ErrorAlert from '../components/ErrorAlert.vue'

const router = useRouter()
const authStore = useAuthStore()
const mode = ref('login')

const form = reactive({
  fullName: '',
  email: '',
  password: '',
  role: 'STUDENT',
  department: '',
  groupName: ''
})

const error = ref(null)
const loading = ref(false)
const isRegisterMode = computed(() => mode.value === 'register')
const pageTitle = computed(() => isRegisterMode.value ? 'Create account' : 'Sign in')

async function handleSubmit() {
  loading.value = true
  error.value = null

  try {
    const user = isRegisterMode.value
      ? await authStore.register({
          fullName: form.fullName,
          email: form.email,
          password: form.password,
          role: form.role,
          department: form.role === 'TEACHER' ? form.department : null,
          groupName: form.role === 'STUDENT' ? form.groupName : null
        })
      : await authStore.login(form.email, form.password)
    await router.push(user.role === 'TEACHER' ? '/courses/owned' : '/courses/enrolled')
  } catch (authError) {
    error.value = normalizeError(authError, isRegisterMode.value ? 'Could not create account' : 'Invalid email or password')
  } finally {
    loading.value = false
  }
}

function switchMode(nextMode) {
  mode.value = nextMode
  error.value = null
}
</script>

<template>
  <div class="auth-shell">
    <div class="panel auth-card stack">
      <div>
        <h1 class="page-title" style="font-size: 32px; margin-bottom: 8px;">{{ pageTitle }}</h1>
        <p class="page-subtitle">{{ pageSubtitle }}</p>
      </div>

      <div class="actions">
        <button
          class="button-secondary"
          type="button"
          :disabled="mode === 'login'"
          @click="switchMode('login')"
        >
          Sign in
        </button>
        <button
          class="button-secondary"
          type="button"
          :disabled="mode === 'register'"
          @click="switchMode('register')"
        >
          Create account
        </button>
      </div>

      <ErrorAlert :error="error" />

      <form class="form-grid" @submit.prevent="handleSubmit">
        <div v-if="isRegisterMode" class="form-row">
          <label for="full-name">Full name</label>
          <input id="full-name" v-model="form.fullName" class="input" type="text" required />
        </div>

        <div class="form-row">
          <label for="email">Email</label>
          <input id="email" v-model="form.email" class="input" type="email" required />
        </div>

        <div class="form-row">
          <label for="password">Password</label>
          <input id="password" v-model="form.password" class="input" type="password" required />
        </div>

        <template v-if="isRegisterMode">
          <div class="form-row">
            <label for="role">Role</label>
            <select id="role" v-model="form.role" class="select">
              <option value="STUDENT">Student</option>
              <option value="TEACHER">Teacher</option>
            </select>
          </div>

          <div v-if="form.role === 'STUDENT'" class="form-row">
            <label for="group-name">Group name</label>
            <input id="group-name" v-model="form.groupName" class="input" type="text" required />
          </div>

          <div v-if="form.role === 'TEACHER'" class="form-row">
            <label for="department">Department</label>
            <input id="department" v-model="form.department" class="input" type="text" required />
          </div>
        </template>

        <button class="button" type="submit" :disabled="loading">
          {{
            loading
              ? isRegisterMode ? 'Creating account...' : 'Signing in...'
              : isRegisterMode ? 'Create account' : 'Login'
          }}
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
