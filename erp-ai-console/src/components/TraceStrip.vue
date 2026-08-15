<script setup>
import { storeToRefs } from 'pinia'
import { useTraceStore } from '@/stores/trace'
import { useIdentityStore } from '@/stores/identity'

const trace = useTraceStore()
const identity = useIdentityStore()
const { lastTraceId, lastMeta } = storeToRefs(trace)
</script>

<template>
  <div class="trace-strip" role="status">
    <span class="label">最近 traceId</span>
    <code class="mono">{{ lastTraceId || '（尚无请求）' }}</code>
    <span v-if="lastMeta" class="label mono">
      {{ lastMeta.path }} · HTTP {{ lastMeta.status }}
    </span>
    <span class="badge">{{ identity.roles }} @ {{ identity.tenantId }}</span>
  </div>
</template>
