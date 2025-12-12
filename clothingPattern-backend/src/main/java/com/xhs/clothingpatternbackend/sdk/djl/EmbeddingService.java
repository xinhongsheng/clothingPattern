package com.xhs.clothingpatternbackend.sdk.djl;

import ai.djl.ModelException;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.IOException;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-12
 * @Description:向量生成服务
 * @Version: 1.0
 */
@Service
public class EmbeddingService {
    private ZooModel<String, float[]> model;

    @PostConstruct
    public void init() throws ModelException, IOException {
        // 1. 定义模型下载和加载的标准
        // 这里直接使用 HuggingFace 上非常成熟的小型语义模型
        Criteria<String, float[]> criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
                .optEngine("PyTorch") // 或者使用 OnnxRuntime，取决于你下载的包
                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                .optProgress(new ProgressBar())
                .build();

        // 2. 加载模型 (第一次启动会从网络下载模型到本地缓存，之后就是离线加载)
        this.model = criteria.loadModel();
        System.out.println("AI 模型加载完成！");
    }

    /**
     * 将文本转化为向量
     * @param text 用户输入的提示词 (Prompt)
     * @return 384维的 float 向量
     */
    public float[] vectorize(String text) {
        try (Predictor<String, float[]> predictor = model.newPredictor()) {
            return predictor.predict(text);
        } catch (TranslateException e) {
            e.printStackTrace();
            return new float[0];
        }
    }
}
