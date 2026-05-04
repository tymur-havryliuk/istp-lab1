<script setup>
import FileUpload from '../../components/FileUpload.vue'
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Files</h1>
        <p class="page-subtitle">Upload several files and keep their IDs for future submissions.</p>
      </div>
    </div>

    <FileUpload multiple />
  </section>
</template>
