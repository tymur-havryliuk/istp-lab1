<script setup>
import { formatStatusLabel } from '../utils/formatters'

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
  <article class="panel stack course-card">
    <div class="page-header">
      <div>
        <h3 style="margin: 0 0 6px;">{{ course.title }}</h3>
        <p class="muted" style="margin: 0;">{{ course.teacherName }}</p>
      </div>
      <span class="badge">{{ formatStatusLabel(course.status) }}</span>
    </div>
    <p class="course-card__description">{{ course.description || 'No course description provided.' }}</p>
    <div class="actions course-card__actions">
      <RouterLink class="button-secondary course-card__details" :to="`/courses/${course.id}`">Details</RouterLink>
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
