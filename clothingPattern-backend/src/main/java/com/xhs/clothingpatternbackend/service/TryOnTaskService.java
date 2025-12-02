package com.xhs.clothingpatternbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhs.clothingpatternbackend.model.entity.TryOnTask;

import java.io.IOException;


/**
* @author 19099
* @description 针对表【try_on_task】的数据库操作Service
* @createDate 2025-12-01 17:22:45
*/
public interface TryOnTaskService extends IService<TryOnTask> {

    // 提交试衣任务并保存到数据库
    String submitTask(Long userId, String personUrl, String topUrl, String bottomUrl) throws IOException;

    // 查询并更新任务状态，成功时自动保存图片到COS
    TryOnTask queryTaskStatus(String taskId) throws Exception;

//    String downloadAndSaveToOss(String imageUrl) throws Exception;
}
