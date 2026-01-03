package com.xhs.clothingpatternbackend.sdk.dashscope;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.xhs.clothingpatternbackend.config.TongYiConfig;
import io.reactivex.Flowable;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-24
 * @Description: AI 服装知识问答
 * @Version: 1.0
 */
@Component
@Slf4j
public class QwenAI {

    @Resource
    private TongYiConfig tongYiConfig;

    /**
     * 系统预设的服装设计师 Prompt
     */
    private static final String SYSTEM_PROMPT = """
        【核心指令】
        你是一个高度智能、专业且富有创造力的 AI 时尚设计顾问。
        用户的输入将包含两部分：【角色设定】和【用户问题】。
        
        请严格遵守以下规则进行回复：
        1. **角色沉浸**：必须完全采纳用户提供的【角色设定】，以该专家的口吻、视角和专业深度进行回答。如果用户未提供角色设定，默认以“全能时尚设计顾问”的身份回答。
        2. **格式规范**：回答必须使用标准的 Markdown 格式，结构清晰，层级分明。
        3. **输出框架**：无论扮演什么角色，请尽量适配以下结构化框架（可根据具体角色特性微调标题，但逻辑深度要保持一致）：
        
        ## 核心观点
        用1-2句话精炼地回答问题的本质或核心建议。
        
        ---
        
        ### 专业解析
        根据当前角色的专业视角，从至少两个维度进行深度拆解（例如：设计原理、文化背景、技术工艺、市场趋势、心理学机制等）。
        - **维度一**：...
        - **维度二**：...
        
        ### 实践指南 / 避坑建议
        提供具体的落地建议，使用 Emoji 增强可读性。
        ✅ **推荐做法**：...
        ❌ **常见误区**：...
        
        ---
        
        ### 💡 灵感与下一步
        给出一个具体的行动建议或灵感关键词。
        
        > "一句符合当前角色风格的金句或设计理念"
        
        ---
        
        **风格要求**：
        - 专业、客观但富有温度。
        - 必须使用 Markdown 语法（##, ###, **, >, ---）。
        - 遇到专业术语时，请用通俗语言简要解释。
        """;

    /**
     * 市场分析师系统预设 Prompt
     */
    private static final String ANALYST_SYSTEM_PROMPT = """
        【核心指令】
        你是一位专业的服装图案市场分析师，擅长根据数据分析市场趋势并为设计师提供专业建议。
        
        请严格遵守以下规则进行回复：
        1. **数据驱动**：基于用户提供的数据进行客观分析，不捕风捉影。
        2. **结构化输出**：回答必须使用 Markdown 格式，条理清晰。
        3. **实用导向**：给出可落地的建议，对设计师有实际参考价值。
        
        **输出框架**：
        
        ## 📊 数据概览
        简要总结当前数据的整体情况。
        
        ## 📈 市场趋势
        分析当前流行的风格、目标人群特点及变化趋势。
        
        ## 🔍 深度洞察
        - **用户增长**：分析平台用户增长的健康度
        - **内容生产**：分析图案/文章的活跃度
        - **互动指标**：分析用户参与度和偏好
        
        ## 🎯 设计师建议
        根据数据分析结果，给设计师的创作方向建议：
        ✅ **热门方向**：当前最受欢迎的风格/人群
        💡 **蓝海机会**：数据显示的市场空白
        ⚠️ **风险提示**：需要避免的趋势
        
        ## 📝 总结
        用1-2句话概括核心发现和行动建议。
        
        ---
        
        **风格要求**：
        - 专业、客观、数据说话
        - 使用 Markdown 语法（##, ###, **, ✅, 💡, ⚠️）
        - 分析要具体，避免空洞说教
        - 建议要实操，能直接落地
        """;

    /**
     * 根据角色获取对应的系统预设
     *
     * @param role 角色类型
     * @return 系统预设 Prompt
     */
    private String getSystemPromptByRole(String role) {
        if ("analyst".equals(role)) {
            return ANALYST_SYSTEM_PROMPT;
        }
        // 默认返回设计师预设
        return SYSTEM_PROMPT;
    }

    /**
     * 流式调用服装知识问答（仅文本）
     *
     * @param userQuestion 用户问题
     * @param onChunk      每次收到文本片段时的回调
     * @throws NoApiKeyException     API Key 异常
     * @throws UploadFileException   上传文件异常
     */
    public void streamCallText(String userQuestion, ChunkCallback onChunk) throws NoApiKeyException, UploadFileException {
        streamCallTextWithRole(userQuestion, null, onChunk);
    }

    /**
     * 流式调用服装知识问答（仅文本，支持角色选择）
     *
     * @param userQuestion 用户问题
     * @param role         角色类型（analyst-市场分析师，其他-设计师）
     * @param onChunk      每次收到文本片段时的回调
     * @throws NoApiKeyException     API Key 异常
     * @throws UploadFileException   上传文件异常
     */
    public void streamCallTextWithRole(String userQuestion, String role, ChunkCallback onChunk) throws NoApiKeyException, UploadFileException {
        MultiModalConversation conv = new MultiModalConversation();

        // 根据角色获取系统预设
        String systemPrompt = getSystemPromptByRole(role);

        // 构建系统消息
        MultiModalMessage systemMessage = MultiModalMessage.builder()
                .role(Role.SYSTEM.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("text", systemPrompt)
                ))
                .build();

        // 构建用户消息
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("text", userQuestion)
                ))
                .build();

        // 构建请求参数
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(tongYiConfig.getDashscopeApiKey())
                .model("qwen3-vl-plus")  // 使用多模态模型
                .messages(Arrays.asList(systemMessage, userMessage))
                .incrementalOutput(true)  // 开启增量输出
                .build();

        // 流式调用
        Flowable<MultiModalConversationResult> result = conv.streamCall(param);
        result.blockingForEach(item -> {
            try {
                var content = item.getOutput().getChoices().get(0).getMessage().getContent();
                // 判断 content 是否存在且不为空
                if (content != null && !content.isEmpty()) {
                    String text = (String) content.get(0).get("text");
                    if (text != null && !text.isEmpty()) {
                        // 回调处理每个文本片段
                        onChunk.onChunk(text);
                    }
                }
            } catch (Exception e) {
                log.error("处理流式响应时出错: {}", e.getMessage(), e);
                onChunk.onError(e);
            }
        });
    }

    /**
     * 流式调用服装知识问答（支持图片）
     *
     * @param userQuestion 用户问题
     * @param imageUrl     图片 URL（可选）
     * @param onChunk      每次收到文本片段时的回调
     * @throws NoApiKeyException     API Key 异常
     * @throws UploadFileException   上传文件异常
     */
    public void streamCallWithImage(String userQuestion, String imageUrl, ChunkCallback onChunk) throws NoApiKeyException, UploadFileException {
        MultiModalConversation conv = new MultiModalConversation();

        // 构建系统消息
        MultiModalMessage systemMessage = MultiModalMessage.builder()
                .role(Role.SYSTEM.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("text", SYSTEM_PROMPT)
                ))
                .build();

        // 构建用户消息内容（包含图片和文本）
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("image", imageUrl);

        Map<String, Object> textContent = new HashMap<>();
        textContent.put("text", userQuestion);

        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(imageContent, textContent))
                .build();

        // 构建请求参数
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(tongYiConfig.getDashscopeApiKey())
                .model("qwen3-vl-plus")  // 使用多模态模型
                .messages(Arrays.asList(systemMessage, userMessage))
                .incrementalOutput(true)  // 开启增量输出
                .build();

        // 流式调用
        Flowable<MultiModalConversationResult> result = conv.streamCall(param);
        result.blockingForEach(item -> {
            try {
                var content = item.getOutput().getChoices().get(0).getMessage().getContent();
                // 判断 content 是否存在且不为空
                if (content != null && !content.isEmpty()) {
                    String text = (String) content.get(0).get("text");
                    if (text != null && !text.isEmpty()) {
                        // 回调处理每个文本片段
                        onChunk.onChunk(text);
                    }
                }
            } catch (Exception e) {
                log.error("处理流式响应时出错: {}", e.getMessage(), e);
                onChunk.onError(e);
            }
        });
    }

    /**
     * 同步调用服装知识问答（返回完整文本）
     *
     * @param userQuestion 用户问题
     * @return 完整回答文本
     */
    public String syncCallText(String userQuestion) {
        StringBuilder fullResponse = new StringBuilder();
        try {
            streamCallText(userQuestion, new ChunkCallback() {
                @Override
                public void onChunk(String chunk) {
                    fullResponse.append(chunk);
                }

                @Override
                public void onError(Exception e) {
                    log.error("同步调用出错: {}", e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("调用 AI 问答服务失败: {}", e.getMessage(), e);
            return "抱歉，服务暂时不可用，请稍后再试。";
        }
        return fullResponse.toString();
    }

    /**
     * 同步调用服装知识问答（支持图片，返回完整文本）
     *
     * @param userQuestion 用户问题
     * @param imageUrl     图片 URL
     * @return 完整回答文本
     */
    public String syncCallWithImage(String userQuestion, String imageUrl) {
        StringBuilder fullResponse = new StringBuilder();
        try {
            streamCallWithImage(userQuestion, imageUrl, new ChunkCallback() {
                @Override
                public void onChunk(String chunk) {
                    fullResponse.append(chunk);
                }

                @Override
                public void onError(Exception e) {
                    log.error("同步调用出错: {}", e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("调用 AI 问答服务失败: {}", e.getMessage(), e);
            return "抱歉，服务暂时不可用，请稍后再试。";
        }
        return fullResponse.toString();
    }

    /**
     * 流式响应回调接口
     */
    public interface ChunkCallback {
        /**
         * 接收文本片段
         *
         * @param chunk 文本片段
         */
        void onChunk(String chunk);

        /**
         * 处理错误
         *
         * @param e 异常
         */
        void onError(Exception e);
    }
}
