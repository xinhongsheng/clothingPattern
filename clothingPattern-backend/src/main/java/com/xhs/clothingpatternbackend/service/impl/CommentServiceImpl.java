package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.model.entity.Comment;
import com.xhs.clothingpatternbackend.service.CommentService;
import com.xhs.clothingpatternbackend.mapper.CommentMapper;
import org.springframework.stereotype.Service;

/**
* @author 小辛
* @description 针对表【comment(图案评论表)】的数据库操作Service实现
* @createDate 2025-11-25 13:04:21
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

}




