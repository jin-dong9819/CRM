package com.demo.controller;

import com.demo.common.Result;
import com.demo.dto.LoginDTO;
import com.demo.dto.PhoneLoginDTO;
import com.demo.interceptor.AuthInterceptor;
import com.demo.service.AuthService;
import com.demo.util.TokenStore;
import com.demo.vo.LoginVO;
import com.demo.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 员工登录：员工编号 + 密码 */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto, HttpServletRequest request) {
        return Result.ok(authService.login(dto, getIp(request)), "登录成功");
    }

    /** 手机号登录：注册用户（sys_user） */
    @PostMapping("/login/phone")
    public Result<LoginVO> loginByPhone(@RequestBody @Valid PhoneLoginDTO dto) {
        return Result.ok(authService.loginByPhone(dto), "登录成功");
    }

    @GetMapping("/profile")
    public Result<UserInfoVO> profile(HttpServletRequest request) {
        Object type = request.getAttribute(AuthInterceptor.PRINCIPAL_TYPE_ATTR);
        if (TokenStore.TYPE_USER.equals(type)) {
            Long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
            return Result.ok(authService.profileOfUser(userId));
        }
        Long empId = (Long) request.getAttribute(AuthInterceptor.EMP_ID_ATTR);
        return Result.ok(authService.profile(empId));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
        authService.logout(token);
        return Result.ok(null, "已退出登录");
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return ip == null || ip.isBlank() ? request.getRemoteAddr() : ip;
    }
}
