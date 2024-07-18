package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

/**
 * @author ajin
 * @create 2024-07-18 17:47
 */
public interface ShoppingService {
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
