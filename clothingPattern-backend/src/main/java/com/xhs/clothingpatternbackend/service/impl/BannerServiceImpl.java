package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.model.entity.Banner;
import com.xhs.clothingpatternbackend.service.BannerService;
import com.xhs.clothingpatternbackend.mapper.BannerMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 小辛
 * @description 针对表【banner(轮播图表)】的数据库操作Service实现
 * @createDate 2025-11-28 16:42:01
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public List<Banner> getBannerList() {
        QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                .orderByAsc("sortOrder")
                .orderByDesc("createTime");
        return this.list(queryWrapper);
    }

    @Override
    public boolean addBanner(Banner banner) {
        return this.save(banner);
    }

    @Override
    public boolean updateBanner(Banner banner) {
        return this.updateById(banner);
    }

    @Override
    public boolean deleteBanner(Long id) {
        return this.removeById(id);
    }

    @Override
    public Banner getBannerById(Long id) {
        return this.getById(id);
    }
}