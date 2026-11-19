<template>
  <header class="navbar">
    <div class="navbar-left">
      <el-icon class="hamburger" @click="appStore.toggleSidebar()">
        <Expand v-if="appStore.collapsed" />
        <Fold v-else />
      </el-icon>
      <Breadcrumb />
    </div>

    <div class="navbar-right">
      <el-tag v-if="useMock" type="warning" effect="light" size="small" class="mock-tag">
        演示数据
      </el-tag>

      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-trigger">
          <div class="avatar">{{ initial }}</div>
          <div class="user-meta">
            <span class="name">{{ userStore.displayName }}</span>
            <span class="sub">{{ userStore.userInfo?.roleNames || '员工' }}</span>
          </div>
          <el-icon class="arrow"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile" :icon="User">个人信息</el-dropdown-item>
            <el-dropdown-item command="logout" :icon="SwitchButton" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <el-dialog v-model="profileVisible" title="个人信息" width="420px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="员工编号">{{ userStore.userInfo?.empNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userStore.userInfo?.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ userStore.userInfo?.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ userStore.userInfo?.roleNames || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="profileVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </header>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, SwitchButton } from '@element-plus/icons-vue'
import Breadcrumb from './Breadcrumb.vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

const appStore = useAppStore()
const userStore = useUserStore()
const router = useRouter()

const useMock = String(import.meta.env.VITE_USE_MOCK) === 'true'
const profileVisible = ref(false)

const initial = computed(() => (userStore.displayName || '?').slice(0, 1))

async function handleCommand(command) {
  if (command === 'profile') {
    profileVisible.value = true
    return
  }
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning',
        confirmButtonText: '退出',
        cancelButtonText: '取消'
      })
    } catch {
      return
    }
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.replace('/login')
  }
}
</script>

<style scoped>
.navbar {
  height: 56px;
  flex: 0 0 56px;
  background: #fff;
  border-bottom: 1px solid var(--crm-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px 0 8px;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.hamburger {
  font-size: 19px;
  color: #4a5562;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: background 0.15s;
}

.hamburger:hover {
  background: #f2f4f8;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.mock-tag {
  letter-spacing: 0.5px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 9px;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 20px;
  transition: background 0.15s;
  outline: none;
}

.user-trigger:hover {
  background: #f2f4f8;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2f6bff, #6b93ff);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-meta .name {
  font-size: 13px;
  font-weight: 600;
  color: var(--crm-text);
}

.user-meta .sub {
  font-size: 11px;
  color: var(--crm-text-secondary);
  margin-top: 2px;
}

.arrow {
  color: #9aa5b1;
  font-size: 12px;
}
</style>
