-- 创建数据库
CREATE DATABASE IF NOT EXISTS rbac_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE rbac_demo;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码(加密)',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '角色名称',
  `code` varchar(64) NOT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '权限名称',
  `code` varchar(64) NOT NULL COMMENT '权限编码',
  `description` varchar(255) DEFAULT NULL COMMENT '权限描述',
  `type` tinyint NOT NULL COMMENT '权限类型(1:菜单,2:按钮,3:API)',
  `parent_id` bigint DEFAULT NULL COMMENT '父权限ID',
  `path` varchar(255) DEFAULT NULL COMMENT '权限路径',
  `sort` int DEFAULT '0' COMMENT '排序值',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_role` (`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_role_permission` (`role_id`,`permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 添加初始数据
-- 添加管理员用户（密码为：123456，使用BCrypt加密）
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `status`) VALUES
('admin', '$2a$10$uxN9VpNQF4abH1W5fR41K.0xdsgEOZO2eLGy4KPUgm8/QWrSNT656', '系统管理员', 'admin@example.com', 1);

-- 添加角色
INSERT INTO `role` (`name`, `code`, `description`, `status`) VALUES
('系统管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限', 1),
('普通用户', 'ROLE_USER', '普通用户，拥有基本权限', 1);

-- 添加基本权限
INSERT INTO `permission` (`name`, `code`, `description`, `type`, `status`) VALUES
('用户管理', 'USER_MANAGE', '用户管理权限', 1, 1),
('用户查看', 'USER_VIEW', '查看用户信息权限', 2, 1),
('用户添加', 'USER_ADD', '添加用户权限', 2, 1),
('用户编辑', 'USER_EDIT', '编辑用户信息权限', 2, 1),
('用户删除', 'USER_DELETE', '删除用户权限', 2, 1),
('角色管理', 'ROLE_MANAGE', '角色管理权限', 1, 1),
('角色查看', 'ROLE_VIEW', '查看角色信息权限', 2, 1),
('角色添加', 'ROLE_ADD', '添加角色权限', 2, 1),
('角色编辑', 'ROLE_EDIT', '编辑角色信息权限', 2, 1),
('角色删除', 'ROLE_DELETE', '删除角色权限', 2, 1),
('权限管理', 'PERMISSION_MANAGE', '权限管理权限', 1, 1),
('权限查看', 'PERMISSION_VIEW', '查看权限信息权限', 2, 1),
('权限添加', 'PERMISSION_ADD', '添加权限权限', 2, 1),
('权限编辑', 'PERMISSION_EDIT', '编辑权限信息权限', 2, 1),
('权限删除', 'PERMISSION_DELETE', '删除权限权限', 2, 1);

-- 分配用户角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 1);

-- 分配管理员角色所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15);

-- 分配普通用户查看权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
(2, 2), (2, 7), (2, 12); 