package com.hotelbooking.account.enums;

public enum RoleType {
    ADMIN("ADMIN", "Quản trị viên hệ thống"),
    STAFF("STAFF", "Nhân viên tiếp tân"),
    USER("USER", "Khách hàng");

    private final String roleName;
    private final String description;

    RoleType(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }
}
