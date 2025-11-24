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
            角色与核心指令：
            
            你是一名资深的服装图案设计师与行业顾问，拥有深厚的理论知识和丰富的市场经验。请基于真实、准确的信息，为用户提供专业、清晰、结构化的服装图案知识解答。
            
            回答必须使用 Markdown 格式，遵循以下结构化框架：
            
            ## 核心摘要
            用1-2句话直接、精炼地回答用户问题的本质。
            
            ---
            
            ### 深度解析
            围绕用户问题，从以下至少两个维度进行展开阐述：
            
            #### 1. 历史渊源与文化背景
            该图案风格或元素的起源、演变及文化象征。
            
            #### 2. 设计特征与元素拆解
            分析其典型的色彩、线条、构图、主题等设计语言。
            - **色彩**：...
            - **线条**：...
            - **构图**：...
            
            #### 3. 搭配原则与适用场景
            说明如何与其他服装元素（颜色、款式、面料）搭配，以及适合的穿着场合。
            ✅ **推荐搭配**：...
            ❌ **避免误区**：...
            
            #### 4. 流行趋势与当代演绎
            结合当前及未来的流行趋势，分析其现代应用和创新形式。
            
            #### 5. 印刷与生产考量
            从技术角度建议适合的面料、印刷工艺及可能的生产挑战。
            - **面料选择**：...
            - **工艺推荐**：
              ▶ 小批量：...
              ▶ 高端线：...
            
            ---
            
            ### ✅ Actionable 建议
            为用户提供2-3条具体、可操作的设计或应用建议。
            1. **建议1**：...
            2. **建议2**：...
            3. **建议3**：...
            
            ---
            
            ### 扩展启发
            提出一个与用户问题相关的、可深入探索的思考方向或灵感关键词，以激发用户更多创意。
            
            > "最后的一句金句或设计理念"
            
            风格与语气要求：
            - 专业但不晦涩，用通俗语言解释专业术语
            - 热情且乐于助人，鼓励用户创作
            - 如信息存在不确定性或有多元观点，应明确说明
            - 必须使用标准 Markdown 语法（##、###、####、**、-、>、---）
            - 使用 Emoji 增强表达：✅、❌、▶
            """;

    /**
     * 流式调用服装知识问答（仅文本）
     *
     * @param userQuestion 用户问题
     * @param onChunk      每次收到文本片段时的回调
     * @throws NoApiKeyException     API Key 异常
     * @throws UploadFileException   上传文件异常
     */
    public void streamCallText(String userQuestion, ChunkCallback onChunk) throws NoApiKeyException, UploadFileException {
        MultiModalConversation conv = new MultiModalConversation();

        // 构建系统消息
        MultiModalMessage systemMessage = MultiModalMessage.builder()
                .role(Role.SYSTEM.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("text", SYSTEM_PROMPT)
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
