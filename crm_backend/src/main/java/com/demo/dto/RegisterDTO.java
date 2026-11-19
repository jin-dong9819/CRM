package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 注册入参：手机号 + 短信验证码 + 密码 */
@Data
public class RegisterDTO {

    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的 11 位手机号")
    private String phone;

    @NotBlank(message = "请输入短信验证码")
    @Pattern(regexp = "^\\d{4}$", message = "验证码为 4 位数字")
    private String code;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 32, message = "密码长度需为 6-32 位")
    private String password;
}
