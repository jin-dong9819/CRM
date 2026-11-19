package com.demo.common;

import lombok.Data;

/**
 * 统一响应结构：{ code, msg, data }
 * 前端 axios 拦截器按 code 判断成功与否，200 为成功
 */
@Data
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok(T data) {
        return build(200, "ok", data);
    }

    public static <T> Result<T> ok(T data, String msg) {
        return build(200, msg, data);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return build(code, msg, null);
    }

    public static <T> Result<T> build(int code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
