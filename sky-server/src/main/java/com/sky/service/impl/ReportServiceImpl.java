package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Employee;
import com.sky.entity.Orders;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author ajin
 * @create 2024-07-23 21:14
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //获取begin到end的日期列表
        ArrayList<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        ArrayList<Double> turnoverList = new ArrayList<>();
        //获取日期对应的营业额列表
        for(LocalDate date : dateList){
            //日期转化
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // 状态为付款成功的获取日期对应的营业额 select sum(amount) from orders where order_time between beginTime and endTime and status = 5
            HashMap<String, Object> map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.getMountByMap(map);
            turnover = (turnover == null? 0.0:turnover);
            turnoverList.add(turnover);
        }
        //封装到VO
        String date = StringUtils.join(dateList, ",");//将dateList转换为字符串
        String turnover = StringUtils.join(turnoverList, ",");
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(date)
                .turnoverList(turnover)
                .build();
        return turnoverReportVO;
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //获取begin到end的日期列表
        ArrayList<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //获取日期对应新增用户的数量
        //获取日期对应总用户量的数量
        ArrayList<Integer> newUserList = new ArrayList<>();
        ArrayList<Integer> totalUserList = new ArrayList<>();
        for(LocalDate date : dateList){
            //日期转化
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //获取日期对应的新增用户数量 select count(id) from employee where create_time >= beginTime and create_time <= endTime and status = 1
            HashMap<String, Object> map = new HashMap<>();
            map.put("endTime", endTime);
            //获取日期对应总用户量的数量 select count(id) from employee where create_time<=endTime and status = 1
            Integer allUserCount = userMapper.countByMap(map);
            map.put("beginTime", beginTime);
            Integer newUsers = userMapper.countByMap(map);
            newUserList.add(newUsers);
            totalUserList.add(allUserCount);
        }

        //封装到VO返回
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }
}
