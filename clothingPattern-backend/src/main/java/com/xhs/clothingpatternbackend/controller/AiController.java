package com.xhs.clothingpatternbackend.controller;

import cn.hutool.core.util.StrUtil;
import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.model.dto.ai.AiQuestionRequest;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.vo.AiAnswerVO;
import com.xhs.clothingpatternbackend.sdk.dashscope.QwenAI;
import com.xhs.clothingpatternbackend.service.UserService;
import com.xhs.clothingpatternbackend.utils.CosImageUploadUtils;
import com.xhs.clothingpatternbackend.utils.CosUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    @Resource
    private CosUtils cosUtils;

    @Resource
    private CosClientConfig cosClientConfig;

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
     * 分析参考图片，提取设计元素（用于以图生图功能）
     *
     * @param imageUrl 图片URL
     * @param request  HTTP 请求
     * @return 图片分析结果
     */
    @PostMapping("/analyze-image")
    public BaseResponse<String> analyzeImage(@RequestParam("imageUrl") String imageUrl,
                                              HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(imageUrl), ErrorCode.PARAMS_ERROR, "图片URL不能为空");

        // 获取登录用户（必须登录）
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 用于分析图片的专用提示词
        String analysisPrompt =
                "你是一名【Surface Pattern / 面料花型】设计分析师。输入图片可能是成衣Mockup（图案贴在衬衫/衣服上）。你的任务是：只还原“平面图案本体（pattern tile）”，提取可用于生成【无缝平铺花型】的设计要素。\n" +
                        "\n" +
                        "严格规则（非常重要）：\n" +
                        "1) 【禁止】在任何输出里出现服装载体相关词或描述：shirt, clothing, garment, apparel, collar, button, pocket, sleeve, mannequin, model, outfit, mockup, drape, fold, fabric texture, photo, studio，以及“衬衫/衣服/衣领/纽扣/口袋/袖子/模特/人像/褶皱/垂坠/拍摄/影棚”等。\n" +
                        "2) 只输出图案本体：图案主体、辅助元素、配色、线条与上色方式、重复结构与排布方式（把衣服当作不存在）。\n" +
                        "3) 每个字段只用【关键词/短语】，用中文逗号“，”分隔；不写完整句子，不解释，不复述规则。\n" +
                        "4) 不确定就写“不明确”，不要猜测。\n" +
                        "\n" +
                        "最后输出三行（仍然用中文字段名，但内容用于直接拼接MJ提示词）：\n" +
                        "TilePrompt：把以上要点整合为一条英文逗号短语，并且【必须包含】seamless repeating pattern, surface pattern design, flat 2D, pattern swatch, clean outlines\n" +
                        "MotifPrompt：把以上要点整合为一条英文逗号短语，并且【必须包含】isolated motifs, flat 2D, clean outlines, plain background\n" +
                        "Negative：输出英文负面词，并且【必须包含】mockup, shirt, clothing, garment, collar, buttons, pocket, mannequin, model, folds, photo, watermark, text, logo";

        try {
            // 调用 AI 服务分析图片
            String analysisResult = qwenAI.syncCallWithImage(analysisPrompt, imageUrl);
            log.info("图片分析完成，用户ID: {}, 图片URL: {}", loginUser.getId(), imageUrl);
            return ResultUtils.success(analysisResult);
        } catch (Exception e) {
            log.error("图片分析失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片分析失败：" + e.getMessage());
        }
    }

    /**
     * 上传参考图片（用于以图生图功能）
     *
     * @param file 图片文件
     * @return 图片URL
     */
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<String> uploadReferenceImage(@RequestParam("file") MultipartFile file) {
        try {
            // 校验文件大小（10MB限制）
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小不能超过10MB");
            }

            // 上传到COS
            String url = CosImageUploadUtils.uploadImageToCos(
                    file,
                    0L,
                    cosUtils,
                    cosClientConfig,
                    "img2img_ref_",
                    "img2img-reference/",
                    true
            );

            log.info("参考图片上传成功，URL: {}", url);
            return ResultUtils.success(url);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("参考图片上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败：" + e.getMessage());
        }
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
