import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/voiceclone',
  },
  {
    path: '/voiceclone',
    name: 'voiceclone',
    component: () => import('../pages/voiceclone/index.vue'),
  },
  /** 与线上网关路径一致：/tool/voiceCloning（全小写 /voiceclone 无法匹配该 URL ） */
  {
    path: '/voiceCloning',
    name: 'voiceCloning',
    component: () => import('../pages/voiceclone/index.vue'),
  },
  {
    path: '/voicedesign',
    name: 'voicedesign',
    component: () => import('../pages/voicedesign/index.vue'),
  },
  /** 与线上路径一致：/tool/voiceDesign */
  {
    path: '/voiceDesign',
    name: 'voiceDesign',
    component: () => import('../pages/voicedesign/index.vue'),
  },
]

const router = createRouter({
  history: createWebHistory('/tool/'),
  base: '/tool/',
  routes,
})

export default router
