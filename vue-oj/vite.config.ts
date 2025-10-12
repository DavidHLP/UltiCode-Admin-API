import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// import vueDevTools from 'vite-plugin-vue-devtools'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // vueDevTools(),
    Components({
      resolvers: [
        ElementPlusResolver({
          importStyle: false, // 如果您需要自定义主题，请设置为true
        }),
        AntDesignVueResolver({
          importStyle: false, // 如果您需要自定义主题，请设置为true
        }),
      ],
      dts: true, // 自动生成组件类型声明文件
      include: [/\.vue$/, /\.vue\?vue/, /\.md$/],
    }),
    // verbatimModulesyntax(),
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
      '/api/problems': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
      '/judge/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
      '/solutions/api/view': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
      '/submissions/api/view': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
    },
  },
})
