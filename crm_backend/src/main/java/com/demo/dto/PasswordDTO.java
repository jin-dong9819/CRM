package com.demo.dto;

import lombok.Data;

/** 重置密码入参，不传则重置为 123456 */
@Data
public class PasswordDTO {

    private String password;
}
