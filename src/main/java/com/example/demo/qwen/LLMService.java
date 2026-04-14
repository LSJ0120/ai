package com.example.demo.qwen;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Slf4j
@Service
public class LLMService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ChatModel chatModel;
    private final ChatClient chatClient;

    @Value("${spring.llm.tongyi.api-url}")
    private String apiUrl;

    @Value("${spring.llm.tongyi.api-key}")
    private String apiKey;

    @Value("${spring.llm.tongyi.model}")
    private String model;

    @Value("${spring.llm.tongyi.temperature:0.7}")
    private Double temperature;

    @Value("${spring.llm.tongyi.max-tokens:2048}")
    private Integer maxTokens;

    public LLMService(ChatModel chatModel, WebClient webClient,  ChatModel chatModel1) {
        this.chatClient = ChatClient.builder(chatModel).defaultSystem("你是Java程序员的开发助手，名字叫lsj").build();
        this.webClient = webClient;
        this.chatModel = chatModel1;
        this.objectMapper = new ObjectMapper();
    }

    LLMRequest buildRequest(String prompt) {
        LLMRequest.Message message = new LLMRequest.Message();
        message.setRole("user");
        message.setContent(prompt);

        LLMRequest.Input input = new LLMRequest.Input();
        input.setMessages(Collections.singletonList(message));

        LLMRequest.Parameters parameters = new LLMRequest.Parameters();
        parameters.setTemperature(temperature);
        parameters.setMax_tokens(maxTokens);

        LLMRequest request = new LLMRequest();
        request.setModel(model);
        request.setInput(input);
        request.setParameters(parameters);
        request.getParameters().setStream(true);
        return request;
    }

    // ===================== 调用千问接口 =====================
    public Mono<String> chat(String prompt) {
        LLMRequest request = buildRequest(prompt);

        return webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LLMResponse.class)
                .map(response -> response.getOutput().getText());
    }

    public Flux<Object> streamChat(String prompt) {
        LLMRequest request = buildRequest(prompt);
        return webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("X-DashScope-SSE", "enable")
                .header("Accept", "text/event-stream")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(line -> log.debug("收到SSE行: {}", line))
                .handle((line, sink) -> {
                    try {
                        String text = parseSseLine(line);
                        if (text != null && !text.isBlank()) {
                            log.debug("解析出文本: {}", text);
                            sink.next(text);
                        }
                    } catch (Exception e) {
                        log.warn("解析SSE行失败: {}, 错误: {}", line, e.getMessage());
                    }
                })
                .doOnError(error -> log.error("流式输出错误: ", error))
                .doOnComplete(() -> log.info("流式输出完成"));
    }

    // 解析千问 SSE 数据流
    private String parseSseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        try {
            String trimmedLine = line.trim();

            // 处理 SSE 格式: data: {...}
            if (trimmedLine.startsWith("data:")) {
                String json = trimmedLine.substring(5).trim();

                // 检查是否为结束标记
                if (json.isEmpty() || json.equals("[DONE]")) {
                    return null;
                }

                LLMResponse resp = new ObjectMapper().readValue(json, LLMResponse.class);
                String text = resp.getOutput() != null ? resp.getOutput().getText() : null;




                return text;
            }


            return null;
        } catch (Exception e) {
            log.warn("解析SSE行异常: {}", e.getMessage());
            return null;
        }
    }

}
