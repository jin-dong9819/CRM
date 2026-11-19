package com.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 客户：建档后的正式客户，商机与订单都挂在客户上 */
@Data
public class CrmCustomer {

    private Long id;
    /** 客户编号，形如 KH20260001，不传则后端自动生成 */
    private String custNo;
    private String name;
    /** 所属行业 */
    private String industry;
    /** 重要客户 / 普通客户 / 潜在客户 */
    private String level;
    private String contact;
    private String phone;
    /** 所在地区 */
    private String address;
    private String owner;
    /** 跟进中 / 合作中 / 已成交 / 已流失 */
    private String status;
    private String remark;
    private Integer delFlag;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
