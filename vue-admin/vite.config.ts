import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(
    ),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/user/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
      '/role/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
      '/api/auth': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      }
    }
  }
})
