package com.demo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 员工列表/详情出参 */
@Data
public class EmployeeVO {

    private Long id;
    private String empNo;
    private String name;
    private Integer gender;
    private String genderLabel;
    private String phone;
    private String email;
    private String avatar;
    private Long deptId;
    private String deptName;
    private List<Long> roleIds;
    private String roleNames;
    private String jobTitle;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    private Integer status;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pwdUpdateTime;
}
