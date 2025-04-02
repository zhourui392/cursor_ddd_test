-- 为权限表添加模块字段
ALTER TABLE permission ADD COLUMN module VARCHAR(64) NOT NULL DEFAULT '未分类' COMMENT '模块名称' AFTER description;

-- 更新已有权限的模块信息
UPDATE permission SET module = '用户管理' WHERE code LIKE 'USER_%';
UPDATE permission SET module = '权限管理' WHERE code LIKE 'ROLE_%' OR code LIKE 'PERMISSION_%';

-- 删除菜单相关权限
DELETE FROM role_permission WHERE permission_id IN (SELECT id FROM permission WHERE code LIKE 'MENU_%');
DELETE FROM permission WHERE code LIKE 'MENU_%'; 