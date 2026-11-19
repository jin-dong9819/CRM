package com.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 角色 */
@Data
public class SysRole {

    private Long id;
    private String roleCode;
    private String roleName;
    /** 数据范围：1全部 2本部门及以下 3本部门 4仅本人 5自定义 */
    private Integer dataScope;
    private Integer sort;
    private Integer status;
    private String remark;
    private Integer delFlag;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
