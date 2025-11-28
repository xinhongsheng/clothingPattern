package com.xhs.clothingpatternbackend.service;

import com.xhs.clothingpatternbackend.model.entity.Banner;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author 小辛
 * @description 针对表【banner(轮播图表)】的数据库操作Service
 * @createDate 2025-11-28 16:42:01
 */
public interface BannerService extends IService<Banner> {

    /**
     * 获取轮播图列表
     * 
     * @return 轮播图列表
     */
    List<Banner> getBannerList();

    /**
     * 添加轮播图
     * 
     * @param banner 轮播图信息
     * @return 是否添加成功
     */
    boolean addBanner(Banner banner);

    /**
     * 更新轮播图
     * 
     * @param banner 轮播图信息
     * @return 是否更新成功
     */
    boolean updateBanner(Banner banner);

    /**
     * 删除轮播图
     * 
     * @param id 轮播图ID
     * @return 是否删除成功
     */
    boolean deleteBanner(Long id);

    /**
     * 根据ID获取轮播图
     * 
     * @param id 轮播图ID
     * @return 轮播图信息
     */
    Banner getBannerById(Long id);
}