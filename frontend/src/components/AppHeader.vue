<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../stores/authStore'

const authStore = useAuthStore()

const userLabel = computed(() => authStore.user?.fullName || 'Guest')
const roleLabel = computed(() => authStore.role || 'PUBLIC')

function handleLogout() {
  authStore.logout()
  window.location.href = '/login'
}
</script>

<template>
  <header class="app-header">
    <div>
      <div class="app-brand">Learning Platform</div>
    </div>

    <div class="app-header__meta">
      <div class="app-header__user">
        <strong>{{ userLabel }}</strong>
        <span>{{ roleLabel }}</span>
      </div>
      <button
        v-if="authStore.isAuthenticated"
        class="button-ghost"
        type="button"
        @click="handleLogout"
      >
        Logout
      </button>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  padding: 20px 24px;
  border-bottom: 1px solid #dce5f2;
  background: #ffffff;
}

.app-brand {
  font-size: 20px;
  font-weight: 700;
}

.app-tagline {
  margin-top: 4px;
  color: #61708a;
  font-size: 14px;
}

.app-header__meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.app-header__user {
  display: grid;
  gap: 2px;
  text-align: right;
}

.app-header__user span {
  color: #61708a;
  font-size: 13px;
}

@media (max-width: 700px) {
  .app-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .app-header__meta {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
