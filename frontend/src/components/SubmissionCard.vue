<script setup>
defineProps({
  submission: {
    type: Object,
    required: true
  }
})
</script>

<template>
  <article class="panel stack">
    <div class="page-header">
      <div>
        <h3 style="margin: 0 0 4px;">{{ submission.studentName }}</h3>
        <div class="muted">Assignment #{{ submission.assignmentId }}</div>
      </div>
      <RouterLink class="button-secondary" :to="`/submissions/${submission.id}`">Open</RouterLink>
    </div>
    <p style="margin: 0;">{{ submission.comment || 'No comment provided.' }}</p>
    <div class="meta-list">
      <div class="meta-item">
        <span>Submitted At</span>
        <strong>{{ submission.submittedAt || '—' }}</strong>
      </div>
      <div class="meta-item">
        <span>Grade</span>
        <strong>{{ submission.grade ?? 'Not graded yet' }}</strong>
      </div>
    </div>
  </article>
</template>
