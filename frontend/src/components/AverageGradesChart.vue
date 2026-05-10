<script setup>
import { computed } from 'vue'
import { Bar, Doughnut } from 'vue-chartjs'
import {
  ArcElement,
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Title,
  Tooltip
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Title, Tooltip, Legend)

const props = defineProps({
  items: {
    type: Array,
    default: () => []
  }
})

const labels = computed(() => props.items.map((item) => item.courseTitle))
const grades = computed(() => props.items.map((item) => Number(item.averageGrade)))
const totalAverage = computed(() => {
  if (!grades.value.length) {
    return 0
  }
  const total = grades.value.reduce((sum, grade) => sum + grade, 0)
  return Math.round((total / grades.value.length) * 10) / 10
})

const gradeBarData = computed(() => ({
  labels: labels.value,
  datasets: [
    {
      label: 'Average grade',
      data: grades.value,
      backgroundColor: ['#1957d2', '#0d8a6b', '#d28a19', '#be3057', '#6f4fd9', '#1689b5']
    }
  ]
}))

const contributionData = computed(() => {
  const total = grades.value.reduce((sum, grade) => sum + grade, 0)
  const values = total === 0
    ? grades.value.map(() => 0)
    : grades.value.map((grade) => Math.round((grade / total) * 1000) / 10)

  return {
    labels: labels.value,
    datasets: [
      {
        label: 'Contribution %',
        data: values,
        backgroundColor: ['#1957d2', '#0d8a6b', '#d28a19', '#be3057', '#6f4fd9', '#1689b5'],
        borderWidth: 1
      }
    ]
  }
})

const gradeBarOptions = {
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    y: {
      beginAtZero: true,
      max: 100
    }
  },
  plugins: {
    legend: {
      display: false
    },
    title: {
      display: true,
      text: 'Середній бал по курсах'
    }
  }
}

const contributionOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom'
    },
    title: {
      display: true,
      text: 'Внесок кожного курсу (%)'
    }
  }
}

</script>

<template>
  <div class="stack">
    <div class="panel">
      <div class="meta-list" style="grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));">
        <div class="meta-item">
          <span>Courses</span>
          <strong>{{ items.length }}</strong>
        </div>
        <div class="meta-item">
          <span>Overall average</span>
          <strong>{{ totalAverage }}</strong>
        </div>
      </div>
    </div>

    <div class="grid grid-2">
      <div class="panel" style="height: 380px;">
        <Bar :data="gradeBarData" :options="gradeBarOptions" />
      </div>
      <div class="panel" style="height: 380px;">
        <Doughnut :data="contributionData" :options="contributionOptions" />
      </div>
    </div>
  </div>
</template>
