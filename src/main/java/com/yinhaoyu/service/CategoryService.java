package com.yinhaoyu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yinhaoyu.entity.Category;

/**
 * @author Vastness
 */
public interface CategoryService extends IService<Category> {
    /**
     * 删除分类
     * @param id 删除分类id
     */
    void remove(Long id);
}
