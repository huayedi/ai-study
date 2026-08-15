import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/chat' },
  {
    path: '/chat',
    name: 'chat',
    component: () => import('@/views/ChatView.vue'),
    meta: { title: 'Chat' },
  },
  {
    path: '/rag',
    name: 'rag',
    component: () => import('@/views/RagView.vue'),
    meta: { title: 'RAG' },
  },
  {
    path: '/flow',
    name: 'flow',
    component: () => import('@/views/FlowView.vue'),
    meta: { title: 'Flow' },
  },
  {
    path: '/eval',
    name: 'eval',
    component: () => import('@/views/EvalView.vue'),
    meta: { title: 'Eval' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.afterEach((to) => {
  document.title = `${to.meta.title || 'Console'} · ERP AI 学习控制台`
})

export default router
