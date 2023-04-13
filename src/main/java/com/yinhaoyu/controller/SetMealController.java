package com.yinhaoyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yinhaoyu.common.Result;
import com.yinhaoyu.dto.SetmealDto;
import com.yinhaoyu.entity.Category;
import com.yinhaoyu.entity.Setmeal;
import com.yinhaoyu.entity.SetmealDish;
import com.yinhaoyu.service.CategoryService;
import com.yinhaoyu.service.SetMealDishService;
import com.yinhaoyu.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Vastness
 */
@Slf4j
@RestController
@RequestMapping("setmeal")
public class SetMealController {
    private final SetMealService setMealService;
    private final CategoryService categoryService;
    private final SetMealDishService setMealDishService;

    public SetMealController(SetMealService setMealService, SetMealDishService setMealDishService, CategoryService categoryService) {
        this.setMealService = setMealService;
        this.setMealDishService = setMealDishService;
        this.categoryService = categoryService;
    }

    @PostMapping
    public Result<String> save(@RequestBody SetmealDto setmealDto) {
        boolean isSave = setMealService.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealDto.getId()));
        boolean isSaveDish = setMealDishService.saveBatch(setmealDishes);
        if (isSave && isSaveDish) {
            return Result.success("");
        }
        return Result.error("保存套餐错误");
    }

    @GetMapping("page")
    public Result<Page<SetmealDto>> pagination(Integer page, Integer pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> pageInfoDto = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        queryWrapper.ne(Setmeal::getIsDeleted, 1);
        setMealService.page(pageInfo, queryWrapper);
        BeanUtils.copyProperties(pageInfo, pageInfoDto);
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> setmealDtoRecords = records.stream().map(setmeal -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, setmealDto);
            Category category = categoryService.getById(setmeal.getCategoryId());
            setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).collect(Collectors.toList());
        pageInfoDto.setRecords(setmealDtoRecords);
        return Result.success(pageInfoDto);
    }

    @GetMapping("{id}")
    public Result<SetmealDto> getById(@PathVariable Long id) {
        Setmeal setmeal = setMealService.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setMealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishes);
        return Result.success(setmealDto);
    }

    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<String> update(@RequestBody SetmealDto setmealDto) {
        // 更新 setmeal 数据里的信息
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Setmeal::getId, setmealDto.getId());
        boolean isSave = setMealService.update(setmealDto, updateWrapper);
        // 更新 setmeal 里 dish 的信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        // 向 setmealDish 里添加 setmealId
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealDto.getId()));
        // 如果套餐的菜品被删除，从数据库中删除
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        List<SetmealDish> setmealDishList = setMealDishService.list(queryWrapper);
        for (SetmealDish setmealDish : setmealDishList) {
            int sum = 0;
            for (SetmealDish dish : setmealDishes) {
                if (dish.getDishId().longValue() == setmealDish.getDishId().longValue()) {
                    // 下面saveOrUpdate需要使用id值判断记录是否存在
                    setmealDish.setDishId(dish.getDishId());
                    break;
                }
                sum++;
            }
            if (sum == setmealDishes.size()) {
                queryWrapper.eq(SetmealDish::getDishId, setmealDish.getDishId());
                setMealDishService.remove(queryWrapper);
            }
        }
        // 添加或者修改套餐里的菜品
        LambdaUpdateWrapper<SetmealDish> setmealDishUpdateWrapper = new LambdaUpdateWrapper<>();
        setmealDishUpdateWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishes.forEach(setmealDish -> {
            setmealDishUpdateWrapper.eq(SetmealDish::getDishId, setmealDish.getDishId());
            setMealDishService.saveOrUpdate(setmealDish, setmealDishUpdateWrapper);
        });
        if (isSave) {
            return Result.success("");
        }
        return Result.error("保存套餐错误");
    }

    @PostMapping("status/{status}")
    public Result<String> updateStatus(@PathVariable Integer status, Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Setmeal::getStatus, status);
            updateWrapper.eq(Setmeal::getId, ids[i]);
            setMealService.update(updateWrapper);
            if (i == ids.length - 1) {
                return Result.success("");
            }
        }
        return Result.error("转换套餐状态失败");
    }

    @DeleteMapping
    public Result<String> delete(Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Setmeal::getIsDeleted, 1);
            updateWrapper.eq(Setmeal::getId, ids[i]);
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getDishId, ids[i]);
            List<SetmealDish> setmealDishList = setMealDishService.list(queryWrapper);
            int finalI = i;
            setmealDishList.stream().filter(setmealDish -> Objects.equals(setmealDish.getDishId(), ids[finalI])).forEach(setmealDish -> {
                LambdaUpdateWrapper<SetmealDish> setmealDishUpdateWrapper = new LambdaUpdateWrapper<>();
                setmealDishUpdateWrapper.set(SetmealDish::getIsDeleted, 1);
                setmealDishUpdateWrapper.eq(SetmealDish::getDishId, ids[finalI]);
                setMealDishService.update(setmealDishUpdateWrapper);
            });
            setMealService.update(updateWrapper);
            if (i == ids.length - 1) {
                return Result.success("");
            }
        }
        return Result.error("菜品删除失败");
    }
}
