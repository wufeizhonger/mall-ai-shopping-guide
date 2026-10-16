# AI 智能导购 Agent（二次开发说明）

> 在 mall-X（Spring Boot 3.5 + Spring AI 1.0 + Vue3）基础上，新增的 **基于 Function Calling 的多轮智能导购 Agent**。

## 一、业务背景

传统电商导购依赖人工客服或固定关键词回复，存在三个问题：用户需求表达模糊（"跑步用、预算 300"）、商品/库存/优惠等信息分散、多轮追问体验差。本模块让 LLM 通过 **工具调用（Function Calling）** 自主检索真实商品、查询库存与计算到手价，完成多轮导购。

## 二、核心能力

| 能力 | 说明 |
|---|---|
| 商品检索 | `searchProducts(keyword)`：按名称/品牌/品类/卖点/标签匹配，相关度排序 |
| 库存查询 | `checkStock(productId)`：实时库存，售罄可识别 |
| 到手价计算 | `calcFinalPrice(productId, couponCode)`：满减 + 优惠券（SAVE50 直减 / NEW10 九折） |
| 多轮记忆 | 按会话 ID 维护滑动窗口（默认 10 轮），控制上下文长度与 Token 成本 |
| 流式输出 | 提供 SSE `/ai/guide/stream`，打字机效果 |

## 三、架构

```
前端(Vue3)
   │  POST /ai/guide/chat | /ai/guide/stream
   ▼
ShoppingGuideController
   ▼
ShoppingGuideService ── 编排 ChatClient、记忆、商品目录
   ├── GuideMemoryStore(InMemory, 滑动窗口)   ← 可替换 Redis/JDBC
   ├── ShoppingTools (@Tool)                  ← LLM 自主调用
   │      └── ProductCatalog（检索/库存/到手价，纯逻辑）
   └── guideChatClient (Spring AI ChatClient + 输入安全 Advisor)
```

## 四、接口

- `POST /ai/guide/chat`：同步导购对话
  ```json
  { "sessionId": "sess-001", "message": "想跑步用，预算300以内，有什么推荐？", "couponCode": "SAVE50" }
  ```
- `POST /ai/guide/stream`：SSE 流式返回（`Content-Type: text/event-stream`）

## 五、测试

无 LLM / 无需 API Key 即可运行的单元测试，共 16 个：

- `ProductCatalogTest`（11）：检索、库存、满减、优惠券、叠加、价格下限
- `InMemoryGuideMemoryStoreTest`（5）：追加读取、窗口裁剪、会话隔离、清空、空值安全

```bash
mvn -pl mall-ai test -Dtest=ProductCatalogTest,InMemoryGuideMemoryStoreTest
# Tests run: 16, Failures: 0, Errors: 0
```

## 六、可扩展方向

- `ProductCatalog` 替换为 mall-portal Feign / 数据库，接入真实商品与库存；
- 记忆存储替换为 Redis，支持多实例部署与持久化；
- 引入 RAG：将商品详情/评价向量化，提升长尾需求的推荐准确率；
- 增加埋点与 RAGAS/LLM-as-a-Judge 评测，量化导购转化率。
