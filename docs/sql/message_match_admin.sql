-- ============================================
-- CampusHub 消息与拓展模块 - 数据库迁移 SQL
-- 日期：2026-06-05
-- 包含：message, conversation, match_rule, match_result
-- ============================================

-- 1. 消息表
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `order_id` BIGINT NULL COMMENT '关联订单ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `msg_type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型:1-聊天 2-系统通知',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '阅读状态:0-未读 1-已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  INDEX `idx_conversation_id` (`conversation_id`),
  INDEX `idx_sender_id` (`sender_id`),
  INDEX `idx_receiver_id` (`receiver_id`),
  INDEX `idx_is_read` (`is_read`),
  CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消息表';

-- 2. 会话表
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user1_id` BIGINT NOT NULL COMMENT '用户1 ID（较小者）',
  `user2_id` BIGINT NOT NULL COMMENT '用户2 ID（较大者）',
  `order_id` BIGINT NULL COMMENT '关联订单ID',
  `last_message` VARCHAR(500) NULL COMMENT '最后一条消息摘要',
  `last_time` DATETIME NULL COMMENT '最后消息时间',
  `unread_count` INT NOT NULL DEFAULT 0 COMMENT '未读计数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_user_pair` (`user1_id`, `user2_id`, `order_id`),
  INDEX `idx_user1` (`user1_id`),
  INDEX `idx_user2` (`user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息会话表';

-- 3. 匹配规则表
CREATE TABLE IF NOT EXISTS `match_rule` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `rule_type` TINYINT NOT NULL DEFAULT 1 COMMENT '规则类型:1-信用匹配 2-信用互补',
  `min_credit_score` INT NOT NULL DEFAULT 0 COMMENT '最低信用分',
  `max_credit_score` INT NOT NULL DEFAULT 200 COMMENT '最高信用分',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级（越高越优先）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用 1-启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匹配规则表';

-- 4. 匹配结果表
CREATE TABLE IF NOT EXISTS `match_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '匹配结果ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_user_id` BIGINT NOT NULL COMMENT '匹配到的目标用户ID',
  `rule_id` INT NOT NULL COMMENT '匹配规则ID',
  `match_score` INT NOT NULL DEFAULT 0 COMMENT '匹配分数',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-待处理 1-已通知',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '匹配时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_target_user_id` (`target_user_id`),
  CONSTRAINT `fk_match_result_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_match_result_target` FOREIGN KEY (`target_user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_match_result_rule` FOREIGN KEY (`rule_id`) REFERENCES `match_rule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匹配结果表';

-- 5. 初始化默认匹配规则
INSERT INTO `match_rule` (`rule_name`, `rule_type`, `min_credit_score`, `max_credit_score`, `priority`, `status`) VALUES
('高信用匹配', 1, 120, 200, 10, 1),
('中等信用匹配', 1, 80, 119, 5, 1),
('信用互补匹配', 2, 100, 200, 3, 1);
