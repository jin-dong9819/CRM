package com.demo.service.impl;

import com.demo.common.BizException;
import com.demo.dto.RegisterDTO;
import com.demo.entity.User;
import com.demo.mapper.UserMapper;
import com.demo.service.UserService;
import com.demo.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public void register(RegisterDTO dto) {
        String phone = dto.getPhone().trim();

        // 先查重：手机号已存在则不允许重复注册
        if (userMapper.selectByPhone(phone) != null) {
            throw new BizException(409, "该手机号已注册，请直接登录");
        }

        User user = new User();
        user.setPhone(phone);
        // 只存 BCrypt 哈希，禁止存明文
        user.setPasswordHash(PasswordUtils.encode(dto.getPassword()));
        userMapper.insertUser(user);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userMapper.selectByPhone(phone) != null;
    }
}
