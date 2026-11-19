package com.demo.vo;

import lombok.Data;

import java.util.List;

/** 当前登录员工信息 */
@Data
public class UserInfoVO {

    private Long id;
    private String empNo;
    private String name;
    private String avatar;
    private Long deptId;
    private String deptName;
    /** 多角色，逗号拼接，便于前端直接展示 */
    private String roleNames;
    private List<String> roleCodes;
    /** 该员工拥有的权限标识并集，供前端按钮级控制 */
    private List<String> permissions;
}
