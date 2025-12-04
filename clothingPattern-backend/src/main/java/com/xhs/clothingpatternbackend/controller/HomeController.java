package com.xhs.clothingpatternbackend.controller;

import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.model.dto.pattern.DataExportRequest;
import com.xhs.clothingpatternbackend.model.vo.HomeStatisticsVO;
import com.xhs.clothingpatternbackend.service.ArticleService;
import com.xhs.clothingpatternbackend.service.PatternService;
import com.xhs.clothingpatternbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-25
 * @Description:首页控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/home")
public class HomeController {
    @Resource
    private PatternService patternService;
    @Resource
    private UserService userService;
    @Resource
    private ArticleService articleService;
    /**
     * 获取首页统计数据
     */
    @GetMapping("/statistics")
    public BaseResponse<HomeStatisticsVO> getHomeStatistics() {
        HomeStatisticsVO statistics = patternService.getHomeStatistics();
        return ResultUtils.success(statistics);
    }
    /**
     * 导出数据报告
     */
    @PostMapping("/data/export")
    public void exportDataReport(@RequestBody DataExportRequest exportRequest,
                                 HttpServletResponse response) throws IOException {
        // 根据导出格式设置响应头
        String format = exportRequest.getFormat();
        if ("excel".equals(format)) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=data_report.xlsx");
        } else if ("pdf".equals(format)) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=data_report.pdf");
        } else if ("csv".equals(format)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=data_report.csv");
        }

        // 调用服务层生成报告并写入响应
        patternService.exportDataReport(exportRequest, response.getOutputStream());
    }
    /**
     * 获取用户数量
     */
    @GetMapping("/user/count")
    public BaseResponse<Long> getUserCount() {
        long count = userService.count();
        return ResultUtils.success(count);
    }
    /**
     * 获取图案数量
     */
    @GetMapping("/pattern/count")
    public BaseResponse<Long> getPatternCount() {
        long count = patternService.count();
        return ResultUtils.success(count);
    }
    /**
     * 获取用户增长数据
     */
    @GetMapping("/user/growth")
    public BaseResponse<List<Map<String, Object>>> getUserGrowth() {
        List<Map<String, Object>> growth = userService.getUserGrowth();
        return ResultUtils.success(growth);
    }
    /**
     * 获取目标人群Top5
     */
    @GetMapping("/target/topFive")
    public BaseResponse<List<Map<String, Object>>> getTargetAudienceTopFive() {
        List<Map<String, Object>> topCount = patternService.getTargetAudienceTopFive();
        return ResultUtils.success(topCount);
    }

    /**
     * 获取过去一周每天的文章第一（点赞数、收藏数、浏览量）
     */
    @GetMapping("/article/topOne")
    public BaseResponse<List<Map<String, Object>>> getArticleTopOne() {
        List<Map<String, Object>> topCount = articleService.getArticleTopOne();
        return ResultUtils.success(topCount);
    }
    /**
     * 获取热门风格
     */
    @GetMapping("/style/hot")
    public BaseResponse<List<Map<String, Object>>> getHotStyleTopFive() {
        List<Map<String, Object>> hotStyle = patternService.getHotStyleTopFive();
        return ResultUtils.success(hotStyle);
    }
    /**
     * 获取作品互动
     * 根据点赞量+评论数去评估
     */
    @GetMapping("/interaction")
    public BaseResponse<List<Map<String, Object>>> getInteraction() {
        List<Map<String, Object>> interaction = patternService.getInteraction();
        return ResultUtils.success(interaction);
    }
    /**
     * 获取不同目标群体对不同风格的喜好，前五名
     */
    @GetMapping("/style/preference")
    public BaseResponse<List<Map<String, Object>>> getStylePreference() {
        List<Map<String, Object>> preference = patternService.getStylePreference();
        return ResultUtils.success(preference);
    }
}
