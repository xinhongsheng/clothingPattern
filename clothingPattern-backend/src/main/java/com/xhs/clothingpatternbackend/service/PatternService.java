package com.xhs.clothingpatternbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternGenerateRequest;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.vo.PatternVO;

import java.util.List;

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
     * @return
     */
    PatternVO getPatternVO(Pattern pattern);

    /**
     * 获取图案封装列表
     *
     * @param patternList
     * @return
     */
    List<PatternVO> getPatternVOList(List<Pattern> patternList);
}
