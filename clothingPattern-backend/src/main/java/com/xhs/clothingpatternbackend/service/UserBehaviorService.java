package com.xhs.clothingpatternbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhs.clothingpatternbackend.model.entity.UserBehavior;

/**
 * 用户行为 Service
 * @author xhs
 */
public interface UserBehaviorService extends IService<UserBehavior> {

    /**
     * 记录用户行为
     * @param userId 用户ID
     * @param patternId 图案ID
     * @param actionType 行为类型 (VIEW, LIKE, DOWNLOAD)
     * @return 是否记录成功
     */
    boolean recordBehavior(Long userId, Long patternId, String actionType);

    /**
     * 获取行为权重
     * @param actionType 行为类型
     * @return 权重值
     */
    int getWeight(String actionType);
}
