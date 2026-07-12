# Coin Marketplace 优化实施计划

> 2026-06-01

## 目标
把当前 `coin-marketplace` 项目从“可用但维护成本高”的状态升级为“可维护、可扩展、体验更好”的产品化基础。

## 本次优化范围
- 前端：建立共享 UI 组件，拆分重载页面逻辑，优化商品列表体验
- 后端：明确服务层与仓储层边界，清理控制器直连仓储的风险点
- 质量保障：补齐核心页面与接口测试目标，形成验收标准

## 优化任务

### 1. 前端共享组件与页面拆分
- 新增通用 UI 组件：
  - `UiProductCard`：商品卡片展示 + 收藏/加入购物车
  - `UiEmptyState`：空结果提示
  - `UiPagination`：分页控件
- 新增可复用逻辑：
  - `useProductSearch`：产品搜索、筛选、分页、排序逻辑
- 重构 `ProductList.vue`：
  - 使用共享组件
  - 简化视图逻辑，页面只负责布局和事件绑定
  - 保留现有筛选、分类、国家、价格、排序逻辑

### 2. 后端架构与边界
- 标记后端需要改进的高优先项：
  - `ProductController` 直接注入 `CategoryRepository`
  - 服务层与控制器层边界不够清晰
- 推荐后续改造方向：
  - 让 `ProductService` 统一处理分类与商品查询
  - `Controller` 只负责请求校验与响应封装

### 3. 质量与验收
- 确定核心验收场景：
  - 商品搜索与筛选
  - 商品列表展示
  - 加入购物车交互
  - 分页与空结果提示
- 下一步补测方向：
  - 前端组件/页面单元测试
  - 后端 `ProductController`/`ProductService` 测试

## 交付成果
- `frontend/store/src/components/ui/UiProductCard.vue`
- `frontend/store/src/components/ui/UiEmptyState.vue`
- `frontend/store/src/components/ui/UiPagination.vue`
- `frontend/store/src/composables/useProductSearch.js`
- `frontend/store/src/views/ProductList.vue`（已重构）
- 优化计划文档

## 验收标准
- 商品列表页在功能上与原来保持一致
- 页面结构更清晰，商品卡片逻辑迁移到共享组件
- 空状态和分页逻辑复用组件化实现
- `ProductList.vue` 的业务逻辑减少，加载与搜索职责转移到 composable
- 计划文档可作为后续迭代的落地指引
