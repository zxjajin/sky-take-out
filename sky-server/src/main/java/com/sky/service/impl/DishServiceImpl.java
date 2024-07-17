package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ajin
 * @create 2024-07-12 22:41
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {


    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 新增菜品和对应口味
     *
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //插入一条菜品
        dishMapper.insert(dish);

        //获取insert语句生成的主键值
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult PageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.PageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断菜品是否为起售状态，起售状态不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断菜品是否被套餐关联，套餐关联不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealCountByDishId(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            //单曲菜品被套餐关联了不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品表中的数据，关联的口味数据也要删除
//        for(Long id : ids){
//            dishMapper.deleteById(id);
//            //删除菜品关联的口味数据
//            dishFlavorMapper.deleteByDishId(id);
//        }
        //根据菜品id集合批量删除菜品数据
        //sql: delete from dish where id in (?,?,?)
        dishMapper.deleteByids(ids);
        //根据菜品id集合批量删除关联的口味数据
        //sql: delete from dish_flavor where dish_id in (?,?,?)
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品和口味
     *
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {
        //先根据id查询菜品表
        Dish dish = dishMapper.getById(id);
        //在根据菜品id查询口味表
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        //最后赋值给DishVO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     */
    @Transactional
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        if (status == StatusConstant.DISABLE) {
            // 如果是停售操作，还需要将包含当前菜品的套餐也停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
            List<Long> setmealIds = setmealDishMapper.getSetmealCountByDishId(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.getBycategoryId(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }


    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> getBycategoryId(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.getBycategoryId(dish);
    }

    /**
     * 修改菜品和对应的口味信息
     *
     * @param dishDTO
     */
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        //根据id修改菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        //方法一
        //删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //根据传过来dishDTo中的flavors集合批量修改菜品口味表
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //如果flavors集合不为空，则删除菜品口味表并重新添加菜品口味表
        if (flavors != null && flavors.size() > 0) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishDTO.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }
        //方法二
//        //根据传过来dishDTo中的flavors集合批量修改菜品口味表
//        List<DishFlavor> flavors = dishDTO.getFlavors();
//        //如果flavors集合不为空，则删除菜品口味表并重新添加菜品口味表
//        if (flavors != null && flavors.size() > 0) {
//            dishFlavorMapper.deleteByDishId(dishDTO.getId());
//            for (DishFlavor flavor : flavors) {
//                flavor.setDishId(dishDTO.getId());
//            }
//            dishFlavorMapper.insertBatch(flavors);
//        } else {
//            //如果flavors集合为空，则根据菜品id查询菜品口味表是否有数据，如果有则删除
//            List<DishFlavor> flavors1 = dishFlavorMapper.getByDishId(dishDTO.getId());
//            if (flavors1 != null && flavors1.size() > 0) {
//                dishFlavorMapper.deleteByDishId(dishDTO.getId());
//            }
//        }
    }
}
