<template>
  <div class="auth-characters" :class="[`auth-characters--${mood}`, { 'is-typing': isTyping }]">
    <div v-if="loginSuccess" class="confetti" aria-hidden="true">
      <span v-for="piece in confettiPieces" :key="piece.id" class="confetti__piece" :style="piece.style" />
    </div>

    <div
      ref="purpleRef"
      class="character character--purple"
      :style="characterStyle(purplePosition, 'purple')"
    >
      <div class="character__eyes character__eyes--purple">
        <AuthEye
          :size="18"
          :pupil-size="7"
          :blinking="blink.purple"
          :mood="mood"
          :sad-rotate="-14"
          :force-x="forcedLook.x"
          :force-y="forcedLook.y"
        />
        <AuthEye
          :size="18"
          :pupil-size="7"
          :blinking="blink.purple"
          :mood="mood"
          :sad-rotate="14"
          :force-x="forcedLook.x"
          :force-y="forcedLook.y"
        />
      </div>
      <span class="mouth mouth--purple" />
    </div>

    <div
      ref="blackRef"
      class="character character--black"
      :style="characterStyle(blackPosition, 'black')"
    >
      <div class="character__eyes character__eyes--black">
        <AuthEye
          :size="16"
          :pupil-size="6"
          :blinking="blink.black"
          :mood="mood"
          :sad-rotate="-18"
          :force-x="forcedLook.x"
          :force-y="forcedLook.y"
        />
        <AuthEye
          :size="16"
          :pupil-size="6"
          :blinking="blink.black"
          :mood="mood"
          :sad-rotate="18"
          :force-x="forcedLook.x"
          :force-y="forcedLook.y"
        />
      </div>
    </div>

    <div
      ref="orangeRef"
      class="character character--orange"
      :style="characterStyle(orangePosition, 'orange')"
    >
      <div class="character__eyes character__eyes--orange">
        <span class="dot-eye" :class="{ 'dot-eye--blink': blink.orange }" />
        <span class="dot-eye" :class="{ 'dot-eye--blink': blink.orange }" />
      </div>
      <span class="mouth mouth--orange" />
    </div>

    <div
      ref="yellowRef"
      class="character character--yellow"
      :style="characterStyle(yellowPosition, 'yellow')"
    >
      <div class="character__eyes character__eyes--yellow">
        <span class="dot-eye" :class="{ 'dot-eye--blink': blink.yellow }" />
        <span class="dot-eye" :class="{ 'dot-eye--blink': blink.yellow }" />
      </div>
      <svg class="line-mouth" width="70" height="22" viewBox="0 0 70 22" aria-hidden="true">
        <path class="line-mouth__path" d="M4 8 Q18 16 35 16 Q52 16 66 8" />
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import AuthEye from './AuthEye.vue'

const props = withDefaults(
  defineProps<{
    isTyping?: boolean
    showPassword?: boolean
    passwordLength?: number
    loginFailed?: boolean
    loginSuccess?: boolean
  }>(),
  {
    isTyping: false,
    showPassword: false,
    passwordLength: 0,
    loginFailed: false,
    loginSuccess: false,
  },
)

type Position = {
  faceX: number
  faceY: number
  skew: number
}

const purpleRef = ref<HTMLElement | null>(null)
const blackRef = ref<HTMLElement | null>(null)
const orangeRef = ref<HTMLElement | null>(null)
const yellowRef = ref<HTMLElement | null>(null)

const purplePosition = reactive<Position>({ faceX: 0, faceY: 0, skew: 0 })
const blackPosition = reactive<Position>({ faceX: 0, faceY: 0, skew: 0 })
const orangePosition = reactive<Position>({ faceX: 0, faceY: 0, skew: 0 })
const yellowPosition = reactive<Position>({ faceX: 0, faceY: 0, skew: 0 })
const blink = reactive({ purple: false, black: false, orange: false, yellow: false })

const confettiPieces = ref<{ id: number; style: Record<string, string> }[]>([])
const mood = computed<'normal' | 'sad' | 'happy'>(() => {
  if (props.loginSuccess) return 'happy'
  if (props.loginFailed) return 'sad'
  return 'normal'
})
const isHidingPassword = computed(() => props.passwordLength > 0 && !props.showPassword)
const forcedLook = computed(() => {
  if (props.loginSuccess) return { x: 0, y: 4 }
  if (props.passwordLength > 0 && props.showPassword) return { x: -4, y: -4 }
  if (isHidingPassword.value) return { x: 4, y: 3 }
  return { x: undefined, y: undefined }
})

let animationFrame: number | null = null
let confettiTimer: ReturnType<typeof setTimeout> | null = null
const blinkTimers: ReturnType<typeof setTimeout>[] = []

const copyPosition = (target: Position, source: Position) => {
  target.faceX = source.faceX
  target.faceY = source.faceY
  target.skew = source.skew
}

const calculatePosition = (
  element: HTMLElement | null,
  event: MouseEvent,
  rangeX = 16,
  rangeY = 10,
): Position => {
  if (!element) {
    return { faceX: 0, faceY: 0, skew: 0 }
  }
  const rect = element.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 3
  const deltaX = event.clientX - centerX
  const deltaY = event.clientY - centerY

  return {
    faceX: Math.max(-rangeX, Math.min(rangeX, deltaX / 18)),
    faceY: Math.max(-rangeY, Math.min(rangeY, deltaY / 24)),
    skew: Math.max(-5, Math.min(5, -deltaX / 120)),
  }
}

const handleMouseMove = (event: MouseEvent) => {
  if (animationFrame) {
    cancelAnimationFrame(animationFrame)
  }
  animationFrame = requestAnimationFrame(() => {
    copyPosition(purplePosition, calculatePosition(purpleRef.value, event, 22, 16))
    copyPosition(blackPosition, calculatePosition(blackRef.value, event, 12, 10))
    copyPosition(orangePosition, calculatePosition(orangeRef.value, event, 10, 8))
    copyPosition(yellowPosition, calculatePosition(yellowRef.value, event, 12, 10))
  })
}

const characterStyle = (position: Position, type: 'purple' | 'black' | 'orange' | 'yellow') => {
  const passwordShift = isHidingPassword.value ? 10 : 0
  const successLift = props.loginSuccess ? -10 : 0
  const scale = props.loginSuccess ? 1.02 : 1

  return {
    '--face-x': `${position.faceX}px`,
    '--face-y': `${position.faceY}px`,
    transform: `translateY(${successLift}px) skewX(${position.skew + passwordShift}deg) scale(${scale})`,
    '--look-offset': type === 'purple' && isHidingPassword.value ? '32px' : '0px',
  }
}

const scheduleBlink = (key: keyof typeof blink) => {
  const timer = setTimeout(
    () => {
      blink[key] = true
      const closeTimer = setTimeout(() => {
        blink[key] = false
        scheduleBlink(key)
      }, 150)
      blinkTimers.push(closeTimer)
    },
    2600 + Math.random() * 2800,
  )
  blinkTimers.push(timer)
}

const generateConfetti = () => {
  const colors = ['#4ecdc4', '#ff6b6b', '#ffd166', '#8bd17c', '#f79d65', '#6c3ff5']
  confettiPieces.value = Array.from({ length: 96 }, (_, index) => ({
    id: index,
    style: {
      left: `${Math.random() * 100}%`,
      backgroundColor: colors[index % colors.length] || '#4ecdc4',
      animationDelay: `${Math.random() * 900}ms`,
      animationDuration: `${2600 + Math.random() * 1700}ms`,
      transform: `rotate(${Math.random() * 180}deg)`,
    },
  }))
  if (confettiTimer) clearTimeout(confettiTimer)
  confettiTimer = setTimeout(() => {
    confettiPieces.value = []
  }, 5200)
}

watch(
  () => props.loginSuccess,
  (success) => {
    if (success) generateConfetti()
  },
)

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove, { passive: true })
  scheduleBlink('purple')
  scheduleBlink('black')
  scheduleBlink('orange')
  scheduleBlink('yellow')
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', handleMouseMove)
  if (animationFrame) cancelAnimationFrame(animationFrame)
  if (confettiTimer) clearTimeout(confettiTimer)
  blinkTimers.forEach((timer) => clearTimeout(timer))
})
</script>

<style scoped>
.auth-characters {
  position: relative;
  width: min(520px, 80vw);
  height: 430px;
  isolation: isolate;
}

.character {
  position: absolute;
  bottom: 0;
  transform-origin: bottom center;
  transition:
    transform 420ms cubic-bezier(0.34, 1.56, 0.64, 1),
    height 260ms ease;
  will-change: transform;
}

.character--purple {
  left: 70px;
  z-index: 1;
  width: 180px;
  height: 390px;
  background: #6c3ff5;
  animation: purple-enter 850ms cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.character--black {
  left: 238px;
  z-index: 2;
  width: 120px;
  height: 308px;
  background: #252525;
  animation: black-enter 780ms 120ms cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.character--orange {
  left: 0;
  z-index: 3;
  width: 238px;
  height: 150px;
  border-radius: 120px 120px 0 0;
  background: #ff9b6b;
  animation: orange-enter 820ms 60ms cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.character--yellow {
  left: 308px;
  z-index: 4;
  width: 140px;
  height: 230px;
  border-radius: 70px 70px 0 0;
  background: #e8d754;
  animation: yellow-enter 760ms 180ms cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.is-typing .character--purple {
  height: 430px;
}

.character__eyes {
  position: absolute;
  display: flex;
  gap: 24px;
  transform: translate(calc(var(--face-x) + var(--look-offset)), var(--face-y));
  transition: transform 180ms ease-out;
}

.character__eyes--purple {
  top: 32px;
  left: 76px;
  gap: 32px;
}

.character__eyes--black {
  top: 34px;
  left: 28px;
}

.character__eyes--orange {
  top: 62px;
  left: 112px;
  gap: 30px;
}

.character__eyes--yellow {
  top: 42px;
  left: 52px;
  gap: 22px;
}

.dot-eye {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  background: #252525;
  transition: height 140ms ease;
}

.dot-eye--blink {
  height: 3px;
  align-self: center;
}

.mouth {
  position: absolute;
  display: block;
  background: #252525;
  transition:
    width 220ms ease,
    height 220ms ease,
    border-radius 220ms ease,
    transform 220ms ease;
}

.mouth--purple {
  top: 65px;
  left: 100px;
  width: 24px;
  height: 8px;
  border-radius: 0 0 12px 12px;
  transform: translate(var(--face-x), var(--face-y));
}

.mouth--orange {
  top: 92px;
  left: 126px;
  width: 26px;
  height: 13px;
  border-radius: 0 0 13px 13px;
  transform: translate(var(--face-x), var(--face-y));
}

.is-typing .mouth--purple {
  width: 8px;
  height: 32px;
  border-radius: 0;
}

.is-typing .mouth--orange {
  width: 15px;
  height: 15px;
  border-radius: 999px;
}

.auth-characters--sad .mouth--purple,
.auth-characters--sad .mouth--orange {
  border-radius: 13px 13px 0 0;
}

.auth-characters--happy .mouth--purple {
  width: 30px;
  height: 16px;
  border-radius: 0 0 15px 15px;
}

.auth-characters--happy .mouth--orange {
  width: 32px;
  height: 18px;
  border-radius: 0 0 16px 16px;
}

.line-mouth {
  position: absolute;
  top: 88px;
  left: 38px;
  transform: translate(var(--face-x), var(--face-y));
  transition: transform 180ms ease-out;
}

.line-mouth__path {
  fill: none;
  stroke: #252525;
  stroke-width: 3;
  stroke-linecap: round;
  transition: d 220ms ease;
}

.auth-characters--sad .line-mouth__path {
  d: path('M4 13 Q18 4 35 13 Q52 22 66 13');
}

.auth-characters--happy .line-mouth__path {
  d: path('M4 5 Q18 17 35 18 Q52 17 66 5');
}

.confetti {
  position: fixed;
  inset: 0;
  z-index: 10;
  pointer-events: none;
  overflow: hidden;
}

.confetti__piece {
  position: absolute;
  top: -24px;
  width: 7px;
  height: 13px;
  border-radius: 2px;
  animation: confetti-fall linear both;
}

@keyframes confetti-fall {
  to {
    translate: 38px 110vh;
    rotate: 720deg;
  }
}

@keyframes purple-enter {
  from {
    opacity: 0;
    transform: translateX(-120px) translateY(40px) rotate(-14deg) scale(0.4);
  }
}

@keyframes black-enter {
  from {
    opacity: 0;
    transform: translateY(-80px) scale(0.5);
  }
}

@keyframes orange-enter {
  from {
    opacity: 0;
    transform: translateX(-150px) translateY(60px) rotate(-18deg) scale(0.35);
  }
}

@keyframes yellow-enter {
  from {
    opacity: 0;
    transform: translateX(150px) translateY(50px) rotate(18deg) scale(0.35);
  }
}

@media (max-width: 1024px) {
  .auth-characters {
    width: 360px;
    height: 250px;
    transform: scale(0.68);
    transform-origin: bottom center;
  }
}

@media (prefers-reduced-motion: reduce) {
  .character,
  .confetti__piece,
  .character--purple,
  .character--black,
  .character--orange,
  .character--yellow {
    animation: none;
    transition: none;
  }
}
</style>
