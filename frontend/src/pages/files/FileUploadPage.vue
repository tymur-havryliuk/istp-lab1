<script setup>
import FileUpload from '../../components/FileUpload.vue'

function handleUploaded() {}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Upload file</h1>
        <p class="page-subtitle">Standalone file upload for the current student.</p>
      </div>
    </div>

    <FileUpload @uploaded="handleUploaded" />
  </section>
</template>
