<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'
import * as coursesApi from '../../api/coursesApi'
import * as assignmentsApi from '../../api/assignmentsApi'
import { getCourseLessons } from '../../utils/courseLessons'
import { normalizeError } from '../../utils/errorUtils'
import { formatDateTime, formatStatusLabel } from '../../utils/formatters'
import AssignmentForm from '../../components/AssignmentForm.vue'
import CourseForm from '../../components/CourseForm.vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const course = ref(null)
const assignments = ref([])
const contentItems = ref([])
const ownedCourseIds = ref([])
const loading = ref(false)
const courseSaving = ref(false)
const assignmentSaving = ref(false)
const contentSaving = ref(false)
const error = ref(null)
const showCourseEdit = ref(false)
const showAssignmentCreate = ref(false)
const showContentCreate = ref(false)
const contentForm = ref({
  title: '',
  description: '',
  scheduledAt: '',
  deliveryMode: 'OFFLINE',
  room: '',
  meetingLink: ''
})

const courseId = computed(() => Number(route.params.id))
const isTeacher = computed(() => authStore.hasRole('TEACHER'))
const canManageCourse = computed(() => isTeacher.value && ownedCourseIds.value.includes(courseId.value))
const lessons = computed(() => {
  const fallbackLessons = getCourseLessons(course.value, assignments.value)

  if (!contentItems.value.length) {
    return fallbackLessons
  }

  return contentItems.value.map((contentItem, index) => {
    const fallbackLesson = fallbackLessons[index] || {}

    return {
      ...fallbackLesson,
      ...contentItem,
      scheduledAt: contentItem.scheduledAt || fallbackLesson.scheduledAt || null,
      room: contentItem.room || fallbackLesson.room || null,
      meetingLink: contentItem.meetingLink || fallbackLesson.meetingLink || null
    }
  })
})

onMounted(loadPage)

async function loadPage() {
  loading.value = true
  error.value = null

  try {
    const ownedCoursesRequest = isTeacher.value ? coursesApi.getOwnedCourses() : Promise.resolve({ data: [] })
    const [courseResponse, assignmentsResponse, contentResponse, ownedCoursesResponse] = await Promise.all([
      coursesApi.getCourseById(route.params.id),
      assignmentsApi.getCourseAssignments(route.params.id),
      coursesApi.getCourseContent(route.params.id),
      ownedCoursesRequest
    ])
    course.value = courseResponse.data
    assignments.value = assignmentsResponse.data
    contentItems.value = contentResponse.data
    ownedCourseIds.value = ownedCoursesResponse.data.map((ownedCourse) => ownedCourse.id)
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not load course details')
  } finally {
    loading.value = false
  }
}

async function handleUpdateCourse(payload) {
  courseSaving.value = true
  error.value = null
  try {
    await coursesApi.updateCourse(route.params.id, payload)
    showCourseEdit.value = false
    await loadPage()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not update course')
  } finally {
    courseSaving.value = false
  }
}

async function handleDeleteCourse() {
  if (!window.confirm('Delete this course?')) {
    return
  }

  try {
    await coursesApi.deleteCourse(route.params.id)
    await router.push('/courses/owned')
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not delete course')
  }
}

async function handleCreateAssignment(payload) {
  assignmentSaving.value = true
  error.value = null
  try {
    await assignmentsApi.createAssignment(route.params.id, normalizeAssignmentPayload(payload))
    showAssignmentCreate.value = false
    await loadPage()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not create assignment')
  } finally {
    assignmentSaving.value = false
  }
}

function normalizeAssignmentPayload(payload) {
  return {
    ...payload,
    deadline: payload.deadline?.length === 16 ? `${payload.deadline}:00` : payload.deadline
  }
}

async function handleCreateContent() {
  contentSaving.value = true
  error.value = null
  try {
    await coursesApi.createCourseContent(route.params.id, normalizeContentPayload(contentForm.value))
    resetContentForm()
    showContentCreate.value = false
    await loadPage()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not add course content')
  } finally {
    contentSaving.value = false
  }
}

async function handleDeleteContent(contentId) {
  if (!window.confirm('Delete this lesson content?')) {
    return
  }

  try {
    await coursesApi.deleteCourseContent(route.params.id, contentId)
    await loadPage()
  } catch (requestError) {
    error.value = normalizeError(requestError, 'Could not delete course content')
  }
}

function normalizeContentPayload(payload) {
  return {
    title: payload.title,
    description: payload.description,
    scheduledAt: payload.scheduledAt?.length === 16 ? `${payload.scheduledAt}:00` : payload.scheduledAt,
    room: payload.deliveryMode === 'OFFLINE' ? payload.room : null,
    meetingLink: payload.deliveryMode === 'ONLINE' ? payload.meetingLink : null
  }
}

function resetContentForm() {
  contentForm.value = {
    title: '',
    description: '',
    scheduledAt: '',
    deliveryMode: 'OFFLINE',
    room: '',
    meetingLink: ''
  }
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Course details</h1>
        <p class="page-subtitle">Course summary plus linked assignments.</p>
      </div>
      <div class="actions">
        <RouterLink class="button-secondary" :to="`/courses/${route.params.id}/assignments`">
          All assignments
        </RouterLink>
        <RouterLink
          v-if="canManageCourse"
          class="button-secondary"
          :to="`/courses/${route.params.id}/students`"
        >
          View students
        </RouterLink>
      </div>
    </div>

    <ErrorAlert :error="error" />
    <LoadingState v-if="loading" />

    <template v-else-if="course">
      <div class="split">
        <div class="panel stack">
          <div class="page-header">
            <div>
              <h2 style="margin: 0 0 8px;">{{ course.title }}</h2>
              <p class="muted" style="margin: 0;">Teacher: {{ course.teacherName }}</p>
            </div>
            <span class="badge">{{ formatStatusLabel(course.status) }}</span>
          </div>
          <p style="margin: 0;">{{ course.description || 'No course description provided.' }}</p>
          <div class="stack">
            <div class="page-header">
              <div>
                <h3 style="margin: 0 0 6px;">Lessons</h3>
                <p class="muted" style="margin: 0;">Quick outline for the main course topics.</p>
              </div>
              <button
                v-if="canManageCourse"
                class="button-secondary"
                type="button"
                @click="showContentCreate = !showContentCreate"
              >
                {{ showContentCreate ? 'Close content form' : 'Add content' }}
              </button>
            </div>
            <div v-if="showContentCreate" class="panel stack">
              <div class="form-row">
                <label for="content-title">Lesson title</label>
                <input id="content-title" v-model="contentForm.title" class="input" type="text" />
              </div>
              <div class="form-row">
                <label for="content-description">Lesson summary</label>
                <textarea id="content-description" v-model="contentForm.description" class="textarea" />
              </div>
              <div class="form-row">
                <label for="content-time">Lesson time</label>
                <input id="content-time" v-model="contentForm.scheduledAt" class="input" type="datetime-local" />
              </div>
              <div class="form-row">
                <label for="content-mode">Delivery mode</label>
                <select id="content-mode" v-model="contentForm.deliveryMode" class="select">
                  <option value="OFFLINE">Offline</option>
                  <option value="ONLINE">Online</option>
                </select>
              </div>
              <div v-if="contentForm.deliveryMode === 'OFFLINE'" class="form-row">
                <label for="content-room">Room</label>
                <input id="content-room" v-model="contentForm.room" class="input" type="text" placeholder="Room 201" />
              </div>
              <div v-else class="form-row">
                <label for="content-link">Meeting link</label>
                <input
                  id="content-link"
                  v-model="contentForm.meetingLink"
                  class="input"
                  type="url"
                  placeholder="https://meet.google.com/..."
                />
              </div>
              <div class="actions">
                <button class="button" type="button" :disabled="contentSaving" @click="handleCreateContent">
                  {{ contentSaving ? 'Saving...' : 'Save content' }}
                </button>
              </div>
            </div>
            <div class="lesson-list">
              <article v-for="lesson in lessons" :key="lesson.id || lesson.title" class="lesson-item">
                <div class="page-header">
                  <strong>{{ lesson.title }}</strong>
                  <button
                    v-if="canManageCourse && lesson.id"
                    class="button-ghost"
                    type="button"
                    @click="handleDeleteContent(lesson.id)"
                  >
                    Delete
                  </button>
                </div>
                <div v-if="lesson.scheduledAt || lesson.room || lesson.meetingLink" class="lesson-item__meta">
                  <span v-if="lesson.meetingLink" class="badge">Online</span>
                  <span v-else-if="lesson.room" class="badge">Offline</span>
                  <span v-if="lesson.scheduledAt">{{ formatDateTime(lesson.scheduledAt) }}</span>
                  <span v-if="lesson.room">Room: {{ lesson.room }}</span>
                  <a
                    v-if="lesson.meetingLink"
                    :href="lesson.meetingLink"
                    class="lesson-link"
                    target="_blank"
                    rel="noreferrer"
                  >
                    Join meeting
                  </a>
                </div>
                <div v-else class="lesson-item__meta">
                  <span>Time and delivery details are not set yet.</span>
                </div>
                <p class="muted" style="margin: 4px 0 0;">{{ lesson.description || lesson.summary }}</p>
              </article>
            </div>
          </div>
          <div v-if="isTeacher" class="actions">
            <button
              class="button-secondary"
              type="button"
              :disabled="!canManageCourse"
              :title="canManageCourse ? 'Edit course' : 'Only the course owner can edit this course'"
              @click="showCourseEdit = !showCourseEdit"
            >
              {{ showCourseEdit ? 'Close edit' : 'Edit course' }}
            </button>
            <button
              class="button-danger"
              type="button"
              :disabled="!canManageCourse"
              :title="canManageCourse ? 'Delete course' : 'Only the course owner can delete this course'"
              @click="handleDeleteCourse"
            >
              Delete course
            </button>
          </div>
          <div v-if="showCourseEdit && canManageCourse" class="panel">
            <CourseForm
              :model-value="course"
              submit-label="Update course"
              :loading="courseSaving"
              @submit="handleUpdateCourse"
            />
          </div>
        </div>

        <div class="panel stack">
          <div class="page-header">
            <div>
              <h3 style="margin: 0 0 6px;">Assignments</h3>
              <p class="muted" style="margin: 0;">Latest work for this course.</p>
            </div>
            <button
              v-if="canManageCourse"
              class="button"
              type="button"
              @click="showAssignmentCreate = !showAssignmentCreate"
            >
              {{ showAssignmentCreate ? 'Close form' : 'Create assignment' }}
            </button>
          </div>

          <div v-if="showAssignmentCreate && canManageCourse" class="panel">
            <AssignmentForm
              submit-label="Create assignment"
              :loading="assignmentSaving"
              @submit="handleCreateAssignment"
            />
          </div>

          <EmptyState
            v-if="!assignments.length"
            title="No assignments found"
            description="This course does not have assignments yet."
          />
          <div v-else class="stack">
            <div v-for="assignment in assignments" :key="assignment.id" class="panel stack">
              <div class="page-header">
                <div>
                  <strong>{{ assignment.title }}</strong>
                  <div class="muted">{{ assignment.deadline }}</div>
                </div>
                <RouterLink class="button-secondary" :to="`/assignments/${assignment.id}`">
                  View assignment
                </RouterLink>
              </div>
              <div>{{ assignment.description || 'No assignment description provided.' }}</div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>
