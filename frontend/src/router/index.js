import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import { pinia } from '../stores'
import LoginPage from '../pages/LoginPage.vue'
import CoursesPage from '../pages/courses/CoursesPage.vue'
import CourseDetailsPage from '../pages/courses/CourseDetailsPage.vue'
import EnrolledCoursesPage from '../pages/courses/EnrolledCoursesPage.vue'
import OwnedCoursesPage from '../pages/courses/OwnedCoursesPage.vue'
import CourseStudentsPage from '../pages/courses/CourseStudentsPage.vue'
import CourseAssignmentsPage from '../pages/assignments/CourseAssignmentsPage.vue'
import AssignmentDetailsPage from '../pages/assignments/AssignmentDetailsPage.vue'
import MySubmissionsPage from '../pages/submissions/MySubmissionsPage.vue'
import AssignmentSubmissionsPage from '../pages/submissions/AssignmentSubmissionsPage.vue'
import SubmissionDetailsPage from '../pages/submissions/SubmissionDetailsPage.vue'
import MyGradesPage from '../pages/grades/MyGradesPage.vue'
import ReportsPage from '../pages/reports/ReportsPage.vue'
import StatisticsPage from '../pages/statistics/StatisticsPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginPage, meta: { public: true, plain: true } },
    { path: '/courses', component: CoursesPage, meta: { requiresAuth: true } },
    { path: '/courses/enrolled', component: EnrolledCoursesPage, meta: { roles: ['STUDENT'] } },
    { path: '/courses/owned', component: OwnedCoursesPage, meta: { roles: ['TEACHER'] } },
    { path: '/courses/:id(\\d+)', component: CourseDetailsPage, meta: { requiresAuth: true } },
    { path: '/courses/:id(\\d+)/students', component: CourseStudentsPage, meta: { roles: ['TEACHER'] } },
    { path: '/courses/:id(\\d+)/assignments', component: CourseAssignmentsPage, meta: { requiresAuth: true } },
    { path: '/assignments/:id(\\d+)/submissions', component: AssignmentSubmissionsPage, meta: { roles: ['TEACHER'] } },
    { path: '/assignments/:id(\\d+)', component: AssignmentDetailsPage, meta: { requiresAuth: true } },
    { path: '/submissions/my', component: MySubmissionsPage, meta: { roles: ['STUDENT'] } },
    { path: '/submissions/:id(\\d+)', component: SubmissionDetailsPage, meta: { requiresAuth: true } },
    { path: '/grades', component: MyGradesPage, meta: { roles: ['STUDENT'] } },
    { path: '/files', redirect: '/submissions/my' },
    { path: '/reports', component: ReportsPage, meta: { roles: ['TEACHER'] } },
    { path: '/statistics', component: StatisticsPage, meta: { requiresAuth: true } }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  const authStore = useAuthStore(pinia)
  authStore.loadFromStorage()

  if (to.meta.public) {
    if (to.path === '/login' && authStore.isAuthenticated) {
      return authStore.role === 'TEACHER' ? '/courses/owned' : '/courses/enrolled'
    }
    return true
  }

  if (!authStore.isAuthenticated) {
    return '/login'
  }

  if (to.meta.roles?.length && !to.meta.roles.includes(authStore.role)) {
    return authStore.role === 'TEACHER' ? '/courses/owned' : '/courses/enrolled'
  }

  return true
})

export default router
