package com.demo.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** 分页响应：{ list, total } */
@Data
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;
    private long total;
}
