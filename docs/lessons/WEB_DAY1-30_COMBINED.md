# Web 前端轨道合并讲义（WEB-D1～WEB-D30）【逐日详版 · 与第1月同级 · 非概述】

> **定位：** 纯学习；通用 ERP AI 助手**学习控制台**前端教材；**不接公司生产、不自动过账/改库存。**  
> **形式：** 与第 1 / 4 月 **同等详细**；本文件为 30 天正文合订，**不是缩写版大纲。** 代码骨架均在 Markdown 中；由你自行粘贴到独立目录 `erp-ai-console/`（学习仓根下），**不要**静默修改 `erp-ai-assistant/` 内已有应用代码。  
> **技术栈（本月固定）：**  
> - **Vue 3** + **Vite** + **`<script setup>` Composition API**  
> - 推荐：**Vue Router**（面板作路由）、**Pinia**（身份头 / session）、纯 CSS 或 **scoped CSS** + **CSS 变量设计令牌**（**不要求** Element Plus 等 UI 库；附录仅可选提及）  
> - **开发：** Vite dev server + `server.proxy` → Spring Boot `http://localhost:8080`，代理前缀 `/api`  
> - **生产学习路径：** `vite build` 产物可拷到 `erp-ai-assistant/src/main/resources/static/` **或** `vite preview` 独立预览 — 讲义讲清两种方式  
> - 调用仓库**学习期 REST**：`/api/ai/chat`、`/api/ai/rag/ask`、`/api/ai/flow/*`、`/api/ai/eval/*`、`/api/ai/stats`、`/api/ai/feedback`  
> - 学习请求头：`X-User-Id` / `X-Roles` / `X-Tenant-Id`（可选 `X-Admin-Token`、`X-Trace-Id`）  
> **前置：** 建议已完成第 1 月 Chat/RAG 概念；可与第 2～5 月**并行**阅读，但 API 以本仓库学习接口为准。  
> **每天结构（固定六段）：** 为什么 → 概念加深 → 怎么做 → 代码骨架 → 坑与排障 → 当天验收。  
> **入口：** `docs/WEB.md`  
> **视觉纪律：** 避免「AI 紫 + 奶油白」模板审美；用**有性格的字体系**与**清晰层级**；学习控制台可比营销页更密，但**一屏一事**；少卡片堆砌；第 4 周在 2～3 处加**有意图**的轻动效。  
> **与第 4 月关系：** 本文是**独立 Web 轨道**，用 Vue 端到端搭控制台；第 4 月 M4-D22～D26 控制台章节可作**可选平行阅读**。

---

## Web 轨道总目标（学完应能对外讲 8～10 分钟）

1. **工程起步：** Vite + Vue 3 SFC、响应式 `ref`/`computed`、设计令牌、App 壳布局、a11y、代理连后端。  
2. **对接后端：** `api/http.js` + composable、`Chat`/`RAG` 组件、sources 与 traceId、loading/错误态。  
3. **治理 UI：** Pinia 身份头、Flow start/decide、Feedback 三键、Eval 表、stats 侧栏、换角色看 sources。  
4. **作品交付：** 持久化、快捷键、轻动效、响应式、Router 整理、`vite build` 进 static、彩排、截图、口述收官。  
5. **工程纪律：** 开发用 proxy、生产注意 `base` 路径、不新开写库魔法接口、XSS 与 `v-html`、APPROVE≠写库。

### 与后端月份的关系

```text
第1月  懂 Chat/RAG API 与 JSON 响应
第2月  懂 Flow HITL、Eval、stats（可选并行）
第3月  懂 Store/reindex/audit（前端只调 REST）
第4月  懂 ACL/反馈/多租户（前端用 Pinia 模拟头）
第5月  可选并行（治理深化）
Web轨  用 Vue 3 页面把上述能力「可演示」—— 本月专注 SFC/状态/路由，不是再讲一遍 Java
```

### 四周路线图

| 周 | Day | 主题 | 结束产出 |
|---|---|---|---|
| 1 | WEB-D1～7 | Vue/Vite 工程、SFC、响应式、tokens、壳布局、a11y、proxy | `npm run dev` 可开壳 + 代理通 |
| 2 | WEB-D8～14 | api 模块、Chat/RAG、loading/error、traceId | 双面板联调成功 |
| 3 | WEB-D15～21 | Pinia、Flow、Feedback、Eval、stats | 换角色 sources 变 |
| 4 | WEB-D22～30 | 持久化、快捷键、动效、响应式、Router、build、彩排、作品集 | 可演示 + 可 build |

### 推荐项目目录（全月目标）

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

### 学习头速查（全月通用）

```text
X-User-Id:     demo-user-01
X-Roles:       FINANCE              # 逗号分隔：FINANCE,PROCUREMENT,ADMIN
X-Tenant-Id:   tenant-a             # RAG/Flow 建议必带
X-Admin-Token: <可选>               # reindex 等管理接口
X-Trace-Id:    <可选>               # 未传则服务端生成；响应 traceId 要显示
```

### 与第 4 月控制台章节对照（可选平行阅读）

| Web 轨道 | 第 4 月 | 说明 |
|---|---|---|
| WEB-D5～6 壳布局 | M4-D22 IA | Web 轨从 Vue SFC 讲起；M4 假设你已会写页面 |
| WEB-D9 http.js | M4-D23 fetch | 同一学习头纪律 |
| WEB-D15 Pinia 身份 | M4-D4 学习头 | store ↔ 请求头 |
| WEB-D18 Feedback | M4-D8～12 | UI 三键 ↔ JSONL |
| WEB-D28 彩排 | M4-D24～26 | 场景 A/B/C 剧本可复用 |

---


# 第 1 周｜Vue/Vite 工程起步、SFC、响应式、设计令牌、壳布局、a11y、代理连后端

---

## WEB-D1 浏览器、Vue 3 学习控制台与 Vite 心智
### 为什么

第 1 周主题增量：**SPA/SFC/Vite 心智与线框**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：浏览器、Vue 3 学习控制台与 Vite 心智。

### 概念加深

**今日核心技能：** SPA/SFC/Vite 心智与线框


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 今天只建目录树与 ASCII 壳，不静默改仓库

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「浏览器、Vue 3 学习控制台与 Vite 心智」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

### 术语

| 术语 | 一句话 |
|---|---|
| Vite | 开发服务器 + 打包器 |
| SFC | 单文件 Vue 组件 |
| proxy | dev 时把 `/api` 转到 8080 |

---

## WEB-D2 创建 Vite + Vue 3 工程与首屏
### 为什么

第 1 周主题增量：**npm create vite、main.js 挂载**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：创建 Vite + Vue 3 工程与首屏。

### 概念加深

**今日核心技能：** npm create vite、main.js 挂载


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** `npm run dev` 看到学习控制台标题

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「创建 Vite + Vue 3 工程与首屏」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D3 单文件组件 SFC 与 `<script setup>` 结构
### 为什么

第 1 周主题增量：**template/script/style 三区、setup 语法糖**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：单文件组件 SFC 与 `<script setup>` 结构。

### 概念加深

**今日核心技能：** template/script/style 三区、setup 语法糖


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 拆分 AppHeader.vue 占位组件

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「单文件组件 SFC 与 `<script setup>` 结构」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D4 响应式基础：`ref`、`reactive` 与模板插值
### 为什么

第 1 周主题增量：**ref 解包、表单 v-model**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：`ref`、`reactive` 与模板插值。

### 概念加深

**今日核心技能：** ref 解包、表单 v-model


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 计数器与占位 traceId 绑定

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「响应式基础：`ref`、`reactive` 与模板插值」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D5 `computed` 与设计令牌 tokens.css
### 为什么

第 1 周主题增量：**派生状态、:root CSS 变量**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：`computed` 与设计令牌 tokens.css。

### 概念加深

**今日核心技能：** 派生状态、:root CSS 变量


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** tokens.css 工具蓝强调色

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「`computed` 与设计令牌 tokens.css」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D6 App 壳布局：Flex/Grid 与 scoped CSS
### 为什么

第 1 周主题增量：**三栏 grid、AppShell.vue**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：Flex/Grid 与 scoped CSS。

### 概念加深

**今日核心技能：** 三栏 grid、AppShell.vue


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** nav/main/aside 分区完成

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「App 壳布局：Flex/Grid 与 scoped CSS」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D7 Vite proxy 连 Spring Boot 与 a11y 起步
### 为什么

第 1 周主题增量：**server.proxy、skip-link、focus-visible**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：Vite proxy 连 Spring Boot 与 a11y 起步。

### 概念加深

**今日核心技能：** server.proxy、skip-link、focus-visible


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** dev 代理 `/api` 通、Tab 可走

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「Vite proxy 连 Spring Boot 与 a11y 起步」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

### proxy 排障

| 现象 | 处理 |
|---|---|
| 404 on /api | Spring 未启或路径少 `/api` 前缀 |
| 502 | 8080 无进程 |
| 200 但 HTML | proxy 路径写错，打到静态页 |

---


# 第 2 周｜api 模块 / composable、Chat、RAG、loading/error、traceId、双面板整合

---

## WEB-D8 fetch 入门与 composable 心智
### 为什么

第 2 周主题增量：**fetch、resp.ok、useXxx 模式**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：fetch 入门与 composable 心智。

### 概念加深

**今日核心技能：** fetch、resp.ok、useXxx 模式


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 浏览器 Console 试 POST

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「fetch 入门与 composable 心智」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D9 `api/http.js`：学习头封装与 trace 透传
### 为什么

第 2 周主题增量：**apiFetch、Pinia 头占位**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：学习头封装与 trace 透传。

### 概念加深

**今日核心技能：** apiFetch、Pinia 头占位


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 统一请求出口

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「`api/http.js`：学习头封装与 trace 透传」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D10 Chat 组件：输入、答案与 need_human
### 为什么

第 2 周主题增量：**ChatPanel.vue、sessionId**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：输入、答案与 need_human。

### 概念加深

**今日核心技能：** ChatPanel.vue、sessionId


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** POST /api/ai/chat 渲染答案

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「Chat 组件：输入、答案与 need_human」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D11 RAG 组件：问题、答案与 sources 列表
### 为什么

第 2 周主题增量：**v-for sources、禁止 v-html**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：问题、答案与 sources 列表。

### 概念加深

**今日核心技能：** v-for sources、禁止 v-html


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** POST /api/ai/rag/ask

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「RAG 组件：问题、答案与 sources 列表」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D12 Loading、空态与错误态组件模式
### 为什么

第 2 周主题增量：**isLoading、errorMessage、空文案**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：Loading、空态与错误态组件模式。

### 概念加深

**今日核心技能：** isLoading、errorMessage、空文案


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 三态 UI 可切换

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「Loading、空态与错误态组件模式」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D13 traceId 诊断条与请求日志
### 为什么

第 2 周主题增量：**TraceStrip、trace store**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：traceId 诊断条与请求日志。

### 概念加深

**今日核心技能：** TraceStrip、trace store


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 侧栏显示最近 traceId

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「traceId 诊断条与请求日志」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

### Pinia 安装（今日首次）

```bash
npm i pinia
```

`main.js`：`import { createPinia } from 'pinia'` → `createApp(App).use(createPinia()).mount('#app')`

---

## WEB-D14 第 2 周整合：Chat + RAG 双面板联调
### 为什么

第 2 周主题增量：**面板切换、共享 trace**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：Chat + RAG 双面板联调。

### 概念加深

**今日核心技能：** 面板切换、共享 trace


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 两面板连续提问成功

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「第 2 周整合：Chat + RAG 双面板联调」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---


# 第 3 周｜Pinia 身份头、Flow、Feedback、Eval、stats、换角色看 sources

---

## WEB-D15 Pinia 身份条：UserId / Roles / Tenant
### 为什么

第 3 周主题增量：**stores/identity.js、IdentityBar**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：UserId / Roles / Tenant。

### 概念加深

**今日核心技能：** stores/identity.js、IdentityBar


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 头写入 apiFetch

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「Pinia 身份条：UserId / Roles / Tenant」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

### Pinia 安装

```bash
npm i pinia
```

`main.js`：`createApp(App).use(createPinia())`

---

## WEB-D16 Flow 组件：start 与 pending 列表
### 为什么

第 3 周主题增量：**FlowPanel start/pending**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：start 与 pending 列表。

### 概念加深

**今日核心技能：** FlowPanel start/pending


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** POST start + GET pending

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「Flow 组件：start 与 pending 列表」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D17 Flow decide：APPROVE / REJECT / EDIT
### 为什么

第 3 周主题增量：**决策按钮、APPROVE≠写库文案**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：APPROVE / REJECT / EDIT。

### 概念加深

**今日核心技能：** 决策按钮、APPROVE≠写库文案


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** decide 后列表更新

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「Flow decide：APPROVE / REJECT / EDIT」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D18 Feedback 三键：useful / wrong / unsafe
### 为什么

第 3 周主题增量：**FeedbackBar、挂 traceId**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：useful / wrong / unsafe。

### 概念加深

**今日核心技能：** FeedbackBar、挂 traceId


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** POST feedback 成功

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「Feedback 三键：useful / wrong / unsafe」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D19 Eval 触发与结果表
### 为什么

第 3 周主题增量：**EvalPanel、表格渲染**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：Eval 触发与结果表。

### 概念加深

**今日核心技能：** EvalPanel、表格渲染


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** run + runs 列表

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「Eval 触发与结果表」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D20 stats 侧栏：token、反馈计数
### 为什么

第 3 周主题增量：**StatsAside、定时刷新可选**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：token、反馈计数。

### 概念加深

**今日核心技能：** StatsAside、定时刷新可选


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** GET /api/ai/stats

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「stats 侧栏：token、反馈计数」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D21 第 3 周整合：换角色 → sources 变
### 为什么

第 3 周主题增量：**切 tenant/role 再 RAG**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：换角色 → sources 变。

### 概念加深

**今日核心技能：** 切 tenant/role 再 RAG


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 同问不同 sources

### 怎么做


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


### 代码骨架

```text
整合彩排（笔记剧本）：
1. IdentityBar 选 FINANCE + tenant-a → RAG 提问 → 记录 sources 数量
2. 改 PROCUREMENT + tenant-b → 同一问题 → sources 应不同（若后端配置支持）
3. Chat 一条 → Feedback useful → stats 刷新看计数变化
4. Flow start → APPROVE → 确认 UI 文案含「不等于写库」
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「第 3 周整合：换角色 → sources 变」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---


# 第 4 周｜持久化、快捷键、动效、响应式、Router、build、彩排、作品集、收官

---

## WEB-D22 localStorage：身份与最近 session 持久化
### 为什么

第 4 周主题增量：**pinia 插件或 watch 持久化**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：身份与最近 session 持久化。

### 概念加深

**今日核心技能：** pinia 插件或 watch 持久化


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 刷新后身份仍在

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「localStorage：身份与最近 session 持久化」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D23 键盘快捷键：聚焦输入、发送、切路由
### 为什么

第 4 周主题增量：**@keydown、router.push**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：聚焦输入、发送、切路由。

### 概念加深

**今日核心技能：** @keydown、router.push


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** Ctrl+Enter 发送

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「键盘快捷键：聚焦输入、发送、切路由」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D24 有意图的动效：两处微交互
### 为什么

第 4 周主题增量：**transition、prefers-reduced-motion**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：两处微交互。

### 概念加深

**今日核心技能：** transition、prefers-reduced-motion


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 提交与成功轻反馈

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「有意图的动效：两处微交互」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D25 响应式：窄屏单列与表格横滚
### 为什么

第 4 周主题增量：**media query、overflow-x**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：窄屏单列与表格横滚。

### 概念加深

**今日核心技能：** media query、overflow-x


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 手机宽可读完

### 怎么做


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


### 代码骨架

```css
/* layout 响应式 */
@media (max-width: 768px) {
  .app-shell { grid-template-columns: 1fr; }
  .identity-bar { flex-direction: column; gap: var(--space-2); }
  table { display: block; overflow-x: auto; }
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「响应式：窄屏单列与表格横滚」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D26 Vue Router 整理：面板作路由
### 为什么

第 4 周主题增量：**router/index.js、RouterLink**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：面板作路由。

### 概念加深

**今日核心技能：** router/index.js、RouterLink


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** /chat /rag /flow 可分享

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「Vue Router 整理：面板作路由」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

### Router 安装

```bash
npm i vue-router@4
```

---

## WEB-D27 `vite build` 与拷入 Spring static
### 为什么

第 4 周主题增量：**base 路径、dist 部署两法**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：`vite build` 与拷入 Spring static。

### 概念加深

**今日核心技能：** base 路径、dist 部署两法


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** build 产物 200

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「`vite build` 与拷入 Spring static」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

### build base 对照

| 部署 | base 建议 |
|---|---|
| Spring static 根 | `./` 或 `/` |
| 子路径 `/console/` | `base: '/console/'` |

---

## WEB-D28 端到端彩排：场景 A/B/C
### 为什么

第 4 周主题增量：**三场景剧本、≤15 分钟**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：场景 A/B/C。

### 概念加深

**今日核心技能：** 三场景剧本、≤15 分钟


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 彩排打勾

### 怎么做


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


### 代码骨架

```text
场景 A｜Chat 演示（3 分钟）
  打开 /chat → 提问 → 展示 answer + need_human + traceId

场景 B｜RAG + 身份（5 分钟）
  换 tenant/role → 同一问题 → 对比 sources → Feedback

场景 C｜Flow + Eval（5 分钟）
  start → pending → APPROVE（读免责声明）→ 跑 eval → stats

计时目标：≤ 15 分钟；Network 无 4xx（除故意测错）
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「端到端彩排：场景 A/B/C」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

### 8 分钟口述提纲

1. 问题（30s）2. 栈 Vue3+Vite（1min）3. 壳+tokens（1min）
4. API+Pinia（2min）5. 治理 Flow/Feedback（2min）6. 纪律（1min）7. 彩蛋（30s）

---

## WEB-D29 作品集截图与 PORTFOLIO 规范
### 为什么

第 4 周主题增量：**三张截图、README URL**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：作品集截图与 PORTFOLIO 规范。

### 概念加深

**今日核心技能：** 三张截图、README URL


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** PORTFOLIO 条目

### 怎么做


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


### 代码骨架

```text
PORTFOLIO 三张截图：
1. 全屏壳 + IdentityBar + trace 侧栏
2. RAG sources 列表（含角色差异标注）
3. Flow pending + Eval 表

README 补充：
  开发：cd erp-ai-console && npm run dev  （Vite 5173 + proxy）
  生产学习：build 后 static 或 preview URL
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「作品集截图与 PORTFOLIO 规范」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---

## WEB-D30 Web 轨收官：口述与进阶方向
### 为什么

第 4 周主题增量：**8 分钟提纲、能力复盘**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教 Java 与 REST；本月教**如何用 Vue 3 把 JSON 变成可读、可演示、可排障的界面**。今天聚焦：口述与进阶方向。

### 概念加深

**今日核心技能：** 8 分钟提纲、能力复盘


| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `apiFetch` + Vue 绑定 |
| 权限 | M4 ACL 学习头 | Pinia → `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D28 浏览器剧本 |


```text
今日增量 → erp-ai-console/src/ 某文件
         → npm run dev 热更新可见
         → 验收清单打勾
```

**今日结束应达到：** 30 天清单全绿

### 怎么做


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


### 代码骨架

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

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| CORS 报错 | 直连 8080 | 用 Vite proxy，路径写 `/api/...` |
| ref 忘 .value | script 里 undefined | script 中 `count.value++` |
| v-html XSS | 模型输出执行脚本 | 默认文本插值；sources 用 v-for |
| proxy 未启后端 | 502 / ECONNREFUSED | 先 `mvn spring-boot:run` |
| 改样式不生效 | scoped 选择器 | 用 `:deep()` 或全局 tokens |

### 当天验收

- 今日主题「Web 轨收官：口述与进阶方向」对应 SFC/模块已改且 `npm run dev` 可验证
- 六段讲义结构自检通过
- 能向同伴用 1 分钟讲清今天增量
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅本地 `erp-ai-console/` 与笔记；build 后再**手动**拷 static |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何用 Vue **显示**字段 |


### 演示自检（可选）

- `npm run dev` 刷新后界面仍正常  
- 浏览器 Console 无红色报错（Vue warn 要处理）  
- 若已接 API：Network 里 `/api/ai/*` 经 proxy 200  

---


---

# 附录

## 附录 A｜`erp-ai-console/` 文件布局（全月目标）

见讲义头「推荐项目目录」。`main.js` 典型入口：

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './styles/tokens.css'
import './styles/layout.css'

createApp(App).use(createPinia()).use(router).mount('#app')
```

## 附录 B｜CSS 变量清单（推荐起点）

| 变量 | 用途 |
|---|---|
| `--font-sans` / `--font-serif` / `--font-mono` | 字体族 |
| `--color-bg` / `--color-surface` | 背景层级 |
| `--color-text` / `--color-muted` | 正文与次要 |
| `--color-accent` | 唯一强调色（链接、焦点）— **工具蓝，非 AI 紫** |
| `--color-danger` / `--color-success` | 错误/成功 |
| `--space-1`～`--space-4` | 间距刻度 |
| `--radius` | 圆角 |

## 附录 C｜`api/http.js` 完整参考

纪律：

1. 所有面板只通过 `apiFetch` 发请求。  
2. 三学习头从 Pinia `identity` store 读取，禁止某组件写死 `FINANCE`。  
3. 成功响应若有 `traceId` 必须写入 `trace` store。  
4. 错误抛出带 `status` 与 `payload`，组件统一 `catch` 渲染。  
5. 开发走 Vite proxy；**不要**在前端写死后端完整 URL（除文档示例外）。

## 附录 D｜可访问性检查清单（演示前）

| 项 | 检查 |
|---|---|
| 焦点可见 | Tab 走一遍，每个控件有 `:focus-visible` |
| 表单标签 | 每个 `input`/`textarea` 有 `<label>` 或 `aria-label` |
| live 区域 | 答案区 `aria-live="polite"` |
| 对比度 | 正文与背景 ≥ 4.5:1 |
| 动效 | 尊重 `prefers-reduced-motion` |
| 仅颜色 | 错误不只靠红色，要有文案 |
| Router | 路由切换后焦点落到 `main` 或标题 |

## 附录 E｜API 对照表（学习期）

| 能力 | 方法 | 路径 | 请求体要点 | 响应要点 |
|---|---|---|---|---|
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

## 附录 F｜常见问题（FAQ）

**Q: 必须用 Element Plus 吗？**  
A: **本月不要求。** 纯 CSS + scoped 足够；组件库仅附录可选提及。

**Q: 开发时 fetch 报 CORS？**  
A: 用 Vite `server.proxy` 把 `/api` 转到 `http://localhost:8080`；**不要**在浏览器直连跨域后端。

**Q: `vite build` 后 Spring 里 404？**  
A: 检查 `base: './'` 或 `/`；静态资源路径与 `index.html` 引用一致；见 WEB-D27。

**Q: 与第 4 月 console 章重复吗？**  
A: 第 4 月偏「要有什么能力」；Web 轨从 **Vue 3 工程化**讲到同一演示面。可并行读 M4-D22～26。

**Q: 反馈点了没反应？**  
A: 先完成 RAG/Chat 拿到 `traceId`；查 Network 是否 `POST /api/ai/feedback` 与 body。

**Q: Flow 点了 APPROVE 会写库吗？**  
A: **学习期 UI 文案必须写清：APPROVE 只推进学习 Flow 状态，不等于生产过账/改库存。**

**Q: 能否接公司 SSO？**  
A: **学习期不做。** 用 Pinia 下拉模拟角色/租户即可。

## 附录 G｜学习纪律（Web 轨）

1. **一天一个增量**：先壳后面板，先 Chat 后 RAG 再 Flow。  
2. **只调现有 REST**：禁止在前端新开「写库」接口。  
3. **统一 apiFetch**：禁止某文件手写 fetch 漏学习头。  
4. **sources 用 `v-for` 列表**：禁止 `v-html` 拼 JSON。  
5. **XSS**：用户与模型输出默认文本插值；`v-html` 仅演示且消毒。  
6. **演示前三场景**：WEB-D28 剧本必跑。  
7. **明确不做**：生产 SSO、自动过账、在线微调 UI。  
8. **目录隔离**：`erp-ai-console/` 与仓库 Java 模块分开，build 产物再**手动**拷 static。

## 附录 H｜为何选 Vue 3（学习轨）

| 点 | 说明 |
|---|---|
| 渐进式 | 可先单文件组件，再 Router/Pinia |
| `<script setup>` | 样板少，适合教材粘贴 |
| 响应式 | `ref`/`computed` 直接映射 UI 状态 |
| 生态 | Router/Pinia 官方推荐，文档中文友好 |
| 与 Vanilla 对比 | 本月**直接上 Vue**，不再要求先写纯 DOM 版 |

可选：若团队已统一 React，可对照本轨 API 层迁移 hooks — **不要**一天重写全部。

## 附录 I｜可选：Element Plus（不强制）

仅当你**已完成**本 30 天且想练组件库时：

```bash
npm i element-plus
```

学习控制台优先保持**稀疏工具感**；表格可用原生 `<table>` + tokens。引入 UI 库不减免 a11y 与 traceId 纪律。

## 附录 J｜文档索引

| 文档 | 路径 |
|---|---|
| Web 轨详版（本文） | `docs/lessons/WEB_DAY1-30_COMBINED.md` |
| Web 轨入口 | `docs/WEB.md` |
| 第1月详版 | `docs/lessons/MONTH1_DAY1-30_COMBINED.md` |
| 第4月详版（控制台可选读） | `docs/lessons/MONTH4_DAY1-30_COMBINED.md` |
| 第5月详版（可选并行） | `docs/lessons/MONTH5_DAY1-30_COMBINED.md` |
| 作品集 | `docs/PORTFOLIO.md` |

## 附录 K｜修订记录

| 日期 | 说明 |
|---|---|
| 2026-08-15 | 从 **Vanilla HTML/CSS/JS** 全面改写为 **Vue 3 + Vite + Composition API** 逐日详版 |
| 2026-08-15 | 开发模式改为 Vite proxy；补充 Pinia/Router、build 进 static 两路径 |

---

> **读完 Web 轨后：** 你应能在浏览器里演示「换角色 → sources 变 → 点反馈 → 跑 eval」，并讲清每块 Vue 组件对应哪条 REST。接公司生产前，还需真实 IAM、前端构建链路与安全评审——本轨刻意不覆盖。

