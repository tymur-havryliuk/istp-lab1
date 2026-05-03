<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({ title: '', description: '' })
  },
  submitLabel: {
    type: String,
    default: 'Save course'
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit'])

const form = reactive({
  title: '',
  description: ''
})

watch(
  () => props.modelValue,
  (value) => {
    form.title = value?.title || ''
    form.description = value?.description || ''
  },
  { immediate: true, deep: true }
)

function handleSubmit() {
  emit('submit', {
    title: form.title,
    description: form.description
  })
}
</script>

<template>
  <form class="form-grid" @submit.prevent="handleSubmit">
    <div class="form-row">
      <label for="course-title">Title</label>
      <input id="course-title" v-model="form.title" class="input" maxlength="150" required />
    </div>
    <div class="form-row">
      <label for="course-description">Description</label>
      <textarea id="course-description" v-model="form.description" class="textarea" />
    </div>
    <div class="actions">
      <button class="button" type="submit" :disabled="loading">
        {{ loading ? 'Saving...' : submitLabel }}
      </button>
    </div>
  </form>
</template>
