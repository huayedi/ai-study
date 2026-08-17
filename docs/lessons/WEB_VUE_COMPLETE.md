# Vue 3 Web 前端完整教材：ERP AI 学习控制台

> **重要说明：** 本文取代旧的「WEB-D1～WEB-D30 逐日合订」结构。这是一份**连续章节式**完整教材，不是按天拆分的 30 天课程，也不是大纲摘要。
>
> **定位：** 纯学习；通用 ERP AI 助手**学习控制台**前端教材；**不接公司生产、不自动过账/改库存。**
>
> **技术栈（固定）：**
> - **Vue 3** + **Vite** + **`<script setup>`** Composition API
> - 推荐：**Vue Router**（面板作路由）、**Pinia**（身份头 / session）
> - 纯 CSS 或 **scoped CSS** + **CSS 变量设计令牌**（**不要求** Element Plus 等 UI 库）
> - **开发：** Vite dev server + `server.proxy` → Spring Boot `http://localhost:8080`，代理前缀 `/api`
> - **生产学习路径：** `vite build` 产物可拷到 `erp-ai-assistant/src/main/resources/static/` **或** `vite preview` 独立预览
>
> **调用学习期 REST：** `/api/ai/chat`、`/api/ai/rag/ask`、`/api/ai/flow/*`、`/api/ai/eval/*`、`/api/ai/stats`、`/api/ai/feedback`
>
> **学习请求头：** `X-User-Id` / `X-Roles` / `X-Tenant-Id`（可选 `X-Admin-Token`、`X-Trace-Id`）
>
> **约定：** 代码骨架在 Markdown 中，粘贴到 `erp-ai-console/`；**勿**改 `erp-ai-assistant/` 应用代码。入口：[docs/WEB.md](../WEB.md)
>
> **视觉纪律：** 避免「AI 紫 + 奶油白」模板审美；用**有性格的字体系**与**清晰层级**；控制台可比营销页更密，但**一屏一事**；少卡片堆砌。
>
> **口述标准答案：** [ORAL_ANSWERS.md](../ORAL_ANSWERS.md#web第19章-口述提纲标准答法)（第19章自述/自检亦有就地答法）

---

## 目录

1. [定位与总目标（可演示的学习控制台）](#1-定位与总目标可演示的学习控制台)
2. [技术栈与工程目录（Vite/Vue3/Router/Pinia）](#2-技术栈与工程目录vitevue3routerpinia)
3. [开发联调：proxy、base、两种交付路径](#3-开发联调proxybase两种交付路径static-拷贝--preview)
4. [设计令牌与视觉纪律](#4-设计令牌与视觉纪律字体层级避免-ai-紫奶油模板)
5. [App 壳与布局](#5-app-壳与布局身份条--主区--侧栏)
6. [HTTP 层](#6-http-层apihttpjs学习请求头traceid错误模型)
7. [Pinia：identity / session](#7-piniainentity--session-状态)
8. [Chat 面板](#8-chat-面板sessionidjson-答案needhuman)
9. [RAG 面板](#9-rag-面板sources-列表gate-文案展示)
10. [Loading / 空态 / 错误态](#10-loading--空态--错误态模式)
11. [Flow HITL](#11-flow-hitlstart列表approverejectedit文案-approve写库)
12. [Feedback](#12-feedbackusefulwrongunsafe--traceid)
13. [Eval 与 stats](#13-eval-与-stats-面板)
14. [路由整理与导航](#14-路由整理与导航)
15. [持久化、快捷键、轻动效、响应式](#15-持久化快捷键轻动效响应式)
16. [可访问性与安全](#16-可访问性与安全焦点对比度xssv-html)
17. [构建发布检查清单](#17-构建发布检查清单)
18. [端到端彩排剧本](#18-端到端彩排剧本角色切换--反馈--租户)
19. [作品集与口述提纲](#19-作品集与口述提纲)
20. [进阶与明确不做](#20-进阶与明确不做)
- [附录 A：完整文件树](#附录-a完整文件树)
- [附录 B：tokens 参考](#附录-btokens-参考)
- [附录 C：API 对照表](#附录-capi-对照表)
- [附录 D：FAQ](#附录-dfaq)
- [附录 E：与后端月度教材交叉索引](#附录-e与后端月度教材交叉索引)
- [附录 F：修订记录](#附录-f修订记录)

---


# 1. 定位与总目标（可演示的学习控制台）

## 为什么

后端月份教材教你 Java、RAG、Flow、ACL、多租户——评审与同伴往往先在**浏览器**里判断你是否理解系统。Web 轨道不是再讲 Spring Boot，而是用 **Vue 3** 把仓库学习期 REST 变成**可读、可演示、可排障**的控制台。

演示时观众每秒在问：这是真联调吗？权限有效吗？出错能排吗？会不会动生产？本章建立「可演示学习控制台」的总目标。

## 概念

## Web 轨道总目标（学完应能对外讲 8～10 分钟）

1. **工程起步：** Vite + Vue 3 SFC、响应式 `ref`/`computed`、设计令牌、App 壳布局、a11y、代理连后端。  
2. **对接后端：** `api/http.js` + composable、`Chat`/`RAG` 组件、sources 与 traceId、loading/错误态。  
3. **治理 UI：** Pinia 身份头、Flow start/decide、Feedback 三键、Eval 表、stats 侧栏、换角色看 sources。  
4. **作品交付：** 持久化、快捷键、轻动效、响应式、Router 整理、`vite build` 进 static、彩排、截图、口述收官。  
5. **工程纪律：** 开发用 proxy、生产注意 `base` 路径、不新开写库魔法接口、XSS 与 `v-html`、APPROVE≠写库。


### 学习控制台 vs 生产 ERP 前台

| 维度 | `erp-ai-console/` | 公司生产前台 |
|------|-------------------|--------------|
| 目的 | 演示 AI 能力边界 | 真实业务办理 |
| 身份 | Pinia 模拟请求头 | SSO / 统一认证 |
| 数据 | 学习期 Mock / 样本库 | 真实主数据 |
| 写操作 | Flow APPROVE 仅改学习状态 | 可能触发过账 |
| 代码位置 | 独立目录，讲义粘贴 | 公司代码库 |

### 与后端月份的关系

```text
第1月  懂 Chat/RAG API 与 JSON 响应
第2月  懂 Flow HITL、Eval、stats（可选并行）
第3月  懂 Store/reindex/audit（前端只调 REST）
第4月  懂 ACL/反馈/多租户（前端用 Pinia 模拟头）
第5月  可选并行（治理深化）
Web轨  用 Vue 3 页面把上述能力「可演示」
```

## 怎么做

**推荐章节顺序（连续阅读，非按天）：**

| 阶段 | 章节 | 结束产出 |
|------|------|----------|
| 基础 | 1～5 | `npm run dev` 可开壳 + proxy 通 |
| 对接 | 6～10 | Chat/RAG 双面板联调成功 |
| 治理 | 7、11～13 | 换角色 sources 变、Flow/Feedback |
| 交付 | 14～20 | Router、build、彩排、作品集 |

**环境：**

```bash
# 终端 A（仓库根）
cd erp-ai-assistant && mvn spring-boot:run

# 终端 B（你创建的目录）
cd erp-ai-console && npm run dev
```

**每章建议节奏：** 读为什么/概念（20 min）→ 粘贴骨架（45～60 min）→ Network 检查（15 min）→ 本章自检（10 min）。

## 可复制代码骨架

```text
# 笔记目录树（今天只画，明天创建）
erp-ai-console/
  src/App.vue
  src/styles/tokens.css
  src/components/AppShell.vue
```

```vue
<!-- 心智：最终 App.vue 组合壳 + 路由视图 -->
<template>
  <AppShell>
    <template #main>路由视图占位</template>
  </AppShell>
</template>
```

## 常见坑

| 坑 | 现象 | 处理 |
|----|------|------|
| CORS | 直连 `localhost:8080` | Vite proxy，路径写 `/api/...` |
| `ref` 忘 `.value` | script 里 undefined | `count.value++`；模板自动解包 |
| `v-html` XSS | 模型输出执行脚本 | 默认文本插值；sources 用 `v-for` |
| proxy 后端未启 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| scoped 不生效 | 样式未命中子树 | `:deep()` 或全局 tokens |
| Pinia setup 外调用 | getActivePinia 报错 | 在 `apiFetch` 内 `useIdentityStore()` |
| `base` 错误 | build 后资源 404 | static 拷贝用 `base: './'` |

| 改 `erp-ai-assistant/` | PR 污染学习后端 | 只在 `erp-ai-console/` 开发 |
| 演示无 Network | 观众不信联调 | F12 展示 proxy 200 |

## 本章自检

- [ ] 能一句话说清学习控制台与生产前台的区别
- [ ] 知道目录名是 `erp-ai-console/`，不碰 `erp-ai-assistant/` 应用代码
- [ ] 能列出 Chat/RAG/Flow/Feedback/Eval（或 Stats）五个演示能力
- [ ] 能口述 Web 轨道与后端月份的分工
- [ ] 前后端可同时运行

---


# 2. 技术栈与工程目录（Vite/Vue3/Router/Pinia）

## 为什么

第 1 周主题增量：**SPA/SFC/Vite 心智与线框**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：浏览器、Vue 3 学习控制台与 Vite 心智。

第 1 周主题增量：**npm create vite、main.js 挂载**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：创建 Vite + Vue 3 工程与首屏。

第 1 周主题增量：**template/script/style 三区、setup 语法糖**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：单文件组件 SFC 与 `<script setup>` 结构。

第 1 周主题增量：**ref 解包、表单 v-model**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：`ref`、`reactive` 与模板插值。

## 概念

### 技术栈职责

| 层 | 选型 | 职责 |
|----|------|------|
| UI 框架 | Vue 3 | 响应式视图、组件化 |
| 构建 | Vite | 开发服务器、HMR、生产打包 |
| 组件语法 | `<script setup>` | Composition API，少样板代码 |
| 路由 | Vue Router 4 | 面板 URL、浏览器前进后退 |
| 状态 | Pinia | 身份头、trace、sessionId |
| 样式 | CSS 变量 + scoped | 设计令牌、组件局部样式 |

### 推荐项目目录

```text
erp-ai-console/          # 建议独立前端目录（学习仓根下），勿与公司项目混淆
  package.json
  vite.config.js
  index.html
  src/
    main.js
    App.vue
    styles/
      tokens.css
      layout.css
      components.css
    api/
      http.js
    stores/
      identity.js
      trace.js
    composables/
      useApiHeaders.js
    components/
      AppShell.vue
      TraceStrip.vue
      ChatPanel.vue
      RagPanel.vue
      FlowPanel.vue
      FeedbackBar.vue
      EvalPanel.vue
      StatsAside.vue
      IdentityBar.vue
    views/
      ChatView.vue
      RagView.vue
      FlowView.vue
      EvalView.vue
    router/
      index.js
```

### SPA / SFC / Vite 心智

- **SPA**：单页应用，路由切换不整页刷新。
- **SFC**：`.vue` 单文件组件 = template + script + style。
- **Vite**：开发时 ESM 原生加载，改文件即 HMR。

### 创建工程

```bash
npm create vite@latest erp-ai-console -- --template vue
cd erp-ai-console && npm install
npm i vue-router pinia
npm run dev
```

```javascript
// src/main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './styles/tokens.css'
import './styles/layout.css'

createApp(App).use(createPinia()).use(router).mount('#app')
```

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```text
# 笔记目录树（今天只画，明天创建）
erp-ai-console/
  src/App.vue
  src/styles/tokens.css
  src/components/AppShell.vue
```

```vue
<!-- 心智：最终 App.vue 组合壳 + 路由视图 -->
<template>
  <AppShell>
    <template #main>路由视图占位</template>
  </AppShell>
</template>
```

```bash
npm create vite@latest erp-ai-console -- --template vue
cd erp-ai-console && npm install && npm run dev
```

```vue
<!-- src/App.vue -->
<script setup>
const title = 'ERP AI 学习控制台'
</script>
<template>
  <h1>{{ title }}</h1>
  <p>纯学习 · Vue 3 + Vite</p>
</template>
```

```vue
<!-- src/components/AppHeader.vue -->
<script setup>
defineProps({ title: String, subtitle: String })
</script>
<template>
  <header class="app-header">
    <h1>{{ title }}</h1>
    <p v-if="subtitle">{{ subtitle }}</p>
  </header>
</template>
<style scoped>
.app-header { padding: var(--space-3); border-bottom: 1px solid var(--color-border); }
</style>
```

```vue
<!-- src/App.vue -->
<script setup>
import AppHeader from './components/AppHeader.vue'
</script>
<template>
  <AppHeader title="ERP AI 学习控制台" subtitle="纯学习 · 不接生产" />
</template>
```

```vue
<script setup>
import { ref } from 'vue'
const traceId = ref('—')
const draft = ref('')
function bumpTrace() {
  traceId.value = 'local-' + Date.now()
}
</script>
<template>
  <label>草稿 <input v-model="draft" /></label>
  <button type="button" @click="bumpTrace">模拟 trace</button>
  <p class="mono">traceId: {{ traceId }}</p>
</template>
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「浏览器、Vue 3 学习控制台与 Vite 心智」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「创建 Vite + Vue 3 工程与首屏」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「单文件组件 SFC 与 `<script setup>` 结构」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「响应式基础：`ref`、`reactive` 与模板插值」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 3. 开发联调：proxy、base、两种交付路径（static 拷贝 / preview）

## 为什么

开发期前端 `localhost:5173`、后端 `8080`。若浏览器直接 `fetch('http://localhost:8080/api/...')` 会 CORS。标准解法：**Vite proxy**——浏览器只请求同源 `/api`，Vite 转发到 8080。

构建后两条学习交付路径：
1. **static 拷贝**：`vite build` → 产物拷入 `erp-ai-assistant/src/main/resources/static/`，Spring Boot 同端口提供。
2. **preview 独立**：`vite preview` 预览构建结果。

## 概念

### proxy 数据流

```text
浏览器  GET http://localhost:5173/api/ai/stats
   ↓ Vite proxy
Spring  GET http://localhost:8080/api/ai/stats
   ↓
JSON 响应（含 traceId）
```

### base 路径选择

| 部署 | `vite.config.js` base | 说明 |
|------|----------------------|------|
| 拷入 Spring static | `'./'` 或 `'/'` | 与 Spring 静态资源策略一致 |
| 子路径部署 | `'/console/'` | 资源 URL 带前缀 |

### 两种交付路径

| 路径 | 步骤 | 何时用 |
|------|------|--------|
| static 拷贝 | `npm run build` → `cp -r dist/* ../erp-ai-assistant/src/main/resources/static/` | 与后端同端口一体化演示 |
| preview | `npm run build && npm run preview` | 纯前端验收 build 产物 |

**注意：** static 拷贝是**手动**学习步骤，讲义不要求改 Maven 自动集成。

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```javascript
// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

```vue
<!-- a11y：App.vue 加 skip-link -->
<template>
  <a class="skip-link" href="#main">跳到主内容</a>
  <!-- ... -->
</template>
<style scoped>
.skip-link { position: absolute; left: -999px; }
.skip-link:focus { left: var(--space-2); background: var(--color-accent); color: #000; padding: var(--space-2); }
button:focus-visible, textarea:focus-visible { outline: 2px solid var(--color-accent); outline-offset: 2px; }
</style>
```

```javascript
// 临时：src/composables/usePing.js — D9 迁入 http.js
import { ref } from 'vue'

export function usePing() {
  const result = ref(null)
  const error = ref(null)
  async function pingChat() {
    error.value = null
    try {
      const resp = await fetch('/api/ai/stats')
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
      result.value = await resp.json()
    } catch (e) {
      error.value = e.message
    }
  }
  return { result, error, pingChat }
}
```

```javascript
// vite.config.js 生产注意
export default defineConfig({
  base: './',  // 拷入 Spring static 子路径时常用相对 base
  build: { outDir: 'dist' },
})
```

```text
方式 A｜拷入 Spring static：
  npm run build
  cp -r dist/* ../erp-ai-assistant/src/main/resources/static/
  mvn spring-boot:run → http://localhost:8080/index.html

方式 B｜独立预览（仍 proxy 或配 nginx）：
  npm run preview
  # 或 preview 时另开终端跑 Spring 8080
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「Vite proxy 连 Spring Boot 与 a11y 起步」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「fetch 入门与 composable 心智」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「`vite build` 与拷入 Spring static」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 4. 设计令牌与视觉纪律（字体、层级、避免 AI 紫奶油模板）

## 为什么

第 1 周主题增量：**ref 解包、表单 v-model**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：`ref`、`reactive` 与模板插值。

第 1 周主题增量：**派生状态、:root CSS 变量**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：`computed` 与设计令牌 tokens.css。

## 概念

### 为什么要设计令牌

控制台信息密度高：身份条、trace、sources、表格。若每处硬编码颜色/间距，换主题或统一风格成本极高。**CSS 变量（设计令牌）** 在 `:root` 定义一次，全站引用。

### 避免「AI 紫奶油」审美

| 反模式 | 问题 | 学习控制台做法 |
|--------|------|----------------|
| 大面积紫渐变 | 像通用 AI 落地页 | 深色底 + 单一强调色（工具蓝/青） |
| 过多圆角卡片堆叠 | 一屏说不清一件事 | 分区明确：身份 / 主区 / 侧栏 |
| 无层级字号 | 标题正文一样大 | h1/h2/body/mono 分级 |
| 浅色奶油底 + 浅灰字 | 对比度不足 | 见第 16 章 a11y |

### 字体建议

- 正文：IBM Plex Sans / Source Sans / 系统 ui-sans
- 数据/trace：`IBM Plex Mono` 等宽
- 可选标题衬线：Source Serif（少量使用）

### `computed` 与令牌联动示例

身份预览、角色标签等**派生状态**用 `computed`，避免在模板里拼字符串。

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```vue
<script setup>
import { ref } from 'vue'
const traceId = ref('—')
const draft = ref('')
function bumpTrace() {
  traceId.value = 'local-' + Date.now()
}
</script>
<template>
  <label>草稿 <input v-model="draft" /></label>
  <button type="button" @click="bumpTrace">模拟 trace</button>
  <p class="mono">traceId: {{ traceId }}</p>
</template>
```

```css
/* src/styles/tokens.css */
:root {
  --font-sans: "IBM Plex Sans", system-ui, sans-serif;
  --font-serif: "Source Serif 4", Georgia, serif;
  --font-mono: "IBM Plex Mono", ui-monospace, monospace;
  --color-bg: #0f1419;
  --color-surface: #1a2332;
  --color-border: #2d3a4f;
  --color-text: #e8edf4;
  --color-muted: #8b9cb3;
  --color-accent: #3d9ed6;
  --color-danger: #e85d5d;
  --color-success: #4caf82;
  --space-1: 0.25rem; --space-2: 0.5rem; --space-3: 1rem; --space-4: 1.5rem;
  --radius: 6px;
}
body { background: var(--color-bg); color: var(--color-text); font-family: var(--font-sans); }
.mono { font-family: var(--font-mono); font-size: 0.8125rem; }
```

```vue
<script setup>
import { ref, computed } from 'vue'
const roles = ref('FINANCE')
const headerPreview = computed(() => `X-Roles: ${roles.value}`)
</script>
<template>
  <p>{{ headerPreview }}</p>
</template>
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「响应式基础：`ref`、`reactive` 与模板插值」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「`computed` 与设计令牌 tokens.css」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 5. App 壳与布局（身份条 + 主区 + 侧栏）

## 为什么

第 1 周主题增量：**派生状态、:root CSS 变量**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：`computed` 与设计令牌 tokens.css。

第 1 周主题增量：**三栏 grid、AppShell.vue**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：Flex/Grid 与 scoped CSS。

第 2 周主题增量：**面板切换、共享 trace**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：Chat + RAG 双面板联调。

## 概念

### 信息架构（IA）

```text
┌─────────────────────────────────────────────┐
│ IdentityBar  X-User-Id / Roles / Tenant      │
├──────────┬──────────────────────┬───────────┤
│ Nav      │ Main（router-view）   │ Aside     │
│ Chat     │ 当前面板              │ Stats     │
│ RAG      │                      │ TraceStrip│
│ Flow     │                      │           │
│ Eval     │                      │           │
└──────────┴──────────────────────┴───────────┘
```

### 布局技术

- CSS Grid：`grid-template-columns: 12rem 1fr 16rem`（桌面）
- 窄屏：第 15 章单列折叠
- **一屏一事**：主区只放一个面板视图；侧栏放诊断信息

### AppShell 插槽约定

| 插槽 | 内容 |
|------|------|
| `#identity` | IdentityBar |
| `#nav` | RouterLink 导航 |
| `#main` | `<router-view />` |
| `#aside` | StatsAside + TraceStrip |

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```css
/* src/styles/tokens.css */
:root {
  --font-sans: "IBM Plex Sans", system-ui, sans-serif;
  --font-serif: "Source Serif 4", Georgia, serif;
  --font-mono: "IBM Plex Mono", ui-monospace, monospace;
  --color-bg: #0f1419;
  --color-surface: #1a2332;
  --color-border: #2d3a4f;
  --color-text: #e8edf4;
  --color-muted: #8b9cb3;
  --color-accent: #3d9ed6;
  --color-danger: #e85d5d;
  --color-success: #4caf82;
  --space-1: 0.25rem; --space-2: 0.5rem; --space-3: 1rem; --space-4: 1.5rem;
  --radius: 6px;
}
body { background: var(--color-bg); color: var(--color-text); font-family: var(--font-sans); }
.mono { font-family: var(--font-mono); font-size: 0.8125rem; }
```

```vue
<script setup>
import { ref, computed } from 'vue'
const roles = ref('FINANCE')
const headerPreview = computed(() => `X-Roles: ${roles.value}`)
</script>
<template>
  <p>{{ headerPreview }}</p>
</template>
```

```vue
<!-- src/components/AppShell.vue -->
<template>
  <div class="app-shell">
    <nav class="app-nav" aria-label="主导航"><slot name="nav" /></nav>
    <main id="main" class="app-main" tabindex="-1"><slot /></main>
    <aside class="app-aside" aria-label="诊断"><slot name="aside" /></aside>
  </div>
</template>
<style scoped>
.app-shell {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr) 260px;
  min-height: calc(100vh - 80px);
  gap: var(--space-3);
}
@media (max-width: 768px) { .app-shell { grid-template-columns: 1fr; } }
</style>
```

```vue
<!-- src/App.vue 整合片段 -->
<script setup>
import { ref } from 'vue'
import ChatPanel from './components/ChatPanel.vue'
import RagPanel from './components/RagPanel.vue'
import TraceStrip from './components/TraceStrip.vue'
import { useTraceStore } from './stores/trace.js'

const trace = useTraceStore()
const panel = ref('chat')
function onTrace(id) {
  trace.setTrace(id, panel.value === 'chat' ? '/api/ai/chat' : '/api/ai/rag/ask')
}
</script>
<template>
  <AppShell>
    <template #nav>
      <button @click="panel='chat'">Chat</button>
      <button @click="panel='rag'">RAG</button>
    </template>
    <ChatPanel v-if="panel==='chat'" @trace="onTrace" />
    <RagPanel v-else @trace="onTrace" />
    <template #aside><TraceStrip /></template>
  </AppShell>
</template>
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「`computed` 与设计令牌 tokens.css」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「App 壳布局：Flex/Grid 与 scoped CSS」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「第 2 周整合：Chat + RAG 双面板联调」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 6. HTTP 层：`api/http.js`、学习请求头、traceId、错误模型

## 为什么

第 2 周主题增量：**apiFetch、Pinia 头占位**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：学习头封装与 trace 透传。

第 2 周主题增量：**TraceStrip、trace store**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：traceId 诊断条与请求日志。

## 概念

### 学习请求头速查

```text
X-User-Id:     demo-user-01
X-Roles:       FINANCE              # 逗号分隔：FINANCE,PROCUREMENT,ADMIN
X-Tenant-Id:   tenant-a             # RAG/Flow 建议必带
X-Admin-Token: <可选>               # reindex 等管理接口
X-Trace-Id:    <可选>               # 未传则服务端生成；响应 traceId 要显示
```

### 统一 `apiFetch` 职责

1. 合并 Pinia 身份头（第 7 章接入）
2. 设置 `Content-Type: application/json`
3. 可选透传 `X-Trace-Id`
4. 解析 JSON；非 2xx 抛结构化错误
5. 从响应提取 `traceId` 写入 trace store

### 错误模型

```javascript
// 期望后端 ApiError 形态（学习期）
// { "message": "...", "traceId": "...", "code": "..." }
```

前端：**不要** `alert` 原始 JSON；用 `role="alert"` 展示 `message`，trace 条显示 `traceId`。

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```javascript
// src/api/http.js
const defaultHeaders = () => ({
  'Content-Type': 'application/json',
  'X-User-Id': 'demo-user-01',
  'X-Roles': 'FINANCE',
  'X-Tenant-Id': 'tenant-a',
})

export async function apiFetch(path, options = {}) {
  const headers = { ...defaultHeaders(), ...(options.headers || {}) }
  const resp = await fetch(path, { ...options, headers })
  const text = await resp.text()
  let payload = null
  try { payload = text ? JSON.parse(text) : null } catch { payload = text }
  if (!resp.ok) {
    const err = new Error(`HTTP ${resp.status}`)
    err.status = resp.status
    err.payload = payload
    throw err
  }
  return payload
}
```

```javascript
// D15 起改为从 Pinia identity store 读头
```

```javascript
// src/stores/trace.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTraceStore = defineStore('trace', () => {
  const lastTraceId = ref('—')
  const lastEndpoint = ref('')
  const log = ref([])

  function setTrace(id, endpoint = '') {
    if (!id) return
    lastTraceId.value = id
    lastEndpoint.value = endpoint
    log.value.unshift({ id, endpoint, at: new Date().toISOString() })
    if (log.value.length > 20) log.value.pop()
  }
  return { lastTraceId, lastEndpoint, log, setTrace }
})
```

```vue
<!-- src/components/TraceStrip.vue -->
<script setup>
import { storeToRefs } from 'pinia'
import { useTraceStore } from '../stores/trace.js'
const { lastTraceId, lastEndpoint } = storeToRefs(useTraceStore())
</script>
<template>
  <div class="trace-strip mono">
    <div>traceId: {{ lastTraceId }}</div>
    <div v-if="lastEndpoint">endpoint: {{ lastEndpoint }}</div>
  </div>
</template>
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「`api/http.js`：学习头封装与 trace 透传」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「traceId 诊断条与请求日志」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 7. Pinia：identity / session 状态

## 为什么

第 3 周主题增量：**stores/identity.js、IdentityBar**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：UserId / Roles / Tenant。

第 4 周主题增量：**pinia 插件或 watch 持久化**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：身份与最近 session 持久化。

## 概念

### 为何用 Pinia

- **identity**：`userId`、`roles`、`tenantId` → 每次 `apiFetch` 带头
- **session**：`sessionId` 跨 Chat 轮次；可 localStorage 持久化
- **trace**：`lastTraceId`、请求日志（与第 6、10 章配合）

### Setup Store 模式

```javascript
export const useIdentityStore = defineStore('identity', () => {
  const userId = ref('demo-user-01')
  function asHeaders() { return { 'X-User-Id': userId.value, ... } }
  return { userId, asHeaders }
})
```

### 与后端 ACL 的关系

学习头**模拟**第 4 月 ACL；真正拦截在后端。前端职责：让观众看见「换角色 → 行为变化」。

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```javascript
// src/stores/identity.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useIdentityStore = defineStore('identity', () => {
  const userId = ref('demo-user-01')
  const roles = ref('FINANCE')
  const tenantId = ref('tenant-a')

  function asHeaders() {
    return {
      'X-User-Id': userId.value,
      'X-Roles': roles.value,
      'X-Tenant-Id': tenantId.value,
    }
  }
  return { userId, roles, tenantId, asHeaders }
})
```

```vue
<!-- src/components/IdentityBar.vue -->
<script setup>
import { storeToRefs } from 'pinia'
import { useIdentityStore } from '../stores/identity.js'
const id = useIdentityStore()
const { userId, roles, tenantId } = storeToRefs(id)
</script>
<template>
  <div class="identity-bar">
    <label>User <input v-model="userId" /></label>
    <label>Roles <select v-model="roles">
      <option>FINANCE</option><option>PROCUREMENT</option><option>ADMIN</option>
    </select></label>
    <label>Tenant <select v-model="tenantId">
      <option>tenant-a</option><option>tenant-b</option>
    </select></label>
  </div>
</template>
```

```javascript
// http.js 改造：import { useIdentityStore } from '../stores/identity.js'
// const id = useIdentityStore(); headers: { ...id.asHeaders(), ... }
```

```javascript
// src/stores/identity.js 持久化片段
import { watch } from 'vue'

const STORAGE_KEY = 'erp-ai-console-identity'

function load() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
  } catch { return {} }
}

export const useIdentityStore = defineStore('identity', () => {
  const saved = load()
  const userId = ref(saved.userId ?? 'demo-user-01')
  // ... roles, tenantId 同理
  watch([userId, roles, tenantId], () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      userId: userId.value,
      roles: roles.value,
      tenantId: tenantId.value,
    }))
  }, { deep: true })
  // ...
})
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「Pinia 身份条：UserId / Roles / Tenant」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「localStorage：身份与最近 session 持久化」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 8. Chat 面板（sessionId、JSON 答案、needHuman）

## 为什么

第 2 周主题增量：**ChatPanel.vue、sessionId**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：输入、答案与 need_human。

## 概念

### Chat API 契约

- **请求：** `POST /api/ai/chat`，body `{ message, sessionId? }`
- **响应：** `answer`、`need_human`（或 `needHuman`）、`traceId`

### sessionId

- 首次本地生成 `sess-${Date.now()}` 或 UUID
- 同 session 多轮对话后端可关联上下文（若实现）
- 可存入 session store + localStorage

### needHuman 展示

当 `need_human === true`：醒目文案「建议转人工（学习演示）」—— 证明你看懂了 JSON 字段，不是普通聊天 UI。

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```vue
<!-- src/components/ChatPanel.vue -->
<script setup>
import { ref } from 'vue'
import { apiFetch } from '../api/http.js'

const message = ref('')
const answer = ref('')
const needHuman = ref(false)
const loading = ref(false)
const error = ref('')
const sessionId = ref(`sess-${Date.now()}`)

async function send() {
  if (!message.value.trim()) return
  loading.value = true
  error.value = ''
  try {
    const data = await apiFetch('/api/ai/chat', {
      method: 'POST',
      body: JSON.stringify({ message: message.value, sessionId: sessionId.value }),
    })
    answer.value = data.answer ?? ''
    needHuman.value = !!data.need_human
    emit('trace', data.traceId)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
const emit = defineEmits(['trace'])
</script>
<template>
  <section aria-labelledby="chat-h">
    <h2 id="chat-h">Chat</h2>
    <label for="chat-msg">消息</label>
    <textarea id="chat-msg" v-model="message" rows="3" />
    <button :disabled="loading" @click="send">{{ loading ? '发送中…' : '发送' }}</button>
    <p v-if="error" class="err">{{ error }}</p>
    <article aria-live="polite" class="answer">{{ answer }}</article>
    <p v-if="needHuman" class="warn">need_human：建议转人工（学习演示）</p>
  </section>
</template>
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「Chat 组件：输入、答案与 need_human」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 9. RAG 面板（sources 列表、Gate 文案展示）

## 为什么

第 2 周主题增量：**v-for sources、禁止 v-html**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：问题、答案与 sources 列表。

第 3 周主题增量：**切 tenant/role 再 RAG**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：换角色 → sources 变。

## 概念

### RAG API 契约

- **请求：** `POST /api/ai/rag/ask`，body `{ question }`
- **响应：** `answer`、`sources[]`、`traceId`；可能有 Gate/拒绝类字段（视后端版本）

### sources 列表

每个 source 常见字段：`title`、`docId`、`snippet`、`score`。用 `v-for` 渲染，**禁止**对 snippet 用 `v-html`。

### Gate 文案

若响应含 `blocked`、`gateMessage`、`refusal` 等（以仓库为准）：在主区展示**拒绝原因**，而非空白。这是 RAG 治理演示要点。

### 换角色看 sources

`X-Roles` + `X-Tenant-Id` 变化后，同一问题 sources 数量或内容应不同（后端配置支持时）—— 第 18 章彩排验证。

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```vue
<!-- src/components/RagPanel.vue -->
<script setup>
import { ref } from 'vue'
import { apiFetch } from '../api/http.js'

const question = ref('')
const answer = ref('')
const sources = ref([])
const loading = ref(false)
const error = ref('')

async function ask() {
  loading.value = true
  error.value = ''
  sources.value = []
  try {
    const data = await apiFetch('/api/ai/rag/ask', {
      method: 'POST',
      body: JSON.stringify({ question: question.value }),
    })
    answer.value = data.answer ?? ''
    sources.value = data.sources ?? []
    emit('trace', data.traceId)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
const emit = defineEmits(['trace'])
</script>
<template>
  <section aria-labelledby="rag-h">
    <h2 id="rag-h">RAG</h2>
    <label for="rag-q">问题</label>
    <textarea id="rag-q" v-model="question" rows="3" />
    <button :disabled="loading" @click="ask">检索并生成</button>
    <p v-if="error" class="err">{{ error }}</p>
    <article aria-live="polite">{{ answer }}</article>
    <ul v-if="sources.length">
      <li v-for="(s, i) in sources" :key="i">
        <strong>{{ s.title || s.docId || 'source' }}</strong>
        <span class="mono">{{ s.snippet }}</span>
      </li>
    </ul>
    <p v-else-if="!loading && answer">（无 sources 字段时显示空列表）</p>
  </section>
</template>
```

```text
整合彩排（笔记剧本）：
1. IdentityBar 选 FINANCE + tenant-a → RAG 提问 → 记录 sources 数量
2. 改 PROCUREMENT + tenant-b → 同一问题 → sources 应不同（若后端配置支持）
3. Chat 一条 → Feedback useful → stats 刷新看计数变化
4. Flow start → APPROVE → 确认 UI 文案含「不等于写库」
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「RAG 组件：问题、答案与 sources 列表」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「第 3 周整合：换角色 → sources 变」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 10. Loading / 空态 / 错误态模式

## 为什么

第 2 周主题增量：**isLoading、errorMessage、空文案**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：Loading、空态与错误态组件模式。

## 概念

### 三态 UI 模式

| 状态 | 用户感知 | 实现 |
|------|----------|------|
| loading | 正在请求 | `loading` ref + `role="status"` |
| empty | 无数据可展示 | `sources.length===0` 且非 loading |
| error | 请求失败 | `catch` 设置 `error` + `role="alert"` |

### 组件化

抽取 `UiState.vue` 或 composable `useAsyncState()`，避免每个面板复制 if-else。

### aria-live

答案区 `aria-live="polite"`：屏幕阅读器在内容更新时播报，不打断用户。

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```vue
<!-- src/components/UiState.vue — 可复用三态 -->
<script setup>
defineProps({
  loading: Boolean,
  error: String,
  empty: Boolean,
  emptyText: { type: String, default: '暂无数据' },
})
</script>
<template>
  <p v-if="loading" role="status">加载中…</p>
  <p v-else-if="error" class="err" role="alert">{{ error }}</p>
  <p v-else-if="empty" class="muted">{{ emptyText }}</p>
  <slot v-else />
</template>
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「Loading、空态与错误态组件模式」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 11. Flow HITL（start、列表、APPROVE/REJECT/EDIT；文案 APPROVE≠写库）

## 为什么

第 3 周主题增量：**FlowPanel start/pending**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：start 与 pending 列表。

第 3 周主题增量：**决策按钮、APPROVE≠写库文案**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：APPROVE / REJECT / EDIT。

第 3 周主题增量：**FeedbackBar、挂 traceId**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：useful / wrong / unsafe。

## 概念

### Flow 学习期 API

| 操作 | 方法 | 路径 |
|------|------|------|
| 启动 | POST | `/api/ai/flow/start` |
| 待办 | GET | `/api/ai/flow/pending` |
| 决策 | POST | `/api/ai/flow/{id}/decide` |
| 审计 | GET | `/api/ai/flow/{id}/audit` |

### HITL 心智

Human-In-The-Loop：AI 建议 → 人工 APPROVE/REJECT/EDIT → 状态机推进。

### APPROVE ≠ 写库（必写 UI 文案）

```text
学习说明：APPROVE 仅推进学习 Flow 状态，不等于生产过账或改库存。
```

演示时主动念出这句话—— 区分学习控制台与生产 ERP。

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```vue
<!-- src/components/FlowPanel.vue 片段 -->
<script setup>
import { ref, onMounted } from 'vue'
import { apiFetch } from '../api/http.js'

const question = ref('')
const pending = ref([])
const loading = ref(false)

async function startFlow() {
  loading.value = true
  try {
    await apiFetch('/api/ai/flow/start', {
      method: 'POST',
      body: JSON.stringify({ question: question.value }),
    })
    await loadPending()
  } finally { loading.value = false }
}

async function loadPending() {
  pending.value = await apiFetch('/api/ai/flow/pending')
}
onMounted(loadPending)
</script>
<template>
  <h2>Flow</h2>
  <textarea v-model="question" rows="2" placeholder="发起流程的问题" />
  <button :disabled="loading" @click="startFlow">Start</button>
  <ul>
    <li v-for="item in pending" :key="item.flowId || item.id">
      {{ item.flowId || item.id }} — {{ item.status }}
    </li>
  </ul>
</template>
```

```vue
<script setup>
async function decide(flowId, decision) {
  await apiFetch(`/api/ai/flow/${flowId}/decide`, {
    method: 'POST',
    body: JSON.stringify({ decision, comment: '' }),
  })
  await loadPending()
}
</script>
<template>
  <div v-for="item in pending" :key="item.flowId">
    <button @click="decide(item.flowId, 'APPROVE')">APPROVE</button>
    <button @click="decide(item.flowId, 'REJECT')">REJECT</button>
    <button @click="decide(item.flowId, 'EDIT')">EDIT</button>
  </div>
  <p class="muted">学习说明：APPROVE 仅推进学习 Flow 状态，<strong>不等于</strong>生产过账或改库存。</p>
</template>
```

```vue
<!-- src/components/FeedbackBar.vue -->
<script setup>
import { ref } from 'vue'
import { apiFetch } from '../api/http.js'
import { useTraceStore } from '../stores/trace.js'

const trace = useTraceStore()
const status = ref('')
const kinds = ['useful', 'wrong', 'unsafe']

async function send(kind) {
  if (!trace.lastTraceId || trace.lastTraceId === '—') {
    status.value = '请先完成 Chat/RAG 获取 traceId'
    return
  }
  await apiFetch('/api/ai/feedback', {
    method: 'POST',
    body: JSON.stringify({
      traceId: trace.lastTraceId,
      kind,
      endpoint: trace.lastEndpoint || 'unknown',
    }),
  })
  status.value = `已提交 ${kind}`
}
</script>
<template>
  <div class="feedback-bar">
    <button v-for="k in kinds" :key="k" @click="send(k)">{{ k }}</button>
    <p aria-live="polite">{{ status }}</p>
  </div>
</template>
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「Flow 组件：start 与 pending 列表」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「Flow decide：APPROVE / REJECT / EDIT」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「Feedback 三键：useful / wrong / unsafe」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 12. Feedback（useful/wrong/unsafe + traceId）

## 为什么

第 3 周主题增量：**FeedbackBar、挂 traceId**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：useful / wrong / unsafe。

## 概念

### Feedback API

`POST /api/ai/feedback`

```json
{
  "traceId": "<来自 Chat/RAG 响应>",
  "kind": "useful|wrong|unsafe",
  "comment": "可选",
  "endpoint": "/api/ai/chat"
}
```

### 三键含义

| kind | 含义 |
|------|------|
| useful | 有帮助 |
| wrong | 事实/逻辑错误 |
| unsafe | 不安全或违规 |

必须先有 `traceId`（第 6、8、9 章）；无 trace 时禁用反馈按钮。

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```vue
<!-- src/components/FeedbackBar.vue -->
<script setup>
import { ref } from 'vue'
import { apiFetch } from '../api/http.js'
import { useTraceStore } from '../stores/trace.js'

const trace = useTraceStore()
const status = ref('')
const kinds = ['useful', 'wrong', 'unsafe']

async function send(kind) {
  if (!trace.lastTraceId || trace.lastTraceId === '—') {
    status.value = '请先完成 Chat/RAG 获取 traceId'
    return
  }
  await apiFetch('/api/ai/feedback', {
    method: 'POST',
    body: JSON.stringify({
      traceId: trace.lastTraceId,
      kind,
      endpoint: trace.lastEndpoint || 'unknown',
    }),
  })
  status.value = `已提交 ${kind}`
}
</script>
<template>
  <div class="feedback-bar">
    <button v-for="k in kinds" :key="k" @click="send(k)">{{ k }}</button>
    <p aria-live="polite">{{ status }}</p>
  </div>
</template>
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「Feedback 三键：useful / wrong / unsafe」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 13. Eval 与 stats 面板

## 为什么

第 3 周主题增量：**EvalPanel、表格渲染**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：Eval 触发与结果表。

第 3 周主题增量：**StatsAside、定时刷新可选**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：token、反馈计数。

第 3 周主题增量：**切 tenant/role 再 RAG**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：换角色 → sources 变。

## 概念

### Eval

- `POST /api/ai/eval/run` body `{ suite }`
- `GET /api/ai/eval/runs` 历史列表
- 表格展示 passed/total、时间、suite 名

### Stats

- `GET /api/ai/stats` — token 计数、反馈计数等（字段以后端为准）
- 放侧栏 `StatsAside.vue`，切换面板时可刷新

### 与治理叙事

Eval = 回归测试；Stats = 运行概览；与第 2 月后端 Eval 章节对照阅读。

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```vue
<!-- src/components/EvalPanel.vue -->
<script setup>
import { ref, onMounted } from 'vue'
import { apiFetch } from '../api/http.js'

const suite = ref('smoke')
const runs = ref([])
const running = ref(false)

async function runEval() {
  running.value = true
  try {
    await apiFetch('/api/ai/eval/run', {
      method: 'POST',
      body: JSON.stringify({ suite: suite.value }),
    })
    await loadRuns()
  } finally { running.value = false }
}

async function loadRuns() {
  runs.value = (await apiFetch('/api/ai/eval/runs')) ?? []
}
onMounted(loadRuns)
</script>
<template>
  <h2>Eval</h2>
  <input v-model="suite" />
  <button :disabled="running" @click="runEval">运行</button>
  <table>
    <thead><tr><th>id</th><th>passed</th><th>total</th></tr></thead>
    <tbody>
      <tr v-for="r in runs" :key="r.id">
        <td>{{ r.id }}</td><td>{{ r.passed }}</td><td>{{ r.total }}</td>
      </tr>
    </tbody>
  </table>
</template>
```

```vue
<!-- src/components/StatsAside.vue -->
<script setup>
import { ref, onMounted } from 'vue'
import { apiFetch } from '../api/http.js'

const stats = ref(null)
const error = ref('')

async function refresh() {
  try {
    stats.value = await apiFetch('/api/ai/stats')
  } catch (e) {
    error.value = e.message
  }
}
onMounted(refresh)
</script>
<template>
  <aside>
    <h3>Stats</h3>
    <button @click="refresh">刷新</button>
    <pre v-if="stats" class="mono">{{ stats }}</pre>
    <p v-if="error" class="err">{{ error }}</p>
  </aside>
</template>
```

```text
整合彩排（笔记剧本）：
1. IdentityBar 选 FINANCE + tenant-a → RAG 提问 → 记录 sources 数量
2. 改 PROCUREMENT + tenant-b → 同一问题 → sources 应不同（若后端配置支持）
3. Chat 一条 → Feedback useful → stats 刷新看计数变化
4. Flow start → APPROVE → 确认 UI 文案含「不等于写库」
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「Eval 触发与结果表」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「stats 侧栏：token、反馈计数」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「第 3 周整合：换角色 → sources 变」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 14. 路由整理与导航

## 为什么

第 4 周主题增量：**router/index.js、RouterLink**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：面板作路由。

第 2 周主题增量：**面板切换、共享 trace**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：Chat + RAG 双面板联调。

## 概念

### 为何面板作路由

- URL 可分享：`/chat`、`/rag`、`/flow`、`/eval`
- 浏览器后退有效
- 与「一屏一事」一致：一个 route 一个 view

### 典型 routes

```javascript
const routes = [
  { path: '/', redirect: '/chat' },
  { path: '/chat', component: () => import('../views/ChatView.vue') },
  { path: '/rag', component: () => import('../views/RagView.vue') },
  { path: '/flow', component: () => import('../views/FlowView.vue') },
  { path: '/eval', component: () => import('../views/EvalView.vue') },
]
```

### View vs Panel

- **View**：路由入口，组合 Panel + 布局
- **Panel**：业务表单与 API 调用，可被 View 引用

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```javascript
// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import ChatView from '../views/ChatView.vue'
import RagView from '../views/RagView.vue'
import FlowView from '../views/FlowView.vue'
import EvalView from '../views/EvalView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/chat' },
    { path: '/chat', component: ChatView, meta: { title: 'Chat' } },
    { path: '/rag', component: RagView, meta: { title: 'RAG' } },
    { path: '/flow', component: FlowView, meta: { title: 'Flow' } },
    { path: '/eval', component: EvalView, meta: { title: 'Eval' } },
  ],
})
```

```vue
<RouterLink to="/chat">Chat</RouterLink>
<RouterView />
```

```vue
<!-- src/App.vue 整合片段 -->
<script setup>
import { ref } from 'vue'
import ChatPanel from './components/ChatPanel.vue'
import RagPanel from './components/RagPanel.vue'
import TraceStrip from './components/TraceStrip.vue'
import { useTraceStore } from './stores/trace.js'

const trace = useTraceStore()
const panel = ref('chat')
function onTrace(id) {
  trace.setTrace(id, panel.value === 'chat' ? '/api/ai/chat' : '/api/ai/rag/ask')
}
</script>
<template>
  <AppShell>
    <template #nav>
      <button @click="panel='chat'">Chat</button>
      <button @click="panel='rag'">RAG</button>
    </template>
    <ChatPanel v-if="panel==='chat'" @trace="onTrace" />
    <RagPanel v-else @trace="onTrace" />
    <template #aside><TraceStrip /></template>
  </AppShell>
</template>
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「Vue Router 整理：面板作路由」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「第 2 周整合：Chat + RAG 双面板联调」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 15. 持久化、快捷键、轻动效、响应式

## 为什么

第 4 周主题增量：**pinia 插件或 watch 持久化**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：身份与最近 session 持久化。

第 4 周主题增量：**@keydown、router.push**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：聚焦输入、发送、切路由。

第 4 周主题增量：**transition、prefers-reduced-motion**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：两处微交互。

第 4 周主题增量：**media query、overflow-x**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：窄屏单列与表格横滚。

## 概念

### localStorage 持久化

- identity：`userId`、`roles`、`tenantId`
- session：`sessionId`
- 用 `watch` 或 pinia 插件；注意 JSON parse 异常兜底

### 快捷键（学习演示）

| 快捷键 | 动作 |
|--------|------|
| Ctrl+Enter | 发送当前聚焦 textarea |
| Alt+1..4 | 跳转 /chat /rag /flow /eval |

注册于 `onMounted`，`onUnmounted` 移除监听。

### 轻动效

- `<Transition name="fade">` 成功提示
- `@media (prefers-reduced-motion: reduce)` 关闭动画

### 响应式

`@media (max-width: 768px)`：壳改单列、表格 `overflow-x: auto`。

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```javascript
// src/stores/identity.js 持久化片段
import { watch } from 'vue'

const STORAGE_KEY = 'erp-ai-console-identity'

function load() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
  } catch { return {} }
}

export const useIdentityStore = defineStore('identity', () => {
  const saved = load()
  const userId = ref(saved.userId ?? 'demo-user-01')
  // ... roles, tenantId 同理
  watch([userId, roles, tenantId], () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      userId: userId.value,
      roles: roles.value,
      tenantId: tenantId.value,
    }))
  }, { deep: true })
  // ...
})
```

```vue
<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

function onKey(e) {
  if (e.ctrlKey && e.key === 'Enter') {
    document.querySelector('textarea:focus')?.dispatchEvent(new Event('submit-chat'))
  }
  if (e.altKey && e.key === '1') router.push('/chat')
  if (e.altKey && e.key === '2') router.push('/rag')
}
onMounted(() => window.addEventListener('keydown', onKey))
onUnmounted(() => window.removeEventListener('keydown', onKey))
</script>
```

```vue
<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
@media (prefers-reduced-motion: reduce) {
  .fade-enter-active, .fade-leave-active { transition: none; }
}
.btn-send:active { transform: scale(0.98); }
</style>
<template>
  <Transition name="fade">
    <p v-if="justSent" class="ok">已发送</p>
  </Transition>
</template>
```

```css
/* layout 响应式 */
@media (max-width: 768px) {
  .app-shell { grid-template-columns: 1fr; }
  .identity-bar { flex-direction: column; gap: var(--space-2); }
  table { display: block; overflow-x: auto; }
}
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「localStorage：身份与最近 session 持久化」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「键盘快捷键：聚焦输入、发送、切路由」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「有意图的动效：两处微交互」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「响应式：窄屏单列与表格横滚」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 16. 可访问性与安全（焦点、对比度、XSS/`v-html`）

## 为什么

第 1 周主题增量：**三栏 grid、AppShell.vue**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：Flex/Grid 与 scoped CSS。

第 1 周主题增量：**server.proxy、skip-link、focus-visible**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：Vite proxy 连 Spring Boot 与 a11y 起步。

## 概念

### 可访问性要点

- 表单：`label` + `for` 关联 `id`
- 按钮：`type="button"` 避免误提交
- 动态区：`aria-live="polite"`
- 错误：`role="alert"`
- 焦点：Tab 顺序合理；自定义控件加 `tabindex` 谨慎

### 对比度

深色主题下 `--color-muted` 勿过浅；WCAG AA 目标对比度 ≥ 4.5:1（正文）。

### XSS 与 `v-html`

模型输出、RAG snippet **默认文本插值**。仅当消毒后可考虑 `v-html`；学习轨道**禁止**对 LLM 输出开 `v-html`。

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```vue
<!-- src/components/AppShell.vue -->
<template>
  <div class="app-shell">
    <nav class="app-nav" aria-label="主导航"><slot name="nav" /></nav>
    <main id="main" class="app-main" tabindex="-1"><slot /></main>
    <aside class="app-aside" aria-label="诊断"><slot name="aside" /></aside>
  </div>
</template>
<style scoped>
.app-shell {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr) 260px;
  min-height: calc(100vh - 80px);
  gap: var(--space-3);
}
@media (max-width: 768px) { .app-shell { grid-template-columns: 1fr; } }
</style>
```

```javascript
// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

```vue
<!-- a11y：App.vue 加 skip-link -->
<template>
  <a class="skip-link" href="#main">跳到主内容</a>
  <!-- ... -->
</template>
<style scoped>
.skip-link { position: absolute; left: -999px; }
.skip-link:focus { left: var(--space-2); background: var(--color-accent); color: #000; padding: var(--space-2); }
button:focus-visible, textarea:focus-visible { outline: 2px solid var(--color-accent); outline-offset: 2px; }
</style>
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「App 壳布局：Flex/Grid 与 scoped CSS」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「Vite proxy 连 Spring Boot 与 a11y 起步」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 17. 构建发布检查清单

## 为什么

第 4 周主题增量：**base 路径、dist 部署两法**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：`vite build` 与拷入 Spring static。

第 4 周主题增量：**三场景剧本、≤15 分钟**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：场景 A/B/C。

第 4 周主题增量：**三张截图、README URL**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：作品集截图与 PORTFOLIO 规范。

## 概念

### build 前检查

- [ ] `npm run build` 无错误
- [ ] `base` 与部署方式一致
- [ ] 生产无 `console.log` 敏感信息
- [ ] 路由 history 模式与服务器 fallback（若独立部署）

### static 拷贝步骤

```bash
cd erp-ai-console
npm run build
cp -r dist/* ../erp-ai-assistant/src/main/resources/static/
# 重启 Spring Boot，访问 http://localhost:8080/
```

### preview 步骤

```bash
npm run build && npm run preview
# 另开终端确保后端 8080 运行；preview 需配置 proxy 或后端 CORS
```

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```javascript
// vite.config.js 生产注意
export default defineConfig({
  base: './',  // 拷入 Spring static 子路径时常用相对 base
  build: { outDir: 'dist' },
})
```

```text
方式 A｜拷入 Spring static：
  npm run build
  cp -r dist/* ../erp-ai-assistant/src/main/resources/static/
  mvn spring-boot:run → http://localhost:8080/index.html

方式 B｜独立预览（仍 proxy 或配 nginx）：
  npm run preview
  # 或 preview 时另开终端跑 Spring 8080
```

```text
场景 A｜Chat 演示（3 分钟）
  打开 /chat → 提问 → 展示 answer + need_human + traceId

场景 B｜RAG + 身份（5 分钟）
  换 tenant/role → 同一问题 → 对比 sources → Feedback

场景 C｜Flow + Eval（5 分钟）
  start → pending → APPROVE（读免责声明）→ 跑 eval → stats

计时目标：≤ 15 分钟；Network 无 4xx（除故意测错）
```

```text
PORTFOLIO 三张截图：
1. 全屏壳 + IdentityBar + trace 侧栏
2. RAG sources 列表（含角色差异标注）
3. Flow pending + Eval 表

README 补充：
  开发：cd erp-ai-console && npm run dev  （Vite 5173 + proxy）
  生产学习：build 后 static 或 preview URL
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「`vite build` 与拷入 Spring static」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「端到端彩排：场景 A/B/C」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「作品集截图与 PORTFOLIO 规范」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 18. 端到端彩排剧本（角色切换 / 反馈 / 租户）

## 为什么

第 3 周主题增量：**切 tenant/role 再 RAG**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：换角色 → sources 变。

第 4 周主题增量：**三场景剧本、≤15 分钟**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：场景 A/B/C。

## 概念

### 场景 A：角色与租户

1. IdentityBar：`FINANCE` + `tenant-a` → RAG 提问 → 记录 sources 数量
2. 改 `PROCUREMENT` + `tenant-b` → 同一问题 → sources 应不同（若后端支持）
3. 指给观众：Network 里 `X-Roles` / `X-Tenant-Id` 已变

### 场景 B：Chat + Feedback

1. Chat 发送一条 → 记下 traceId
2. 点 useful → `POST /api/ai/feedback` 200
3. Stats 刷新看反馈计数变化

### 场景 C：Flow HITL

1. Flow start → pending 出现
2. APPROVE → 列表更新
3. **念出**：APPROVE 不等于写库

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```text
整合彩排（笔记剧本）：
1. IdentityBar 选 FINANCE + tenant-a → RAG 提问 → 记录 sources 数量
2. 改 PROCUREMENT + tenant-b → 同一问题 → sources 应不同（若后端配置支持）
3. Chat 一条 → Feedback useful → stats 刷新看计数变化
4. Flow start → APPROVE → 确认 UI 文案含「不等于写库」
```

```text
场景 A｜Chat 演示（3 分钟）
  打开 /chat → 提问 → 展示 answer + need_human + traceId

场景 B｜RAG + 身份（5 分钟）
  换 tenant/role → 同一问题 → 对比 sources → Feedback

场景 C｜Flow + Eval（5 分钟）
  start → pending → APPROVE（读免责声明）→ 跑 eval → stats

计时目标：≤ 15 分钟；Network 无 4xx（除故意测错）
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

## 本章自检

- 今日主题「第 3 周整合：换角色 → sources 变」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「端到端彩排：场景 A/B/C」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 19. 作品集与口述提纲

## 为什么

第 4 周主题增量：**三张截图、README URL**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：作品集截图与 PORTFOLIO 规范。

第 4 周主题增量：**8 分钟提纲、能力复盘**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：口述与进阶方向。

## 概念

### 作品集物料

- 截图：壳布局、Chat、RAG sources、Flow、Eval 表、trace 条
- 30～60 秒录屏：按第 18 章剧本
- README：`erp-ai-console/README.md` 说明如何 dev/build

### 8～10 分钟口述提纲

1. 项目定位（学习控制台、技术栈）— 1 min
2. 身份模拟与 ACL 演示 — 2 min
3. RAG sources + Gate — 2 min
4. Flow APPROVE 与学习边界 — 2 min
5. Feedback + trace + stats — 2 min
6. Q&A — 1 min

## 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

**Step 1｜读昨天产物（10 分钟）**  
打开 `erp-ai-console/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴到 SFC 或 js；每改一块就保存看 HMR。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：对接 API 的日子必看请求头与 traceId。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

## 可复制代码骨架

```text
PORTFOLIO 三张截图：
1. 全屏壳 + IdentityBar + trace 侧栏
2. RAG sources 列表（含角色差异标注）
3. Flow pending + Eval 表

README 补充：
  开发：cd erp-ai-console && npm run dev  （Vite 5173 + proxy）
  生产学习：build 后 static 或 preview URL
```

```text
Web 轨 30 天复盘：
  □ Vite + Vue SFC + ref/computed
  □ tokens + AppShell + a11y + proxy
  □ apiFetch + Chat/RAG + trace
  □ Pinia 身份 + Flow + Feedback + Eval + stats
  □ 持久化 + 快捷键 + 动效 + 响应式 + Router
  □ build 进 static + 三场景彩排 + 截图

收官口述模板：
  我用 Vue 3 学习控制台接了学习期 ERP AI REST；
  Pinia 模拟 ACL/租户，RAG 展示 sources，
  反馈挂 traceId，Eval 可回归；Flow APPROVE 不等于写库。
```

## 常见坑

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 口述标准答法（先自测再对照）

> 完整版：[ORAL_ANSWERS.md](../ORAL_ANSWERS.md#web第19章-口述提纲标准答法)

**1′ 定位** — Vue3+Vite 学习控制台，对接 `erp-ai-assistant` 的 `/api/ai/*`；不接公司 SSO。  
**2′ ACL** — Pinia/请求头模拟角色与租户；切换后 RAG sources 变化可演示。  
**3′ RAG** — 展示 sources 与 Gate；强调引用非模型编造。  
**4′ Flow** — WAIT_HUMAN → decide；口述「APPROVE≠写库」。  
**5′ 反馈/观测** — 反馈挂 traceId；stats/eval 可回归。  
**6′ Q&A** — 边界：假权限、假账本、学习仓。

**收官一句（标准稿）：**  
「我用 Vue3 控制台接学习期 ERP AI：模拟 ACL/租户，RAG 展示 sources，反馈挂 traceId，Eval 可回归；Flow 的 APPROVE 不等于写库。」

### 本章自检 · 对照通过标准

- `npm run build` 成功；`npm run dev` 无红错  
- 经 proxy 的 `/api/ai/*` 为 200（若后端已启）  
- 能按上面提纲 8～10 分钟讲完，且明确说出 **APPROVE≠写库**  
- PORTFOLIO 三张截图 + console README 可复现启动步骤

## 本章自检

- 今日主题「作品集截图与 PORTFOLIO 规范」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

- 今日主题「Web 轨收官：口述与进阶方向」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200

---


# 20. 进阶与明确不做

## 为什么

明确边界避免学习轨道膨胀成「假生产」或过度工程化。

## 概念

### 可选进阶（学完再做）

| 方向 | 说明 |
|------|------|
| TypeScript | `apiFetch` 泛型、DTO 类型 |
| Vitest + Vue Test Utils | Panel 单元测试 |
| `@vueuse/core` | `useLocalStorage`、`useEventListener` |
| OpenAPI 生成客户端 | 与后端契约同步 |
| Element Plus / Naive UI | 仅当团队要求；本教材不要求 |

### 明确不做（学习轨道）

- 不接公司生产 SSO / 真实主数据
- 不在前端实现「真 ACL」隐藏按钮（模拟头即可）
- 不新增写库/过账接口
- 不把 `erp-ai-console` 静默 merge 进 `erp-ai-assistant` 源码树
- 不用 `v-html` 渲染模型输出
- 不做 30 天打卡式进度条（已改为本章式教材）

## 怎么做

完成第 1～19 章后再选一项进阶；每项单独开分支实验，不污染主学习线。

## 可复制代码骨架

```text
# 进阶实验分支命名建议
git checkout -b experiment/vue-typescript-api
```

## 常见坑

| 坑 | 现象 | 处理 |
|----|------|------|
| CORS | 直连 `localhost:8080` | Vite proxy，路径写 `/api/...` |
| `ref` 忘 `.value` | script 里 undefined | `count.value++`；模板自动解包 |
| `v-html` XSS | 模型输出执行脚本 | 默认文本插值；sources 用 `v-for` |
| proxy 后端未启 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| scoped 不生效 | 样式未命中子树 | `:deep()` 或全局 tokens |
| Pinia setup 外调用 | getActivePinia 报错 | 在 `apiFetch` 内 `useIdentityStore()` |
| `base` 错误 | build 后资源 404 | static 拷贝用 `base: './'` |

## 本章自检

- [ ] 能说出 3 项「明确不做」
- [ ] 进阶方向与主教材解耦
- [ ] 完整教材已通读并可演示

---


# 附录 A：完整文件树

```text
erp-ai-console/          # 建议独立前端目录（学习仓根下），勿与公司项目混淆
  package.json
  vite.config.js
  index.html
  src/
    main.js
    App.vue
    styles/
      tokens.css
      layout.css
      components.css
    api/
      http.js
    stores/
      identity.js
      trace.js
    composables/
      useApiHeaders.js
    components/
      AppShell.vue
      TraceStrip.vue
      ChatPanel.vue
      RagPanel.vue
      FlowPanel.vue
      FeedbackBar.vue
      EvalPanel.vue
      StatsAside.vue
      IdentityBar.vue
    views/
      ChatView.vue
      RagView.vue
      FlowView.vue
      EvalView.vue
    router/
      index.js
```

---

# 附录 B：tokens 参考

```css
/* src/styles/tokens.css — 完整参考 */
:root {
  --font-sans: "IBM Plex Sans", system-ui, sans-serif;
  --font-serif: "Source Serif 4", Georgia, serif;
  --font-mono: "IBM Plex Mono", ui-monospace, monospace;
  --color-bg: #0f1419;
  --color-surface: #1a2332;
  --color-border: #2d3a4f;
  --color-text: #e8edf4;
  --color-muted: #8b9cb3;
  --color-accent: #3d9ed6;
  --color-danger: #e85d5d;
  --color-success: #4caf82;
  --color-warn: #e6a23c;
  --space-1: 0.25rem;
  --space-2: 0.5rem;
  --space-3: 1rem;
  --space-4: 1.5rem;
  --space-5: 2rem;
  --radius: 6px;
  --shadow-sm: 0 1px 2px rgba(0,0,0,0.2);
}
body {
  background: var(--color-bg);
  color: var(--color-text);
  font-family: var(--font-sans);
  line-height: 1.5;
  margin: 0;
}
.mono { font-family: var(--font-mono); font-size: 0.8125rem; }
```

---

# 附录 C：API 对照表

| Chat | POST | `/api/ai/chat` | `{ message, sessionId? }` | `answer`, `need_human`, `traceId` |
| RAG | POST | `/api/ai/rag/ask` | `{ question }` | `answer`, `sources[]`, `traceId` |
| Flow 启动 | POST | `/api/ai/flow/start` | `{ question }` | `flowId`, `status` |
| Flow 待办 | GET | `/api/ai/flow/pending` | — | 列表 |
| Flow 决策 | POST | `/api/ai/flow/{id}/decide` | `{ decision, comment? }` | 新状态 |
| Flow 审计 | GET | `/api/ai/flow/{id}/audit` | — | 事件序列 |
| Feedback | POST | `/api/ai/feedback` | `{ traceId, kind, comment?, endpoint }` | ok |
| Eval 运行 | POST | `/api/ai/eval/run` | `{ suite }` | `id`, `passed`, `total` |
| Eval 历史 | GET | `/api/ai/eval/runs` | — | runs 列表 |
| Stats | GET | `/api/ai/stats` | — | token/反馈计数等 |

**学习头（建议每个请求携带）：**

```http
X-User-Id: demo-user-01
X-Roles: FINANCE
X-Tenant-Id: tenant-a
```


### 学习请求头

```text
X-User-Id:     demo-user-01
X-Roles:       FINANCE              # 逗号分隔：FINANCE,PROCUREMENT,ADMIN
X-Tenant-Id:   tenant-a             # RAG/Flow 建议必带
X-Admin-Token: <可选>               # reindex 等管理接口
X-Trace-Id:    <可选>               # 未传则服务端生成；响应 traceId 要显示
```

---

# 附录 D：FAQ

**Q: proxy 502？**  
A: 先 `mvn spring-boot:run`；确认 `vite.config.js` proxy target 为 `http://localhost:8080`。

**Q: Feedback 无 traceId？**  
A: 先完成 Chat/RAG 拿到响应 `traceId`；查 Network 是否 `POST /api/ai/feedback`。

**Q: build 后白屏？**  
A: 检查 `base` 与静态资源路径；Console 是否有 404。

**Q: 换角色 sources 不变？**  
A: 确认 `apiFetch` 已合并 Pinia 头；后端是否按租户/角色过滤。


---

# 附录 E：与后端月度教材交叉索引

### 与第 4 月控制台章节对照（可选平行阅读）

| Web 轨道 | 第 4 月 | 说明 |
|---|---|---|
| WEB-D5～6 壳布局 | M4-D22 IA | Web 轨从 Vue SFC 讲起；M4 假设你已会写页面 |
| WEB-D9 http.js | M4-D23 fetch | 同一学习头纪律 |
| WEB-D15 Pinia 身份 | M4-D4 学习头 | store ↔ 请求头 |
| WEB-D18 Feedback | M4-D8～12 | UI 三键 ↔ JSONL |
| WEB-D28 彩排 | M4-D24～26 | 场景 A/B/C 剧本可复用 |


| 本教材章节 | 后端月份 | 说明 |
|------------|----------|------|
| 第 5 章 壳布局 | M4-D22 IA | Web 从 SFC 讲起 |
| 第 6 章 http.js | M4-D23 fetch | 同一学习头纪律 |
| 第 7 章 Pinia 身份 | M4-D4 学习头 | store ↔ 请求头 |
| 第 12 章 Feedback | M4-D8～12 | UI 三键 ↔ JSONL |
| 第 18 章 彩排 | M4-D24～26 | 场景剧本可复用 |
| 第 8 章 Chat | MONTH1 Chat API | JSON 字段显示 |
| 第 9 章 RAG | MONTH1 RAG | sources 列表 |
| 第 11 章 Flow | MONTH2 Flow | HITL 状态机 |

---

# 附录 F：修订记录

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-08-15 | 2.0 | 发布连续章节式完整教材 `WEB_VUE_COMPLETE.md`，取代 WEB-D1～30 逐日合订结构 |
| 此前 | 1.x | 旧「WEB-D1～30」逐日合订（已退役删除） |
