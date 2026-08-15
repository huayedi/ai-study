import { useIdentityStore } from '@/stores/identity'
import { useTraceStore } from '@/stores/trace'

export class ApiError extends Error {
  constructor(message, { status, body } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

function buildHeaders(extra = {}) {
  const identity = useIdentityStore()
  const headers = {
    'Content-Type': 'application/json',
    'X-User-Id': identity.userId || 'demo-user',
    'X-Roles': identity.roles || 'GUEST',
    'X-Tenant-Id': identity.tenantId || 'tenant-a',
    ...extra,
  }
  if (identity.adminToken) {
    headers['X-Admin-Token'] = identity.adminToken
  }
  return headers
}

/**
 * 统一 fetch：自动带学习头，记录最近 traceId
 */
export async function apiFetch(path, options = {}) {
  const { method = 'GET', body, headers: extraHeaders } = options
  const res = await fetch(path, {
    method,
    headers: buildHeaders(extraHeaders),
    body: body == null ? undefined : JSON.stringify(body),
  })

  const text = await res.text()
  let data = null
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = { raw: text }
    }
  }

  const traceId = res.headers.get('X-Trace-Id') || data?.traceId
  if (traceId) {
    useTraceStore().setLast(traceId, {
      path,
      status: res.status,
      at: Date.now(),
    })
  }

  if (!res.ok) {
    const msg =
      data?.message ||
      data?.error ||
      data?.raw ||
      `HTTP ${res.status} ${res.statusText}`
    throw new ApiError(String(msg), { status: res.status, body: data })
  }
  return data
}

export const aiApi = {
  chat(payload) {
    return apiFetch('/api/ai/chat', { method: 'POST', body: payload })
  },
  ragAsk(payload) {
    return apiFetch('/api/ai/rag/ask', { method: 'POST', body: payload })
  },
  flowStart(payload) {
    return apiFetch('/api/ai/flow/start', { method: 'POST', body: payload })
  },
  flowGet(id) {
    return apiFetch(`/api/ai/flow/${encodeURIComponent(id)}`)
  },
  flowList(state = 'WAIT_HUMAN') {
    const q = state ? `?state=${encodeURIComponent(state)}` : ''
    return apiFetch(`/api/ai/flow${q}`)
  },
  flowDecide(id, payload) {
    return apiFetch(`/api/ai/flow/${encodeURIComponent(id)}/decide`, {
      method: 'POST',
      body: payload,
    })
  },
  feedback(payload) {
    return apiFetch('/api/ai/feedback', { method: 'POST', body: payload })
  },
  evalRun(payload) {
    return apiFetch('/api/ai/eval/run', { method: 'POST', body: payload })
  },
  evalRuns() {
    return apiFetch('/api/ai/eval/runs')
  },
  evalRunGet(id) {
    return apiFetch(`/api/ai/eval/runs/${encodeURIComponent(id)}`)
  },
  stats() {
    return apiFetch('/api/ai/stats')
  },
}
