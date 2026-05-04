<script setup>
import { computed, reactive, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({ grade: '', feedback: '' })
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit'])

const form = reactive({
  grade: '',
  feedback: ''
})

const submitLabel = computed(() =>
  props.modelValue?.grade === null || props.modelValue?.grade === undefined || props.modelValue?.grade === ''
    ? 'Save grade'
    : 'Update grade'
)

watch(
  () => props.modelValue,
  (value) => {
    form.grade = value?.grade ?? ''
    form.feedback = value?.feedback || ''
  },
  { immediate: true, deep: true }
)

function handleSubmit() {
  emit('submit', {
    grade: Number(form.grade),
    feedback: form.feedback
  })
}
</script>

<template>
  <form class="form-grid" @submit.prevent="handleSubmit">
    <div class="form-row">
      <label for="grade-value">Grade</label>
      <input id="grade-value" v-model="form.grade" class="input" type="number" min="0" max="100" required />
    </div>
    <div class="form-row">
      <label for="grade-feedback">Feedback</label>
      <textarea id="grade-feedback" v-model="form.feedback" class="textarea" />
    </div>
    <div class="actions">
      <button class="button" type="submit" :disabled="loading">
        {{ loading ? 'Saving...' : submitLabel }}
      </button>
    </div>
  </form>
</template>
