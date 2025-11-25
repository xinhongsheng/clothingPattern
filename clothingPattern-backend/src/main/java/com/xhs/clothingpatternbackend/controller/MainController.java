package com.xhs.clothingpatternbackend.controller;


import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.model.dto.pattern.DataExportRequest;
import com.xhs.clothingpatternbackend.model.vo.HomeStatisticsVO;
import com.xhs.clothingpatternbackend.service.PatternService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/")
public class MainController {

    @Resource
    private PatternService patternService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }




}
