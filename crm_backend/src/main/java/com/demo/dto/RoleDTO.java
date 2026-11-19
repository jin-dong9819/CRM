package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 角色新增/编辑入参 */
@Data
public class RoleDTO {

    @NotBlank(message = "请输入角色编码")
    private String roleCode;

    @NotBlank(message = "请输入角色名称")
    private String roleName;

    @NotNull(message = "请选择数据范围")
    private Integer dataScope;

    private Integer sort = 0;

    private Integer status = 1;

    private String remark;
}
