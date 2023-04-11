package com.yinhaoyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yinhaoyu.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类管理Mapper
 * @author Vastness
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
