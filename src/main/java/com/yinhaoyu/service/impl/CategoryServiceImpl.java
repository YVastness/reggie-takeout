package com.yinhaoyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinhaoyu.common.CustomException;
import com.yinhaoyu.entity.Category;
import com.yinhaoyu.entity.Dish;
import com.yinhaoyu.entity.Setmeal;
import com.yinhaoyu.mapper.CategoryMapper;
import com.yinhaoyu.service.CategoryService;
import com.yinhaoyu.service.DishService;
import com.yinhaoyu.service.SetMealService;
import org.springframework.stereotype.Service;

/**
 * @author Vastness
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    private final DishService dishService;
    private final SetMealService setMealService;

    public CategoryServiceImpl(DishService dishService, SetMealService setMealService) {
        this.dishService = dishService;
        this.setMealService = setMealService;
    }

    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId, id);

        if (dishService.count(dishQueryWrapper) > 0) {
            throw new CustomException("当前分类已经关联菜品，不能删除");
        }
        LambdaQueryWrapper<Setmeal> setMealQueryWrapper = new LambdaQueryWrapper<>();
        setMealQueryWrapper.eq(Setmeal::getCategoryId, id);
        if (setMealService.count(setMealQueryWrapper) > 0) {
            throw new CustomException("当前分类已经关联套餐，不能删除");
        }
        this.removeById(id);
    }
}
