package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // 启动类上方添加注释，表示这是一个Spring Boot启动类
public class DemoApplication {
    public static void main(String[] args) {
        // 必须把 args 传进去：--spring.profiles.active=prod 这类启动参数才不会丢，
        // 否则 systemd 里的 --spring.profiles.active=prod 会被静默忽略，生产跑的是开发配置
        SpringApplication.run(DemoApplication.class, args);
    }
}
