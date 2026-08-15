import { ref } from 'vue'

export function useAsyncState() {
  const loading = ref(false)
  const error = ref('')
  const empty = ref(false)

  async function run(fn) {
    loading.value = true
    error.value = ''
    empty.value = false
    try {
      const result = await fn()
      return result
    } catch (e) {
      error.value = e?.message || String(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  return { loading, error, empty, run }
}
