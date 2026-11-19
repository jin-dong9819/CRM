<template>
  <div class="register-page">
    <!-- 左侧品牌区（与登录页保持一致） -->
    <section class="brand">
      <div class="brand-inner">
        <div class="brand-logo">
          <span class="mark">易</span>
          <span class="word">易配通</span>
        </div>
        <h1>从一个手机号<br />开始你的客户经营</h1>
        <p class="desc">
          注册后即可用手机号登录，<br />
          接待门店、报价开单、跟踪回款。
        </p>
        <ul class="features">
          <li><el-icon><Cellphone /></el-icon>手机号即账号，短信验证码快速开通</li>
          <li><el-icon><Lock /></el-icon>密码 BCrypt 加密存储，不落明文</li>
          <li><el-icon><CircleCheck /></el-icon>验证码 2 分钟有效，一次注册一次校验</li>
        </ul>
        <div class="brand-footer">© 2026 易配通汽配客户管理系统</div>
      </div>
    </section>

    <!-- 右侧注册表单 -->
    <section class="form-side">
      <div class="register-card">
        <header class="card-header">
          <h2>注册账号</h2>
          <p>请填写手机号，获取验证码后完成注册</p>
        </header>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          label-position="top"
          @keyup.enter="handleRegister"
        >
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model.trim="form.phone"
              placeholder="请输入 11 位手机号"
              :prefix-icon="Iphone"
              maxlength="11"
              clearable
              autocomplete="tel"
            />
          </el-form-item>

          <el-form-item label="短信验证码" prop="code">
            <div class="code-row">
              <el-input
                v-model.trim="form.code"
                placeholder="4 位数字验证码"
                :prefix-icon="Message"
                maxlength="4"
                autocomplete="one-time-code"
              />
              <el-button
                class="code-btn"
                :disabled="countdown > 0 || sending"
                :loading="sending"
                @click="handleSendCode"
              >
                {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="设置密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="6-32 位，建议字母 + 数字"
              :prefix-icon="Lock"
              show-password
              autocomplete="new-password"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              :prefix-icon="Lock"
              show-password
              autocomplete="new-password"
            />
          </el-form-item>

          <el-button
            type="primary"
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            {{ loading ? '注册中…' : '注 册' }}
          </el-button>
        </el-form>

        <div v-if="devCode" class="dev-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>开发模式验证码：<b>{{ devCode }}</b>（已自动填入）</span>
        </div>

        <div class="card-footer">
          已有账号？
          <el-link type="primary" :underline="false" @click="goLogin">立即登录</el-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Iphone, Lock, Message, InfoFilled, Cellphone, CircleCheck
} from '@element-plus/icons-vue'
import { registerApi, sendSmsCodeApi } from '@/api/auth'

const router = useRouter()

const formRef = ref()
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
const devCode = ref('')

let timer = null

const form = reactive({
  phone: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (!value) return callback(new Error('请再次输入密码'))
  if (value !== form.password) return callback(new Error('两次输入的密码不一致'))
  callback()
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' },
    { pattern: /^\d{4}$/, message: '验证码为 4 位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需为 6-32 位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirm, trigger: 'blur' }]
}

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

async function handleSendCode() {
  if (countdown.value > 0 || sending.value) return
  // 只校验手机号，避免用户还没填其他字段就被拦下
  const valid = await formRef.value?.validateField('phone').catch(() => false)
  if (valid === false) return

  sending.value = true
  try {
    const data = await sendSmsCodeApi(form.phone)
    startCountdown()
    if (data?.devCode) {
      devCode.value = data.devCode
      form.code = data.devCode
      ElMessage.success('验证码已生成，请查看下方提示')
    } else {
      ElMessage.success('验证码已发送，请注意查收')
    }
  } catch {
    // 错误提示由响应拦截器统一处理
  } finally {
    sending.value = false
  }
}

async function handleRegister() {
  if (loading.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await registerApi({
      phone: form.phone,
      code: form.code,
      password: form.password
    })
    ElMessage.success('注册成功，请使用手机号登录')
    router.replace({ path: '/login', query: { phone: form.phone } })
  } catch {
    // 错误提示由响应拦截器统一处理
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.replace('/login')
}

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.register-page {
  display: flex;
  height: 100vh;
  background: #f4f6fa;
  overflow: hidden;
}

/* ---------- 左侧品牌 ---------- */
.brand {
  flex: 0 0 46%;
  position: relative;
  background: linear-gradient(150deg, #1b2b4b 0%, #20396b 45%, #2f6bff 100%);
  color: #fff;
  overflow: hidden;
}

.brand::before {
  content: '';
  position: absolute;
  width: 520px;
  height: 520px;
  right: -180px;
  top: -160px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(120, 165, 255, 0.45), transparent 65%);
}

.brand::after {
  content: '';
  position: absolute;
  width: 380px;
  height: 380px;
  left: -140px;
  bottom: -150px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(47, 107, 255, 0.55), transparent 65%);
}

.brand-inner {
  position: relative;
  z-index: 1;
  height: 100%;
  padding: 56px 64px;
  display: flex;
  flex-direction: column;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 56px;
}

.brand-logo .mark {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 20px;
  backdrop-filter: blur(4px);
}

.brand-logo .word {
  font-size: 19px;
  font-weight: 700;
  letter-spacing: 3px;
}

.brand h1 {
  font-size: 36px;
  line-height: 1.35;
  margin: 0 0 20px;
  font-weight: 700;
  letter-spacing: 1px;
}

.brand .desc {
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
  line-height: 1.9;
  margin: 0 0 40px;
}

.features {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.features li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.88);
}

.features li .el-icon {
  font-size: 16px;
  color: #9dc0ff;
}

.brand-footer {
  margin-top: auto;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
}

/* ---------- 右侧表单 ---------- */
.form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  overflow-y: auto;
}

.register-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 34px 40px 28px;
  box-shadow: 0 18px 50px rgba(20, 38, 76, 0.1);
  border: 1px solid rgba(230, 233, 239, 0.8);
}

.card-header {
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
  color: var(--crm-text);
}

.card-header p {
  margin: 0;
  font-size: 13px;
  color: var(--crm-text-secondary);
}

.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.code-row :deep(.el-input) {
  flex: 1;
  min-width: 0;
}

.code-btn {
  flex: 0 0 122px;
  height: 40px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 4px;
  font-size: 15px;
  letter-spacing: 2px;
  border-radius: 8px;
  box-shadow: 0 8px 18px rgba(47, 107, 255, 0.25);
}

.dev-tip {
  margin-top: 16px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f7f9ff;
  border: 1px dashed #c3d2ff;
  color: #5b6b82;
  font-size: 12px;
  display: flex;
  gap: 8px;
  line-height: 1.6;
}

.dev-tip b {
  color: #2f6bff;
  letter-spacing: 1px;
}

.dev-tip .el-icon {
  color: #2f6bff;
  margin-top: 2px;
  flex: 0 0 auto;
}

.card-footer {
  margin-top: 18px;
  text-align: center;
  font-size: 13px;
  color: var(--crm-text-secondary);
}

@media (max-width: 900px) {
  .brand {
    display: none;
  }
}
</style>
