package com.yinhaoyu.dto;

import com.yinhaoyu.entity.Dish;
import com.yinhaoyu.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vastness
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
