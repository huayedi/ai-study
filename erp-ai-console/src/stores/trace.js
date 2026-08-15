import { defineStore } from 'pinia'

export const useTraceStore = defineStore('trace', {
  state: () => ({
    lastTraceId: '',
    lastMeta: null,
    feedbackCount: 0,
    chatSessionId: localStorage.getItem('erp-ai-console.sessionId') || '',
  }),
  actions: {
    setLast(traceId, meta) {
      this.lastTraceId = traceId || ''
      this.lastMeta = meta || null
    },
    setSessionId(id) {
      this.chatSessionId = id || ''
      if (id) localStorage.setItem('erp-ai-console.sessionId', id)
      else localStorage.removeItem('erp-ai-console.sessionId')
    },
    bumpFeedback() {
      this.feedbackCount += 1
    },
  },
})
