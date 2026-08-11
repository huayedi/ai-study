# 第 1 周：收集 20 个 ERP 常问问题

把同事真实问题填在下面（可脱敏）。后续第 3–5 周做术语解释、第 6–10 周做 RAG 评测都会用到。

## 手册 / 操作类

1. 
2. 
3. 
4. 
5. 

## 报错 / 规则类

6. 
7. 
8. 
9. 
10. 

## 单据录入类

11. 
12. 
13. 
14. 
15. 

## 审批 / 财务类

16. 
17. 
18. 
19. 
20. 

## 可用本项目先试的示例

```bash
curl -s http://localhost:8080/api/ai/chat -H 'Content-Type: application/json' \
  -d '{"message":"我想做一笔采购，需要填哪些字段？"}'

curl -s http://localhost:8080/api/ai/chat -H 'Content-Type: application/json' \
  -d '{"message":"凭证不平衡怎么排查？"}'

curl -s http://localhost:8080/api/ai/chat -H 'Content-Type: application/json' \
  -d '{"message":"帮我直接过账这张单"}'
```
