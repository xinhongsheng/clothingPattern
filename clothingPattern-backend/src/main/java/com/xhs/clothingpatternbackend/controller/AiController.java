package com.xhs.clothingpatternbackend.controller;

import cn.hutool.core.util.StrUtil;
import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.model.dto.ai.AiQuestionRequest;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.vo.AiAnswerVO;
import com.xhs.clothingpatternbackend.sdk.dashscope.QwenAI;
import com.xhs.clothingpatternbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-24
 * @Description: AI 知识问答接口
 * @Version: 1.0
 */
@RestController
@RequestMapping("/ai")
@Slf4j
public class AiController {

    @Resource
    private QwenAI qwenAI;

    @Resource
    private UserService userService;

    /**
     * 同步问答（返回完整答案）
     *
     * @param aiQuestionRequest 问题请求
     * @param request           HTTP 请求
     * @return AI 回答
     */
    @PostMapping("/ask")
    public BaseResponse<AiAnswerVO> askQuestion(@RequestBody AiQuestionRequest aiQuestionRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(aiQuestionRequest == null, ErrorCode.PARAMS_ERROR);
        String question = aiQuestionRequest.getQuestion();
        String imageUrl = aiQuestionRequest.getImageUrl();

        // 校验问题
        ThrowUtils.throwIf(StrUtil.isBlank(question), ErrorCode.PARAMS_ERROR, "问题不能为空");
        ThrowUtils.throwIf(question.length() > 1000, ErrorCode.PARAMS_ERROR, "问题过长");

        // 获取登录用户（必须登录）
        User loginUser = null;
        loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 调用 AI 服务
        String answer;
        if (StrUtil.isNotBlank(imageUrl)) {
            // 带图片的问答
            answer = qwenAI.syncCallWithImage(question, imageUrl);
        } else {
            // 纯文本问答
            answer = qwenAI.syncCallText(question);
        }

        // 构建响应
        AiAnswerVO aiAnswerVO = new AiAnswerVO();
        aiAnswerVO.setQuestion(question);
        aiAnswerVO.setAnswer(answer);
        aiAnswerVO.setImageUrl(imageUrl);

        return ResultUtils.success(aiAnswerVO);
    }

    /**
     * 流式问答（SSE 流式输出）
     *
     * @param aiQuestionRequest 问题请求
     * @param request           HTTP 请求
     * @return SSE 流
     */
    @PostMapping("/ask/stream")
    public SseEmitter askQuestionStream(@RequestBody AiQuestionRequest aiQuestionRequest,
                                         HttpServletRequest request) {
        ThrowUtils.throwIf(aiQuestionRequest == null, ErrorCode.PARAMS_ERROR);
        String question = aiQuestionRequest.getQuestion();
        String imageUrl = aiQuestionRequest.getImageUrl();
        String role = aiQuestionRequest.getRole();

        // 校验问题
        ThrowUtils.throwIf(StrUtil.isBlank(question), ErrorCode.PARAMS_ERROR, "问题不能为空");
        ThrowUtils.throwIf(question.length() > 10000, ErrorCode.PARAMS_ERROR, "问题过长");
        // 获取登录用户（必须登录）
        User loginUser = null;
        loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 创建 SSE Emitter，超时时间 5 分钟
        SseEmitter emitter = new SseEmitter(300000L);

        // 异步处理
        CompletableFuture.runAsync(() -> {
            try {
                if (StrUtil.isNotBlank(imageUrl)) {
                    // 带图片的流式问答
                    qwenAI.streamCallWithImage(question, imageUrl, new QwenAI.ChunkCallback() {
                        @Override
                        public void onChunk(String chunk) {
                            try {
                                // SSE 中换行符会导致消息截断，转义为\\n
                                String escapedChunk = chunk.replace("\n", "\\n");
                                emitter.send(SseEmitter.event().data(escapedChunk));
                            } catch (IOException e) {
                                log.error("发送 SSE 消息失败: {}", e.getMessage());
                                emitter.completeWithError(e);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            log.error("AI 问答出错: {}", e.getMessage(), e);
                            emitter.completeWithError(e);
                        }
                    });
                } else {
                    // 纯文本的流式问答（支持角色选择）
                    qwenAI.streamCallTextWithRole(question, role, new QwenAI.ChunkCallback() {
                        @Override
                        public void onChunk(String chunk) {
                            try {
                                // SSE 中换行符会导致消息截断，转义为\\n
                                String escapedChunk = chunk.replace("\n", "\\n");
                                emitter.send(SseEmitter.event().data(escapedChunk));
                            } catch (IOException e) {
                                log.error("发送 SSE 消息失败: {}", e.getMessage());
                                emitter.completeWithError(e);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            log.error("AI 问答出错: {}", e.getMessage(), e);
                            emitter.completeWithError(e);
                        }
                    });
                }
                // 完成流式响应
                emitter.complete();
            } catch (Exception e) {
                log.error("流式问答异常: {}", e.getMessage(), e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 获取常见问题列表
     *
     * @return 常见问题
     */
    @GetMapping("/questions")
    public BaseResponse<String[]> getCommonQuestions() {
        String[] questions = {
                "什么是波点图案？如何在服装设计中应用？",
                "复古风格的服装图案有哪些特点？",
                "如何设计适合夏季的清新图案？",
                "民族风图案在现代服装中如何运用？",
                "抽象艺术风格的图案设计技巧有哪些？",
                "如何选择适合不同面料的印花工艺？"
        };
        return ResultUtils.success(questions);
    }
}
