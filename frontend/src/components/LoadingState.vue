<template>
  <div class="loading-state panel">
    <div class="loading-state__spinner" />
    <span>Loading data...</span>
  </div>
</template>

<style scoped>
.loading-state {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.loading-state__spinner {
  width: 18px;
  height: 18px;
  border-radius: 999px;
  border: 2px solid #c6d7f6;
  border-top-color: #1957d2;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
