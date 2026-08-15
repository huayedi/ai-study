<script setup>
import { onMounted, onUnmounted } from 'vue'
import AppShell from './components/AppShell.vue'
import { useIdentityStore } from './stores/identity'

const identity = useIdentityStore()

function onKey(e) {
  // Ctrl/Cmd+K 聚焦当前面板主输入（由各面板监听自定义事件）
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault()
    window.dispatchEvent(new CustomEvent('console:focus-input'))
  }
}

onMounted(() => {
  identity.hydrateFromStorage()
  window.addEventListener('keydown', onKey)
})
onUnmounted(() => window.removeEventListener('keydown', onKey))
</script>

<template>
  <AppShell />
</template>
