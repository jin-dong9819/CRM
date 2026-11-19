package com.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 权限（目录 / 菜单 / 按钮） */
@Data
public class SysPermission {

    private Long id;
    private Long parentId;
    private String permName;
    private String permCode;
    /** 1目录 2菜单 3按钮 */
    private Integer permType;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 组装树时使用，不映射数据库列 */
    private List<SysPermission> children = new ArrayList<>();
}
