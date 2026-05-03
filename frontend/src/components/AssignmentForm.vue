<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({ title: '', description: '', deadline: '' })
  },
  submitLabel: {
    type: String,
    default: 'Save assignment'
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit'])

const form = reactive({
  title: '',
  description: '',
  deadline: ''
})

watch(
  () => props.modelValue,
  (value) => {
    form.title = value?.title || ''
    form.description = value?.description || ''
    form.deadline = value?.deadline || ''
  },
  { immediate: true, deep: true }
)

function handleSubmit() {
  emit('submit', {
    title: form.title,
    description: form.description,
    deadline: form.deadline
  })
}
</script>

<template>
  <form class="form-grid" @submit.prevent="handleSubmit">
    <div class="form-row">
      <label for="assignment-title">Title</label>
      <input id="assignment-title" v-model="form.title" class="input" maxlength="150" required />
    </div>
    <div class="form-row">
      <label for="assignment-description">Description</label>
      <textarea id="assignment-description" v-model="form.description" class="textarea" />
    </div>
    <div class="form-row">
      <label for="assignment-deadline">Deadline</label>
      <input id="assignment-deadline" v-model="form.deadline" class="input" type="datetime-local" required />
    </div>
    <div class="actions">
      <button class="button" type="submit" :disabled="loading">
        {{ loading ? 'Saving...' : submitLabel }}
      </button>
    </div>
  </form>
</template>
