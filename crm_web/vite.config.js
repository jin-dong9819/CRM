import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  const apiTarget = env.VITE_API_TARGET || 'http://localhost:8080'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      host: '0.0.0.0',
      port: 5173,
      open: true,
      proxy: {
        // 前端统一以 /api 开头，代理到 Spring Boot 服务
        '/api': {
          target: apiTarget,
          changeOrigin: true,
          // 后端没启动时代理默认抛 500，前端只会看到 "Request failed with status code 500"
          // 这里接管错误，返回明确提示，避免误判成代码 bug
          configure: (proxy) => {
            proxy.on('error', (err, req, res) => {
              const body = JSON.stringify({
                code: 502,
                msg: `后端服务未启动或不可达（${apiTarget}），请先运行 mvn spring-boot:run`,
                data: null
              })
              if (res && typeof res.writeHead === 'function' && !res.headersSent) {
                res.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' })
                res.end(body)
              }
            })
          }
        }
      }
    },
    build: {
      outDir: 'dist',
      chunkSizeWarningLimit: 1500
    }
  }
})
