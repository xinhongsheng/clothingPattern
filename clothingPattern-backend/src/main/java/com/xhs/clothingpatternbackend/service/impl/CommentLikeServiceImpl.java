package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.model.entity.CommentLike;
import com.xhs.clothingpatternbackend.service.CommentLikeService;
import com.xhs.clothingpatternbackend.mapper.CommentLikeMapper;
import org.springframework.stereotype.Service;

/**
* @author 小辛
* @description 针对表【comment_like(评论点赞表)】的数据库操作Service实现
* @createDate 2025-11-26 15:12:15
*/
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike>
    implements CommentLikeService{

}




