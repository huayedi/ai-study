# Web 前端轨道合并讲义（WEB-D1～WEB-D30）【逐日详版 · 与第1月同级 · 非概述】

> **定位：** 纯学习；通用 ERP AI 助手**学习控制台**前端教材；**不接公司生产、不自动过账/改库存。**  
> **形式：** 与第 1 / 4 月 **同等详细**；本文件为 30 天正文合订，**不是缩写版大纲。** 代码骨架均在 Markdown 中；由你自行粘贴到 `erp-ai-assistant/src/main/resources/static/`。  
> **技术栈（本月固定）：**  
> - **Vanilla HTML + CSS + JavaScript**（ES modules 可选，第 1 月 Web 轨道**不要求** React/Vue）  
> - 由 **Spring Boot** 托管静态资源：`src/main/resources/static/`  
> - 调用仓库**学习期 REST**：`/api/ai/chat`、`/api/ai/rag/ask`、`/api/ai/flow/*`、`/api/ai/eval/*`、`/api/ai/stats`、`/api/ai/feedback`（与第 4 月概念对齐）  
> - 学习请求头：`X-User-Id` / `X-Roles` / `X-Tenant-Id`（可选 `X-Admin-Token`、`X-Trace-Id`）  
> **前置：** 建议已完成第 1 月 Chat/RAG 概念；可与第 2～4 月**并行**阅读，但 API 以本仓库学习接口为准。  
> **每天结构（固定六段）：** 为什么 → 概念加深 → 怎么做 → 代码骨架 → 坑与排障 → 当天验收。  
> **入口：** `docs/WEB.md`  
> **视觉纪律：** 避免「AI 紫 + 奶油白」模板审美；用**有性格的字体系**与**清晰层级**；学习控制台可比营销页更密，但**一屏一事**；少卡片堆砌；第 4 周在 2～3 处加**有意图**的轻动效。  
> **与第 4 月关系：** 本文是**独立 Web 轨道**；第 4 月 M4-D22～D26 控制台章节可作**可选平行阅读**，但 Web 轨道必须能单独学完 HTML/CSS/JS 与产品 UI。

---

## Web 轨道总目标（学完应能对外讲 8～10 分钟）

1. **页面基础：** 语义 HTML、CSS 变量设计系统、Flex/Grid 布局、可访问性起步。  
2. **对接后端：** `fetch` 封装、Chat/RAG 面板、sources 与 traceId 展示、loading/错误态。  
3. **治理 UI：** 角色/租户头切换、Flow start/decide、Feedback 三键、Eval 触发与结果表、stats 侧栏。  
4. **作品交付：** localStorage 持久化、快捷键、响应式、端到端彩排、截图规范、口述收官。  
5. **工程纪律：** 同域 static、不新开写库魔法接口、XSS 防护、与后端 ACL/tenant 概念一致。

### 与后端月份的关系

```text
第1月  懂 Chat/RAG API 与 JSON 响应
第2月  懂 Flow HITL、Eval、stats（可选并行）
第3月  懂 Store/reindex/audit（前端只调 REST）
第4月  懂 ACL/反馈/多租户（前端用头模拟）
Web轨  用 Vanilla 页面把上述能力「可演示」—— 本月专注 HTML/CSS/JS，不是再讲一遍 Java
```

### 四周路线图

| 周 | Day | 主题 | 结束产出 |
|---|---|---|---|
| 1 | WEB-D1～7 | 页面基础与视觉系统 | `index.html` 壳 + tokens.css + 本地 Spring 打开 |
| 2 | WEB-D8～14 | 对接后端 | fetch 工具 + Chat/RAG 面板 + traceId 条 |
| 3 | WEB-D15～21 | 工作流与治理 UI | 身份头 + Flow + Feedback + Eval + stats |
| 4 | WEB-D22～30 | 打磨与作品 | 持久化/快捷键/动效/响应式/彩排/收官 |

### 推荐 static 目录（全月目标）

```text
erp-ai-assistant/src/main/resources/static/
  index.html              # 学习控制台入口（或 console.html）
  css/
    tokens.css            # 设计变量
    layout.css            # 壳布局
    components.css        # 面板、按钮、表格
  js/
    api.js                # fetch 封装 + 学习头
    chat.js               # Chat 面板
    rag.js                # RAG + sources
    flow.js               # Flow 面板
    feedback.js           # 反馈按钮
    eval.js               # Eval 触发与表格
    storage.js            # localStorage
    shortcuts.js          # 键盘快捷键
    app.js                # 初始化
```

### 学习头速查（全月通用）

```text
X-User-Id:     demo-user-01
X-Roles:       FINANCE              # 逗号分隔：FINANCE,PROCUREMENT,ADMIN
X-Tenant-Id:   tenant-a             # RAG/Flow 建议必带
X-Admin-Token: <可选>               # reindex 等管理接口
X-Trace-Id:    <可选>               # 未传则服务端生成
```

### 与第 4 月控制台章节对照（可选平行阅读）

| Web 轨道 | 第 4 月 | 说明 |
|---|---|---|
| WEB-D5～7 壳布局 | M4-D22 IA | Web 轨从 HTML/CSS 讲起；M4 假设你已会写页面 |
| WEB-D9 api.js | M4-D23 fetch | 同一 `apiHeaders()` 纪律 |
| WEB-D15 身份条 | M4-D4 学习头 | 前端控件 ↔ 请求头 |
| WEB-D18 Feedback | M4-D8～12 | UI 三键 ↔ JSONL |
| WEB-D26 彩排 | M4-D24～26 | 场景 A/B/C 剧本可复用 |

---


# 第 1 周｜页面基础与视觉系统（HTML / CSS / 壳 / a11y / Spring static）

---

## WEB-D1 浏览器、学习控制台与第一份 HTML 文档

### 为什么

学习版 ERP AI 助手需要**人看得懂的界面**：观众不会盯 curl。第 1 月你学会了 `/api/ai/chat` 返回什么；本月从**浏览器如何呈现**讲起。今天不碰复杂框架，只建立「学习控制台」心智：左侧导航、中间工作区、右侧诊断条——全部用原生 HTML 搭骨架。

### 概念加深

| 概念 | 含义 | 本月怎么用 |
|---|---|---|
| 文档对象模型 DOM | 浏览器把 HTML 解析成树 | 后面用 JS 改 `#answer` 文本 |
| 学习控制台 | 调现有 REST 的静态页 | 不是生产运营后台 |
| 同域部署 | 页面与 API 同一 origin | 避免 CORS 折腾 |
| Spring static | `classpath:/static/` | 启动后 `http://localhost:8080/index.html` |

```text
┌─────────────────────────────────────────────────────────┐
│ ERP AI 学习控制台                    [身份区 占位]        │
├──────────┬──────────────────────────────┬───────────────┤
│ nav      │  main（问答/Flow）            │  aside 诊断    │
│ · Chat   │                              │  traceId      │
│ · RAG    │                              │  stats 占位   │
│ · Flow   │                              │               │
└──────────┴──────────────────────────────┴───────────────┘
```

**视觉方向（本月全程）：** 深色底 + 高对比正文 + **一种标题字体 + 一种正文字体**（如 `IBM Plex Sans` + `Source Serif 4`），**不要**满屏渐变紫。

### 怎么做

**Step 1｜创建目录（10 分钟）**  
在笔记画目录树（见讲义头「推荐 static 目录」），今天只建 `index.html`。

**Step 2｜写最小 HTML5 文档（25 分钟）**  
含 `lang="zh-CN"`、`<meta charset>`、`<title>`、壳分区 `header` / `nav` / `main` / `aside` / `footer`。

**Step 3｜浏览器本地预览（15 分钟）**  
可直接双击打开；记笔记：**file:// 调不了后端 API**，最终要在 Spring 里打开。

**Step 4｜对照第 1 月 Chat 响应（15 分钟）**  
读 `MONTH1` D1 JSON 字段：`answer`、`need_human`、`traceId`（若有）——后面要渲染到 `main`。

**Step 5｜写验收句（5 分钟）**  
「我能指出壳上五个区域各放什么。」

### 代码骨架

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>ERP AI 学习控制台</title>
  <link rel="stylesheet" href="/css/tokens.css" />
  <link rel="stylesheet" href="/css/layout.css" />
</head>
<body>
  <a class="skip-link" href="#main">跳到主内容</a>
  <header class="app-header">
    <h1 class="app-title">ERP AI 学习控制台</h1>
    <p class="app-subtitle">纯学习 · 不接生产</p>
  </header>
  <div class="app-shell">
    <nav class="app-nav" aria-label="主导航">
      <ul>
        <li><a href="#panel-chat">Chat</a></li>
        <li><a href="#panel-rag">RAG</a></li>
        <li><a href="#panel-flow">Flow</a></li>
      </ul>
    </nav>
    <main id="main" class="app-main" tabindex="-1">
      <section id="panel-chat" aria-labelledby="chat-heading">
        <h2 id="chat-heading">Chat</h2>
        <p class="placeholder">WEB-D10 接入 /api/ai/chat</p>
      </section>
    </main>
    <aside class="app-aside" aria-label="诊断信息">
      <div id="trace-strip">traceId: —</div>
    </aside>
  </div>
  <footer class="app-footer">
    <small>教材口径 · 反馈不自动改模型</small>
  </footer>
</body>
</html>
```

### 坑与排障

| 现象 | 可能原因 | 处理 |
|---|---|---|
| 中文乱码 | 缺 charset | `<meta charset="UTF-8">` |
| 壳挤成一团 | 无 layout.css | D5 再做；今天允许丑 |
| 直接双击调 API | file 协议 | D7 用 Spring 打开 |
| 一次塞满所有面板 | 范围蔓延 | 今天只要空壳 + 占位 |

### 当天验收

- 浏览器能打开 `index.html` 并看到五区壳  
- 能口述 DOM / 学习控制台 / 同域 三词  
- 笔记里有 ASCII 线框  
- 未引入 React/Vue（本月第 1 月不要求）


### 浏览器开发者工具（今日必开）

| 面板 | 今天用途 |
|---|---|
| Elements | 看 DOM 树是否与线框一致 |
| Console | 是否有 404（css 尚未创建可忽略） |
| Network | 预览 Spring 后再看请求 |

### 阅读作业（15 分钟）

- `docs/MONTH1.md` 入口 — 回忆 Chat 响应长什么样  
- `docs/MONTH4.md` 第 4 月控制台主题 — **可选**，知道月底要演示什么

### 术语

| 术语 | 一句话 |
|---|---|
| static | Spring 直接当文件发出的前端资源 |
| 壳 | 导航 + 主区 + 侧栏的空布局 |
| placeholder | 占位文案，提醒哪天一接 API |


---
## WEB-D2 语义 HTML：导航、区块与表单控件

### 为什么

第 1 周主题：**语义标签让控制台可被读屏理解，也让 CSS 有稳定挂钩**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`<nav>`/`<main>`/`<section>`/`<label>`。

### 概念加深

**今日核心技能：** `<nav>`/`<main>`/`<section>`/`<label>`；表单 `textarea`+`button`；`aria-labelledby`；避免 div 汤

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```html
<section id="panel-rag" aria-labelledby="rag-heading">
  <h2 id="rag-heading">RAG 问答</h2>
  <form id="rag-form">
    <label for="rag-question">问题</label>
    <textarea id="rag-question" name="question" rows="3" required
      placeholder="例如：采购订单审批流程是什么？"></textarea>
    <button type="submit">检索并生成</button>
  </form>
  <article aria-live="polite" id="rag-answer" class="answer-block"></article>
</section>
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「语义 HTML：导航、区块与表单控件」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### WEB-D2 增量文件对照

| 文件 | 今日是否修改 |
|---|---|
| index.html | 按需 |
| css/tokens.css | 按需 |
| css/layout.css | 按需 |
| css/components.css | 按需 |
| js/api.js | 第 2 周起 |
| js/app.js | 初始化各面板 |

### 常见 Console 报错

| 报错 | 原因 |
|---|---|
| Failed to fetch | 后端未启或路径错 |
| Unexpected token | 后端返回非 JSON |
| Cannot read property | DOM id 与 JS 不一致 |


---
## WEB-D3 CSS 变量与设计令牌（tokens.css）

### 为什么

第 1 周主题：**用 `--color-*`/`--space-*` 统一全站，避免 AI 模板紫**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`:root` 变量。

### 概念加深

**今日核心技能：** `:root` 变量；亮/暗两套可选；强调色只用一处；ERP 控制台偏「工具感」

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```css
/* css/tokens.css */
:root {
  --font-sans: "IBM Plex Sans", system-ui, sans-serif;
  --font-serif: "Source Serif 4", Georgia, serif;
  --font-mono: "IBM Plex Mono", ui-monospace, monospace;

  --color-bg: #0f1419;
  --color-surface: #1a2332;
  --color-border: #2d3a4f;
  --color-text: #e8edf4;
  --color-muted: #8b9cb3;
  --color-accent: #3d9ed6;       /* 工具蓝，非 AI 紫 */
  --color-danger: #e85d5d;
  --color-success: #4caf82;

  --space-1: 0.25rem;
  --space-2: 0.5rem;
  --space-3: 1rem;
  --space-4: 1.5rem;
  --radius: 6px;
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「CSS 变量与设计令牌（tokens.css）」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### 色板反例（避免）

| 反例 | 问题 |
|---|---|
| 满屏 `#7C3AED` 渐变 | 廉价「AI 风」 |
| 5 种强调色 | 无层级 |
| 浅灰字 on 浅灰底 | 对比不及格 |

### 推荐工具色

- 背景 `#0f1419` / 表面 `#1a2332`  
- 强调 **一处** 工具蓝 `#3d9ed6`  
- 危险/成功仅用于错误与正向反馈


---
## WEB-D4 字体与视觉层级：标题、正文、等宽诊断

### 为什么

第 1 周主题：**学习控制台信息密度高，靠字号/字重/行高分区，而不是多叠卡片**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`@font-face` 或 Google Fonts。

### 概念加深

**今日核心技能：** `@font-face` 或 Google Fonts；`clamp()` 响应字号；`.mono` 给 traceId；标题 font-family 与正文区分

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```css
/* css/components.css 片段 */
.app-title {
  font-family: var(--font-serif);
  font-size: clamp(1.25rem, 2vw, 1.75rem);
  font-weight: 600;
  letter-spacing: -0.02em;
}
.answer-block {
  font-family: var(--font-sans);
  font-size: 1rem;
  line-height: 1.6;
  max-width: 65ch;
}
#trace-strip {
  font-family: var(--font-mono);
  font-size: 0.8125rem;
  color: var(--color-muted);
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「字体与视觉层级：标题、正文、等宽诊断」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### WEB-D4 增量文件对照

| 文件 | 今日是否修改 |
|---|---|
| index.html | 按需 |
| css/tokens.css | 按需 |
| css/layout.css | 按需 |
| css/components.css | 按需 |
| js/api.js | 第 2 周起 |
| js/app.js | 初始化各面板 |

### 常见 Console 报错

| 报错 | 原因 |
|---|---|
| Failed to fetch | 后端未启或路径错 |
| Unexpected token | 后端返回非 JSON |
| Cannot read property | DOM id 与 JS 不一致 |


---
## WEB-D5 Flex 与 Grid：三栏壳布局

### 为什么

第 1 周主题：**壳布局定好后，后面只往 panel 里填内容**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`.app-shell { display:grid }`。

### 概念加深

**今日核心技能：** `.app-shell { display:grid }`；侧栏固定宽；主区 `minmax`；`gap` 用 token

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```css
/* css/layout.css */
.app-shell {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr) 260px;
  grid-template-rows: 1fr;
  min-height: calc(100vh - 120px);
  gap: var(--space-3);
}
.app-nav { border-right: 1px solid var(--color-border); padding: var(--space-3); }
.app-main { padding: var(--space-4); overflow-y: auto; }
.app-aside { border-left: 1px solid var(--color-border); padding: var(--space-3); }
@media (max-width: 768px) {
  .app-shell { grid-template-columns: 1fr; }
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「Flex 与 Grid：三栏壳布局」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### Grid 区域命名（可选进阶）

```css
.app-shell {
  grid-template-areas:
    "nav main aside";
}
.app-nav { grid-area: nav; }
.app-main { grid-area: main; }
.app-aside { grid-area: aside; }
```

### 一屏一事

| 区域 | 只做一件事 |
|---|---|
| nav | 切换面板 |
| main | 当前能力交互 |
| aside | 诊断与 stats |


---
## WEB-D6 可访问性起步：焦点、对比度、跳过链接

### 为什么

第 1 周主题：**演示时可能被问「残障用户能用吗」——学习期也要及格线**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`:focus-visible`。

### 概念加深

**今日核心技能：** `:focus-visible`；对比度 ≥4.5:1；`skip-link`；按钮勿只用颜色区分状态

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```css
.skip-link {
  position: absolute;
  left: -999px;
  top: var(--space-2);
  background: var(--color-accent);
  color: #000;
  padding: var(--space-2) var(--space-3);
  z-index: 100;
}
.skip-link:focus { left: var(--space-2); }
button:focus-visible,
textarea:focus-visible {
  outline: 2px solid var(--color-accent);
  outline-offset: 2px;
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「可访问性起步：焦点、对比度、跳过链接」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### WEB-D6 增量文件对照

| 文件 | 今日是否修改 |
|---|---|
| index.html | 按需 |
| css/tokens.css | 按需 |
| css/layout.css | 按需 |
| css/components.css | 按需 |
| js/api.js | 第 2 周起 |
| js/app.js | 初始化各面板 |

### 常见 Console 报错

| 报错 | 原因 |
|---|---|
| Failed to fetch | 后端未启或路径错 |
| Unexpected token | 后端返回非 JSON |
| Cannot read property | DOM id 与 JS 不一致 |


---
## WEB-D7 Spring Boot 托管 static：本地打开与缓存

### 为什么

第 1 周主题：**file:// 无法调 `/api/ai/*`；今天把页面放进 Spring 并跑通**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`src/main/resources/static/`。

### 概念加深

**今日核心技能：** `src/main/resources/static/`；`mvn spring-boot:run`；访问 `http://localhost:8080/index.html`；DevTools 可选

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

启动后 `GET /index.html` 应 200；为第 2 周 fetch 做准备

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```text
# 放置文件
erp-ai-assistant/src/main/resources/static/index.html
erp-ai-assistant/src/main/resources/static/css/tokens.css
erp-ai-assistant/src/main/resources/static/css/layout.css

# 启动（在 erp-ai-assistant 目录）
mvn spring-boot:run

# 浏览器
http://localhost:8080/index.html
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「Spring Boot 托管 static：本地打开与缓存」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### Spring static 排障

| HTTP | 含义 |
|---|---|
| 200 | 路径正确 |
| 404 | 文件不在 `resources/static/` 或文件名错 |
| 403 | 少见；查是否被安全链拦截 |

### curl 验证

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/index.html
# 期望 200
```

### 下周预告

第 2 周将在同一 URL 下 `fetch('/api/ai/chat')` —— 这就是同域的价值。


---

# 第 2 周｜对接后端（fetch / Chat / RAG / 错误态 / traceId）

---

## WEB-D8 fetch 入门：JSON、HTTP 状态与同域

### 为什么

第 2 周主题：**浏览器发请求 = 调你已经学过的 REST**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`fetch(url, {method, headers, body})`。

### 概念加深

**今日核心技能：** `fetch(url, {method, headers, body})`；`resp.ok`；`await resp.json()`；同域无需 CORS

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

对照 `POST /api/ai/chat`

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```javascript
// 临时写在 <script> 或 js/app.js — WEB-D9 再抽到 api.js
async function probeChat() {
  const resp = await fetch('/api/ai/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message: '你好，请用一句话介绍采购流程' })
  });
  if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
  const data = await resp.json();
  console.log('chat response', data);
  return data;
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「fetch 入门：JSON、HTTP 状态与同域」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### HTTP 状态速查

| 状态 | UI 建议 |
|---|---|
| 200 | 正常渲染 |
| 400 | 展示 `error` 字段 |
| 401/403 | 「权限/头缺失」文案 |
| 500 | 通用失败 + traceId |
| 网络断开 | 「无法连接后端，Spring 是否启动？」 |


---
## WEB-D9 api.js：学习头封装与 trace 透传

### 为什么

第 2 周主题：**每个面板都复制 headers 会错；统一 `apiHeaders()`**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`api.js` 导出 `apiFetch(path, options)`。

### 概念加深

**今日核心技能：** `api.js` 导出 `apiFetch(path, options)`；自动加 `Content-Type` 与三学习头；保存 `lastTraceId`

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

头字段与 M4-D4 一致

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```javascript
// js/api.js
const state = { lastTraceId: null, sessionId: null };

export function apiHeaders() {
  const userId = document.getElementById('hdr-user-id')?.value || 'demo-user-01';
  const roles = document.getElementById('hdr-roles')?.value || 'GUEST';
  const tenantId = document.getElementById('hdr-tenant')?.value || 'tenant-a';
  return {
    'Content-Type': 'application/json',
    'X-User-Id': userId,
    'X-Roles': roles,
    'X-Tenant-Id': tenantId,
  };
}

export async function apiFetch(path, { method = 'GET', body } = {}) {
  const resp = await fetch(path, {
    method,
    headers: apiHeaders(),
    body: body != null ? JSON.stringify(body) : undefined,
  });
  const text = await resp.text();
  const data = text ? JSON.parse(text) : {};
  if (!resp.ok) {
    const err = new Error(data.message || data.error || `HTTP ${resp.status}`);
    err.status = resp.status;
    err.payload = data;
    throw err;
  }
  if (data.traceId) state.lastTraceId = data.traceId;
  return data;
}

export function getLastTraceId() { return state.lastTraceId; }
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「api.js：学习头封装与 trace 透传」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### apiHeaders 与 M4 对照

```text
控件 hdr-user-id  →  X-User-Id
控件 hdr-roles    →  X-Roles（单选或逗号拼接）
控件 hdr-tenant   →  X-Tenant-Id
```

### 单元测试思维（前端）

在 Console 手动：

```javascript
import { apiHeaders } from './js/api.js';
// 改下拉后应看到 roles 变化
```

### 纪律

**禁止**在 `chat.js` 再写一套 headers。


---
## WEB-D10 Chat 面板：会话输入、JSON 答案与 need_human

### 为什么

第 2 周主题：**Chat 是最短闭环，先通再 RAG**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：表单提交拦截。

### 概念加深

**今日核心技能：** 表单提交拦截；渲染 `answer`；`need_human` 用徽章；禁用重复提交

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

`POST /api/ai/chat` body: `{sessionId?, message}`

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```javascript
// js/chat.js
import { apiFetch, getLastTraceId } from './api.js';

export function initChatPanel() {
  const form = document.getElementById('chat-form');
  const out = document.getElementById('chat-answer');
  const badge = document.getElementById('chat-need-human');
  const trace = document.getElementById('trace-strip');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const message = document.getElementById('chat-message').value.trim();
    if (!message) return;
    form.classList.add('is-loading');
    try {
      const data = await apiFetch('/api/ai/chat', {
        method: 'POST',
        body: { message },
      });
      out.textContent = data.answer ?? JSON.stringify(data, null, 2);
      badge.hidden = !data.need_human;
      badge.textContent = data.need_human ? '需人工确认' : '';
      trace.textContent = `traceId: ${getLastTraceId() ?? '—'}`;
    } catch (err) {
      out.textContent = `错误：${err.message}`;
    } finally {
      form.classList.remove('is-loading');
    }
  });
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「Chat 面板：会话输入、JSON 答案与 need_human」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### Chat 响应字段 UI 映射

| JSON 字段 | UI 元素 |
|---|---|
| `answer` | `#chat-answer` textContent |
| `need_human` | 徽章显示/隐藏 |
| `traceId` | `#trace-strip` |
| `sessionId` | 可写入 hidden input 供下轮 |

### HTML 片段

```html
<form id="chat-form">
  <label for="chat-message">消息</label>
  <textarea id="chat-message" rows="2" required></textarea>
  <button type="submit">发送</button>
  <span id="chat-need-human" class="badge" hidden></span>
</form>
<div id="chat-answer" class="answer-block" aria-live="polite"></div>
```


---
## WEB-D11 RAG 面板：问题框、答案与 sources 列表

### 为什么

第 2 周主题：**sources 是 RAG 可信度核心，必须结构化展示**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`<ol class="sources">`。

### 概念加深

**今日核心技能：** `<ol class="sources">`；每项 docId/score/snippet；空 sources 要有文案

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

`POST /api/ai/rag/ask`

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```javascript
// js/rag.js
import { apiFetch } from './api.js';

function renderSources(container, sources = []) {
  container.replaceChildren();
  if (!sources.length) {
    container.textContent = '（无命中来源 — 检查角色/租户或语料）';
    return;
  }
  const ol = document.createElement('ol');
  ol.className = 'sources-list';
  for (const s of sources) {
    const li = document.createElement('li');
    li.innerHTML = `<strong>${escapeHtml(s.docId)}</strong>
      <span class="score">score ${s.score?.toFixed?.(3) ?? '—'}</span>
      <p>${escapeHtml(s.snippet ?? '')}</p>`;
    ol.appendChild(li);
  }
  container.appendChild(ol);
}

function escapeHtml(s) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「RAG 面板：问题框、答案与 sources 列表」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### sources 展示纪律

1. **必须列表化** — 观众要能扫 docId。  
2. **空数组要有解释** — 如「无命中」而非空白。  
3. **snippet 勿 innerHTML 未转义** — 防 XSS。

### wireframe

```text
┌─ RAG ─────────────────────────┐
│ [问题 textarea          ] [问] │
│ 答案：........................ │
│ 来源：                         │
│  1. procurement-policy.md      │
│  2. erp-glossary.md            │
└────────────────────────────────┘
```


---
## WEB-D12 Loading、空态与错误态

### 为什么

第 2 周主题：**演示时网络会慢、API 会 400——界面不能傻等**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`.loading` 骨架。

### 概念加深

**今日核心技能：** `.loading` 骨架；`try/catch`；展示 `error.message`；按钮 `aria-busy`

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```css
.is-loading button[type="submit"] { opacity: 0.6; pointer-events: none; }
.is-loading button[type="submit"]::after { content: " …"; }
.error-banner {
  border-left: 4px solid var(--color-danger);
  background: color-mix(in srgb, var(--color-danger) 12%, transparent);
  padding: var(--space-2) var(--space-3);
}
.empty-state { color: var(--color-muted); font-style: italic; }
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「Loading、空态与错误态」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### 状态机（面板级）

```text
idle → submitting → success
                 ↘ error → idle
```

按钮在 `submitting` 时 `disabled` + `aria-busy="true"`。


---
## WEB-D13 traceId 诊断条与请求日志

### 为什么

第 2 周主题：**反馈与 Flow 都靠 traceId 关联**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：侧栏固定显示。

### 概念加深

**今日核心技能：** 侧栏固定显示；复制按钮；可选最近 5 条请求列表

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

与 M4 反馈飞轮衔接

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```javascript
// 侧栏 trace + 最近请求
const recent = [];
export function logRequest(meta) {
  recent.unshift(meta);
  if (recent.length > 5) recent.pop();
  const ul = document.getElementById('recent-requests');
  if (!ul) return;
  ul.replaceChildren(...recent.map(r => {
    const li = document.createElement('li');
    li.textContent = `${r.method} ${r.path} → ${r.status}`;
    return li;
  }));
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「traceId 诊断条与请求日志」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### WEB-D13 增量文件对照

| 文件 | 今日是否修改 |
|---|---|
| index.html | 按需 |
| css/tokens.css | 按需 |
| css/layout.css | 按需 |
| css/components.css | 按需 |
| js/api.js | 第 2 周起 |
| js/app.js | 初始化各面板 |

### 常见 Console 报错

| 报错 | 原因 |
|---|---|
| Failed to fetch | 后端未启或路径错 |
| Unexpected token | 后端返回非 JSON |
| Cannot read property | DOM id 与 JS 不一致 |


---
## WEB-D14 第 2 周整合：Chat+RAG 双面板联调

### 为什么

第 2 周主题：**一周结束应能「问 Chat、问 RAG、看见 traceId」**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：面板切换不丢状态。

### 概念加深

**今日核心技能：** 面板切换不丢状态；统一 loading；周复盘表

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

回归：mock provider 下两接口

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```html
<!-- WEB-D14 增量：在昨日文件基础上追加 class 或脚本引用 -->
<!-- 详见本周前后天数骨架；保持与 tokens.css 变量一致 -->
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「第 2 周整合：Chat+RAG 双面板联调」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 第 2 周复盘表

| 项 | 是/否 |
|---|---|
| Chat 能问一句 |  |
| RAG 能显示 sources |  |
| 错误时有人话提示 |  |
| traceId 侧栏有值 |  |
| 未混用多套 fetch |  |

### 并行阅读（可选）

`docs/lessons/MONTH4_DAY1-30_COMBINED.md` M4-D23 静态页清单 — 对照你是否已覆盖 Chat/RAG。

### WEB-D14 增量文件对照

| 文件 | 今日是否修改 |
|---|---|
| index.html | 按需 |
| css/tokens.css | 按需 |
| css/layout.css | 按需 |
| css/components.css | 按需 |
| js/api.js | 第 2 周起 |
| js/app.js | 初始化各面板 |

### 常见 Console 报错

| 报错 | 原因 |
|---|---|
| Failed to fetch | 后端未启或路径错 |
| Unexpected token | 后端返回非 JSON |
| Cannot read property | DOM id 与 JS 不一致 |


---

# 第 3 周｜工作流与治理 UI（Flow / 头切换 / Feedback / Eval）

---

## WEB-D15 身份条：UserId / Roles / Tenant 切换

### 为什么

第 3 周主题：**模拟 ACL 与多租户在 UI 上必须一键切换**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：header 三个控件。

### 概念加深

**今日核心技能：** header 三个控件；变更写入 `apiHeaders()`；localStorage 下日再接

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

对照 M4-D4、M4-D18

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```html
<header class="identity-bar">
  <label>User <input id="hdr-user-id" value="demo-user-01" autocomplete="username" /></label>
  <label>Roles
    <select id="hdr-roles">
      <option value="GUEST">GUEST</option>
      <option value="FINANCE">FINANCE</option>
      <option value="PROCUREMENT">PROCUREMENT</option>
      <option value="ADMIN">ADMIN</option>
    </select>
  </label>
  <label>Tenant
    <select id="hdr-tenant">
      <option value="tenant-a">tenant-a</option>
      <option value="tenant-b">tenant-b</option>
    </select>
  </label>
</header>
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「身份条：UserId / Roles / Tenant 切换」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### 身份切换演示脚本

1. PROCUREMENT + 「采购审批制度」  
2. 改 FINANCE 不刷新页，再问  
3. 观众对比 sources docId 列表  

**头必须在 apiFetch 里读 DOM**，不能缓存旧值。


---
## WEB-D16 Flow 面板：start 与 pending 列表

### 为什么

第 3 周主题：**HITL 需要看见「待人类确认」**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`POST /api/ai/flow/start`。

### 概念加深

**今日核心技能：** `POST /api/ai/flow/start`；`GET /api/ai/flow/pending`；列表渲染

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

对照 MONTH3 Flow 章

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```javascript
// js/flow.js 片段
export async function startFlow(question) {
  return apiFetch('/api/ai/flow/start', { method: 'POST', body: { question } });
}
export async function loadPending() {
  return apiFetch('/api/ai/flow/pending');
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「Flow 面板：start 与 pending 列表」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### Flow 状态（UI 标签）

| status | 展示 |
|---|---|
| RUNNING | 灰 |
| WAIT_HUMAN | 强调 |
| APPROVED | 绿 |
| REJECTED | 红 |

列表项点击展开 detail，避免一屏堆满 JSON。


---
## WEB-D17 Flow decide：APPROVE / REJECT / EDIT

### 为什么

第 3 周主题：**按钮语义必须清晰，避免误触「写库」**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：三个按钮。

### 概念加深

**今日核心技能：** 三个按钮；`POST .../decide`；展示返回状态；AUDIT 只读链接

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```javascript
export async function decideFlow(flowId, decision, comment = '') {
  return apiFetch(`/api/ai/flow/${flowId}/decide`, {
    method: 'POST',
    body: { decision, comment },
  });
}
// decision: APPROVE | REJECT | EDIT
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「Flow decide：APPROVE / REJECT / EDIT」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### WEB-D17 增量文件对照

| 文件 | 今日是否修改 |
|---|---|
| index.html | 按需 |
| css/tokens.css | 按需 |
| css/layout.css | 按需 |
| css/components.css | 按需 |
| js/api.js | 第 2 周起 |
| js/app.js | 初始化各面板 |

### 常见 Console 报错

| 报错 | 原因 |
|---|---|
| Failed to fetch | 后端未启或路径错 |
| Unexpected token | 后端返回非 JSON |
| Cannot read property | DOM id 与 JS 不一致 |


---
## WEB-D18 Feedback 三键：useful / wrong / unsafe

### 为什么

第 3 周主题：**飞轮从 UI 开始**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`POST /api/ai/feedback`。

### 概念加深

**今日核心技能：** `POST /api/ai/feedback`；挂 `lastTraceId`；comment 可选；成功 toast

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

对照 M4-D8～12

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```javascript
// js/feedback.js
import { apiFetch, getLastTraceId } from './api.js';

export async function sendFeedback(kind) {
  const traceId = getLastTraceId();
  if (!traceId) throw new Error('无 traceId，请先完成一次 Chat/RAG');
  await apiFetch('/api/ai/feedback', {
    method: 'POST',
    body: {
      traceId,
      kind,
      comment: document.getElementById('fb-comment')?.value || '',
      endpoint: 'rag',
    },
  });
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「Feedback 三键：useful / wrong / unsafe」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### FeedbackKind 与按钮文案

| kind | 按钮建议文案 | 色语义 |
|---|---|---|
| USEFUL | 有用 | success |
| WRONG | 答案错 | danger |
| UNSAFE | 不安全 | danger + 二次确认 |

### 请求体

```json
{
  "traceId": "t-abc",
  "kind": "WRONG",
  "comment": "sources 缺 finance-close",
  "endpoint": "rag"
}
```

**不做：** 点踩后自动改答案或改 prompt。


---
## WEB-D19 Eval 触发与结果表

### 为什么

第 3 周主题：**一键跑 suite 并读 run 结果**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`POST /api/ai/eval/run`。

### 概念加深

**今日核心技能：** `POST /api/ai/eval/run`；表格 passed/total；链到 run id

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```javascript
// js/eval.js
export async function runEval(suite) {
  const run = await apiFetch('/api/ai/eval/run', {
    method: 'POST',
    body: { suite },
  });
  renderEvalTable(run);
  return run;
}

function renderEvalTable(run) {
  const tbody = document.querySelector('#eval-table tbody');
  const row = document.createElement('tr');
  row.innerHTML = `<td>${run.id}</td><td>${run.suite}</td>
    <td>${run.passed}/${run.total}</td><td>${run.passed === run.total ? '✓' : '✗'}</td>`;
  tbody.prepend(row);
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「Eval 触发与结果表」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### Eval 表结构

```html
<table id="eval-table">
  <thead>
    <tr><th>runId</th><th>suite</th><th>通过</th><th>OK?</th></tr>
  </thead>
  <tbody></tbody>
</table>
```

跑长跑时按钮显示 loading，防止连点。


---
## WEB-D20 stats 侧栏：token、反馈计数

### 为什么

第 3 周主题：**演示一眼看到「系统活着」**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`GET /api/ai/stats`。

### 概念加深

**今日核心技能：** `GET /api/ai/stats`；定时或按钮刷新；数字用 tabular-nums

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```javascript
export async function refreshStats() {
  const stats = await apiFetch('/api/ai/stats');
  document.getElementById('stat-feedback').textContent = stats.feedbackTotal ?? '—';
  document.getElementById('stat-tokens').textContent = stats.totalTokens ?? '—';
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「stats 侧栏：token、反馈计数」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### WEB-D20 增量文件对照

| 文件 | 今日是否修改 |
|---|---|
| index.html | 按需 |
| css/tokens.css | 按需 |
| css/layout.css | 按需 |
| css/components.css | 按需 |
| js/api.js | 第 2 周起 |
| js/app.js | 初始化各面板 |

### 常见 Console 报错

| 报错 | 原因 |
|---|---|
| Failed to fetch | 后端未启或路径错 |
| Unexpected token | 后端返回非 JSON |
| Cannot read property | DOM id 与 JS 不一致 |


---
## WEB-D21 第 3 周整合：换角色→sources 变

### 为什么

第 3 周主题：**治理 UI 串起来**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：手测矩阵 3 格。

### 概念加深

**今日核心技能：** 手测矩阵 3 格；Flow+Feedback 不互相覆盖 traceId

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

剧本预演 M4 场景 A

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```html
<!-- WEB-D21 增量：在昨日文件基础上追加 class 或脚本引用 -->
<!-- 详见本周前后天数骨架；保持与 tokens.css 变量一致 -->
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「第 3 周整合：换角色→sources 变」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 第 3 周复盘

| 项 | 是/否 |
|---|---|
| 换 Roles → sources 变 |  |
| Flow pending 能看见 |  |
| Feedback 依赖 traceId |  |
| Eval 表能新增一行 |  |
| stats 能刷新 |  |

### 手测矩阵（抄到笔记）

| Tenant | Role | 问题 | sources 期望 |
|---|---|---|---|
| tenant-a | FINANCE | 关账 | 含 finance doc |
| tenant-a | GUEST | 关账 | 少/无 |
| tenant-b | ADMIN | 供应商 | 不串 tenant-a |


---

# 第 4 周｜打磨与作品（持久化 / 快捷键 / 动效 / 响应式 / 收官）

---

## WEB-D22 localStorage：身份与最近 session 持久化

### 为什么

第 4 周主题：**刷新页面不丢演示设置**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`storage.js`。

### 概念加深

**今日核心技能：** `storage.js`；JSON 序列化；版本号 key；迁移策略

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```javascript
// js/storage.js
const KEY = 'erp-ai-console-v1';
export function savePrefs(prefs) {
  localStorage.setItem(KEY, JSON.stringify({ v: 1, ...prefs }));
}
export function loadPrefs() {
  try { return JSON.parse(localStorage.getItem(KEY) || '{}'); }
  catch { return {}; }
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「localStorage：身份与最近 session 持久化」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### localStorage 字段建议

| 字段 | 内容 |
|---|---|
| userId | 字符串 |
| roles | 字符串 |
| tenantId | 字符串 |
| lastSessionId | Chat 用 |
| panel | 上次打开的面板 id |

版本 `v:1` 便于以后迁移 key。


---
## WEB-D23 键盘快捷键：聚焦输入、发送、切面板

### 为什么

第 4 周主题：**彩排时少碰鼠标更专业**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`/` 聚焦。

### 概念加深

**今日核心技能：** `/` 聚焦；`Ctrl+Enter` 发送；`?` 显示帮助层

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```javascript
// js/shortcuts.js
document.addEventListener('keydown', (e) => {
  if (e.key === '/' && document.activeElement?.tagName !== 'TEXTAREA') {
    e.preventDefault();
    document.getElementById('rag-question')?.focus();
  }
  if (e.ctrlKey && e.key === 'Enter') {
    document.getElementById('rag-form')?.requestSubmit();
  }
});
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「键盘快捷键：聚焦输入、发送、切面板」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### 快捷键帮助层

```html
<dialog id="shortcut-help">
  <p><kbd>/</kbd> 聚焦问题框</p>
  <p><kbd>Ctrl</kbd>+<kbd>Enter</kbd> 提交</p>
</dialog>
```

`?` 键打开；Esc 关闭。


---
## WEB-D24 有意图的动效：两处微交互

### 为什么

第 4 周主题：**动效服务反馈，不做炫技**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：`@media (prefers-reduced-motion)`。

### 概念加深

**今日核心技能：** `@media (prefers-reduced-motion)`；提交按钮 loading；sources 展开过渡

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

全页不超过 2～3 处

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```css
@media (prefers-reduced-motion: no-preference) {
  .sources-list li {
    animation: fade-in 0.2s ease-out;
  }
}
@keyframes fade-in {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: none; }
}
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「有意图的动效：两处微交互」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### 动效纪律

| 允许 | 不允许 |
|---|---|
| 按钮 loading 点 | 全页 parallax |
| sources 淡入 | 无限循环闪动 |
| 面板切换 150ms | 拖慢演示的动画 |

务必加 `prefers-reduced-motion: reduce` 关闭动画。


---
## WEB-D25 响应式：窄屏单列与表格横滚

### 为什么

第 4 周主题：**笔记本投屏 + 手机自查**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：断点 768px。

### 概念加深

**今日核心技能：** 断点 768px；nav 变顶栏；表格 `overflow-x:auto`

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```html
<!-- WEB-D25 增量：在昨日文件基础上追加 class 或脚本引用 -->
<!-- 详见本周前后天数骨架；保持与 tokens.css 变量一致 -->
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「响应式：窄屏单列与表格横滚」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### 断点策略

| 宽度 | 布局 |
|---|---|
| ≥1024px | 三栏 |
| 768～1023 | 侧栏收窄 |
| <768px | 单列；aside 折叠到 main 下 |

表格在小屏 `overflow-x: auto`，不要挤爆字号。


---
## WEB-D26 端到端彩排：场景 A/B/C

### 为什么

第 4 周主题：**作品级串测**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：A 换角色 sources。

### 概念加深

**今日核心技能：** A 换角色 sources；B 反馈+jsonl；C 租户隔离；计时 15 分钟

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |

对照 M4-D24～26

```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```text
场景 A（ACL）  同问采购政策 → 换 GUEST → sources 减少
场景 B（反馈） RAG 一问 → 点 wrong → 查 stats/文件有记录
场景 C（租户） tenant-a vs tenant-b 同问 → sources 不串
计时 15 分钟；失败项记入 PORTFOLIO 已知限制
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「端到端彩排：场景 A/B/C」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 彩排计时单

| 分钟 | 动作 |
|---|---|
| 0～2 | 介绍控制台三柱（隔离/反馈/可演示） |
| 2～6 | 场景 A 换角色 |
| 6～10 | 场景 B 反馈 |
| 10～14 | 场景 C 租户 |
| 14～15 | Q&A |

### 失败处理

单步失败**不要**现场改 Java；指到 traceId + Network，说「学习期已知限制」。

### WEB-D26 增量文件对照

| 文件 | 今日是否修改 |
|---|---|
| index.html | 按需 |
| css/tokens.css | 按需 |
| css/layout.css | 按需 |
| css/components.css | 按需 |
| js/api.js | 第 2 周起 |
| js/app.js | 初始化各面板 |

### 常见 Console 报错

| 报错 | 原因 |
|---|---|
| Failed to fetch | 后端未启或路径错 |
| Unexpected token | 后端返回非 JSON |
| Cannot read property | DOM id 与 JS 不一致 |


---
## WEB-D27 作品集截图与 PORTFOLIO 规范

### 为什么

第 4 周主题：**好 UI 要会被记录**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：1440 宽截图。

### 概念加深

**今日核心技能：** 1440 宽截图；遮 API Key；三张图：壳/RAG sources/Eval 表

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```html
<!-- WEB-D27 增量：在昨日文件基础上追加 class 或脚本引用 -->
<!-- 详见本周前后天数骨架；保持与 tokens.css 变量一致 -->
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「作品集截图与 PORTFOLIO 规范」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 截图规范

| 图 | 内容 | 文件名建议 |
|---|---|---|
| 1 | 全壳 + 身份条 | `web-console-shell.png` |
| 2 | RAG + sources | `web-rag-sources.png` |
| 3 | Eval 结果表 | `web-eval-table.png` |

- 宽 1440px；隐藏 API Key；README 链接 `docs/PORTFOLIO.md`

### WEB-D27 增量文件对照

| 文件 | 今日是否修改 |
|---|---|
| index.html | 按需 |
| css/tokens.css | 按需 |
| css/layout.css | 按需 |
| css/components.css | 按需 |
| js/api.js | 第 2 周起 |
| js/app.js | 初始化各面板 |

### 常见 Console 报错

| 报错 | 原因 |
|---|---|
| Failed to fetch | 后端未启或路径错 |
| Unexpected token | 后端返回非 JSON |
| Cannot read property | DOM id 与 JS 不一致 |


---
## WEB-D28 口述彩排：8 分钟讲清 Web 轨

### 为什么

第 4 周主题：**面试/答辩常问前端你怎么接 AI API**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：结构：问题→壳→API→治理→纪律。

### 概念加深

**今日核心技能：** 结构：问题→壳→API→治理→纪律；计时

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```html
<!-- WEB-D28 增量：在昨日文件基础上追加 class 或脚本引用 -->
<!-- 详见本周前后天数骨架；保持与 tokens.css 变量一致 -->
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「口述彩排：8 分钟讲清 Web 轨」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### 8 分钟口述提纲

1. **问题**（30s）学习 AI 助手需要可演示 UI  
2. **栈**（1min）Vanilla + Spring static  
3. **壳**（1min）nav/main/aside + tokens  
4. **API**（2min）Chat/RAG + headers  
5. **治理**（2min）Flow/Feedback/Eval  
6. **纪律**（1min）不接生产、不自动改模型  
7. **彩蛋**（30s）快捷键或动效一处  


---
## WEB-D29 收官打磨：lint、死链、README 入口

### 为什么

第 4 周主题：**交付前清单**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：HTML 校验。

### 概念加深

**今日核心技能：** HTML 校验；链接 `/css`；README 增 console URL

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```html
<!-- WEB-D29 增量：在昨日文件基础上追加 class 或脚本引用 -->
<!-- 详见本周前后天数骨架；保持与 tokens.css 变量一致 -->
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「收官打磨：lint、死链、README 入口」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 与仓库的关系（今日提醒）

| 项 | 说明 |
|---|---|
| 改 Java？ | 本月**不要求**；API 以仓库已有学习接口为准 |
| 改哪 | 仅 `src/main/resources/static/` 与你本地笔记 |
| 对照 | 第 1 月懂 JSON 字段；本月懂如何**显示**字段 |

### 演示自检（可选）

- 刷新后界面仍正常  
- 控制台无红色报错（F12 Console）  
- 若已接 API：Network 里请求路径与附录 E 一致

### 收官检查清单

- [ ] `index.html` 引用 css/js 无 404  
- [ ] 身份切换后 Network 头正确  
- [ ] 三场景彩排 ≤15 分钟  
- [ ] PORTFOLIO 三张截图  
- [ ] README 有 `http://localhost:8080/index.html`  
- [ ] 未把 API Key 写进 JS  


---
## WEB-D30 Web 轨收官与进阶方向

### 为什么

第 4 周主题：**巩固 30 天成果，选一条下月前端线**。Web 轨道每天都要回答「观众在浏览器里看到什么变化」。后端月份教的是 Java 与 REST；本月教的是**如何用 HTML/CSS/JS 把响应变成可读、可演示、可排障的界面**。今天聚焦：复盘表。

### 概念加深

**今日核心技能：** 复盘表；可选读「何时上组件框架」；与第 5 月衔接

| 对照 | 后端月份（可选读） | 前端本月 |
|---|---|---|
| API | 第 1～4 月 Controller | `fetch` + JSON 渲染 |
| 权限 | M4 ACL 学习头 | 下拉框写入 `X-Roles` |
| 可演示 | M4 三场景彩排 | WEB-D26 浏览器剧本 |



```text
今日增量 → static/ 某文件几行
         → 浏览器刷新可见
         → 验收清单打勾
```

### 怎么做

**Step 1｜读昨天产物（10 分钟）**  
打开 `static/` 对应文件，确认昨天验收项仍绿。

**Step 2｜今日实现（45～60 分钟）**  
按「代码骨架」粘贴并改类名；每改一块就刷新浏览器。

**Step 3｜Network 面板（15 分钟）**  
F12 → Network：即使今天只做样式，也习惯打开面板，为第 2 周对接 API 做准备。

**Step 4｜笔记一条坑（5 分钟）**  
从「坑与排障」表选一行，写你是否遇到过。

**Step 5｜对照验收（10 分钟）**  
逐条打勾；未过则标「明天补」。

### 代码骨架

```text
Web 轨 30 天复盘：
  □ 壳 + tokens + a11y
  □ Chat/RAG + traceId
  □ 身份头 + Flow + Feedback + Eval
  □ 持久化 + 彩排 + 截图
下月可选：组件框架 / 可视化 Flow / 设计系统深化 — 只选一条
```

### 坑与排障

| 坑 | 现象 | 处理 |
|---|---|---|
| 改 CSS 不生效 | 缓存/路径错 | 硬刷新；查 `/css/` 是否 200 |
| 内联样式泛滥 | 难维护 | 用 class + tokens.css |
| 复制骨架漏闭合标签 | 布局炸裂 | 用编辑器格式化 HTML |
| 过早上框架 | 学不动基础 | 本月 Vanilla；见附录「何时再上组件框架」 |

### 当天验收

- 今日主题「Web 轨收官与进阶方向」对应文件已改且可刷新验证  
- 六段讲义结构自检通过  
- 能向同伴用 1 分钟讲清今天增量  
- 笔记链接到相关 API 或 MONTH 章节（若有）


### 30 天能力清单

```text
□ HTML 语义壳 + Spring static
□ CSS tokens 与三栏布局
□ a11y 基线（焦点/对比/live）
□ apiFetch + 三学习头
□ Chat / RAG / traceId
□ Flow / Feedback / Eval / stats
□ localStorage + 快捷键 + 轻动效
□ 响应式 + 三场景彩排 + 截图
```

### 下月前端方向（只选一条）

| 方向 | 内容 |
|---|---|
| 设计系统 | 深化 tokens、暗色主题、组件文档 |
| 轻量 Vue/React | 只迁移一个面板 |
| Flow 可视化 | 节点状态图 |
| 构建工具 | Vite 打包 static（仍不调生产） |

### 收官口述模板

```text
我用 Vanilla 静态页接了学习期 ERP AI REST：
身份头模拟 ACL/租户，RAG 展示 sources，
反馈挂 traceId，Eval 可回归。
明确不做 SSO 与自动过账。
```


---


# 附录

## 附录 A｜`static/` 文件布局（全月目标）

```text
erp-ai-assistant/src/main/resources/static/
  index.html
  css/
    tokens.css
    layout.css
    components.css
  js/
    api.js
    chat.js
    rag.js
    flow.js
    feedback.js
    eval.js
    storage.js
    shortcuts.js
    app.js
```

`index.html` 底部：

```html
<script type="module" src="/js/app.js"></script>
```

## 附录 B｜CSS 变量清单（推荐起点）

| 变量 | 用途 |
|---|---|
| `--font-sans` / `--font-serif` / `--font-mono` | 字体族 |
| `--color-bg` / `--color-surface` | 背景层级 |
| `--color-text` / `--color-muted` | 正文与次要 |
| `--color-accent` | 唯一强调色（链接、焦点） |
| `--color-danger` / `--color-success` | 错误/成功 |
| `--space-1`～`--space-4` | 间距刻度 |
| `--radius` | 圆角 |

## 附录 C｜`api.js` fetch 助手（完整参考）

见 WEB-D9 代码骨架；纪律：

1. 所有面板只通过 `apiFetch` 发请求。  
2. 三学习头从身份条读取，禁止某面板写死 `FINANCE`。  
3. 成功响应若有 `traceId` 必须更新 `state.lastTraceId`。  
4. 错误抛出带 `status` 与 `payload`，面板统一 `catch` 渲染。

## 附录 D｜可访问性检查清单（演示前）

| 项 | 检查 |
|---|---|
| 焦点可见 | Tab 走一遍，每个控件有 `:focus-visible` |
| 表单标签 | 每个 `input`/`textarea` 有 `<label>` 或 `aria-label` |
|  live 区域 | 答案区 `aria-live="polite"` |
| 对比度 | 正文与背景 ≥ 4.5:1 |
| 动效 | 尊重 `prefers-reduced-motion` |
| 仅颜色 | 错误不只靠红色，要有文案 |

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

**Q: 必须用 React 吗？**  
A: **本月不要求。** Vanilla HTML/CSS/JS 足够完成学习控制台；见附录 H。

**Q: 双击 html 为什么 fetch 失败？**  
A: `file://` 无同源 API。用 Spring Boot 托管 static（WEB-D7）。

**Q: 与第 4 月 console 章重复吗？**  
A: 第 4 月偏「要有什么能力」；Web 轨从 **HTML/CSS/JS 零基础**讲到同一演示面。可并行读 M4-D22～26。

**Q: 反馈点了没反应？**  
A: 先完成 RAG/Chat 拿到 `traceId`；查 Network 是否 `POST /api/ai/feedback` 与 body。

**Q: 能否接公司 SSO？**  
A: **学习期不做。** 用下拉模拟角色/租户即可。

## 附录 G｜学习纪律（Web 轨）

1. **一天一个增量**：先壳后面板，先 Chat 后 RAG 再 Flow。  
2. **只调现有 REST**：禁止在前端新开「写库」接口。  
3. **统一 apiFetch**：禁止某文件手写 fetch 漏学习头。  
4. **sources 用列表渲染**：禁止把 JSON 字符串直接 innerHTML。  
5. **XSS**：用户与模型输出用 `textContent` 或转义。  
6. **演示前三场景**：WEB-D26 剧本必跑。  
7. **明确不做**：生产 SSO、自动过账、在线微调 UI。

## 附录 H｜何时再上组件框架（可选）

| 条件 | 建议 |
|---|---|
| 已完成本 30 天且 Vanilla 控制台稳定 | 可考虑 Vue/React **一个**面板试点 |
| 仅为「看起来专业」 | 先打磨 tokens 与排版 |
| 团队已统一 React | 下月可迁移 `api.js` 逻辑到 hooks，**不要**一天重写全部 |

迁移时保留：`apiHeaders` 纪律、traceId 条、三场景彩排。

## 附录 I｜文档索引

| 文档 | 路径 |
|---|---|
| Web 轨详版（本文） | `docs/lessons/WEB_DAY1-30_COMBINED.md` |
| Web 轨入口 | `docs/WEB.md` |
| 第1月详版 | `docs/lessons/MONTH1_DAY1-30_COMBINED.md` |
| 第4月详版（控制台可选读） | `docs/lessons/MONTH4_DAY1-30_COMBINED.md` |
| 作品集 | `docs/PORTFOLIO.md` |

## 附录 J｜修订记录

| 日期 | 说明 |
|---|---|
| 2026-08-15 | 初版：Web 前端轨道 30 天逐日详版，与第1月同级 |

---

> **读完 Web 轨后：** 你应能在浏览器里演示「换角色 → sources 变 → 点反馈 → 跑 eval」，并讲清每块 UI 对应哪条 REST。接公司生产前，还需真实 IAM、前端构建链路与安全评审——本轨刻意不覆盖。

