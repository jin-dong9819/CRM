<template>
  <div class="not-found">
    <div class="code">404</div>
    <h2>页面走丢了</h2>
    <p>你访问的页面不存在，或者已经被移动到别处。</p>
    <el-button type="primary" @click="$router.replace('/')">返回首页</el-button>
  </div>
</template>

<script setup>
// 404 兜底页
</script>

<style scoped>
.not-found {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: #f4f6fa;
  color: var(--crm-text);
}

.code {
  font-size: 96px;
  font-weight: 800;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #2f6bff, #8fb4ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  line-height: 1;
}

h2 {
  margin: 12px 0 0;
  font-size: 22px;
}

p {
  margin: 0 0 18px;
  color: var(--crm-text-secondary);
  font-size: 14px;
}
</style>
