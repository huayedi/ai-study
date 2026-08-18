# ai-study

Java 5 年 ERP 工程师转 AI 应用（自学提升）仓库。

## 内容

| 路径 | 说明 |
|---|---|
| [docs/LEARNING_PLAN.md](docs/LEARNING_PLAN.md) | 20 周详细学习计划（每天 2–3 小时） |
| [docs/TECH.md](docs/TECH.md) | **技术节点路线图**：何时学 Spring AI / Python / Vue 等 |
| [docs/BILIBILI.md](docs/BILIBILI.md) | **B 站视频对照**：精选 BV + 与 MONTH 对齐 |
| [docs/ORAL_ANSWERS.md](docs/ORAL_ANSWERS.md) | **口述/自测标准答案**（MONTH1～8 · WEB） |
| [docs/MYSQL.md](docs/MYSQL.md) | **学习库 MySQL**（`154.8.183.10:3306/ai`，测试非生产） |
| [docs/STUDY_NOTES.md](docs/STUDY_NOTES.md) | 每日订正笔记 + 代码对照 + 打卡跟踪 |
| [docs/lessons/](docs/lessons/) | 教材式讲义（推荐主学） |
| [docs/MONTH1.md](docs/MONTH1.md) | 第1月入口 → 合订详版 |
| [docs/MONTH2.md](docs/MONTH2.md) | 第2月入口 |
| [docs/MONTH3.md](docs/MONTH3.md) | 第3月入口 |
| [docs/MONTH4.md](docs/MONTH4.md) | 第4月入口 |
| [docs/MONTH5.md](docs/MONTH5.md) | 第5月入口 |
| [docs/MONTH6.md](docs/MONTH6.md) | 第6月入口 |
| [docs/MONTH7.md](docs/MONTH7.md) | 第7月入口 |
| [docs/MONTH8.md](docs/MONTH8.md) | 第8月完整教材（工作流可视化，非 30 天） |
| [docs/WEB.md](docs/WEB.md) | Web 前端完整教材（Vue 3） |
| [erp-ai-assistant/](erp-ai-assistant/) | 可运行 Spring Boot 项目（Chat + 学习版 RAG） |

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

另开终端：

```bash
curl -s http://localhost:8080/api/ai/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"我想做一笔采购，需要填哪些字段？"}'
```

默认 `mock` 模式，无需 API Key。完整说明见 `erp-ai-assistant/README.md`。
