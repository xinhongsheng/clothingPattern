package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.mapper.UserBehaviorMapper;
import com.xhs.clothingpatternbackend.model.entity.UserBehavior;
import com.xhs.clothingpatternbackend.service.UserBehaviorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户行为 Service 实现
 * @author xhs
 */
@Slf4j
@Service
public class UserBehaviorServiceImpl extends ServiceImpl<UserBehaviorMapper, UserBehavior>
        implements UserBehaviorService {

    /**
     * 行为类型常量
     */
    public static final String ACTION_VIEW = "VIEW";
    public static final String ACTION_LIKE = "LIKE";
    public static final String ACTION_DOWNLOAD = "DOWNLOAD";

    @Override
    public boolean recordBehavior(Long userId, Long patternId, String actionType) {
        if (userId == null || patternId == null || actionType == null) {
            return false;
        }

        // VIEW 行为可以重复记录（每次浏览都记录）
        // LIKE 和 DOWNLOAD 行为避免重复记录
        if (!ACTION_VIEW.equals(actionType)) {
            int count = baseMapper.countByUserAndPatternAndAction(userId, patternId, actionType);
            if (count > 0) {
                log.debug("用户 {} 对图案 {} 的 {} 行为已存在，跳过记录", userId, patternId, actionType);
                return true;
            }
        }

        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setPatternId(patternId);
        behavior.setActionType(actionType);
        behavior.setWeight(getWeight(actionType));
        behavior.setCreateTime(LocalDateTime.now());

        boolean result = this.save(behavior);
        if (result) {
            log.debug("记录用户行为成功: userId={}, patternId={}, actionType={}", userId, patternId, actionType);
        }
        return result;
    }

    @Override
    public int getWeight(String actionType) {
        if (actionType == null) {
            return 1;
        }
        return switch (actionType.toUpperCase()) {
            case ACTION_LIKE -> 5;
            case ACTION_DOWNLOAD -> 3;
            case ACTION_VIEW -> 1;
            default -> 1;
        };
    }
}
