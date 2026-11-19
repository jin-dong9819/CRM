<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <section class="brand">
      <div class="brand-inner">
        <div class="brand-logo">
          <span class="mark">易</span>
          <span class="word">易配通</span>
        </div>
        <h1>让每一个小件<br />都不缺货</h1>
        <p class="desc">
          玻璃水、清洗剂、三滤、雨刮等易损件，<br />
          从客户建档到送货回款，一个后台全看清。
        </p>
        <ul class="features">
          <li><el-icon><Aim /></el-icon>修理厂与车主档案，按车型快速查件</li>
          <li><el-icon><TrendCharts /></el-icon>复购与断货提醒，补单不再靠记性</li>
          <li><el-icon><Lock /></el-icon>角色与部门权限精细可控</li>
        </ul>
        <div class="brand-footer">© 2026 易配通汽配客户管理系统</div>
      </div>
    </section>

    <!-- 右侧登录表单 -->
    <section class="form-side">
      <div class="login-card">
        <header class="card-header">
          <h2>欢迎回来</h2>
          <p>员工请用员工编号登录，注册用户请用手机号登录</p>
        </header>

        <el-tabs v-model="activeTab" class="login-tabs" stretch>
          <!-- 员工登录 -->
          <el-tab-pane label="员工登录" name="emp">
            <el-form
              ref="empFormRef"
              :model="empForm"
              :rules="empRules"
              size="large"
              label-position="top"
              @keyup.enter="handleLogin"
            >
              <el-form-item label="员工编号" prop="empNo">
                <el-input
                  v-model.trim="empForm.empNo"
                  placeholder="请输入员工编号，如 E1001"
                  :prefix-icon="User"
                  clearable
                  autocomplete="username"
                />
              </el-form-item>

              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="empForm.password"
                  type="password"
                  placeholder="请输入登录密码"
                  :prefix-icon="Lock"
                  show-password
                  autocomplete="current-password"
                />
              </el-form-item>

              <div class="form-extra">
                <el-checkbox v-model="empForm.remember">记住员工编号</el-checkbox>
                <el-link type="primary" :underline="false" @click="onForget">忘记密码？</el-link>
              </div>

              <el-button
                type="primary"
                class="submit-btn"
                :loading="loading"
                @click="handleLogin"
              >
                {{ loading ? '登录中…' : '登 录' }}
              </el-button>
            </el-form>

            <div v-if="useMock" class="demo-tip">
              <el-icon><InfoFilled /></el-icon>
              <span>
                演示环境：员工编号 <b>E1001</b>，密码 <b>123456</b>
                <el-link type="primary" :underline="false" @click="fillDemo">一键填入</el-link>
              </span>
            </div>
          </el-tab-pane>

          <!-- 手机号登录（注册用户） -->
          <el-tab-pane label="手机号登录" name="phone">
            <el-form
              ref="phoneFormRef"
              :model="phoneForm"
              :rules="phoneRules"
              size="large"
              label-position="top"
              @keyup.enter="handlePhoneLogin"
            >
              <el-form-item label="手机号" prop="phone">
                <el-input
                  v-model.trim="phoneForm.phone"
                  placeholder="请输入注册手机号"
                  :prefix-icon="Iphone"
                  maxlength="11"
                  clearable
                  autocomplete="tel"
                />
              </el-form-item>

              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="phoneForm.password"
                  type="password"
                  placeholder="请输入登录密码"
                  :prefix-icon="Lock"
                  show-password
                  autocomplete="current-password"
                />
              </el-form-item>

              <el-button
                type="primary"
                class="submit-btn"
                :loading="phoneLoading"
                @click="handlePhoneLogin"
              >
                {{ phoneLoading ? '登录中…' : '登 录' }}
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="card-footer">
          还没有账号？
          <el-link type="primary" :underline="false" @click="goRegister">立即注册</el-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Aim, TrendCharts, InfoFilled, Iphone } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { phoneLoginApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const useMock = String(import.meta.env.VITE_USE_MOCK) === 'true'
const REMEMBER_KEY = 'crm_remember_emp_no'

const activeTab = ref('emp')
const empFormRef = ref()
const phoneFormRef = ref()
const loading = ref(false)
const phoneLoading = ref(false)

const empForm = reactive({
  empNo: '',
  password: '',
  remember: true
})

const phoneForm = reactive({
  phone: '',
  password: ''
})

const empRules = {
  empNo: [
    { required: true, message: '请输入员工编号', trigger: 'blur' },
    { pattern: /^[A-Za-z]\d{3,9}$/, message: '员工编号格式如 E1001', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

onMounted(() => {
  // 从注册页跳回来时自动切到手机号登录并回填手机号
  const registeredPhone = route.query.phone
  if (typeof registeredPhone === 'string' && registeredPhone) {
    activeTab.value = 'phone'
    phoneForm.phone = registeredPhone
  }

  const saved = localStorage.getItem(REMEMBER_KEY)
  if (saved) {
    empForm.empNo = saved
    empForm.remember = true
  }
})

function fillDemo() {
  empForm.empNo = 'E1001'
  empForm.password = '123456'
}

function onForget() {
  ElMessage.info('请联系系统管理员重置密码（分机 8001）')
}

function goRegister() {
  router.push('/register')
}

function afterLogin() {
  const redirect = route.query.redirect
  router.replace(typeof redirect === 'string' ? redirect : '/dashboard/overview')
}

async function handleLogin() {
  if (loading.value) return
  const valid = await empFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({ empNo: empForm.empNo.toUpperCase(), password: empForm.password })

    if (empForm.remember) localStorage.setItem(REMEMBER_KEY, empForm.empNo.toUpperCase())
    else localStorage.removeItem(REMEMBER_KEY)

    ElMessage.success('登录成功')
    afterLogin()
  } catch {
    // 错误提示已在响应拦截器中统一处理
  } finally {
    loading.value = false
  }
}

async function handlePhoneLogin() {
  if (phoneLoading.value) return
  const valid = await phoneFormRef.value?.validate().catch(() => false)
  if (!valid) return

  phoneLoading.value = true
  try {
    const data = await phoneLoginApi({
      phone: phoneForm.phone,
      password: phoneForm.password
    })
    userStore.setToken(data.token)
    userStore.setUserInfo(data.user)
    ElMessage.success('登录成功')
    afterLogin()
  } catch {
    // 错误提示已在响应拦截器中统一处理
  } finally {
    phoneLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
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
  margin-bottom: 64px;
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
  font-size: 38px;
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
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 36px 40px 26px;
  box-shadow: 0 18px 50px rgba(20, 38, 76, 0.1);
  border: 1px solid rgba(230, 233, 239, 0.8);
}

.card-header {
  margin-bottom: 18px;
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

.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
}

.form-extra {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 2px 0 22px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 2px;
  border-radius: 8px;
  box-shadow: 0 8px 18px rgba(47, 107, 255, 0.25);
}

.demo-tip {
  margin-top: 22px;
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

.demo-tip b {
  color: #2f6bff;
}

.demo-tip .el-icon {
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
