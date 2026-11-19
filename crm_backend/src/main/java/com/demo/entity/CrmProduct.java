package com.demo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 产品（汽车易损件 / 养护用品） */
@Data
public class CrmProduct {

    private Long id;
    /** 产品编码，形如 AP0101，不传则后端自动生成 */
    private String productCode;
    /** 产品名称，如「玻璃水 -25℃」 */
    private String productName;
    /** 所属分类ID，指向 crm_product_category */
    private Long categoryId;
    /** 分类名称（关联查询出来，不入库） */
    private String categoryName;
    /** 品牌 */
    private String brand;
    /** 型号 / 零件号 */
    private String model;
    /** 规格参数：容量、香型、包装数量等 */
    private String spec;
    /** 适用车型 */
    private String fitment;
    /** 计量单位：瓶/桶/盒/支/套... */
    private String unit;
    /** 标准售价 */
    private BigDecimal price;
    /** 成本价 */
    private BigDecimal costPrice;
    /** 1 在售 0 停售 */
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
