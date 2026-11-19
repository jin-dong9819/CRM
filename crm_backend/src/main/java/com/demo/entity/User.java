package com.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * sys_user 表实体：手机号注册用户
 * 与 sys_employee（员工）是两套账号体系，这里只存手机号与密码哈希
 */
@Data
@NoArgsConstructor
public class User {

    private Long id;
    private String phone;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
