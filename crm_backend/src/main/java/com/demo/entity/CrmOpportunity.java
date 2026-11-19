package com.demo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 商机：跟进中的销售机会，靠 customerId 关联客户 */
@Data
public class CrmOpportunity {

    private Long id;
    /** 商机编号，形如 SJ20260001，不传则后端自动生成 */
    private String oppNo;
    private String name;
    private Long customerId;
    /** 客户名称冗余一份，列表页直接展示 */
    private String customerName;
    private BigDecimal amount;
    /** 初步接洽 / 需求确认 / 方案报价 / 商务谈判 / 赢单 / 输单 */
    private String stage;
    /** 赢率（%） */
    private Integer probability;
    private LocalDate expectDate;
    private String owner;
    private String remark;
    private Integer delFlag;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
