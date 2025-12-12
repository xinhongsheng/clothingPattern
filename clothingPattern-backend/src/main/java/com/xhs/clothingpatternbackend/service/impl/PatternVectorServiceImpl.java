package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhs.clothingpatternbackend.model.entity.PatternVector;
import com.xhs.clothingpatternbackend.service.PatternVectorService;
import com.xhs.clothingpatternbackend.mapper.PatternVectorMapper;
import org.springframework.stereotype.Service;

/**
* @author 小辛
* @description 针对表【pattern_vector(图案AI特征向量表)】的数据库操作Service实现
* @createDate 2025-12-12 16:42:32
*/
@Service
public class PatternVectorServiceImpl extends ServiceImpl<PatternVectorMapper, PatternVector>
    implements PatternVectorService{

}




