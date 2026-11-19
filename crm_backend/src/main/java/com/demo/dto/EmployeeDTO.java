package com.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/** 员工新增/编辑入参：roleIds 支持多角色 */
@Data
public class EmployeeDTO {

    @NotBlank(message = "请输入员工编号")
    private String empNo;

    /** 新增时可不传，默认 123456 */
    private String password;

    @NotBlank(message = "请输入姓名")
    private String name;

    private Integer gender;

    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    /** 头像地址，可为空；为空时前端用姓名首字兜底 */
    private String avatar;

    @NotNull(message = "请选择所属部门")
    private Long deptId;

    @NotEmpty(message = "请至少选择一个角色")
    private List<Long> roleIds;

    private String jobTitle;

    private LocalDate hireDate;

    private Integer status = 1;

    private String remark;
}
