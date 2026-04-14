package com.example.demo.doubao;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class DoubaoConfig {
    @Value("${doubao.access-key}")
    private String accessKey;

    @Value("${doubao.secret-key}")
    private String secretKey;

    @Value("${doubao.endpoint-id}")
    private String endpointId;

    @Value("${doubao.model}")
    private String model;

    @Value("${doubao.api-key}")
    private String apiKey;
}