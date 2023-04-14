package com.yinhaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinhaoyu.entity.ShoppingCart;
import com.yinhaoyu.mapper.ShoppingCartMapper;
import com.yinhaoyu.service.ShoppingCartService;
import org.springframework.stereotype.Service;

/**
 * @author Vastness
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}