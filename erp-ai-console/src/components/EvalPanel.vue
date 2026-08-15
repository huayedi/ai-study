<script setup>
import { ref } from 'vue'
import { aiApi } from '@/api/http'
import { useAsyncState } from '@/composables/useAsyncState'

const suite = ref('rag')
const run = ref(null)
const runIds = ref([])
const { loading, error, run: runAsync } = useAsyncState()

async function startEval() {
  run.value = null
  await runAsync(async () => {
    const data = await aiApi.evalRun({ suite: suite.value })
    run.value = data
    await refreshList()
    return data
  })
}

async function refreshList() {
  try {
    const data = await aiApi.evalRuns()
    runIds.value = Array.isArray(data) ? data : data?.ids || data?.runs || []
  } catch {
    runIds.value = []
  }
}

async function openRun(id) {
  await runAsync(async () => {
    const data = await aiApi.evalRunGet(id)
    run.value = data
    return data
  })
}

const results = () => run.value?.results || []
</script>

<template>
  <section class="panel" aria-labelledby="eval-title">
    <h2 id="eval-title">Eval</h2>
    <p class="hint">跑题集回归。后端未实现时会报错——属预期，前端已预留面板。</p>

    <div class="row">
      <div class="field" style="margin: 0; min-width: 10rem">
        <label for="suite">suite</label>
        <select id="suite" v-model="suite">
          <option value="rag">rag</option>
          <option value="chat">chat</option>
          <option value="flow">flow</option>
        </select>
      </div>
      <button class="btn btn-primary" :disabled="loading" @click="startEval">Run</button>
      <button class="btn" :disabled="loading" @click="refreshList">刷新 runs</button>
    </div>

    <div v-if="error" class="state-box error" style="margin-top: 1rem">{{ error }}</div>

    <div v-if="runIds.length" style="margin-top: 1rem">
      <div class="row" style="flex-wrap: wrap">
        <button
          v-for="id in runIds"
          :key="id"
          class="btn mono"
          style="font-size: 0.75rem"
          @click="openRun(typeof id === 'string' ? id : id.runId || id.id)"
        >
          {{ typeof id === 'string' ? id : id.runId || id.id }}
        </button>
      </div>
    </div>

    <div v-if="run" style="margin-top: 1rem">
      <div class="row" style="margin-bottom: 0.5rem">
        <span class="badge ok">passed {{ run.passed }}/{{ run.total }}</span>
        <span class="badge mono">{{ run.runId || run.id }}</span>
      </div>
      <div class="table-wrap">
        <table class="data">
          <thead>
            <tr>
              <th>caseId</th>
              <th>passed</th>
              <th>reasons</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in results()" :key="r.caseId">
              <td class="mono">{{ r.caseId }}</td>
              <td>{{ r.passed ? '✓' : '✗' }}</td>
              <td>{{ (r.reasons || []).join('; ') }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>
