package com.yinhaoyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yinhaoyu.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类管理Mapper
 * @author Vastness
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
