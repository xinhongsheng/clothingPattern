package com.xhs.clothingpatternbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhs.clothingpatternbackend.model.dto.pattern.DataExportRequest;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternEditRequest;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternGenerateRequest;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.vo.HomeStatisticsVO;
import com.xhs.clothingpatternbackend.model.vo.PatternVO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
* @author 小辛
* @description 针对表【pattern(服装图案表（智能图案生成模块核心表）)】的数据库操作Service
* @createDate 2025-11-21 15:44:19
*/
public interface PatternService extends IService<Pattern> {

    /**
     * 生成图案
     *
     * @param patternGenerateRequest
     * @param loginUser
     * @return
     */
    Long generatePattern(PatternGenerateRequest patternGenerateRequest, User loginUser);

    /**
     * 审核图案
     *
     * @param id
     * @param auditStatus
     * @param rejectReason
     * @param auditorId
     * @return
     */
    boolean auditPattern(Long id, String auditStatus, String rejectReason, Long auditorId);

    /**
     * 校验图案数据
     *
     * @param pattern
     * @param add
     */
    void validPattern(Pattern pattern, boolean add);

    /**
     * 获取查询条件
     *
     * @param patternQueryRequest
     * @return
     */
    QueryWrapper<Pattern> getQueryWrapper(PatternQueryRequest patternQueryRequest);

    /**
     * 获取图案封装
     *
     * @param pattern
     * @param loginUserId 当前登录用户ID，可为null（未登录）
     * @return
     */
    PatternVO getPatternVO(Pattern pattern, Long loginUserId);

    /**
     * 获取图案封装列表
     *
     * @param patternList
     * @param loginUserId 当前登录用户ID，可为null（未登录）
     * @return
     */
    List<PatternVO> getPatternVOList(List<Pattern> patternList, Long loginUserId);
    /**
     * 编辑图片
     * @param patternEditRequest
     * @param loginUser
     */
    void editPicture(PatternEditRequest patternEditRequest, User loginUser);
    /**
     * 检查图片权限
     * @param loginUser
     * @param pattern
     */
    void checkPictureAuth(User loginUser, Pattern pattern);
    /**
     * 填充审核参数
     * @param pattern
     * @param loginUser
     */
    void fillReviewParams(Pattern pattern, User loginUser);

    /**
     * 获取首页统计数据
     * @return
     */
    HomeStatisticsVO getHomeStatistics();

    /**
     * 导出数据报告
     * @param exportRequest
     * @param outputStream
     */
    void exportDataReport(DataExportRequest exportRequest,
                          OutputStream outputStream) throws IOException;
    /**
     * 获取目标群体Top5
     * @return
     */
    List<Map<String, Object>> getTargetAudienceTopFive();

    List<Map<String, Object>> getHotStyleTopFive();

    List<Map<String, Object>> getInteraction();
}
