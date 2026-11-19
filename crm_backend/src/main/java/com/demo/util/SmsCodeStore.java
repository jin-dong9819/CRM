package com.demo.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信验证码 + 发送频控的本地内存存储（替代 Redis）。
 *
 * 特点：不依赖任何外部服务，本地开发直接可用；但只在本机有效，服务重启后数据清空，
 * 多实例部署时各实例互相看不到。生产环境仍建议换回 Redis（把内部实现换成
 * StringRedisTemplate 即可，对外方法签名保持不变）。
 */
@Component
public class SmsCodeStore {

    /** 验证码：手机号 -> 验证码 + 过期时间 */
    private final Map<String, Entry> codeStore = new ConcurrentHashMap<>();

    /** 频控：手机号 -> 允许再次发送的时间点 */
    private final Map<String, LocalDateTime> limitStore = new ConcurrentHashMap<>();

    /** 保存验证码，ttlSeconds 秒后失效 */
    public void putCode(String phone, String code, long ttlSeconds) {
        sweep();
        codeStore.put(phone, new Entry(code, LocalDateTime.now().plusSeconds(ttlSeconds)));
    }

    /** 获取验证码；不存在或已过期返回 null */
    public String getCode(String phone) {
        Entry entry = codeStore.get(phone);
        if (entry == null) {
            return null;
        }
        if (entry.expireAt().isBefore(LocalDateTime.now())) {
            codeStore.remove(phone);
            return null;
        }
        return entry.code();
    }

    public void removeCode(String phone) {
        codeStore.remove(phone);
    }

    /** 是否仍在频控时间内 */
    public boolean isLimited(String phone) {
        LocalDateTime allowAt = limitStore.get(phone);
        if (allowAt == null) {
            return false;
        }
        if (allowAt.isBefore(LocalDateTime.now())) {
            limitStore.remove(phone);
            return false;
        }
        return true;
    }

    /** 标记频控，seconds 秒内不允许再次发送 */
    public void markLimited(String phone, long seconds) {
        limitStore.put(phone, LocalDateTime.now().plusSeconds(seconds));
    }

    public void removeLimit(String phone) {
        limitStore.remove(phone);
    }

    /**
     * 顺手清理过期条目。
     * 内存 Map 没有 Redis 的自动过期，不清理会随手机号数量缓慢增长；
     * 在写入时做一次全量扫描，验证码场景数据量很小，成本可以忽略。
     */
    private void sweep() {
        LocalDateTime now = LocalDateTime.now();
        codeStore.entrySet().removeIf(e -> e.getValue().expireAt().isBefore(now));
        limitStore.entrySet().removeIf(e -> e.getValue().isBefore(now));
    }

    /** 验证码条目：验证码 + 过期时间 */
    private record Entry(String code, LocalDateTime expireAt) {
    }
}
