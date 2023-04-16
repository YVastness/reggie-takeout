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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 套餐管理
 *
 * @author Vastness
 */
@Slf4j
@RestController
@RequestMapping("setmeal")
public class SetMealController {
    private final SetMealService setMealService;
    private final CategoryService categoryService;
    private final SetMealDishService setMealDishService;
    private final RedisTemplate<Object, Object> redisTemplate;

    public SetMealController(SetMealService setMealService, SetMealDishService setMealDishService, CategoryService categoryService, RedisTemplate<Object, Object> redisTemplate) {
        this.setMealService = setMealService;
        this.setMealDishService = setMealDishService;
        this.categoryService = categoryService;
        this.redisTemplate = redisTemplate;
    }


    /**
     * 添加套餐
     *
     * @param setmealDto 组合套餐和菜品信息的映射类
     * @return 添加菜品是否成功
     */
    @PostMapping
    @CacheEvict(value = "setMealCache", key = "#setmealDto.categoryId")
    public Result<String> save(@RequestBody SetmealDto setmealDto) {
        boolean isSave = setMealService.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealDto.getId()));
        boolean isSaveDish = setMealDishService.saveBatch(setmealDishes);
        if (isSave && isSaveDish) {
            // 清除套餐缓存
            String key = "setMealCategory_" + setmealDto.getCategoryId();
            redisTemplate.delete(key);
            return Result.success("");
        }
        return Result.error("保存套餐错误");
    }

    /**
     * 分页查询
     *
     * @param page     页码
     * @param pageSize 页的大小
     * @param name     按名查询
     * @return 页信息
     */
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


    /**
     * 通过套餐id获取套餐信息
     *
     * @param id 套餐id
     * @return 套餐信息
     */
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

    /**
     * 更新套餐信息
     *
     * @param setmealDto 组合套餐和菜品的映射类
     * @return 更新套餐售卖状态是否成功
     */
    @PutMapping
    @CacheEvict(value = "setMealCache", key = "#setmealDto.categoryId")
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
            // 清除套餐缓存
            String key = "setMealCategory_" + setmealDto.getCategoryId();
            redisTemplate.delete(key);
            return Result.success("");
        }
        return Result.error("保存套餐错误");
    }

    /**
     * 更新套餐售卖状态
     *
     * @param status 售卖状态
     * @param ids    需要更新售卖状态的套餐
     * @return 更新套餐售卖状态是否成功
     */

    @CacheEvict(value = "setMealCache", allEntries = true)
    @PostMapping("status/{status}")
    public Result<String> updateStatus(@PathVariable Integer status, Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Setmeal::getStatus, status);
            updateWrapper.eq(Setmeal::getId, ids[i]);
            setMealService.update(updateWrapper);
            if (i == ids.length - 1) {
                // 清除套餐缓存
                Set<Object> keys = redisTemplate.keys("setMealCategory_*");
                if (keys != null) {
                    redisTemplate.delete(keys);
                }
                return Result.success("");
            }
        }
        return Result.error("转换套餐状态失败");
    }

    /**
     * 删除套餐
     *
     * @param ids 被删除套餐们的id
     * @return 套餐们是否被删除
     */
    @CacheEvict(value = "setMealCache", allEntries = true)
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


    /**
     * 显示一个套餐分类里的套餐信息
     *
     * @param setmeal 通过分类id获取套餐信息
     * @return 返回套餐信息
     */
    @Cacheable(value = "setMealCache", key = "#setmeal.categoryId")
    @GetMapping("list")
    public Result<List<Setmeal>> list(Setmeal setmeal) {
        // 不存在需要先查询数据库，将查询的数据缓存到redis中
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getIsDeleted, 0);
        queryWrapper.eq(Setmeal::getStatus, setmeal.getStatus());
        List<Setmeal> setmealList = setMealService.list(queryWrapper);
        return Result.success(setmealList);
    }
}
