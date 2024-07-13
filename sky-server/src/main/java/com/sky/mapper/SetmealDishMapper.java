package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author ajin
 * @create 2024-07-13 17:15
 */
@Mapper
public interface SetmealDishMapper {

    List<Long> getSetmealCountByDishId(List<Long> ids);
}
