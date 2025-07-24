import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import ElTableInfiniteScroll from 'el-table-infinite-scroll'

import App from './App.vue'
import router from './router'

const app = createApp(App)
app.directive('el-table-infinite-scroll', ElTableInfiniteScroll)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
