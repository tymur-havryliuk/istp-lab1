<script setup>
defineProps({
  error: {
    type: Object,
    default: null
  }
})
</script>

<template>
  <div v-if="error" class="error-alert">
    <strong class="error-alert__title">
      <template v-if="error.status === 400">Request error</template>
      <template v-else-if="error.status === 401">Session expired</template>
      <template v-else-if="error.status === 403">No permission</template>
      <template v-else-if="error.status === 404">Not found</template>
      <template v-else>Server error</template>
    </strong>
    <div>{{ error.message }}</div>
  </div>
</template>

<style scoped>
.error-alert {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 8px;
  border: 1px solid #f3b3b3;
  background: #fff3f3;
  color: #a41f1f;
}

.error-alert__title {
  font-size: 14px;
}
</style>
