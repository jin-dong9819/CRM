package com.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 产品分类：两级，parentId = 0 为一级（产品种类） */
@Data
public class CrmProductCategory {

    private Long id;
    private Long parentId;
    private String categoryName;
    private Integer sort;
    /** 1 启用 0 停用 */
    private Integer status;
    private LocalDateTime createTime;
}
