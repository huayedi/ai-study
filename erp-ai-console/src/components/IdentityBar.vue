<script setup>
import { storeToRefs } from 'pinia'
import { useIdentityStore } from '@/stores/identity'

const identity = useIdentityStore()
const { userId, roles, tenantId } = storeToRefs(identity)

function save() {
  identity.persist()
}
</script>

<template>
  <div class="identity-bar" aria-label="学习身份头">
    <div class="field">
      <label for="uid">X-User-Id</label>
      <input id="uid" v-model="userId" @change="save" />
    </div>
    <div class="field">
      <label for="roles">X-Roles</label>
      <select id="roles" v-model="roles" @change="save">
        <option value="GUEST">GUEST</option>
        <option value="FINANCE">FINANCE</option>
        <option value="PROCUREMENT">PROCUREMENT</option>
        <option value="ADMIN">ADMIN</option>
        <option value="FINANCE,PROCUREMENT">FINANCE,PROCUREMENT</option>
      </select>
    </div>
    <div class="field">
      <label for="tenant">X-Tenant-Id</label>
      <select id="tenant" v-model="tenantId" @change="save">
        <option value="tenant-a">tenant-a</option>
        <option value="tenant-b">tenant-b</option>
        <option value="learning">learning</option>
      </select>
    </div>
  </div>
</template>
