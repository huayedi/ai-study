<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { aiApi } from '@/api/http'
import { useAsyncState } from '@/composables/useAsyncState'
import { useTraceStore } from '@/stores/trace'
import FeedbackBar from './FeedbackBar.vue'

const message = ref('创建采购订单应该如何去设计')
const inputEl = ref(null)
const result = ref(null)
const { loading, error, run } = useAsyncState()
const trace = useTraceStore()

async function send() {
  result.value = null
  await run(async () => {
    const data = await aiApi.chat({
      sessionId: trace.chatSessionId || undefined,
      message: message.value,
    })
    result.value = data
    if (data?.sessionId) trace.setSessionId(data.sessionId)
    return data
  })
}

function clearSession() {
  trace.setSessionId('')
  result.value = null
}

function focusInput() {
  inputEl.value?.focus()
}

onMounted(() => window.addEventListener('console:focus-input', focusInput))
onUnmounted(() => window.removeEventListener('console:focus-input', focusInput))
</script>

<template>
  <section class="panel" aria-labelledby="chat-title">
    <h2 id="chat-title">Chat</h2>
    <p class="hint">多轮请保留 sessionId。Ctrl/Cmd+K 聚焦输入框。</p>

    <div class="field">
      <label for="chat-msg">消息</label>
      <textarea id="chat-msg" ref="inputEl" v-model="message" @keydown.ctrl.enter="send" />
    </div>

    <div class="row">
      <button class="btn btn-primary" :disabled="loading || !message.trim()" @click="send">
        {{ loading ? '发送中…' : '发送' }}
      </button>
      <button class="btn btn-ghost" type="button" @click="clearSession">清空会话</button>
      <span class="badge mono">session: {{ trace.chatSessionId || '（新会话）' }}</span>
    </div>

    <div v-if="error" class="state-box error" style="margin-top: 1rem">{{ error }}</div>

    <div v-if="result" style="margin-top: 1rem">
      <div class="row" style="margin-bottom: 0.5rem">
        <span v-if="result.reply?.needHuman" class="badge warn">needHuman</span>
        <span class="badge mono">{{ result.model }}</span>
        <span class="badge mono">{{ result.latencyMs }} ms</span>
      </div>
      <pre class="answer">{{ result.reply?.answer || JSON.stringify(result.reply, null, 2) }}</pre>
      <FeedbackBar :trace-id="result.traceId" />
    </div>
  </section>
</template>
