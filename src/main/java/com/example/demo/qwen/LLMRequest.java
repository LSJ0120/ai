package com.example.demo.qwen;

import lombok.Data;

import java.util.List;

@Data
public class LLMRequest {
    private String model;
    private Input input;
    private Parameters parameters;

    @Data
    public static class Input {
        private List<Message> messages;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    public static class Parameters {
        private boolean stream = true;
        private Double temperature;
        private Integer max_tokens;
    }
}
