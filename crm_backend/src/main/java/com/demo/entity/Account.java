package com.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private int id;
    private String username;
    // 金额用 BigDecimal，不要用 double（浮点精度会丢钱）
    private BigDecimal balance;
}
