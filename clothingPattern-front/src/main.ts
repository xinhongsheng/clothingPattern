

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

import DataVVue3 from '@kjgl77/datav-vue3'
const app = createApp(App)
app.use(Antd)
app.use(createPinia())
app.use(router)
app.use(DataVVue3)

app.mount('#app')
