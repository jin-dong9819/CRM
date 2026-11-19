package com.demo.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 线索：销售最前端，转化后进入客户 */
@Data
public class CrmLead {

    private Long id;
    /** 线索编号，形如 XS20260001，不传则后端自动生成 */
    private String leadNo;
    private String name;
    /** 公司名称 */
    private String company;
    private String contact;
    private String phone;
    /** 线索来源：官网表单 / 电话咨询 / 展会活动 ... */
    private String source;
    /** 意向等级：A / B / C */
    private String level;
    private String owner;
    /** 未跟进 / 跟进中 / 已转化 / 已失效 */
    private String status;
    private LocalDate nextFollowTime;
    private String remark;
    private Integer delFlag;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
