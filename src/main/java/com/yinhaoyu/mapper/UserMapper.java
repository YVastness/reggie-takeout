package com.yinhaoyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yinhaoyu.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Vastness
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}