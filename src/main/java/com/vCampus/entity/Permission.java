package com.vCampus.entity;

/**
 * 权限实体类
 * 定义系统中的具体权限
 */
public class Permission {
    private Integer permissionId;
    private String permissionName;
    private String permissionCode;
    private String resource;
    private String action;
    private String description;
    
    // 预定义权限常量
    // 用户管理权限
    public static final String USER_VIEW = "user:view";
    public static final String USER_CREATE = "user:create";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";
    public static final String USER_MANAGE_ROLES = "user:manage_roles";
    
    // 学生管理权限
    public static final String STUDENT_VIEW = "student:view";
    public static final String STUDENT_CREATE = "student:create";
    public static final String STUDENT_UPDATE = "student:update";
    public static final String STUDENT_DELETE = "student:delete";
    public static final String STUDENT_MANAGE = "student:manage";
    
    // 教师管理权限
    public static final String TEACHER_VIEW = "teacher:view";
    public static final String TEACHER_CREATE = "teacher:create";
    public static final String TEACHER_UPDATE = "teacher:update";
    public static final String TEACHER_DELETE = "teacher:delete";
    public static final String TEACHER_MANAGE = "teacher:manage";
    
    // 图书馆权限
    public static final String LIBRARY_VIEW = "library:view";
    public static final String LIBRARY_BORROW = "library:borrow";
    public static final String LIBRARY_RETURN = "library:return";
    public static final String LIBRARY_MANAGE = "library:manage";
    public static final String LIBRARY_ADMIN = "library:admin";
    
    // 商店权限
    public static final String SHOP_VIEW = "shop:view";
    public static final String SHOP_PURCHASE = "shop:purchase";
    public static final String SHOP_MANAGE = "shop:manage";
    public static final String SHOP_ADMIN = "shop:admin";
    
    // 系统管理权限
    public static final String SYSTEM_ADMIN = "system:admin";
    public static final String SYSTEM_CONFIG = "system:config";
    public static final String SYSTEM_LOG = "system:log";
    
    // 构造函数
    public Permission() {}
    
    public Permission(Integer permissionId, String permissionName, String permissionCode, 
                     String resource, String action, String description) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
        this.permissionCode = permissionCode;
        this.resource = resource;
        this.action = action;
        this.description = description;
    }
    
    // Getter 和 Setter 方法
    public Integer getPermissionId() {
        return permissionId;
    }
    
    public void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }
    
    public String getPermissionName() {
        return permissionName;
    }
    
    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }
    
    public String getPermissionCode() {
        return permissionCode;
    }
    
    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }
    
    public String getResource() {
        return resource;
    }
    
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "Permission{" +
                "permissionId=" + permissionId +
                ", permissionName='" + permissionName + '\'' +
                ", permissionCode='" + permissionCode + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return permissionId != null ? permissionId.equals(that.permissionId) : that.permissionId == null;
    }
    
    @Override
    public int hashCode() {
        return permissionId != null ? permissionId.hashCode() : 0;
    }
}
