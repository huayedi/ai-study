<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { aiApi } from '@/api/http'
import { useAsyncState } from '@/composables/useAsyncState'
import FeedbackBar from './FeedbackBar.vue'

const question = ref('采购主链路有哪些单据？')
const inputEl = ref(null)
const result = ref(null)
const { loading, error, run } = useAsyncState()

async function ask() {
  result.value = null
  await run(async () => {
    const data = await aiApi.ragAsk({ question: question.value })
    result.value = data
    return data
  })
}

function focusInput() {
  inputEl.value?.focus()
}
onMounted(() => window.addEventListener('console:focus-input', focusInput))
onUnmounted(() => window.removeEventListener('console:focus-input', focusInput))
</script>

<template>
  <section class="panel" aria-labelledby="rag-title">
    <h2 id="rag-title">RAG</h2>
    <p class="hint">sources 必须来自检索；切换顶部角色/租户可观察可见范围变化（后端支持时）。</p>

    <div class="field">
      <label for="rag-q">问题</label>
      <textarea id="rag-q" ref="inputEl" v-model="question" @keydown.ctrl.enter="ask" />
    </div>

    <div class="row">
      <button class="btn btn-primary" :disabled="loading || !question.trim()" @click="ask">
        {{ loading ? '检索中…' : '提问' }}
      </button>
    </div>

    <div v-if="error" class="state-box error" style="margin-top: 1rem">{{ error }}</div>

    <div v-if="result" style="margin-top: 1rem">
      <div class="row" style="margin-bottom: 0.5rem">
        <span v-if="result.reply?.needHuman" class="badge warn">needHuman</span>
        <span class="badge mono">{{ result.latencyMs }} ms</span>
        <span class="badge">sources {{ result.sources?.length || 0 }}</span>
      </div>
      <pre class="answer">{{ result.reply?.answer || JSON.stringify(result.reply, null, 2) }}</pre>

      <ul v-if="result.sources?.length" class="sources">
        <li v-for="(s, i) in result.sources" :key="i">
          <div class="mono">{{ s.docId }} · {{ s.section || '（无章节）' }}</div>
          <div class="meta">score={{ s.score }}</div>
          <div v-if="s.excerpt">{{ s.excerpt }}</div>
        </li>
      </ul>
      <div v-else class="state-box" style="margin-top: 1rem">无 sources（空命中或未返回）</div>

      <FeedbackBar :trace-id="result.traceId" />
    </div>
  </section>
</template>
