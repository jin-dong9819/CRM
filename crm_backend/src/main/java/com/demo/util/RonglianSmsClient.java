package com.demo.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service      //spring框架帮助我们管理类的创建对象
public class RonglianSmsClient {

    /**
     * 官方接口地址。做成配置项而不是常量，是为了本地测试能把请求指向 MockWebServer，
     * 不必真的发短信。
     */
    private static final String DEFAULT_BASE_URL =
            "https://app.cloopen.com:8883";

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final ZoneId TIME_ZONE =
            ZoneId.of("Asia/Shanghai");

    private final String accountSid;
    private final String authToken;
    private final String appId;
    private final String templateId;
    private final String baseUrl;

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Spring 自动从 application.yml 注入配置，业务代码无需手动创建对象
    public RonglianSmsClient(
            @Value("${ronglian.sms.account-sid}") String accountSid,
            @Value("${ronglian.sms.auth-token}") String authToken,
            @Value("${ronglian.sms.app-id}") String appId,
            @Value("${ronglian.sms.template-id}") String templateId,
            @Value("${ronglian.sms.base-url:" + DEFAULT_BASE_URL + "}") String baseUrl,
            ObjectMapper objectMapper) {

        this.accountSid = requireValue(accountSid, "accountSid");
        this.authToken = requireValue(authToken, "authToken");
        this.appId = requireValue(appId, "appId");
        this.templateId = requireValue(templateId, "templateId");
        this.baseUrl = trimTrailingSlash(requireValue(baseUrl, "baseUrl"));
        this.objectMapper = objectMapper;

        if (!this.accountSid.matches("[A-Za-z0-9]+")) {
            throw new IllegalArgumentException("accountSid 格式不正确");
        }
    }

    /**
     * 发送模板短信。
     *
     * @param to    国内手机号，多个号码使用英文逗号分隔，最多 200 个
     * @param datas 模板变量，按模板中的顺序传入；无变量时不传
     * @return 平台响应，成功仅表示接口受理，不代表短信已经送达
     */
    public JsonNode send(String to, String... datas)
            throws IOException, InterruptedException {

        String recipients = normalizeRecipients(to);

        String timestamp = ZonedDateTime.now(TIME_ZONE)
                .format(TIME_FORMAT);

        // 签名和 Authorization 必须使用同一个时间戳
        String sig = md5Upper(accountSid + authToken + timestamp);

        String authorization = Base64.getEncoder().encodeToString(
                (accountSid + ":" + timestamp)
                        .getBytes(StandardCharsets.UTF_8)
        );

        String url = baseUrl
                + "/2013-12-26/Accounts/"
                + accountSid
                + "/SMS/TemplateSMS?sig="
                + sig;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("to", recipients);
        body.put("appId", appId);
        body.put("templateId", templateId);

        if (datas != null && datas.length > 0) {
            for (String value : datas) {
                requireValue(value, "模板变量");
            }
            body.put("datas", datas);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json;charset=utf-8")
                .header("Authorization", authorization)
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(body),
                        StandardCharsets.UTF_8
                ))
                .build();

        HttpResponse<String> response;
        try {
            // 不自动重试，避免请求超时后重复发送短信
            response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(
                    "短信接口 HTTP 请求失败，状态码：" + response.statusCode()
            );
        }

        JsonNode result = objectMapper.readTree(response.body());

        if (result == null || !result.isObject()) {
            throw new IOException("短信接口返回了无效 JSON");
        }

        String statusCode = result.path("statusCode").asText();

        if (!"000000".equals(statusCode)) {
            throw new IOException(
                    "短信发送失败，statusCode=" + statusCode
                            + "，statusMsg="
                            + result.path("statusMsg").asText()
            );
        }

        return result;
    }

    private static String normalizeRecipients(String to) {
        String[] phones = requireValue(to, "接收手机号").split(",", -1);

        if (phones.length > 200) {
            throw new IllegalArgumentException("每批最多发送 200 个手机号");
        }

        for (int i = 0; i < phones.length; i++) {
            phones[i] = phones[i].trim();

            if (!phones[i].matches("1\\d{10}")) {
                throw new IllegalArgumentException(
                        "第 " + (i + 1) + " 个国内手机号格式不正确"
                );
            }
        }

        return String.join(",", phones);
    }

    private static String md5Upper(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5")
                    .digest(value.getBytes(StandardCharsets.UTF_8));

            char[] hex = "0123456789ABCDEF".toCharArray();
            StringBuilder result = new StringBuilder(32);

            for (byte b : digest) {
                result.append(hex[(b & 0xFF) >>> 4]);
                result.append(hex[b & 0x0F]);
            }

            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 Java 环境不支持 MD5", e);
        }
    }

    private static String requireValue(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " 不能为空");
        }
        return value.trim();
    }

    private static String trimTrailingSlash(String value) {
        return value.endsWith("/")
                ? value.substring(0, value.length() - 1)
                : value;
    }

}