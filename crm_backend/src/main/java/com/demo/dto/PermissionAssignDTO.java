package com.demo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 角色分配权限入参 */
@Data
public class PermissionAssignDTO {

    private List<Long> permissionIds = new ArrayList<>();
}
