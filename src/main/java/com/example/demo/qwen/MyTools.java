package com.example.demo.qwen;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class MyTools {

    // AI 可以自动调用这个方法
    @Tool(description = "根据城市名获取当前实时温度，单位摄氏度")
    public double getCurrentTemperature(String city) {
        System.out.println("AI 调用工具：查询城市温度 → " + city);
        return 26.5;
    }

    @Tool(description = "根据订单编号查询订单状态")
    public String getOrderStatus(String orderNo) {
        System.out.println("AI 调用工具：查询订单 → " + orderNo);
        return "订单状态：待发货";
    }
}