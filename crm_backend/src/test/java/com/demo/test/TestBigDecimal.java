package com.demo.test;

import java.math.BigDecimal;

public class TestBigDecimal {
    public static void main(String[] args) {
        BigDecimal bigDecimal1 = new BigDecimal("1.211");
        BigDecimal bigDecimal2 = new BigDecimal("0.231");

        //0.9800000000000001 精度丢失问题
        //涉及到钱 不能使用double类型

        BigDecimal bigDecimal3 = bigDecimal1.subtract(bigDecimal2);
        System.out.println(bigDecimal3.doubleValue());
    }
}
