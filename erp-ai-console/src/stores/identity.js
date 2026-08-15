import { defineStore } from 'pinia'

const KEY = 'erp-ai-console.identity'

export const useIdentityStore = defineStore('identity', {
  state: () => ({
    userId: 'demo-user',
    roles: 'FINANCE',
    tenantId: 'tenant-a',
    adminToken: '',
  }),
  actions: {
    hydrateFromStorage() {
      try {
        const raw = localStorage.getItem(KEY)
        if (!raw) return
        const data = JSON.parse(raw)
        Object.assign(this, {
          userId: data.userId || this.userId,
          roles: data.roles || this.roles,
          tenantId: data.tenantId || this.tenantId,
          adminToken: data.adminToken || '',
        })
      } catch {
        /* ignore */
      }
    },
    persist() {
      localStorage.setItem(
        KEY,
        JSON.stringify({
          userId: this.userId,
          roles: this.roles,
          tenantId: this.tenantId,
          adminToken: this.adminToken,
        }),
      )
    },
    update(partial) {
      Object.assign(this, partial)
      this.persist()
    },
  },
})
