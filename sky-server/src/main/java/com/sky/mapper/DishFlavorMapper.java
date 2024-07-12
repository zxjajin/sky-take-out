package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ajin
 * @create 2024-07-12 22:45
 */
@Mapper
public interface DishFlavorMapper {

    void insert(DishFlavor dishFlavor);

    void insertBatch(List<DishFlavor> flavors);
}
