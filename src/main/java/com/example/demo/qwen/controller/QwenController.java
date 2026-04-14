package com.example.demo.qwen.controller;

import com.example.demo.qwen.CarInfoResponse;
import com.example.demo.qwen.KnowledgeBase;
import com.example.demo.qwen.MyTools;
import com.example.demo.qwen.repository.InMemoryChatHistoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RestController
@RequestMapping("/ai")
public class QwenController {

    private final InMemoryChatHistoryRepository inMemoryChatHistoryRepository;
    private final ChatClient chatClient;
    private final MyTools myTools;
    private final ChatModel chatModel;
    private final KnowledgeBase knowledgeBase;
    private final WebClient webClient;
    private final ChatMemory chatMemory;

    public QwenController(ChatClient chatClient, ChatMemory chatMemory, MyTools myTools, ChatModel chatModel, KnowledgeBase knowledgeBase, WebClient webClient, InMemoryChatHistoryRepository inMemoryChatHistoryRepository) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.myTools = myTools;
        this.chatModel = chatModel;
        this.knowledgeBase = knowledgeBase;
        this.webClient = webClient;
        this.inMemoryChatHistoryRepository = inMemoryChatHistoryRepository;
    }

    @PostMapping("/chat")
    public Flux<String> chat(String prompt, String chatId) {
        inMemoryChatHistoryRepository.save("chat", chatId);
        return chatClient.prompt(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content();
    }

    @GetMapping("/struct")
    public CarInfoResponse struct(@RequestParam String prompt) {

        // ====================== 提示词工程 ======================
        String systemPrompt = "你是汽车领域专家，只做结构化信息提取。\n" +
                "规则：\n" +
                "1. 只返回 JSON，不返回任何多余文字、解释、标点\n" +
                "2. 字段严格按照以下结构：brand, category, seatCount, fuelType, isCommercial\n" +
                "3. 未知字段填空，不要编造\n" +
                "4. 必须是标准 JSON 格式";

        // ====================== 调用 AI ======================
        return chatClient.prompt()
                .system(systemPrompt)       // 系统提示词
                .user(prompt)               // 用户问题
                .call()
                .entity(CarInfoResponse.class); // 直接转对象
    }

    @GetMapping("/tool")
    public String tool(@RequestParam String input) {
        return chatClient.prompt(input).tools(myTools).call().content();
    }

    @GetMapping("/rag")
    public String rag(@RequestParam String prompt) {

        // 1. 检索知识库
        String context = knowledgeBase.search(prompt);

        // 2. 构造 RAG 提示词
        String ragPrompt = "你是企业智能助手，请根据下面的【参考资料】回答问题。\n" +
                "只使用参考资料里的内容，不要编造。\n" +
                "如果资料里没有答案，就说“未找到相关信息”。\n\n" +
                "【参考资料】\n" +
                context + "\n\n" +
                "【用户问题】\n" +
                prompt;

        // 3. 调用 AI
        return chatClient.prompt()
                .user(ragPrompt)
                .call()
                .content();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_HTML_VALUE + ";charset=utf-8")
    public Flux<String> streamChat(@RequestParam String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
}
