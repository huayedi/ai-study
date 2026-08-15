<script setup>
import { ref } from 'vue'
import { aiApi } from '@/api/http'
import { useTraceStore } from '@/stores/trace'

const props = defineProps({
  traceId: { type: String, default: '' },
})

const msg = ref('')
const busy = ref(false)
const trace = useTraceStore()

async function send(kind) {
  const id = props.traceId || trace.lastTraceId
  if (!id) {
    msg.value = '没有 traceId，请先发起 Chat/RAG/Flow 请求'
    return
  }
  busy.value = true
  msg.value = ''
  try {
    await aiApi.feedback({ traceId: id, kind, note: '' })
    trace.bumpFeedback()
    msg.value = `已反馈：${kind}`
  } catch (e) {
    msg.value = `反馈失败（后端可能尚未实现）：${e.message}`
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <div class="row" style="margin-top: 0.75rem">
    <span class="label" style="color: var(--color-muted); font-size: 0.8125rem">反馈</span>
    <button class="btn" :disabled="busy" @click="send('useful')">useful</button>
    <button class="btn" :disabled="busy" @click="send('wrong')">wrong</button>
    <button class="btn btn-danger" :disabled="busy" @click="send('unsafe')">unsafe</button>
    <span class="mono" style="color: var(--color-muted)">{{ msg }}</span>
  </div>
</template>
