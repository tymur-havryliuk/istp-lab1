<script setup>
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Title,
  Tooltip
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const props = defineProps({
  items: {
    type: Array,
    default: () => []
  }
})

const chartData = computed(() => ({
  labels: props.items.map((item) => item.courseTitle),
  datasets: [
    {
      label: 'Average grade',
      data: props.items.map((item) => item.averageGrade),
      backgroundColor: ['#1957d2', '#0d8a6b', '#d28a19', '#be3057', '#6f4fd9', '#1689b5']
    }
  ]
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
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
</script>

<template>
  <div class="panel" style="height: 420px;">
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>
