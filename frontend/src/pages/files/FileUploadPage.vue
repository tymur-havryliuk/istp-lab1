<script setup>
import FileUpload from '../../components/FileUpload.vue'
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Files</h1>
        <p class="page-subtitle">Upload several files, keep their IDs for future submissions and remove unused uploads.</p>
      </div>
    </div>

    <FileUpload multiple />
  </section>
</template>
