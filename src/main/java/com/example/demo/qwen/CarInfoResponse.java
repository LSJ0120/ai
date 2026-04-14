package com.example.demo.qwen;

import lombok.Data;

@Data
public class CarInfoResponse {
    private String brand;        // 品牌
    private String category;     // 车型分类
    private Integer seatCount;   // 座位数
    private String fuelType;     // 能源类型
    private Boolean isCommercial;// 是否营运车辆
}