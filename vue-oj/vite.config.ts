import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    Components({
      resolvers: [
        ElementPlusResolver({
          importStyle: false, // 如果您需要自定义主题，请设置为true
        }),
      ],
      dts: true, // 自动生成组件类型声明文件
      include: [/\.vue$/, /\.vue\?vue/, /\.md$/],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    proxy: {
      '/api/auth': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
      '/question-bank/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
    },
  },
})
