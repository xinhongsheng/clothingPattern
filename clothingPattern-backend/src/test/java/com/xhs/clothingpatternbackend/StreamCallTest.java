package com.xhs.clothingpatternbackend;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.xhs.clothingpatternbackend.config.TongYiConfig;
import com.xhs.clothingpatternbackend.sdk.dashscope.QwenAI;
import io.reactivex.Flowable;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-24
 * @Description: 流式调用测试
 * @Version: 1.0
 */
@SpringBootTest
public class StreamCallTest {


    @Resource
    private TongYiConfig tongYiConfig;

    @Resource
    private QwenAI qwenAI;

    @Test
    public void testStreamCall() throws NoApiKeyException, UploadFileException {
        // 若使用新加坡地域的模型，请取消下列注释
        //  static {Constants.baseHttpApiUrl="https://dashscope-intl.aliyuncs.com/api/v1";}
            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                    .content(Arrays.asList(Collections.singletonMap("image", "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241022/emyrja/dog_and_girl.jpeg"),
                            Collections.singletonMap("text", "图中描绘的是什么景象？"))).build();
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                    // 新加坡和北京地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
                    .apiKey(tongYiConfig.getDashscopeApiKey())
                    .model("qwen3-vl-plus")  // 可按需更换为其它多模态模型，并修改相应的 messages
                    .messages(Arrays.asList(userMessage))
                    .incrementalOutput(true)
                    .build();
            Flowable<MultiModalConversationResult> result = conv.streamCall(param);
            result.blockingForEach(item -> {
                try {
                    var content = item.getOutput().getChoices().get(0).getMessage().getContent();
                    // 判断content是否存在且不为空
                    if (content != null &&  !content.isEmpty()) {
                        System.out.println(content.get(0).get("text"));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
    }

    /**
     * 测试服装知识问答（纯文本）
     */
    @Test
    public void testQwenAIText() {
        String question = "什么是波点图案？如何在服装设计中应用？";
        System.out.println("用户问题：" + question);
        System.out.println("\n=== AI 回答（流式输出）===");

        try {
            qwenAI.streamCallText(question, new QwenAI.ChunkCallback() {
                @Override
                public void onChunk(String chunk) {
                    System.out.print(chunk);
                }

                @Override
                public void onError(Exception e) {
                    System.err.println("\n错误：" + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n\n=== 流式输出结束 ===");
    }

    /**
     * 测试服装知识问答（同步调用）
     */
    @Test
    public void testQwenAISyncText() {
        String question = "复古风格的服装图案有哪些特点？";
        System.out.println("用户问题：" + question);
        System.out.println("\n=== AI 回答（同步）===");

        String answer = qwenAI.syncCallText(question);
        System.out.println(answer);

        System.out.println("\n=== 回答结束 ===");
    }

    /**
     * 测试服装知识问答（带图片）
     */
    @Test
    public void testQwenAIWithImage() {
        String question = "请分析这个图案的设计风格和适用场景。";
        String imageUrl = "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241022/emyrja/dog_and_girl.jpeg";

        System.out.println("用户问题：" + question);
        System.out.println("图片URL：" + imageUrl);
        System.out.println("\n=== AI 回答（带图片）===");

        String answer = qwenAI.syncCallWithImage(question, imageUrl);
        System.out.println(answer);

        System.out.println("\n=== 回答结束 ===");
    }
}
