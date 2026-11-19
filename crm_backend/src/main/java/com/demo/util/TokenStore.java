package com.demo.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易 Token 存储：单机内存版，重启即失效。
 * 后续接入 Redis 时只需替换本类实现。
 *
 * token 主体分两类：
 * - emp  ：员工（sys_employee），可访问全部管理接口
 * - user ：手机号注册用户（sys_user），仅能访问自身相关接口
 */
@Component
public class TokenStore {

    public static final String TYPE_EMP = "emp";
    public static final String TYPE_USER = "user";

    /** 有效期 8 小时 */
    private static final long EXPIRE_HOURS = 8;

    private final Map<String, TokenInfo> store = new ConcurrentHashMap<>();

    private record TokenInfo(String type, Long id, LocalDateTime expireAt) {
    }

    /** 员工登录 */
    public String create(Long empId) {
        return create(TYPE_EMP, empId);
    }

    /** 注册用户登录 */
    public String createUser(Long userId) {
        return create(TYPE_USER, userId);
    }

    private String create(String type, Long id) {
        String token = UUID.randomUUID().toString().replace("-", "");
        store.put(token, new TokenInfo(type, id, LocalDateTime.now().plusHours(EXPIRE_HOURS)));
        return token;
    }

    /** 校验并返回 token 主体，过期或不存在返回 null */
    public Principal getPrincipal(String token) {
        if (token == null || token.isBlank()) return null;
        TokenInfo info = store.get(token);
        if (info == null) return null;
        if (info.expireAt().isBefore(LocalDateTime.now())) {
            store.remove(token);
            return null;
        }
        return new Principal(info.type(), info.id());
    }

    /** 校验并返回员工ID，过期、不存在或非员工登录返回 null */
    public Long getEmpId(String token) {
        Principal principal = getPrincipal(token);
        return principal != null && TYPE_EMP.equals(principal.type()) ? principal.id() : null;
    }

    public void remove(String token) {
        if (token != null) store.remove(token);
    }

    /** token 主体：类型 + 主键 */
    public record Principal(String type, Long id) {
    }
}
