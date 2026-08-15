<script setup>
import { RouterLink, RouterView } from 'vue-router'
import IdentityBar from './IdentityBar.vue'
import TraceStrip from './TraceStrip.vue'
import StatsAside from './StatsAside.vue'
</script>

<template>
  <div class="shell">
    <header class="shell-header">
      <div class="shell-brand">
        <strong class="brand-title">ERP AI 学习控制台</strong>
        <small>纯学习 · Vue 3 · 不接生产</small>
      </div>
      <nav class="shell-nav" aria-label="主面板">
        <RouterLink to="/chat">Chat</RouterLink>
        <RouterLink to="/rag">RAG</RouterLink>
        <RouterLink to="/flow">Flow</RouterLink>
        <RouterLink to="/eval">Eval</RouterLink>
      </nav>
      <IdentityBar />
    </header>

    <TraceStrip />

    <div class="shell-body">
      <main class="shell-main">
        <RouterView />
      </main>
      <aside class="shell-aside" aria-label="侧栏统计">
        <StatsAside />
      </aside>
    </div>
  </div>
</template>
