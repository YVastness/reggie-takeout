package com.yinhaoyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yinhaoyu.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * 套餐管理Mapper
 *
 * @author Vastness
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
