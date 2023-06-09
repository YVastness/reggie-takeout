package com.yinhaoyu.dto;

import com.yinhaoyu.entity.Setmeal;
import com.yinhaoyu.entity.SetmealDish;
import lombok.Data;

import java.util.List;

/**
 * @author Vastness
 */
@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
