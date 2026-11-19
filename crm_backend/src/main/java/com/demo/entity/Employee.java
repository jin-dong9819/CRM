package com.demo.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 一张表    对应 一个类
 * 一行数据  对应  一个对象
 * 一个字段  对应  一个属性
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    private int id;
    private String name;
    private int gender;
    private LocalDate hireDate;     //规范问题    数据库中命名 使用下划线   java使用驼峰

}

