package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.model.entity.UserLike;
import com.xhs.clothingpatternbackend.service.LikeService;
import com.xhs.clothingpatternbackend.mapper.LikeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author 小辛
* @description 针对表【like(图案点赞表)】的数据库操作Service实现
* @createDate 2025-11-25 13:04:21
*/
@Service
@Slf4j
public class LikeServiceImpl extends ServiceImpl<LikeMapper, UserLike>
    implements LikeService{

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long patternId, Long userId) {
        if (patternId == null || patternId <= 0 || userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 查询记录（必须包括已删除的，避免唯一索引冲突）
        // 使用原生 SQL 查询，绕过 MyBatis-Plus 的逻辑删除过滤
        LambdaQueryWrapper<UserLike> lambdaQuery = new LambdaQueryWrapper<>();
        lambdaQuery.eq(UserLike::getUserId, userId)
                   .eq(UserLike::getPatternId, patternId)
                   .last("LIMIT 1"); // 确保只返回一条记录
        UserLike existingUserLike = this.getOne(lambdaQuery, false); // false 表示不抛出异常

        if (existingUserLike != null) {
            // 记录当前状态
            Integer oldStatus = existingUserLike.getIsDelete();
            // 切换 isDelete 状态：1变0，0变1
            Integer newStatus = oldStatus == 1 ? 0 : 1;
            
            // 使用 UpdateWrapper 明确更新 isDelete 字段
            UpdateWrapper<UserLike> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", existingUserLike.getId())
                        .set("isDelete", newStatus);
            boolean result = this.update(updateWrapper);
            
            log.info("点赞状态切换: patternId={}, userId={}, oldStatus={}, newStatus={}, result={}", 
                    patternId, userId, oldStatus, newStatus, result);

            // 返回当前点赞状态：isDelete=0 表示已点赞，isDelete=1 表示已取消
            return newStatus == 0;
        } else {
            // 新记录
            UserLike userLike = new UserLike();
            userLike.setUserId(userId);
            userLike.setPatternId(patternId);
            userLike.setIsDelete(0); // 0-未删除（点赞状态）
            boolean result = this.save(userLike);
            
            log.info("新增点赞记录: patternId={}, userId={}, result={}", patternId, userId, result);
            
            return true; // 新点赞返回 true
        }
    }

    @Override
    public long getLikeCount(Long patternId) {
        if (patternId == null || patternId <= 0) {
            return 0;
        }

        QueryWrapper<UserLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patternId", patternId)
                .eq("isDelete", 0); // 只统计有效的点赞
        long count = this.count(queryWrapper);
        
        log.debug("获取点赞数: patternId={}, count={}", patternId, count);
        
        return count;
    }

    @Override
    public boolean isLiked(Long patternId, Long userId) {
        if (patternId == null || patternId <= 0 || userId == null || userId <= 0) {
            return false;
        }

        QueryWrapper<UserLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .eq("patternId", patternId)
                .eq("isDelete", 0); // 只查询有效的点赞
        return this.count(queryWrapper) > 0;
    }
}




