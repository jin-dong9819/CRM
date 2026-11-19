package com.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 登录出参 */
@Data
@AllArgsConstructor
public class LoginVO {

    private String token;
    private UserInfoVO user;
}
