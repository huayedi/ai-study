<script setup>
import { onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { aiApi } from '@/api/http'
import { useTraceStore } from '@/stores/trace'
import { useIdentityStore } from '@/stores/identity'

const stats = ref(null)
const err = ref('')
const trace = useTraceStore()
const identity = useIdentityStore()
const { lastTraceId, feedbackCount, chatSessionId } = storeToRefs(trace)

async function refresh() {
  err.value = ''
  try {
    stats.value = await aiApi.stats()
  } catch (e) {
    err.value = e.message
    stats.value = null
  }
}

onMounted(refresh)
</script>

<template>
  <div class="aside-card">
    <h3>本次前端会话</h3>
    <div class="stat-line"><span>角色</span><span>{{ identity.roles }}</span></div>
    <div class="stat-line"><span>租户</span><span>{{ identity.tenantId }}</span></div>
    <div class="stat-line"><span>session</span><span>{{ chatSessionId || '—' }}</span></div>
    <div class="stat-line"><span>trace</span><span>{{ lastTraceId || '—' }}</span></div>
    <div class="stat-line"><span>feedback</span><span>{{ feedbackCount }}</span></div>
  </div>

  <div class="aside-card">
    <h3>/api/ai/stats</h3>
    <button class="btn" style="margin-bottom: 0.5rem" @click="refresh">刷新</button>
    <div v-if="err" class="meta" style="color: var(--color-muted); font-size: 0.8125rem">
      {{ err }}
    </div>
    <template v-else-if="stats">
      <div v-for="(v, k) in stats" :key="k" class="stat-line">
        <span>{{ k }}</span>
        <span>{{ v }}</span>
      </div>
    </template>
    <div v-else class="meta" style="color: var(--color-muted); font-size: 0.8125rem">暂无数据</div>
  </div>

  <div class="aside-card">
    <h3>纪律</h3>
    <p style="margin: 0; font-size: 0.8125rem; color: var(--color-muted)">
      本控制台仅学习演示。APPROVE ≠ 写库；不接公司生产数据。
    </p>
  </div>
</template>
