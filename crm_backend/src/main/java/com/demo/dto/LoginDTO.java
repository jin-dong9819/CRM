package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 登录入参：员工编号 + 密码 */
@Data
public class LoginDTO {

    @NotBlank(message = "请输入员工编号")
    private String empNo;

    @NotBlank(message = "请输入密码")
    private String password;
}
