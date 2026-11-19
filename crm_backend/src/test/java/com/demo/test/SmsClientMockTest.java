package com.demo.test;

import com.demo.util.RonglianSmsClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 短信客户端的离线测试：用 JDK 自带的 HttpServer 起一个假接口，
 * 把 client 的 base-url 指过去，验证请求拼装和响应解析，全程不出网、不发短信、不花钱。
 * <p>
 * 这是替代 Test_SMS 的做法 —— 那里打的是真实容联云接口，只能手动开。
 */
public class SmsClientMockTest {

    private static final String ACCOUNT_SID = "aabbccdd";
    private static final String AUTH_TOKEN = "token123";
    private static final String APP_ID = "appId123";
    private static final String TEMPLATE_ID = "1";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpServer server;

    /** 假接口这次要返回的内容，各用例可改 */
    private volatile String responseBody;
    private volatile int responseStatus;

    /** 假接口收到的请求，供断言使用 */
    private volatile String requestUri;
    private volatile String requestBody;
    private volatile String authorizationHeader;

    @BeforeEach
    public void startFakeServer() throws IOException {
        responseStatus = 200;
        responseBody = "{\"statusCode\":\"000000\","
                + "\"templateSMS\":{\"smsMessageSid\":\"sid-1\",\"dateCreated\":\"20261118152500\"}}";

        // 端口传 0 表示由系统分配空闲端口，避免和本机其他服务撞车
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requestUri = exchange.getRequestURI().toString();
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            authorizationHeader = exchange.getRequestHeaders().getFirst("Authorization");

            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            exchange.sendResponseHeaders(responseStatus, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        server.start();
    }

    @AfterEach
    public void stopFakeServer() {
        server.stop(0);
    }

    private RonglianSmsClient newClient() {
        return new RonglianSmsClient(
                ACCOUNT_SID, AUTH_TOKEN, APP_ID, TEMPLATE_ID,
                "http://127.0.0.1:" + server.getAddress().getPort(),
                objectMapper);
    }

    @Test
    public void test1_发送成功时请求参数正确() throws Exception {
        JsonNode result = newClient().send("18538313405", "1234", "2");

        // 1. 返回体原样透出
        assertEquals("000000", result.path("statusCode").asText());
        assertEquals("sid-1", result.path("templateSMS").path("smsMessageSid").asText());

        // 2. URL 拼装：/{版本}/Accounts/{sid}/SMS/TemplateSMS?sig=32位大写MD5
        assertTrue(requestUri.startsWith(
                        "/2013-12-26/Accounts/" + ACCOUNT_SID + "/SMS/TemplateSMS?sig="),
                "实际请求路径：" + requestUri);
        String sig = requestUri.substring(requestUri.indexOf("sig=") + 4);
        assertTrue(sig.matches("[0-9A-F]{32}"), "sig 应为 32 位大写 MD5，实际：" + sig);

        // 3. 请求体：手机号、appId、模板、变量
        JsonNode body = objectMapper.readTree(requestBody);
        assertEquals("18538313405", body.path("to").asText());
        assertEquals(APP_ID, body.path("appId").asText());
        assertEquals(TEMPLATE_ID, body.path("templateId").asText());
        assertEquals("1234", body.path("datas").get(0).asText());
        assertEquals("2", body.path("datas").get(1).asText());

        // 4. Authorization 是 Base64("accountSid:yyyyMMddHHmmss")
        String decoded = new String(
                Base64.getDecoder().decode(authorizationHeader), StandardCharsets.UTF_8);
        assertTrue(decoded.matches(ACCOUNT_SID + ":\\d{14}"),
                "Authorization 解码后应为 accountSid:14位时间戳，实际：" + decoded);
    }

    @Test
    public void test2_平台返回失败码时抛出IOException() {
        responseBody = "{\"statusCode\":\"160203\",\"statusMsg\":\"模板不存在\"}";

        IOException e = assertThrows(IOException.class,
                () -> newClient().send("18538313405", "1234"));

        assertTrue(e.getMessage().contains("160203"), "异常信息应带上平台错误码：" + e.getMessage());
    }

    @Test
    public void test3_HTTP非2xx时抛出IOException() {
        responseStatus = 500;
        responseBody = "internal error";

        IOException e = assertThrows(IOException.class,
                () -> newClient().send("18538313405", "1234"));

        assertTrue(e.getMessage().contains("500"), "异常信息应带上 HTTP 状态码：" + e.getMessage());
    }

    @Test
    public void test4_手机号格式错误时本地就拦下() {
        // 不发请求，直接参数校验失败（号码长度不对 / 非数字）
        assertThrows(IllegalArgumentException.class,
                () -> newClient().send("12345", "1234"));
        assertThrows(IllegalArgumentException.class,
                () -> newClient().send("1853831340a", "1234"));
        assertThrows(IllegalArgumentException.class,
                () -> newClient().send(" ", "1234"));
    }
}
