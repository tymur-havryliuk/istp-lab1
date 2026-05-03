<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from './components/AppLayout.vue'

const route = useRoute()
const usePlainLayout = computed(() => route.meta.plain === true)
</script>

<template>
  <RouterView v-if="usePlainLayout" />
  <AppLayout v-else>
    <RouterView />
  </AppLayout>
</template>
