<script setup>
defineProps({
  course: {
    type: Object,
    required: true
  },
  canEnroll: {
    type: Boolean,
    default: false
  },
  isEnrolled: {
    type: Boolean,
    default: false
  }
})

defineEmits(['enroll'])
</script>

<template>
  <article class="panel stack">
    <div class="page-header">
      <div>
        <h3 style="margin: 0 0 6px;">{{ course.title }}</h3>
        <p class="muted" style="margin: 0;">{{ course.teacherName }}</p>
      </div>
      <span class="badge">{{ course.status }}</span>
    </div>
    <p style="margin: 0;">{{ course.description || 'No course description provided.' }}</p>
    <div class="actions">
      <RouterLink class="button-secondary" :to="`/courses/${course.id}`">Details</RouterLink>
      <button
        v-if="canEnroll"
        type="button"
        class="button"
        :disabled="isEnrolled"
        @click="$emit('enroll', course)"
      >
        {{ isEnrolled ? 'Enrolled' : 'Enroll' }}
      </button>
    </div>
  </article>
</template>
