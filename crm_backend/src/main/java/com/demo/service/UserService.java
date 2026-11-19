package com.demo.service;

import com.demo.dto.RegisterDTO;

public interface UserService {

    /** 手机号注册：校验重复 -> 密码加密 -> 入库 */
    void register(RegisterDTO dto);

    /** 手机号是否已注册 */
    boolean existsByPhone(String phone);
}
