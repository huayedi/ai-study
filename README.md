# ai-study

Java 5 年 ERP 工程师转 AI 应用（自学提升）仓库。

## 内容

| 路径 | 说明 |
|---|---|
| [docs/LEARNING_PLAN.md](docs/LEARNING_PLAN.md) | 20 周详细学习计划（每天 2–3 小时） |
| [docs/STUDY_NOTES.md](docs/STUDY_NOTES.md) | 每日订正笔记 + 代码对照 + 打卡跟踪 |
| [docs/lessons/](docs/lessons/) | 教材式讲义（推荐主学） |
| [docs/WEB.md](docs/WEB.md) | **Web 前端完整教材入口（Vue 3）** |
| [docs/lessons/WEB_VUE_COMPLETE.md](docs/lessons/WEB_VUE_COMPLETE.md) | Vue 学习控制台完整教科书 |
| [docs/lessons/MONTH_30_DAY_PLAN.md](docs/lessons/MONTH_30_DAY_PLAN.md) | **30 天逐日总计划** |
| [docs/lessons/day13-30-combined.md](docs/lessons/day13-30-combined.md) | **第1月 Day13～30 合并讲义** |
| [docs/lessons/MONTH2_DAY1-30_COMBINED.md](docs/lessons/MONTH2_DAY1-30_COMBINED.md) | **第2月整月讲义+对照代码** |
| [docs/lessons/MONTH3_DAY1-30_COMBINED.md](docs/lessons/MONTH3_DAY1-30_COMBINED.md) | **第3月整月讲义+对照代码** |
| [erp-ai-assistant/](erp-ai-assistant/) | 可运行 Spring Boot 项目（Chat + 学习版 RAG） |
| [erp-ai-console/](erp-ai-console/) | **Vue 3 学习控制台（已生成代码）** |

## IDEA 导入（Maven 没弹出来时）

**原因：** 可运行模块在子目录 `erp-ai-assistant/`。若只打开了空仓库、或未加载根 `pom.xml`，IDEA 不会自动弹出 Maven。

**推荐做法（任选其一）：**

1. **打开仓库根目录 `ai-study`**（根目录已有聚合 `pom.xml`）  
   - 若仍无 Maven 窗口：右键根目录 `pom.xml` → **Add as Maven Project**  
   - 或：`View` → `Tool Windows` → `Maven`
2. **直接打开子模块**：`File` → `Open` → 选择 `erp-ai-assistant` 目录（或该目录下的 `pom.xml`）
3. 确认已启用 Maven 插件：`Settings` → `Plugins` → 搜索 `Maven` → 勾选启用并重启
4. JDK：`Settings` → `Build, Execution, Deployment` → `Build Tools` → `Maven` → `Runner`  
   - JRE 选 **JDK 21**（或 Project SDK 21）
5. 仍不识别时：右键 `pom.xml` → `Maven` → `Reload project`

导入成功后，右侧应出现 **Maven** 工具窗口，能看到 `ai-study` / `erp-ai-assistant`。

## 立刻开始

```bash
cd erp-ai-assistant
mvn spring-boot:run
```

### Web 学习控制台（可选）

```bash
cd erp-ai-console
npm install
npm run dev
```

浏览器打开 http://localhost:5173 （`/api` 代理到后端 8080）。

另开终端：

```bash
curl -s http://localhost:8080/api/ai/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"我想做一笔采购，需要填哪些字段？"}'
```

默认 `mock` 模式，无需 API Key。完整说明见 `erp-ai-assistant/README.md`。
