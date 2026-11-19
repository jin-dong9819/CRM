package com.demo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 角色列表出参：dataScope 传数字，dataScopeLabel 给前端直接展示 */
@Data
public class RoleVO {

    private Long id;
    private String roleCode;
    private String roleName;
    private Integer dataScope;
    private String dataScopeLabel;
    private Integer sort;
    private Integer status;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
