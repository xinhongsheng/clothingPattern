package com.xhs.clothingpatternbackend.controller;

import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.model.entity.Banner;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.service.BannerService;
import com.xhs.clothingpatternbackend.service.UserService;
import com.xhs.clothingpatternbackend.utils.CosUtils;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/banner")
public class BannerController {
    @Resource
    private BannerService bannerService;

    @Resource
    private UserService userService;

    @Resource
    private CosUtils cosUtils;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 获取轮播图列表
     */
    @GetMapping("/list")
    public BaseResponse<List<Banner>> getBannerList() {
        List<Banner> bannerList = bannerService.getBannerList();
        return ResultUtils.success(bannerList);
    }

    /**
     * 添加轮播图
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addBanner(@RequestBody Banner banner, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        boolean result = bannerService.addBanner(banner);
        return ResultUtils.success(result);
    }

    /**
     * 更新轮播图
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateBanner(@RequestBody Banner banner, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        boolean result = bannerService.updateBanner(banner);
        return ResultUtils.success(result);
    }

    /**
     * 删除轮播图
     */
    @PostMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteBanner(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        boolean result = bannerService.deleteBanner(id);
        return ResultUtils.success(result);
    }

    /**
     * 上传轮播图
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadBanner(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持JPG和PNG格式的图片");
        }

        // 验证文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小不能超过5MB");
        }

        File tempFile = null;
        try {
            // 创建临时文件
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".png";
            tempFile = File.createTempFile("banner_", suffix);
            file.transferTo(tempFile);

            // 上传到COS
            String key = "banner/" + System.currentTimeMillis() + suffix;
            cosUtils.putPictureObject(key, tempFile);

            // 构建COS URL
            String cosUrl = cosClientConfig.getHost() + "/" + key;

            return ResultUtils.success(cosUrl);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    System.out.println("临时文件删除失败: " + tempFile.getAbsolutePath());
                }
            }
        }
    }
}