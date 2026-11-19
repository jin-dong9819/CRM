package com.demo.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 员工（登录账号） */
@Data
public class SysEmployee {

    private Long id;
    private String empNo;
    /** BCrypt 密文，不对外返回 */
    private String password;
    private String name;
    /** 0未知 1男 2女 */
    private Integer gender;
    private String phone;
    private String email;
    private String avatar;
    private Long deptId;
    private String jobTitle;
    private LocalDate hireDate;
    private Integer status;
    private String remark;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime pwdUpdateTime;
    private Integer delFlag;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
