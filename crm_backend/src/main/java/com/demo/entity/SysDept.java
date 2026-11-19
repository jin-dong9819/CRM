package com.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 部门（多级组织架构） */
@Data
public class SysDept {

    private Long id;
    private Long parentId;
    /** 祖先ID链，如 0,1,2 */
    private String ancestors;
    private String deptName;
    private String deptCode;
    private String leader;
    private String phone;
    private String email;
    private Integer sort;
    private Integer status;
    private Integer delFlag;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    /** 子部门，仅用于树形返回 */
    private List<SysDept> children;
}
