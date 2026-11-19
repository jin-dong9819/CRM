package com.demo.controller;

import com.demo.common.BizException;
import com.demo.common.Result;
import com.demo.dto.RegisterDTO;
import com.demo.service.UserService;
import com.demo.util.CodeUtils;
import com.demo.util.RonglianSmsClient;
import com.demo.util.SmsCodeStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 注册相关接口（与 AuthController 同属 /api/auth，路径不冲突）
 * 1. POST /api/auth/send-code  发送短信验证码
 * 2. POST /api/auth/register   手机号 + 验证码 + 密码 注册
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    /** 验证码有效期 2 分钟 */
    private static final Duration CODE_TTL = Duration.ofMinutes(2);
    /** 同一手机号 60 秒内只能发一次 */
    private static final Duration LIMIT_TTL = Duration.ofSeconds(60);

    private final SmsCodeStore smsCodeStore;
    private final RonglianSmsClient ronglianSmsClient;
    private final UserService userService;

    /** 开发模式：不真正发短信，验证码直接返回给前端，方便本地联调 */
    @Value("${sms.dev-mode:false}")
    private boolean devMode;

    /** 发送短信验证码 */
    @PostMapping("/send-code")
    public Result<Map<String, Object>> sendCode(@RequestParam String phone) {
        String target = phone == null ? "" : phone.trim();
        if (!target.matches("^1[3-9]\\d{9}$")) {
            throw new BizException(400, "请输入正确的 11 位手机号");
        }
        if (userService.existsByPhone(target)) {
            throw new BizException(409, "该手机号已注册，请直接登录");
        }
        if (smsCodeStore.isLimited(target)) {
            throw new BizException(429, "验证码已发送，请稍后再试");
        }

        String code = CodeUtils.generateCode();
        smsCodeStore.putCode(target, code, CODE_TTL.toSeconds());
        smsCodeStore.markLimited(target, LIMIT_TTL.toSeconds());

        Map<String, Object> data = new HashMap<>();
        if (devMode) {
            log.info("【开发模式】手机号 {} 的验证码为 {}", target, code);
            data.put("devCode", code);
        } else {
            try {
                // 模板变量：验证码、有效分钟数
                ronglianSmsClient.send(target, code, String.valueOf(CODE_TTL.toMinutes()));
            } catch (Exception e) {
                // 发送失败要清掉缓存，否则用户被 60 秒限流卡住
                smsCodeStore.removeCode(target);
                smsCodeStore.removeLimit(target);
                log.error("短信发送失败，手机号={}", target, e);
                throw new BizException(500, "短信发送失败，请稍后重试");
            }
        }
        return Result.ok(data, "验证码已发送");
    }

    /** 注册：先校验本地缓存的验证码，再落库 */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        String phone = dto.getPhone().trim();

        String cachedCode = smsCodeStore.getCode(phone);
        if (cachedCode == null) {
            throw new BizException(400, "验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(dto.getCode().trim())) {
            throw new BizException(400, "验证码不正确");
        }

        userService.register(dto);

        // 注册成功后验证码立即失效，防止重复使用
        smsCodeStore.removeCode(phone);
        smsCodeStore.removeLimit(phone);
        return Result.ok(null, "注册成功");
    }
}
