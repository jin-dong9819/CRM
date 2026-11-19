package com.demo.vo;

import lombok.Data;

/** 员工-角色行，用于分页后批量组装角色信息 */
@Data
public class EmployeeRoleVO {

    private Long employeeId;
    private Long roleId;
    private String roleName;
    private String roleCode;
}
