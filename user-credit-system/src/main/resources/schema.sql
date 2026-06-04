

CREATE DATABASE IF NOT EXISTS user_credit_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE user_credit_db;

-- 用户表（先创建主表）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                        `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
                        `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                        `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
                        `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                        `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
                        `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
                        `status` TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
                        `credit_score` INT DEFAULT 100 COMMENT '信用分（0-200）',
                        `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`),
                        UNIQUE KEY `uk_email` (`email`),
                        KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 评分表（后创建从表）
DROP TABLE IF EXISTS `user_rating`;
CREATE TABLE `user_rating` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评分ID',
                               `from_user_id` BIGINT NOT NULL COMMENT '评分人ID',
                               `to_user_id` BIGINT NOT NULL COMMENT '被评分人ID',
                               `score` INT NOT NULL COMMENT '评分（1-5星）',
                               `content` TEXT COMMENT '评价内容',
                               `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_from_user` (`from_user_id`),
                               KEY `idx_to_user` (`to_user_id`),
                               CONSTRAINT `fk_rating_from_user` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `fk_rating_to_user` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户评分表';

-- 插入测试数据
INSERT INTO `user` (`username`, `password`, `email`, `real_name`, `phone`, `role`, `status`, `credit_score`) VALUES
                                                                                                                 ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@campus.com', '管理员', '13800000001', 'ADMIN', 1, 150),
                                                                                                                 ('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'zhangsan@campus.com', '张三', '13800000002', 'USER', 1, 100),
                                                                                                                 ('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'lisi@campus.com', '李四', '13800000003', 'USER', 1, 100),
                                                                                                                 ('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'wangwu@campus.com', '王五', '13800000004', 'USER', 1, 100);

-- 插入测试评分数据
INSERT INTO `user_rating` (`from_user_id`, `to_user_id`, `score`, `content`) VALUES
                                                                                 (2, 3, 5, '帮助我搬东西，非常热心！'),
                                                                                 (3, 2, 4, '很靠谱的伙伴'),
                                                                                 (4, 2, 5, '学习上的好帮手'),
                                                                                 (2, 4, 3, '一般般吧'),
                                                                                 (3, 4, 4, '态度很好');

-- 默认密码：123456（BCrypt加密后的值相同）






