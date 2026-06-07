# Bug 修复日志



## 1. 修复日志总览

| Bug ID | 模块 | 问题类型 | 严重程度 | 状态 |
|---|---|---|---|---|
| BUG-001 | 订单管理 | 状态流转与权限校验缺陷 | 高 | 已修复 |
| BUG-002 | 消息中心 | 会话未读数与消息状态不一致 | 中 | 已修复 |
| BUG-003 | 智能匹配 | 匹配结果缺少规则追踪且旧结果残留 | 中 | 已修复 |
| BUG-004 | 评价信用 | 评价保存与信用分更新一致性风险 | 中 | 已修复 |
| BUG-005 | 前后端接口 | Long 类型订单/用户 ID 前端精度风险 | 中 | 已修复 |

---

## BUG-001：订单状态流转与权限校验缺陷

### 问题现象

在需求/订单模块早期联调中，订单状态流转存在潜在风险：

1. 已被接取的任务仍可能被重复接单；
2. 非接取人可能提交任务；
3. 非发布人可能确认任务完成；
4. 发布人可能在任务未提交时直接确认完成；
5. 订单状态可能出现不符合业务流程的跳转。

### 影响范围

该问题会直接影响 P0 核心流程：

```text
用户注册 -> 登录 -> 发布需求 -> 另一用户接单 -> 完成 -> 评价
```

如果状态流转不受约束，完整业务流程的真实性和安全性无法保证。

### 根因分析

订单接口初始设计偏向 CRUD，容易只按订单 id 更新状态，而没有把订单当作状态机处理。订单状态流转实际上需要同时满足：

1. 当前用户身份正确；
2. 当前订单状态正确；
3. 操作后状态更新一致；
4. 重复操作需要被拒绝。

例如：

- `/claim/{id}` 必须确认 `is_claimed = 0`；
- `/submit/{id}` 必须确认当前用户是 `taker_id`；
- `/confirm/{id}` 必须确认当前用户是 `user_id`，且订单状态为 `SUBMITTED`。

### 修复方案

在 `OrderController` 中补充后端强校验：

1. 创建订单时初始化：
   - `status = NEW`
   - `isClaimed = 0`
   - `isCompleted = 0`

2. 接单接口 `/claim/{id}`：
   - 检查任务是否存在；
   - 检查任务是否已经被接取；
   - 设置 `takerId`、`isClaimed = 1`、`status = IN_PROGRESS`、`acceptTime`。

3. 提交接口 `/submit/{id}`：
   - 检查任务是否存在；
   - 检查当前用户是否为接取人；
   - 检查任务是否已经完成；
   - 设置 `status = SUBMITTED`、`submittedAt`。

4. 确认完成接口 `/confirm/{id}`：
   - 检查任务是否存在；
   - 检查当前用户是否为发布人；
   - 检查任务是否已经确认完成；
   - 检查状态必须为 `SUBMITTED`；
   - 设置 `status = COMPLETED`、`isCompleted = 1`、`completedAt`。

### 验证结果

通过以下测试验证：

- `OrderApiIntegrationTest`
  - 未登录发布任务失败；
  - 发布任务成功；
  - 接单成功；
  - 重复接单失败；
  - 非接取人提交失败；
  - 接取人提交成功；
  - 非发布人确认失败；
  - 发布人确认完成成功。

- `FullBusinessFlowIntegrationTest`
  - 完整流程：注册 -> 登录 -> 发布需求 -> 接单 -> 提交 -> 确认完成 -> 评价。

最终验证：

```text
Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
```

---

## BUG-002：消息会话未读数与消息状态不一致

### 问题现象

消息模块早期联调时，首次发送消息和读取消息可能出现以下问题：

1. 首次创建会话后，未读数为空，后续累加可能异常；
2. 消息插入成功但会话摘要未更新；
3. 用户读取消息后，消息已读状态与会话未读数不同步；
4. 会话列表中的最后一条消息摘要过长，影响前端展示。

### 影响范围

该问题影响 P1 消息通知模块：

- 用户无法稳定看到会话列表；
- 未读数可能错误；
- 消息发送与会话状态可能不一致；
- 前端“消息中心”展示不可靠。

### 根因分析

发送消息不是单条数据插入，而是涉及多个步骤：

```text
查找或创建会话 -> 插入消息 -> 更新会话最后消息 -> 更新未读数
```

如果这些步骤没有放在事务中，或者未读数字段没有空值保护，就可能产生部分成功、部分失败的数据不一致问题。

### 修复方案

在 `MessageServiceImpl` 中进行修复：

1. `sendMessage` 使用事务：

```java
@Transactional(rollbackFor = Exception.class)
```

2. 查找不到会话时自动创建 `Conversation`。
3. 插入 `Message` 后更新会话：
   - `lastMessage`
   - `lastTime`
   - `unreadCount`

4. 对 `unreadCount` 做空值保护：

```java
conversation.setUnreadCount((conversation.getUnreadCount() == null ? 0 : conversation.getUnreadCount()) + 1);
```

5. 读取消息时调用 `markAsRead`，并将会话未读数清零。
6. 对 `lastMessage` 做长度截断，避免前端展示异常。

### 验证结果

通过 `MessageApiIntegrationTest` 验证：

- 用户可以发送消息；
- 接收方未读数增加；
- 会话列表可以查询；
- 会话详情可以查询；
- 空消息发送失败；
- 给自己发消息失败；
- 不带 token 访问失败。

最终验证：

```text
Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
```

---

## BUG-003：智能匹配结果缺少规则追踪且旧结果残留

### 问题现象

智能匹配模块早期设计中，匹配结果存在两个问题：

1. 匹配结果只有目标用户和分数，无法解释由哪条规则命中；
2. 用户多次执行匹配时，旧匹配结果可能保留在 `match_result` 中，导致前端结果重复或过期。

### 影响范围

该问题影响 P2 扩展功能中的智能匹配模块：

- 匹配结果缺少可解释性；
- 用户看到的结果可能不是最新计算结果；
- 后续如果引入更多匹配规则，无法追踪规则效果。

### 根因分析

匹配算法初始关注分数计算，但忽略了结果落库后的可追踪性。实际业务需要知道：

1. 哪个候选用户被匹配；
2. 分数是多少；
3. 主要由哪条规则触发；
4. 当前结果是否为最新一次匹配。

### 修复方案

在 `MatchEngine` 和 `MatchServiceImpl` 中修复：

1. `MatchEngine.execute` 对每个候选用户计算总分；
2. 记录最高分规则：
   - `bestRuleId`
   - `bestRuleScore`
3. 将 `bestRuleId` 写入 `MatchResult.ruleId`；
4. 结果按 `matchScore` 降序排列；
5. 限制返回 TopN；
6. `MatchServiceImpl.executeMatch` 执行新匹配前删除旧结果：

```java
matchResultMapper.deleteByUserId(userId);
```

7. 无启用规则时返回明确异常，无候选用户时返回空列表。

### 验证结果

通过 `MatchEngineTest` 验证：

- 命中信用规则时返回匹配结果；
- 结果按分数降序排列；
- TopN 限制生效；
- 无候选用户返回空列表；
- 无规则命中返回空列表。

最终验证：

```text
Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
```

---

## BUG-004：评价保存与信用分更新存在一致性风险

### 问题现象

评价信用模块早期联调中，存在以下潜在问题：

1. 评价记录保存成功，但信用分更新失败，导致评价与信用分不一致；
2. 被评价用户不存在时，信用分更新逻辑可能异常；
3. 用户首次被评价时平均分为空，信用分计算缺少默认值；
4. 信用分可能超过上限或低于下限。

### 影响范围

该问题影响 P1 评价与信用体系：

- 用户信用分不能准确反映评价结果；
- 评价记录与用户信用分之间可能不一致；
- 评分边界缺少约束会破坏信用分公平性。

### 根因分析

评价提交不是简单插入评价表，还需要更新用户信用分。两个操作应被视为一个业务事务：

```text
保存评价 -> 重新计算平均分 -> 更新被评价用户信用分
```

如果没有事务保护，或者平均分为空时没有默认值，就会产生数据不一致。

### 修复方案

在 `RatingServiceImpl` 和信用分算法中修复：

1. `saveRatingAndUpdateCredit` 使用事务：

```java
@Transactional(rollbackFor = Exception.class)
```

2. 先保存评价，再查询被评价用户平均分；
3. 平均分为空时使用默认值 `3.0`；
4. 用户不存在时安全返回，避免空指针；
5. 使用 `CreditAlgorithm` 统一计算信用分；
6. 信用分设置上下限，避免超过 200 或低于 0。

### 验证结果

通过以下测试验证：

- `CreditAlgorithmTest`
  - 高分增加信用分；
  - 低分扣减信用分；
  - 信用分上限 200；
  - 信用分下限 0。

- `RatingApiIntegrationTest`
  - 用户可以评价其他用户；
  - 自评失败；
  - 非法评分失败；
  - 可查询收到的评价；
  - 可查询发出的评价；
  - 可查询信用分。

最终验证：

```text
Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
```

---

## BUG-005：前端接收 Long 类型 ID 存在精度风险

### 问题现象

订单、用户、消息等模块都使用 Long 类型作为主键。如果后端直接将 Long 类型数字返回给前端，前端 JavaScript 在处理较大整数时可能出现精度丢失，导致：

1. 前端拿到的订单 id 与数据库真实 id 不一致；
2. 接单、提交、确认完成时传错 id；
3. 查询详情或评价关联对象失败。

### 影响范围

该问题影响前后端联调中的关键接口调用，尤其是：

- 订单详情；
- 订单状态流转；
- 用户评价；
- 消息会话；
- 匹配结果展示。

### 根因分析

Java 后端 Long 类型最大值可能超过 JavaScript 安全整数范围。前端如果用 Number 接收，可能丢失低位精度。

### 修复方案

在关键实体的 Long 类型 id 字段上使用字符串序列化，例如 `Order` 实体中：

```java
@JsonSerialize(using = ToStringSerializer.class)
private Long id;

@JsonSerialize(using = ToStringSerializer.class)
@TableField("user_id")
private Long userId;

@JsonSerialize(using = ToStringSerializer.class)
@TableField("taker_id")
private Long takerId;
```

这样前端接收到的是字符串形式 id，可以避免 JavaScript Number 精度丢失。

### 验证结果

通过前后端联调和订单集成测试验证：

- 发布任务后可拿到订单 id；
- 任务大厅可展示订单；
- 接单、提交、确认完成接口可以使用返回的 id 继续操作；
- 完整业务流测试通过。

最终验证：

```text
Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
```

---

## 2. 总体验证结果

修复完成后，执行 Maven 全量测试，结果如下：

```text
Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
```

覆盖测试文件包括：

- `CreditAlgorithmTest`
- `MatchEngineTest`
- `UserApiIntegrationTest`
- `RatingApiIntegrationTest`
- `MessageApiIntegrationTest`
- `AdminApiIntegrationTest`
- `OrderApiIntegrationTest`
- `FullBusinessFlowIntegrationTest`
- `UserCreditApplicationTests`

---

## 3. 经验总结

1. 订单模块不能只做 CRUD，必须按照状态机进行后端强校验；
2. 涉及多表更新的消息、评价模块必须使用事务保证一致性；
3. 智能匹配结果需要保留规则 id，才能支持可解释性；
4. 前后端联调时需要注意 Long 类型 id 的序列化问题；
5. 自动化测试应覆盖正常流程和异常流程，不能只测试“接口能调用”。
