<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import AppHeader from './AppHeader.vue'

const route = useRoute()
const authStore = useAuthStore()

const navItems = computed(() => {
  if (!authStore.isAuthenticated) {
    return []
  }

  if (authStore.role === 'STUDENT') {
    return [
      { label: 'Courses', to: '/courses' },
      { label: 'My Courses', to: '/courses/enrolled' },
      { label: 'My Submissions', to: '/submissions/my' },
      { label: 'My Grades', to: '/grades' },
      { label: 'Upload File', to: '/files' },
      { label: 'Statistics', to: '/statistics' }
    ]
  }

  return [
    { label: 'Courses', to: '/courses' },
    { label: 'My Owned Courses', to: '/courses/owned' },
    { label: 'Reports', to: '/reports' },
    { label: 'Statistics', to: '/statistics' }
  ]
})
</script>

<template>
  <div class="shell">
    <AppHeader />
    <div class="shell__body">
      <aside class="shell__nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="shell__nav-link"
          :class="{ 'shell__nav-link--active': route.path === item.to }"
        >
          {{ item.label }}
        </RouterLink>
      </aside>
      <main class="shell__main">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
}

.shell__body {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  min-height: calc(100vh - 89px);
}

.shell__nav {
  display: grid;
  align-content: start;
  gap: 8px;
  padding: 20px 14px;
  border-right: 1px solid #dce5f2;
  background: #ffffff;
}

.shell__nav-link {
  border-radius: 8px;
  padding: 10px 12px;
  color: #3b475a;
  font-weight: 500;
}

.shell__nav-link--active,
.shell__nav-link:hover {
  background: #edf3ff;
  color: #164bb6;
}

.shell__main {
  padding: 24px;
}

@media (max-width: 900px) {
  .shell__body {
    grid-template-columns: 1fr;
  }

  .shell__nav {
    grid-auto-flow: column;
    overflow-x: auto;
    border-right: 0;
    border-bottom: 1px solid #dce5f2;
  }
}
</style>
