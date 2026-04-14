package com.example.demo.doubao;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class DoubaoService {

    private final DoubaoConfig config;


    public String chat(String prompt) {
        String url = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";

        Map<String, Serializable> params = Map.of(
                "model", config.getModel(),
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                }
        );

        HttpResponse resp = HttpRequest.post(url)
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .body(JSON.toJSONString(params))
                .timeout(20000)
                .execute();

        String body = resp.body();
        System.out.println("=== 原始返回 ===");
        System.out.println(body);

        JSONObject json = JSON.parseObject(body);

        // 安全判断，绝不空指针
        if (json.containsKey("error")) {
            return "接口错误：" + json.getJSONObject("error").getString("message");
        }
        if (!json.containsKey("choices") || json.getJSONArray("choices").isEmpty()) {
            return "无返回内容";
        }

        return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }
}