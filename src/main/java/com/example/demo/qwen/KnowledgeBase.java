package com.example.demo.qwen;

import org.springframework.stereotype.Component;

/**
 * 企业知识库（纯内存版本）
 * 真实场景：替换成 ES / Redis / PGVector / Milvus
 */
@Component
public class KnowledgeBase{

    // 模拟文档
    private final String[] docs = {
            "豆包公司上班时间：周一到周五 9:00-18:00，午休 12:00-13:00",
            "豆包公司请假流程：提交 OA 申请 → 上级审批 → 人事备案",
            "豆包公司薪资发放：每月 10 号发上个月工资",
            "豆包公司加班规则：工作日加班 1.5 倍工资，周末 2 倍，法定假日 3 倍",
            "豆包公司试用期：2 个月，转正需通过部门答辩"
    };

    // 最简单检索：包含关键词就算匹配
    public String search(String question) {
        StringBuilder result = new StringBuilder();
        for (String doc : docs) {
            if (doc.contains(question) || question.contains(doc)) {
                result.append(doc).append("\n");
            }
        }
        return result.isEmpty() ? "未找到相关信息" : result.toString();
    }
}