package com.sky.service;

import com.sky.dto.DishDTO;

/**
 * @author ajin
 * @create 2024-07-12 22:40
 */
public interface DishService {
    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);
}
