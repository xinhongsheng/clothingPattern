<template>
  <span
    ref="eyeRef"
    class="auth-eye"
    :class="{ 'auth-eye--sad': mood === 'sad', 'auth-eye--blink': blinking }"
    :style="{
      width: `${size}px`,
      height: blinking ? '3px' : mood === 'sad' ? `${size * 0.56}px` : `${size}px`,
      transform: mood === 'sad' ? `rotate(${sadRotate}deg)` : 'rotate(0deg)',
    }"
  >
    <span
      v-if="!blinking"
      class="auth-eye__pupil"
      :style="{
        width: `${pupilSize}px`,
        height: `${pupilSize}px`,
        transform: `translate(${pupilPosition.x}px, ${pupilPosition.y}px)`,
      }"
    />
  </span>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = withDefaults(
  defineProps<{
    size?: number
    pupilSize?: number
    maxDistance?: number
    blinking?: boolean
    forceX?: number
    forceY?: number
    mood?: 'normal' | 'sad' | 'happy'
    sadRotate?: number
  }>(),
  {
    size: 20,
    pupilSize: 7,
    maxDistance: 5,
    blinking: false,
    forceX: undefined,
    forceY: undefined,
    mood: 'normal',
    sadRotate: 0,
  },
)

const eyeRef = ref<HTMLElement | null>(null)
const mouseX = ref(0)
const mouseY = ref(0)

const handleMouseMove = (event: MouseEvent) => {
  mouseX.value = event.clientX
  mouseY.value = event.clientY
}

const pupilPosition = computed(() => {
  if (props.forceX !== undefined && props.forceY !== undefined) {
    return { x: props.forceX, y: props.forceY }
  }
  if (!eyeRef.value) {
    return { x: 0, y: 0 }
  }

  const rect = eyeRef.value.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 2
  const deltaX = mouseX.value - centerX
  const deltaY = mouseY.value - centerY
  const distance = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), props.maxDistance)
  const angle = Math.atan2(deltaY, deltaX)

  return {
    x: Math.cos(angle) * distance,
    y: Math.sin(angle) * distance,
  }
})

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', handleMouseMove)
})
</script>

<style scoped>
.auth-eye {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 999px;
  background: #fff;
  transition:
    height 180ms ease,
    border-radius 220ms ease,
    transform 220ms ease;
}

.auth-eye--sad {
  border-radius: 0 0 999px 999px;
}

.auth-eye--blink {
  align-self: center;
}

.auth-eye__pupil {
  display: block;
  border-radius: 50%;
  background: #252525;
  transition: transform 90ms ease-out;
}
</style>
