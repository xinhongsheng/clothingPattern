package com.xhs.clothingpatternbackend;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-01
 * @Description:
 * @Version: 1.0
 */
@SpringBootTest
public class XianGongyunTest {
    @Test
    public void test() throws Exception {
        Request request = new Request.Builder()
                .url("https://tl1y93s3nxuzh6wb-8188.container.x-gpu.com/")
                .build();
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
    }

}
