<template>
  <main class="auth-page">
    <section class="auth-stage" aria-label="注册装饰动画">
      <RouterLink to="/" class="brand-link">
        <img :src="logoUrl" alt="服装图案智能创作平台" class="brand-logo" />
        <span>服装图案智能创作平台</span>
      </RouterLink>

      <div class="characters-wrap">
        <AuthCharacters
          :is-typing="isTyping"
          :show-password="showPassword"
          :password-length="(formState.userPassword || '').length + (formState.checkPassword || '').length"
          :login-failed="authFailed"
          :login-success="authSuccess"
        />
      </div>

      <div class="stage-copy">
        <h1>创建灵感账号</h1>
        <p>保存你的图案作品、收藏偏好和智能创作记录。</p>
      </div>
    </section>

    <section class="auth-panel" aria-label="用户注册">
      <div class="mobile-brand">
        <img :src="logoUrl" alt="服装图案智能创作平台" class="brand-logo" />
        <span>服装图案智能创作平台</span>
      </div>

      <div class="form-header">
        <p class="eyebrow">Create Account</p>
        <h2>用户注册</h2>
        <p>设置账号密码，开始管理你的服装图案灵感。</p>
      </div>

      <a-form :model="formState" name="registerForm" autocomplete="off" class="auth-form" @finish="handleSubmit">
        <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号!' }]">
          <label class="field-label" for="register-account">账号</label>
          <input
            id="register-account"
            v-model="formState.userAccount"
            class="auth-input"
            placeholder="请输入账号"
            autocomplete="username"
            @focus="markTyping(true)"
            @blur="markTyping(false)"
          />
        </a-form-item>

        <a-form-item
          name="userPassword"
          :rules="[
            { required: true, message: '请输入密码!' },
            { min: 8, message: '密码不能少于8位' },
          ]"
        >
          <label class="field-label" for="register-password">密码</label>
          <div class="password-field">
            <input
              id="register-password"
              v-model="formState.userPassword"
              class="auth-input"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              autocomplete="new-password"
              @focus="markTyping(true)"
              @blur="markTyping(false)"
            />
            <button
              class="password-toggle"
              type="button"
              :aria-label="showPassword ? '隐藏密码' : '显示密码'"
              @click="showPassword = !showPassword"
            >
              <svg v-if="showPassword" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <svg v-else viewBox="0 0 24 24" aria-hidden="true">
                <path d="M3 3l18 18" />
                <path d="M10.6 10.6A2.8 2.8 0 0 0 12 14.8c.8 0 1.5-.3 2-.8" />
                <path d="M7.4 7.7C4.4 9.4 2.5 12 2.5 12s3.5 6 9.5 6c1.8 0 3.4-.5 4.8-1.2" />
                <path d="M12 6c6 0 9.5 6 9.5 6a16 16 0 0 1-2.1 2.5" />
              </svg>
            </button>
          </div>
        </a-form-item>

        <a-form-item
          name="checkPassword"
          :rules="[
            { required: true, message: '请输入确认密码!' },
            { min: 8, message: '确认密码不能少于8位' },
          ]"
        >
          <label class="field-label" for="register-check-password">确认密码</label>
          <div class="password-field">
            <input
              id="register-check-password"
              v-model="formState.checkPassword"
              class="auth-input"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请再次输入密码"
              autocomplete="new-password"
              @focus="markTyping(true)"
              @blur="markTyping(false)"
            />
          </div>
        </a-form-item>

        <p v-if="errorMessage" class="auth-error">{{ errorMessage }}</p>

        <div class="tips">
          已有账号？<RouterLink to="/user/login">去登录</RouterLink>
        </div>

        <a-form-item>
          <button class="submit-button" type="submit" :disabled="submitting">
            <span>{{ submitting ? '注册中...' : '注册' }}</span>
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M5 12h14" />
              <path d="m13 6 6 6-6 6" />
            </svg>
          </button>
        </a-form-item>
      </a-form>
    </section>
  </main>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { userRegister } from '@/api/userController.ts'
import AuthCharacters from '@/components/auth/AuthCharacters.vue'
import logoUrl from '@/assets/logo.svg'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const isTyping = ref(false)
const showPassword = ref(false)
const submitting = ref(false)
const authFailed = ref(false)
const authSuccess = ref(false)
const errorMessage = ref('')
let feedbackTimer: ReturnType<typeof setTimeout> | null = null

const markTyping = (typing: boolean) => {
  isTyping.value = typing
}

const resetFeedbackLater = (key: 'failed' | 'success', delay = 2600) => {
  if (feedbackTimer) {
    clearTimeout(feedbackTimer)
  }
  feedbackTimer = setTimeout(() => {
    if (key === 'failed') authFailed.value = false
    if (key === 'success') authSuccess.value = false
  }, delay)
}

const handleSubmit = async (values: API.UserRegisterRequest) => {
  submitting.value = true
  authFailed.value = false
  authSuccess.value = false
  errorMessage.value = ''

  if (values.userPassword !== values.checkPassword) {
    submitting.value = false
    authFailed.value = true
    errorMessage.value = '两次输入的密码不一致'
    message.error(errorMessage.value)
    resetFeedbackLater('failed')
    return
  }

  try {
    const res = await userRegister(values)
    if (res.data.code === 0 && res.data.data) {
      authSuccess.value = true
      message.success('注册成功')
      window.setTimeout(() => {
        router.push({
          path: '/user/login',
          replace: true,
        })
      }, 800)
    } else {
      throw new Error(res.data.message || '注册失败')
    }
  } catch (error: any) {
    authFailed.value = true
    errorMessage.value = error?.message || '注册失败，请稍后重试'
    message.error('注册失败：' + errorMessage.value)
    resetFeedbackLater('failed')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.auth-page {
  position: fixed;
  inset: 0;
  z-index: 20;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(420px, 0.95fr);
  overflow: hidden;
  background: #f7fbff;
}

.auth-stage {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
  padding: 46px;
  color: #10201e;
  background:
    linear-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.08) 1px, transparent 1px),
    linear-gradient(135deg, #e7efff 0%, #f7fbff 44%, #e1f5ea 100%);
  background-size:
    22px 22px,
    22px 22px,
    auto;
}

.brand-link,
.mobile-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #10201e;
  font-weight: 700;
  text-decoration: none;
}

.brand-logo {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  object-fit: cover;
}

.characters-wrap {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  min-height: 470px;
}

.stage-copy {
  max-width: 520px;
}

.stage-copy h1 {
  margin: 0 0 10px;
  font-size: 36px;
  line-height: 1.2;
  color: #0f1720;
}

.stage-copy p {
  margin: 0;
  color: #40544f;
  font-size: 16px;
  line-height: 1.7;
}

.auth-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 100%;
  padding: 48px clamp(32px, 7vw, 88px);
  background: #ffffff;
}

.mobile-brand {
  display: none;
  justify-content: center;
  margin-bottom: 28px;
}

.form-header {
  margin-bottom: 30px;
  text-align: left;
}

.eyebrow {
  margin: 0 0 10px;
  color: #2f8f83;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.form-header h2 {
  margin: 0 0 10px;
  color: #101820;
  font-size: 32px;
  line-height: 1.2;
}

.form-header p {
  margin: 0;
  color: #5b6670;
  line-height: 1.6;
}

.auth-form {
  width: 100%;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  color: #26323d;
  font-size: 14px;
  font-weight: 600;
}

.auth-input {
  width: 100%;
  height: 48px;
  padding: 0 14px;
  color: #101820;
  font-size: 16px;
  border: 1px solid #d8e1e8;
  border-radius: 8px;
  outline: none;
  background: #fbfdff;
  transition:
    border-color 160ms ease,
    box-shadow 160ms ease,
    background 160ms ease;
}

.auth-input:focus {
  border-color: #2f8f83;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(47, 143, 131, 0.14);
}

.password-field {
  position: relative;
}

.password-field .auth-input {
  padding-right: 48px;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  color: #64727e;
  border: 0;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
}

.password-toggle:hover {
  color: #101820;
  background: #edf5f3;
}

.password-toggle svg,
.submit-button svg {
  width: 20px;
  height: 20px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.auth-error {
  margin: 0 0 16px;
  padding: 10px 12px;
  color: #b42318;
  border: 1px solid rgba(180, 35, 24, 0.24);
  border-radius: 8px;
  background: #fff2f0;
}

.tips {
  margin-bottom: 18px;
  color: #5b6670;
  font-size: 14px;
  text-align: right;
}

.tips a {
  color: #2f8f83;
  font-weight: 700;
}

.submit-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 48px;
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  border: 0;
  border-radius: 8px;
  background: #101820;
  cursor: pointer;
  transition:
    transform 180ms ease,
    box-shadow 180ms ease,
    opacity 180ms ease;
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(16, 24, 32, 0.18);
}

.submit-button:disabled {
  opacity: 0.62;
  cursor: not-allowed;
}

@media (max-width: 1024px) {
  .auth-page {
    display: flex;
    flex-direction: column;
    overflow-y: auto;
  }

  .auth-stage {
    min-height: 300px;
    padding: 24px 20px 0;
  }

  .auth-stage .brand-link,
  .stage-copy {
    display: none;
  }

  .characters-wrap {
    min-height: 250px;
  }

  .auth-panel {
    flex: 1;
    padding: 28px 22px 40px;
  }

  .mobile-brand {
    display: flex;
  }

  .form-header {
    text-align: center;
  }
}
</style>
