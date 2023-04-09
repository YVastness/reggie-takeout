package com.yinhaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinhaoyu.entity.Dish;
import com.yinhaoyu.mapper.DishMapper;
import com.yinhaoyu.service.DishService;
import org.springframework.stereotype.Service;

/**
 * @author Vastness
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService{
}
