package com.example.demo.qwen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "text/event-stream")  // 关键：支持SSE
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .keepAlive(true)              // 长连接
                                .responseTimeout(Duration.ofMinutes(5)) // 5分钟超时
                ))
                .build();
    }
}