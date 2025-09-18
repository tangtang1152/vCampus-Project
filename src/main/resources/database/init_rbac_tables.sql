-- RBAC权限控制系统数据库初始化脚本 (Access数据库兼容版本)

-- 创建角色表
CREATE TABLE roles (
    role_id AUTOINCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    description MEMO,
    created_at DATETIME,
    updated_at DATETIME
);

-- 创建权限表
CREATE TABLE permissions (
    permission_id AUTOINCREMENT PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL,
    permission_code VARCHAR(100) NOT NULL,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description MEMO,
    created_at DATETIME,
    updated_at DATETIME
);

-- 创建角色权限关联表
CREATE TABLE role_permissions (
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,
    created_at DATETIME,
    PRIMARY KEY (role_id, permission_id)
);

-- 创建用户角色关联表
CREATE TABLE user_roles (
    user_role_id AUTOINCREMENT PRIMARY KEY,
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    assigned_date DATETIME,
    expire_date DATETIME,
    is_active YESNO DEFAULT TRUE,
    assigned_by VARCHAR(100),
    created_at DATETIME,
    updated_at DATETIME
);

-- 注意：UCanAccess驱动不支持某些索引创建功能
-- 对于小型应用，索引不是必需的，查询性能仍然可以接受