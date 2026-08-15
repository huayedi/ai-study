import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 开发：把 /api 代理到 Spring Boot，避免浏览器跨域
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  // 若 build 后拷到 Spring static/ 根目录，base 用 '/'
  base: '/',
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
})
