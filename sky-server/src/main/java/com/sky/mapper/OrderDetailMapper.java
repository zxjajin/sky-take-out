package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ajin
 * @create 2024-07-19 19:39
 */
@Mapper
public interface OrderDetailMapper {
    void insertBatch(ArrayList<OrderDetail> orderDetailList);

    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> getByOrderId(Long id);
}
