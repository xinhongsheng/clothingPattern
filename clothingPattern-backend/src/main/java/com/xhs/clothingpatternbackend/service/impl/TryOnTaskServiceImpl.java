package com.xhs.clothingpatternbackend.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.mapper.TryOnTaskMapper;
import com.xhs.clothingpatternbackend.model.entity.TryOnTask;
import com.xhs.clothingpatternbackend.sdk.dashscope.DashScopeApiAiTryOnClient;
import com.xhs.clothingpatternbackend.service.TryOnTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

/**
* @author 19099
* @description 针对表【try_on_task】的数据库操作Service实现
* @createDate 2025-12-01 17:22:45
*/
@Service
public class TryOnTaskServiceImpl extends ServiceImpl<TryOnTaskMapper, TryOnTask>
    implements TryOnTaskService {

    @Autowired
    private DashScopeApiAiTryOnClient dashScopeApiClient;

    // 提交试衣任务并保存到数据库
    public String submitTask(Long userId, String personUrl, String topUrl) throws IOException {
        String taskId = dashScopeApiClient.submitTryOn(personUrl, topUrl);
        TryOnTask task = new TryOnTask();
        task.setUserId(userId);
        task.setPersonImageUrl(personUrl);
        task.setTopGarmentUrl(topUrl);
        task.setDashscopeTaskId(taskId);
        task.setTaskStatus("PENDING");
        task.setCreateTime(new Date());
        this.save(task);
        return taskId;
    }

    // 查询并更新任务状态
    public TryOnTask queryTaskStatus(String taskId) throws IOException {
        JSONObject taskInfo = dashScopeApiClient.queryTask(taskId);
        String status = taskInfo.getString("status");
        String resultUrl = "SUCCEEDED".equals(status) ? taskInfo.getJSONObject("output").getString("result_image_url") : null;

        TryOnTask task = this.lambdaQuery().eq(TryOnTask::getDashscopeTaskId, taskId).one();
        if (task != null) {
            task.setTaskStatus(status);
            task.setResultImageUrl(resultUrl);
            this.updateById(task);
        }
        return task;
    }
    

}




