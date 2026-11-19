package com.demo.test;

import com.demo.util.RonglianSmsClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 短信客户端联调测试。
 * <p>
 * 注意：这条用例会**真实调用容联云接口并发出一条短信**，依赖账号余额、模板/签名审核状态，
 * 还可能被同号码发送频控拦住（平台返回非 000000 即发送失败）。把它留在默认的 mvn package 里，
 * 等于让一次外部服务的抖动决定整个构建的成败，所以这里默认跳过。
 * <p>
 * 需要联调时显式打开：
 * <pre>
 * mvn test -Dtest=Test_SMS -Dsms.live=true
 * </pre>
 */
@SpringBootTest
public class Test_SMS {

    @Autowired
    private RonglianSmsClient client;

    @Test
    @EnabledIfSystemProperty(named = "sms.live", matches = "true")
    public void test1() throws Exception {
        // 成功后平台返回 statusCode=000000，其余均为失败（客户端内部已抛 IOException）
        JsonNode result = client.send("18538313405", "1234", "2");
        assertEquals("000000", result.path("statusCode").asText());
    }
}
