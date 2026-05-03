<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  }
})
</script>

<template>
  <div class="empty-state panel">
    <strong>{{ title }}</strong>
    <span v-if="description" class="muted">{{ description }}</span>
  </div>
</template>

<style scoped>
.empty-state {
  display: grid;
  gap: 6px;
  justify-items: start;
}
</style>
