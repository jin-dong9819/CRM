package com.demo.interceptor;

import com.demo.common.Result;
import com.demo.util.TokenStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/** 登录拦截：校验 Authorization: Bearer token */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    /** 员工ID，仅员工登录时写入 */
    public static final String EMP_ID_ATTR = "empId";
    /** 注册用户ID，仅手机号登录时写入 */
    public static final String USER_ID_ATTR = "userId";
    /** token 主体类型：emp / user */
    public static final String PRINCIPAL_TYPE_ATTR = "principalType";

    private final TokenStore tokenStore;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String header = request.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
        TokenStore.Principal principal = tokenStore.getPrincipal(token);

        if (principal == null) {
            write(response, Result.fail(401, "登录已过期，请重新登录"));
            return false;
        }

        // 注册用户没有角色与权限，系统管理接口一律拒绝
        if (TokenStore.TYPE_USER.equals(principal.type())
                && request.getRequestURI().startsWith("/api/system")) {
            write(response, Result.fail(403, "当前账号无权访问系统管理"));
            return false;
        }

        request.setAttribute(PRINCIPAL_TYPE_ATTR, principal.type());
        if (TokenStore.TYPE_EMP.equals(principal.type())) {
            request.setAttribute(EMP_ID_ATTR, principal.id());
        } else {
            request.setAttribute(USER_ID_ATTR, principal.id());
        }
        return true;
    }

    private void write(HttpServletResponse response, Result<Void> result) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
