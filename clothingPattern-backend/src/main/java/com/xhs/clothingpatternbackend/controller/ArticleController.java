package com.xhs.clothingpatternbackend.controller;

import com.qcloud.cos.model.PutObjectResult;
import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.model.dto.article.ArticleAddRequest;
import com.xhs.clothingpatternbackend.model.dto.article.ArticleQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.Article;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.vo.ArticleVO;
import com.xhs.clothingpatternbackend.model.vo.LikeResult;
import com.xhs.clothingpatternbackend.model.vo.PageResult;
import com.xhs.clothingpatternbackend.service.ArticleService;
import com.xhs.clothingpatternbackend.service.UserService;
import com.xhs.clothingpatternbackend.utils.CosUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 文章接口
 */
@RestController
@RequestMapping("/article")
@Slf4j
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Resource
    private CosUtils cosUtils;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 获取文章列表
     */
    @PostMapping("/list")
    public BaseResponse<PageResult<ArticleVO>> getArticleList(@Valid @RequestBody ArticleQueryRequest query,
                                                               HttpServletRequest request) {
        // 获取当前用户ID（未登录用户为null）
        Long currentUserId = null;
        try {
            User loginUser = userService.getLoginUser(request);
            if (loginUser != null) {
                currentUserId = loginUser.getId();
            }
        } catch (Exception e) {
            // 未登录，保持currentUserId为null
        }

        PageResult<ArticleVO> result = articleService.getArticleList(query, currentUserId);
        return ResultUtils.success(result);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public BaseResponse<ArticleVO> getArticleDetail(@PathVariable Long id,
                                                     HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        // 获取当前用户ID（未登录用户为null）
        Long currentUserId = null;
        try {
            User loginUser = userService.getLoginUser(request);
            if (loginUser != null) {
                currentUserId = loginUser.getId();
            }
        } catch (Exception e) {
            // 未登录，保持currentUserId为null
        }

        ArticleVO article = articleService.getArticleDetail(id, currentUserId);
        return ResultUtils.success(article);
    }

    /**
     * 搜索文章
     */
    @GetMapping("/search")
    public BaseResponse<PageResult<ArticleVO>> searchArticles(@RequestParam String keyword,
                                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                                               HttpServletRequest request) {
        // 获取当前用户ID（未登录用户为null）
        Long currentUserId = null;
        try {
            User loginUser = userService.getLoginUser(request);
            if (loginUser != null) {
                currentUserId = loginUser.getId();
            }
        } catch (Exception e) {
            // 未登录，保持currentUserId为null
        }

        ArticleQueryRequest query = new ArticleQueryRequest();
        query.setKeyword(keyword);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);

        PageResult<ArticleVO> result = articleService.getArticleList(query, currentUserId);
        return ResultUtils.success(result);
    }

    /**
     * 获取热门文章
     */
    @GetMapping("/hot")
    public BaseResponse<List<ArticleVO>> getHotArticles(@RequestParam(defaultValue = "10") Integer limit,
                                                         HttpServletRequest request) {
        // 获取当前用户ID（未登录用户为null）
        Long currentUserId = null;
        try {
            User loginUser = userService.getLoginUser(request);
            if (loginUser != null) {
                currentUserId = loginUser.getId();
            }
        } catch (Exception e) {
            // 未登录，保持currentUserId为null
        }

        List<ArticleVO> articles = articleService.getHotArticles(limit, currentUserId);
        return ResultUtils.success(articles);
    }

    /**
     * 获取推荐文章
     */
    @GetMapping("/recommend")
    public BaseResponse<List<ArticleVO>> getRecommendArticles(@RequestParam(defaultValue = "10") Integer limit,
                                                               HttpServletRequest request) {
        // 获取当前用户ID（未登录用户为null）
        Long currentUserId = null;
        try {
            User loginUser = userService.getLoginUser(request);
            if (loginUser != null) {
                currentUserId = loginUser.getId();
            }
        } catch (Exception e) {
            // 未登录，保持currentUserId为null
        }

        List<ArticleVO> articles = articleService.getRecommendArticles(limit, currentUserId);
        return ResultUtils.success(articles);
    }

    /**
     * 添加文章
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addArticle(@Valid @RequestBody ArticleAddRequest request,
                                             HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        boolean result = articleService.addArticle(request, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新文章
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateArticle(@Valid @RequestBody Article article,
                                                HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        boolean result = articleService.updateArticle(article, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 删除文章
     */
    @PostMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteArticle(@PathVariable Long id,
                                                HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        boolean result = articleService.deleteArticle(id, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 发布文章
     */
    @PostMapping("/publish/{id}")
    public BaseResponse<Boolean> publishArticle(@PathVariable Long id,
                                                 HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        boolean result = articleService.publishArticle(id, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 下架文章
     */
    @PostMapping("/offline/{id}")
    public BaseResponse<Boolean> offlineArticle(@PathVariable Long id,
                                                 HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        boolean result = articleService.offlineArticle(id, loginUser.getId());
        return ResultUtils.success(result);
    }
    /**
     * 上架文章
     */
    @PostMapping("/listed/{id}")
    public BaseResponse<Boolean> listedArticle(@PathVariable Long id,
                                                HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        boolean result = articleService.listedArticle(id, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 点赞文章
     */
    @PostMapping("/like")
    public BaseResponse<LikeResult> likeArticle(@RequestParam Long articleId,
                                                 HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(articleId == null || articleId <= 0, ErrorCode.PARAMS_ERROR);

        LikeResult result = articleService.likeArticle(articleId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 收藏文章
     */
    @PostMapping("/collect")
    public BaseResponse<Boolean> collectArticle(@RequestParam Long articleId,
                                                 HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(articleId == null || articleId <= 0, ErrorCode.PARAMS_ERROR);

        boolean result = articleService.collectArticle(articleId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 取消收藏
     */
    @PostMapping("/collect/cancel")
    public BaseResponse<Boolean> cancelCollectArticle(@RequestParam Long articleId,
                                                       HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(articleId == null || articleId <= 0, ErrorCode.PARAMS_ERROR);

        boolean result = articleService.cancelCollectArticle(articleId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 获取点赞状态
     */
    @GetMapping("/like/status")
    public BaseResponse<Boolean> getLikeStatus(@RequestParam Long articleId,
                                                HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(articleId == null || articleId <= 0, ErrorCode.PARAMS_ERROR);

        boolean status = articleService.getLikeStatus(articleId, loginUser.getId());
        return ResultUtils.success(status);
    }

    /**
     * 获取收藏状态
     */
    @GetMapping("/collect/status")
    public BaseResponse<Boolean> getCollectStatus(@RequestParam Long articleId,
                                                   HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(articleId == null || articleId <= 0, ErrorCode.PARAMS_ERROR);

        boolean status = articleService.getCollectStatus(articleId, loginUser.getId());
        return ResultUtils.success(status);
    }

    /**
     * 上传文章封面图片
     */
    @PostMapping("/upload/cover")
    public BaseResponse<String> uploadCoverImage(@RequestParam("file") MultipartFile file,
                                                  HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持JPG和PNG格式的图片");
        }

        // 验证文件大小（2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小不能超过2MB");
        }

        File tempFile = null;
        try {
            // 创建临时文件
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".png";
            tempFile = File.createTempFile("article_cover_", suffix);
            file.transferTo(tempFile);

            // 上传到COS
            String key = "article/cover/" + loginUser.getId() + "/" + System.currentTimeMillis() + suffix;
            cosUtils.putPictureObject(key, tempFile);
            
            // 构建COS URL
            String cosUrl = cosClientConfig.getHost() + "/" + key;
            
            log.info("文章封面上传成功，用户ID: {}, URL: {}", loginUser.getId(), cosUrl);
            return ResultUtils.success(cosUrl);
            
        } catch (IOException e) {
            log.error("上传文章封面失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }
}

