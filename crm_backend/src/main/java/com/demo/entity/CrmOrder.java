package com.demo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 订单：成交后的合同订单，含付款状态与交付状态两条线 */
@Data
public class CrmOrder {

    private Long id;
    /** 订单编号，形如 DD20260001，不传则后端自动生成 */
    private String orderNo;
    private Long customerId;
    /** 客户名称冗余一份，列表页直接展示 */
    private String customerName;
    private String productName;
    private BigDecimal amount;
    /** 未付款 / 部分付款 / 已付款 */
    private String payStatus;
    /** 待审核 / 已确认 / 已发货 / 已完成 / 已取消 */
    private String status;
    private LocalDate orderDate;
    private String owner;
    private String remark;
    private Integer delFlag;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
