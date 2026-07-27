# API Reference

Base URL: `http://localhost:8080`

All responses use the standard envelope:

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

Paginated responses contain:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [ ... ],
    "totalElements": 100,
    "totalPages": 5,
    "number": 0,
    "size": 20
  }
}
```

---

## 1. 认证管理 (Auth)

### POST /api/auth/register
用户注册

**Request Body:**
```json
{"username": "string", "password": "string", "email": "string"}
```

**Response (201):**
```json
{"code": 200, "message": "success", "data": {"token": "...", "user": {"id": 1, "username": "...", "email": "..."}}}
```

---

### POST /api/auth/login
用户登录

**Request Body:**
```json
{"username": "string", "password": "string"}
```

**Response:**
```json
{"code": 200, "message": "success", "data": {"token": "...", "user": {"id": 1, "username": "...", "role": "..."}}}
```

---

### POST /api/auth/forgot-password
发送密码重置邮件

**Request Body:**
```json
{"email": "string"}
```

**Response:**
```json
{"code": 200, "message": "success", "data": null}
```

---

### POST /api/auth/reset-password
使用重置令牌设置新密码

**Request Body:**
```json
{"token": "string", "newPassword": "string"}
```

**Response:**
```json
{"code": 200, "message": "success", "data": null}
```

---

## 2. 商品管理 (Products)

### GET /api/products
搜索商品

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 关键词 |
| categoryId | Long | 否 | 分类ID |
| ratingCompany | String | 否 | 评级公司 |
| ratingGrade | String | 否 | 评级等级 |
| minPrice | Double | 否 | 最低价格 |
| maxPrice | Double | 否 | 最高价格 |
| country | String | 否 | 国家 |
| year | Integer | 否 | 年份 |
| sort | String | 否 | 排序字段,方向 (例: `price,desc`) |
| page | int | 否 | 页码 (默认 0) |
| size | int | 否 | 每页数量 (默认 20) |

**Response (200):** 分页商品列表

---

### GET /api/products/categories
获取分类列表

**Response:**
```json
{"code": 200, "message": "success", "data": [{"id": 1, "name": "Ancient Coins", "slug": "ancient-coins"}]}
```

---

### GET /api/products/{id}
获取商品详情

**Path Parameters:** `id` - 商品ID

**Response:** 商品详细信息

---

### GET /api/products/{id}/related
获取相关商品推荐

**Path Parameters:** `id` - 商品ID

**Response:** 相关商品列表

---

### POST /api/products
创建商品 (需登录)

**Request Body:**
```json
{"title": "string", "description": "string", "price": 0.00, "categoryId": 1, ...}
```

**Response (201):** 创建的商品信息

---

### PUT /api/products/{id}
更新商品 (需登录)

**Path Parameters:** `id` - 商品ID

**Request Body:** 同创建商品

**Response:** 更新后的商品信息

---

### PUT /api/products/{id}/status
更新商品上下架状态 (需登录)

**Path Parameters:** `id` - 商品ID

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 是 | 状态值 (ACTIVE/INACTIVE) |

---

### POST /api/products/{id}/copy
复制商品 (需登录)

**Path Parameters:** `id` - 商品ID

**Response:** 复制的商品信息

---

### PUT /api/products/batch-status
批量更新商品状态 (需登录)

**Request Body:**
```json
{"ids": [1, 2, 3], "status": "ACTIVE"}
```

---

### GET /api/products/my
获取当前卖家商品列表 (需登录)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 (默认 0) |
| size | int | 否 | 每页数量 (默认 50) |

**Response:** 分页商品列表

---

### POST /api/products/{id}/mark-printed
标记商品条码已打印 (需登录)

**Path Parameters:** `id` - 商品ID

---

### POST /api/products/mark-printed-batch
批量标记商品条码已打印 (需登录)

**Request Body:**
```json
{"ids": [1, 2, 3]}
```

---

### GET /api/products/barcode/{barcode}
根据条码查询商品

**Path Parameters:** `barcode` - 商品条码

---

## 3. 订单管理 (Orders)

### POST /api/orders
创建订单 (需登录)

**Request Body:**
```json
{"productId": 1, "quantity": 1, "addressId": 1, "remark": "optional"}
```

**Response (201):** 订单信息

---

### POST /api/orders/batch
批量创建订单 (需登录)

**Request Body:**
```json
{"items": [{"productId": 1, "quantity": 1}], "addressId": 1, "remark": "optional"}
```

**Response (201):** 订单信息

---

### POST /api/orders/{id}/pay
支付订单 (需登录)

**Path Parameters:** `id` - 订单ID

**Request Body (可选):**
```json
{"returnUrl": "https://...", "cancelUrl": "https://..."}
```

**Response:** 支付信息（含支付链接）

---

### GET /api/orders/{id}
查询订单详情

**Path Parameters:** `id` - 订单ID

**Response:** 订单详细信息

---

### GET /api/orders/buyer
查询买家订单列表 (需登录)

**Response:** 当前买家的所有订单

---

### GET /api/orders/buyer/items
查询买家购买过的商品 (需登录)

**Response:** 购买商品列表

---

### GET /api/orders/buyer/unshipped
查询买家未发货订单 (需登录)

**Response:** 未发货订单列表

---

### GET /api/orders/seller
查询卖家订单列表 (需登录)

**Response:** 当前卖家的所有订单

---

### POST /api/orders/{id}/cancel
取消订单 (需登录)

**Path Parameters:** `id` - 订单ID

---

### POST /api/orders/{id}/ship
卖家发货 (需登录)

**Path Parameters:** `id` - 订单ID

**Request Body (可选):**
```json
{"trackingCompany": "SF Express", "trackingNumber": "SF1234567890"}
```

---

### POST /api/orders/{id}/complete
确认收货完成订单 (需登录)

**Path Parameters:** `id` - 订单ID

---

## 4. 卖家管理 (Seller)

### POST /api/seller/apply
申请成为卖家 (需登录)

**Request Body:**
```json
{"shopName": "string", "shopDescription": "string", "contactPhone": "string"}
```

---

### GET /api/seller/status
获取卖家申请状态 (需登录)

**Response:**
```json
{"code": 200, "message": "success", "data": {"status": "APPROVED", "shopName": "..."}}
```

---

### GET /api/seller/dashboard
获取卖家后台统计数据 (需登录)

**Response:** 仪表盘数据（含订单数、商品数、销售额等）

---

### PUT /api/seller/profile
更新卖家资料 (需登录)

**Request Body:**
```json
{"shopName": "new name", "shopDescription": "new description"}
```

---

## 5. 购物车管理 (Cart)

### GET /api/cart
查看购物车 (需登录)

**Response:** 购物车商品列表

---

### POST /api/cart
添加商品到购物车 (需登录)

**Request Body:**
```json
{"productId": 1, "quantity": 1}
```

**Response (201):** 购物车条目

---

### PUT /api/cart/{productId}
更新购物车商品数量 (需登录)

**Path Parameters:** `productId` - 商品ID

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| quantity | Integer | 是 | 新数量 |

---

### DELETE /api/cart/{productId}
删除购物车中的商品 (需登录)

**Path Parameters:** `productId` - 商品ID

---

### DELETE /api/cart
清空购物车 (需登录)

---

### POST /api/cart/merge
合并本地购物车到服务端 (需登录)

**Request Body:**
```json
[{"productId": 1, "quantity": 2}]
```

---

## 6. 支付管理 (Payments)

### POST /api/payments/create
创建支付 (需登录)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| method | String | 是 | 支付方式 |

**Request Body:**
```json
{"orderId": 1, "amount": 100.00}
```

**Response:** 支付信息

---

### GET /api/payments/query
查询支付状态 (需登录)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| method | String | 是 | 支付方式 |
| transactionNo | String | 是 | 交易号 |

**Response:** 支付状态和详情

---

### POST /api/payments/refund
发起退款 (需登录)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| method | String | 是 | 支付方式 |
| transactionNo | String | 是 | 交易号 |
| amount | BigDecimal | 是 | 退款金额 |

**Response:** 退款信息

---

### GET /api/payments/methods
获取支持的支付方式

**Response:**
```json
{"code": 200, "message": "success", "data": ["alipay", "wechat", "stripe"]}
```

---

## 7. 广告管理 (Admin)

### GET /api/admin/ads
获取广告列表 (需 ADMIN 角色)

**Response:** 所有广告列表

---

### GET /api/admin/ads/{id}
获取广告详情 (需 ADMIN 角色)

**Path Parameters:** `id` - 广告ID

---

### POST /api/admin/ads
创建广告 (需 ADMIN 角色)

**Request Body:**
```json
{"title": "string", "imageUrl": "string", "linkUrl": "string", "sortOrder": 1}
```

---

### PUT /api/admin/ads/{id}
更新广告 (需 ADMIN 角色)

**Path Parameters:** `id` - 广告ID

**Request Body:** 同创建广告

---

### DELETE /api/admin/ads/{id}
删除广告 (需 ADMIN 角色)

**Path Parameters:** `id` - 广告ID

---

### PUT /api/admin/ads/{id}/toggle-status
切换广告启用/禁用状态 (需 ADMIN 角色)

**Path Parameters:** `id` - 广告ID

---

## 8. 新闻管理 (Admin)

### GET /api/admin/news
获取新闻列表 (需 ADMIN 角色)

**Response:** 所有新闻列表

---

### GET /api/admin/news/{id}
获取新闻详情 (需 ADMIN 角色)

**Path Parameters:** `id` - 新闻ID

---

### POST /api/admin/news
创建新闻 (需 ADMIN 角色)

**Request Body:**
```json
{"title": "string", "content": "string", "coverImage": "string"}
```

---

### PUT /api/admin/news/{id}
更新新闻 (需 ADMIN 角色)

**Path Parameters:** `id` - 新闻ID

---

### DELETE /api/admin/news/{id}
删除新闻 (需 ADMIN 角色)

**Path Parameters:** `id` - 新闻ID

---

### PUT /api/admin/news/{id}/publish
发布新闻 (需 ADMIN 角色)

**Path Parameters:** `id` - 新闻ID

**Response:** 更新后的新闻

---

### PUT /api/admin/news/{id}/unpublish
取消发布新闻 (需 ADMIN 角色)

**Path Parameters:** `id` - 新闻ID

**Response:** 更新后的新闻

---

## 9. 后台订单管理 (Admin)

### GET /api/admin/orders
分页查询所有订单 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 (默认 0) |
| size | int | 否 | 每页数量 (默认 20) |

---

### GET /api/admin/orders/{id}
获取订单详情 (需 ADMIN 角色)

**Path Parameters:** `id` - 订单ID

---

### POST /api/admin/orders/{id}/mark-paid
标记订单已付款 (需 ADMIN 角色)

**Path Parameters:** `id` - 订单ID

---

### POST /api/admin/orders/{id}/cancel
取消订单 (需 ADMIN 角色)

**Path Parameters:** `id` - 订单ID

---

### POST /api/admin/orders/{id}/complete
强制完成订单 (需 ADMIN 角色)

**Path Parameters:** `id` - 订单ID

---

### POST /api/admin/orders/{id}/ship
发货并填写物流信息 (需 ADMIN 角色)

**Path Parameters:** `id` - 订单ID

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| trackingNumber | String | 是 | 运单号 |
| trackingCompany | String | 是 | 物流公司 |

---

### PUT /api/admin/orders/{id}
更新订单信息 (需 ADMIN 角色)

**Request Body:**
```json
{"address": "new address", "remark": "new remark"}
```

---

## 10. 后台商品管理 (Admin)

### GET /api/admin/products
分页查询所有商品 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 (默认 0) |
| size | int | 否 | 每页数量 (默认 20) |
| status | String | 否 | 商品状态过滤 |
| printed | Boolean | 否 | 打印状态过滤 |
| dateFrom | LocalDate | 否 | 创建日期起始 |
| dateTo | LocalDate | 否 | 创建日期结束 |

---

### GET /api/admin/products/by-date
按日期范围查询商品 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dateFrom | LocalDate | 否 | 起始日期 |
| dateTo | LocalDate | 否 | 结束日期 |
| printed | Boolean | 否 | 打印状态 |

---

### GET /api/admin/products/search
按关键字搜索商品 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | String | 是 | 关键字 |
| page | int | 否 | 页码 (默认 0) |
| size | int | 否 | 每页数量 (默认 20) |

---

### GET /api/admin/products/barcode/{barcode}
根据条码查询商品 (需 ADMIN 角色)

**Path Parameters:** `barcode` - 商品条码

---

### POST /api/admin/products
创建商品 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sellerId | Long | 是 | 卖家用户ID |

**Request Body:** 商品创建信息

---

### PUT /api/admin/products/{id}
更新商品 (需 ADMIN 角色)

**Path Parameters:** `id` - 商品ID

---

### PUT /api/admin/products/{id}/status
更新商品上下架状态 (需 ADMIN 角色)

**Path Parameters:** `id` - 商品ID

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 是 | 状态值 |

---

### POST /api/admin/products/{id}/mark-printed
标记商品已打印 (需 ADMIN 角色)

**Path Parameters:** `id` - 商品ID

---

### POST /api/admin/products/mark-printed-batch
批量标记商品已打印 (需 ADMIN 角色)

**Request Body:**
```json
[1, 2, 3]
```

---

## 11. 后台卖家管理 (Admin)

### GET /api/admin/sellers/applications
查询卖家入驻申请 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 否 | 申请状态过滤 |

---

### POST /api/admin/sellers/applications/{id}/approve
审核通过卖家入驻 (需 ADMIN 角色)

**Path Parameters:** `id` - 申请ID

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| adminId | Long | 是 | 管理员用户ID |

---

### POST /api/admin/sellers/applications/{id}/reject
拒绝卖家入驻 (需 ADMIN 角色)

**Path Parameters:** `id` - 申请ID

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| adminId | Long | 是 | 管理员用户ID |
| reason | String | 是 | 拒绝原因 |

---

### GET /api/admin/sellers/profiles
查询所有已入驻卖家 (需 ADMIN 角色)

---

## 12. 工单管理 (Admin)

### GET /api/admin/tickets
查询工单列表 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 否 | 工单状态过滤 |

---

### GET /api/admin/tickets/{id}
获取工单详情 (需 ADMIN 角色)

**Path Parameters:** `id` - 工单ID

---

### POST /api/admin/tickets/{id}/reply
回复工单 (需 ADMIN 角色)

**Path Parameters:** `id` - 工单ID

**Request Body:**
```json
{"reply": "工单回复内容"}
```

---

### PUT /api/admin/tickets/{id}/status
更新工单状态 (需 ADMIN 角色)

**Path Parameters:** `id` - 工单ID

**Request Body:**
```json
{"status": "RESOLVED"}
```

---

## 13. 后台用户管理 (Admin)

### GET /api/admin/users
分页查询用户列表 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 (默认 0) |
| size | int | 否 | 每页数量 (默认 20) |

---

### PUT /api/admin/users/{id}/toggle-status
启用或禁用用户账号 (需 ADMIN 角色)

**Path Parameters:** `id` - 用户ID

---

## 14. 审计日志 (Admin)

### GET /api/admin/audit-logs/entity/{entityType}/{entityId}
查询实体变更历史 (需 ADMIN 角色)

**Path Parameters:**
| 参数 | 类型 | 说明 |
|------|------|------|
| entityType | String | 实体类型 |
| entityId | Long | 实体ID |

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 (默认 0) |
| size | int | 否 | 每页数量 (默认 50) |

---

### GET /api/admin/audit-logs
多条件查询审计日志 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| entityType | String | 否 | 实体类型 |
| operation | String | 否 | 操作类型 |
| operatorId | Long | 否 | 操作人ID |
| dateFrom | LocalDate | 否 | 起始日期 |
| dateTo | LocalDate | 否 | 结束日期 |
| page | int | 否 | 页码 (默认 0) |
| size | int | 否 | 每页数量 (默认 50) |

---

## 15. 条码管理 (Admin)

### GET /api/admin/barcode-codes/type/{codeType}
根据类型查询条码编码 (需 ADMIN 角色)

**Path Parameters:** `codeType` - 编码类型

---

### GET /api/admin/barcode-codes/type/{codeType}/active
查询活跃的条码编码 (需 ADMIN 角色)

**Path Parameters:** `codeType` - 编码类型

---

### GET /api/admin/barcode-codes/parent
根据父级查询条码编码 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| parentType | String | 是 | 父级类型 |
| parentValue | String | 是 | 父级值 |

---

### GET /api/admin/barcode-codes/search
搜索条码编码 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | String | 是 | 关键字 |

---

### POST /api/admin/barcode-codes
创建条码编码 (需 ADMIN 角色)

**Request Body:**
```json
{"codeType": "string", "code": "string", "label": "string", "parentType": "string", "parentValue": "string", "active": true}
```

---

### PUT /api/admin/barcode-codes/{id}
更新条码编码 (需 ADMIN 角色)

**Path Parameters:** `id` - 编码ID

---

### DELETE /api/admin/barcode-codes/{id}
删除条码编码 (需 ADMIN 角色)

**Path Parameters:** `id` - 编码ID

---

## 16. 库存管理 (Admin)

### POST /api/admin/inventory/entry
创建入库记录 (需 ADMIN 角色)

**Request Body:**
```json
{"productName": "string", "quantity": 10, "purchasePrice": 100.00, "supplier": "string", "batchDate": "2026-07-26"}
```

**Response:** 生成的商品信息（含条码）

---

### GET /api/admin/inventory/entries
分页查询入库记录 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | String | 否 | 关键字 |
| page | int | 否 | 页码 (默认 0) |
| size | int | 否 | 每页数量 (默认 50) |

---

### GET /api/admin/inventory/entries/by-date
按日期范围查询入库记录 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dateFrom | LocalDate | 否 | 起始日期 |
| dateTo | LocalDate | 否 | 结束日期 |

---

### GET /api/admin/inventory/for-listing
查询可用于上架的商品 (需 ADMIN 角色)

---

### GET /api/admin/inventory/search
搜索库存商品 (需 ADMIN 角色)

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | String | 是 | 关键字 |
| page | int | 否 | 页码 (默认 0) |
| size | int | 否 | 每页数量 (默认 20) |

---

### POST /api/admin/inventory/list-for-sale/{productId}
上架商品并设置售价 (需 ADMIN 角色)

**Path Parameters:** `productId` - 商品ID

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| price | BigDecimal | 是 | 售价 |

---

### POST /api/admin/inventory/outbound/{productId}
库存出库 (需 ADMIN 角色)

**Path Parameters:** `productId` - 商品ID

**Request Body:**
```json
{"quantity": 1, "reason": "sale"}
```

**Response:** 更新后的商品信息

---

### PUT /api/admin/inventory/batch/{batchId}
更新入库批次信息 (需 ADMIN 角色)

**Path Parameters:** `batchId` - 批次ID

**Request Body:** 入库信息

---

### POST /api/admin/inventory/generate-barcode
生成商品条码 (需 ADMIN 角色)

**Request Body:** 入库信息

**Response:** 生成的条码字符串

---

## 17. 公开广告接口

### GET /api/ads/active
获取当前活跃的广告列表 (公开，无需认证)

**Response:** 活跃广告列表

---

## 18. 公开新闻接口

### GET /api/news
获取所有已发布的新闻 (公开，无需认证)

**Response:** 已发布新闻列表

---

### GET /api/news/{id}
获取已发布的新闻详情 (公开，无需认证)

**Path Parameters:** `id` - 新闻ID

---

## 通用响应码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
