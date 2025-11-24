package com.xhs.clothingpatternbackend;

import com.volcengine.ark.runtime.model.images.generation.GenerateImagesRequest;
import com.volcengine.ark.runtime.model.images.generation.ImagesResponse;
import com.volcengine.ark.runtime.model.images.generation.ResponseFormat;
import com.volcengine.ark.runtime.service.ArkService;
import com.xhs.clothingpatternbackend.config.DouBaoConfig;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-22
 * @Description:
 * @Version: 1.0
 */
@SpringBootTest
public class DouBaoTest {
    @Autowired
    private  DouBaoConfig douBaoConfig;
    /**
     * 文生图
     */
    @Test
    public void testTextGenImage(){
        String apiKey = douBaoConfig.getApiKey();
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        ArkService service = ArkService.builder()
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3") // The base URL for model invocation .
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .apiKey(apiKey)
                .build();

        GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                .model(douBaoConfig.getModelId()) //Replace with Model ID .
                .prompt("星际穿越，黑洞，黑洞里冲出一辆快支离破碎的复古列车，强视觉冲击力，电影大片，末日既视感，动感，对比色，oc渲染，光线追踪，动态模糊，景深，超现实主义，深蓝，画面通过细腻的丰富的色彩层次塑造主体与场景，质感真实，暗黑风背景的光影效果营造出氛围，整体兼具艺术幻想感，夸张的广角透视效果，耀光，反射，极致的光影，强引力，吞噬")
                .size("2K")
                .sequentialImageGeneration("disabled")
                .responseFormat(ResponseFormat.Url)
                .stream(false)
                .watermark(true)
                .build();
        ImagesResponse imagesResponse = service.generateImages(generateRequest);
        System.out.println(imagesResponse.getData().get(0).getUrl());

        service.shutdownExecutor();

    }

    /**
     * 图生图
     */
    @Test
    public void testSingleImageGenImage(){
        String apiKey = douBaoConfig.getApiKey();
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        ArkService service = ArkService.builder()
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3") // The base URL for model invocation .
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .apiKey(apiKey)
                .build();

        GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                .model(douBaoConfig.getModelId()) //Replace with Model ID .
                .prompt("生成狗狗趴在草地上的近景画面")
                .image("https://ark-project.tos-cn-beijing.volces.com/doc_image/seedream4_imageToimage.png")
                .size("2K")
                .sequentialImageGeneration("disabled")
                .responseFormat(ResponseFormat.Url)
                .stream(false)
                .watermark(true)
                .build();

        ImagesResponse imagesResponse = service.generateImages(generateRequest);
        System.out.println(imagesResponse.getData().get(0).getUrl());

        service.shutdownExecutor();
    }
    /**
     * 多图输入单图输出
     */
    @Test
    public void testImageGenerations(){
        String apiKey = douBaoConfig.getApiKey();
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        ArkService service = ArkService.builder()
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3") // The base URL for model invocation .
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .apiKey(apiKey)
                .build();

        GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                .model(douBaoConfig.getModelId()) //Replace with Model ID .
                .prompt("将图1的服装换为图2的服装")
                .image(Arrays.asList(
                        "https://ark-project.tos-cn-beijing.volces.com/doc_image/seedream4_imagesToimage_1.png",
                        "https://ark-project.tos-cn-beijing.volces.com/doc_image/seedream4_imagesToimage_2.png"
                ))
                .size("2K")
                .sequentialImageGeneration("disabled")
                .responseFormat(ResponseFormat.Url)
                .stream(false)
                .watermark(true)
                .build();
        ImagesResponse imagesResponse = service.generateImages(generateRequest);
        System.out.println(imagesResponse.getData().get(0).getUrl());

        service.shutdownExecutor();
    }
    /**
     * 文字生成一组图
     */
    @Test
    public void testImageGenImages(){
        String apiKey = douBaoConfig.getApiKey();
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        ArkService service = ArkService.builder()
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3") // The base URL for model invocation .
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .apiKey(apiKey)
                .build();

        GenerateImagesRequest.SequentialImageGenerationOptions sequentialImageGenerationOptions = new GenerateImagesRequest.SequentialImageGenerationOptions();
        sequentialImageGenerationOptions.setMaxImages(4);
        GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                .model(douBaoConfig.getModelId())  //Replace with Model ID .
                .prompt("生成一组共4张连贯插画，核心为同一庭院一角的四季变迁，以统一风格展现四季独特色彩、元素与氛围")
                .responseFormat(ResponseFormat.Url)
                .size("2K")
                .sequentialImageGeneration("auto")
                .sequentialImageGenerationOptions(sequentialImageGenerationOptions)
                .stream(false)
                .watermark(true)
                .build();
        ImagesResponse imagesResponse = service.generateImages(generateRequest);
        // Iterate through all image data
        if (imagesResponse != null && imagesResponse.getData() != null) {
            for (int i = 0; i < imagesResponse.getData().size(); i++) {
                // Retrieve image information
                String url = imagesResponse.getData().get(i).getUrl();
                String size = imagesResponse.getData().get(i).getSize();
                System.out.printf("Image %d:%n", i + 1);
                System.out.printf("  URL: %s%n", url);
                System.out.printf("  Size: %s%n", size);
                System.out.println();
            }
            service.shutdownExecutor();
        }
    }
    /**
     * 单图生成一组图
     */
    @Test
    public void testSingleImageGenImages(){
        String apiKey = douBaoConfig.getApiKey();
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        ArkService service = ArkService.builder()
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3") // The base URL for model invocation .
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .apiKey(apiKey)
                .build();

        GenerateImagesRequest.SequentialImageGenerationOptions sequentialImageGenerationOptions = new GenerateImagesRequest.SequentialImageGenerationOptions();
        sequentialImageGenerationOptions.setMaxImages(5);
        GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                .model(douBaoConfig.getModelId()) //Replace with Model ID .
                .prompt("参考这个LOGO，做一套户外运动品牌视觉设计，品牌名称为GREEN，包括包装袋、帽子、纸盒、手环、挂绳等。绿色视觉主色调，趣味、简约现代风格")
                .image("https://ark-project.tos-cn-beijing.volces.com/doc_image/seedream4_imageToimages.png")
                .responseFormat(ResponseFormat.Url)
                .size("2K")
                .sequentialImageGeneration("auto")
                .sequentialImageGenerationOptions(sequentialImageGenerationOptions)
                .stream(false)
                .watermark(true)
                .build();
        ImagesResponse imagesResponse = service.generateImages(generateRequest);
        // Iterate through all image data
        if (imagesResponse != null && imagesResponse.getData() != null) {
            for (int i = 0; i < imagesResponse.getData().size(); i++) {
                // Retrieve image information
                String url = imagesResponse.getData().get(i).getUrl();
                String size = imagesResponse.getData().get(i).getSize();
                System.out.printf("Image %d:%n", i + 1);
                System.out.printf("  URL: %s%n", url);
                System.out.printf("  Size: %s%n", size);
                System.out.println();
            }


            service.shutdownExecutor();
        }
    }
    /**
     * 多图生成一组图
     */
    @Test
    public void testMulImagesGenImages(){
        String apiKey = douBaoConfig.getApiKey();
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        ArkService service = ArkService.builder()
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3") // The base URL for model invocation .
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .apiKey(apiKey)
                .build();

        GenerateImagesRequest.SequentialImageGenerationOptions sequentialImageGenerationOptions = new GenerateImagesRequest.SequentialImageGenerationOptions();
        sequentialImageGenerationOptions.setMaxImages(3);
        GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                .model(douBaoConfig.getModelId()) //Replace with Model ID .
                .prompt("生成3张女孩和奶牛玩偶在游乐园开心地坐过山车的图片，涵盖早晨、中午、晚上")
                .image(Arrays.asList(
                        "https://ark-project.tos-cn-beijing.volces.com/doc_image/seedream4_imagesToimages_1.png",
                        "https://ark-project.tos-cn-beijing.volces.com/doc_image/seedream4_imagesToimages_2.png"
                ))
                .responseFormat(ResponseFormat.Url)
                .size("2K")
                .sequentialImageGeneration("auto")
                .sequentialImageGenerationOptions(sequentialImageGenerationOptions)
                .stream(false)
                .watermark(true)
                .build();
        ImagesResponse imagesResponse = service.generateImages(generateRequest);

        // Iterate through all image data
        if (imagesResponse != null && imagesResponse.getData() != null) {
            for (int i = 0; i < imagesResponse.getData().size(); i++) {
                // Retrieve image information
                String url = imagesResponse.getData().get(i).getUrl();
                String size = imagesResponse.getData().get(i).getSize();
                System.out.printf("Image %d:%n", i + 1);
                System.out.printf("  URL: %s%n", url);
                System.out.printf("  Size: %s%n", size);
                System.out.println();
            }


            service.shutdownExecutor();
        }

    }
}
