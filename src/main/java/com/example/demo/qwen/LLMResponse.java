package com.example.demo.qwen;

import lombok.Data;


@Data
public class LLMResponse {
    private Output output;  // 必须是 output
    private Usage usage;

    @Data
    public static class Output {
        private String text;       // 必须是 text
        private String finish_reason;
    }

    @Data
    public static class Usage {
        private Integer total_tokens;
        private Integer input_tokens;
        private Integer output_tokens;
    }
}
