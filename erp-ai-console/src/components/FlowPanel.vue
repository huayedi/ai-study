<script setup>
import { computed, ref } from 'vue'
import { aiApi } from '@/api/http'
import { useAsyncState } from '@/composables/useAsyncState'
import FeedbackBar from './FeedbackBar.vue'

const question = ref('采购主链路有哪些单据？')
const current = ref(null)
const pending = ref([])
const editedAnswer = ref('')
const note = ref('')
const { loading, error, run } = useAsyncState()

const state = computed(() => current.value?.state || current.value?.flowState || '')

async function start() {
  await run(async () => {
    const data = await aiApi.flowStart({ question: question.value })
    current.value = data
    await refreshPending()
    return data
  })
}

async function refreshPending() {
  try {
    const list = await aiApi.flowList('WAIT_HUMAN')
    pending.value = Array.isArray(list) ? list : list?.items || list?.content || []
  } catch {
    pending.value = []
  }
}

async function decide(decision) {
  const id = current.value?.flowId || current.value?.id
  if (!id) return
  await run(async () => {
    const body = { decision, note: note.value }
    if (decision === 'EDIT') body.editedAnswer = editedAnswer.value
    const data = await aiApi.flowDecide(id, body)
    current.value = data
    await refreshPending()
    return data
  })
}

async function openItem(item) {
  const id = item.flowId || item.id
  await run(async () => {
    const data = await aiApi.flowGet(id)
    current.value = data
    return data
  })
}
</script>

<template>
  <section class="panel" aria-labelledby="flow-title">
    <h2 id="flow-title">Flow · HITL</h2>
    <p class="hint">
      APPROVE / EDIT / REJECT 都只是对人建议的决策，<strong>不等于写 ERP / 过账</strong>。
    </p>

    <div class="field">
      <label for="flow-q">问题</label>
      <textarea id="flow-q" v-model="question" />
    </div>
    <div class="row">
      <button class="btn btn-primary" :disabled="loading" @click="start">Start</button>
      <button class="btn" :disabled="loading" @click="refreshPending">刷新待确认</button>
    </div>

    <div v-if="error" class="state-box error" style="margin-top: 1rem">
      {{ error }}
      <div class="hint" style="margin-top: 0.5rem">
        若后端尚未实现 /api/ai/flow/*，请按第 2～4 月教材补齐后再演示。
      </div>
    </div>

    <div v-if="pending.length" style="margin-top: 1rem">
      <h3 style="font-size: 0.95rem; margin: 0 0 0.5rem">WAIT_HUMAN 队列</h3>
      <ul class="sources">
        <li v-for="(item, i) in pending" :key="i">
          <div class="mono">{{ item.flowId || item.id }}</div>
          <div class="meta">{{ item.userQuestion || item.question || '' }}</div>
          <button class="btn" style="margin-top: 0.35rem" @click="openItem(item)">打开</button>
        </li>
      </ul>
    </div>

    <div v-if="current" style="margin-top: 1rem">
      <div class="row" style="margin-bottom: 0.5rem">
        <span class="badge">state: {{ state || '（未知）' }}</span>
        <span class="badge mono">{{ current.flowId || current.id }}</span>
      </div>
      <pre class="answer">{{ current.draftAnswer || current.editedAnswer || current.reply?.answer || JSON.stringify(current, null, 2) }}</pre>

      <p class="disclaimer">人工确认的是助手建议，不是授权系统去改账或改库存。</p>

      <div class="field" style="margin-top: 1rem">
        <label>EDIT 正文（仅 EDIT 需要）</label>
        <textarea v-model="editedAnswer" />
      </div>
      <div class="field">
        <label>备注 note</label>
        <input v-model="note" />
      </div>
      <div class="row">
        <button class="btn btn-primary" :disabled="loading || state !== 'WAIT_HUMAN'" @click="decide('APPROVE')">
          APPROVE
        </button>
        <button class="btn" :disabled="loading || state !== 'WAIT_HUMAN'" @click="decide('EDIT')">EDIT</button>
        <button class="btn btn-danger" :disabled="loading || state !== 'WAIT_HUMAN'" @click="decide('REJECT')">
          REJECT
        </button>
      </div>
      <FeedbackBar :trace-id="current.traceId" />
    </div>
  </section>
</template>
