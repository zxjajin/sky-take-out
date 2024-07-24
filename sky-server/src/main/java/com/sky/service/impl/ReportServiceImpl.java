package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //获取begin到end的日期列表
        ArrayList<LocalDate> dateList = getDateList(begin, end);
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
        ArrayList<LocalDate> dateList = getDateList(begin, end);
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

    /**
     * 统计指定日期范围内的订单数据。
     * 
     * @param begin 起始日期。
     * @param end 结束日期。
     * @return 返回订单统计报告。
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 生成日期列表，包括起始和结束日期之间的所有日期
        //获取begin到end的日期列表
        ArrayList<LocalDate> dateList = getDateList(begin, end);
        
        // 初始化订单总数和有效订单数列表
        //获取对应日期订单总数列表  select count(id) from orders where order_time >= beginTime and order_time <= endTime
        //获取对应日期有效订单数列表 select count(id) from orders where order_time >= beginTime and order_time <= endTime and status = 5
        ArrayList<Integer> totalOrderList = new ArrayList<>();
        ArrayList<Integer> validOrderList = new ArrayList<>();
        
        // 初始化订单总数和有效订单数的累计值
        Integer totalOrderSum = 0;
        Integer validOrderSum = 0;
        
        // 遍历日期列表，统计每天的订单数和有效订单数
        for(LocalDate date : dateList){
            // 构造每天的开始和结束时间
            //日期转化
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            
            // 查询每天的订单总数
            HashMap<String, Object> map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            Integer totalOrderCount = orderMapper.countByMap(map);
            
            // 查询每天的有效订单数
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            
            // 累加每天的订单数和有效订单数
            totalOrderList.add(totalOrderCount);
            validOrderList.add(validOrderCount);
            //获取订单总数
            totalOrderSum += totalOrderCount;
            //获取有效订单数
            validOrderSum += validOrderCount;
        }

//        Integer totalOrderCount = totalOrderList.stream().reduce(Integer::sum).get();
//        Integer validOrderCount = validOrderList.stream().reduce(Integer::sum).get();

        // 计算订单完成率
        //获取订单完成率
        double orderCompletionRate = 0.0;
        if (totalOrderSum != 0) {
            // 有效的订单数除以总订单数，得到完成率
            orderCompletionRate = validOrderSum.doubleValue() / totalOrderSum;
            // 转换为百分比
//            orderCompletionRate *= 100;
        }
        
        // 封装统计结果到OrderReportVO对象中返回
        //封装数据到vo
        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(totalOrderList, ","))
                .validOrderCountList(StringUtils.join(validOrderList, ","))
                .totalOrderCount(totalOrderSum)
                .validOrderCount(validOrderSum)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量排名top10
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");
        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据--查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/aa.xlsx");

        //基于模板文件创建一个新的Excel文件
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            //获取第一个Sheet页
            XSSFSheet sheet = excel.getSheetAt(0);
            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间" + dateBegin + "至" + dateEnd);
            //获取第四行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            //获得第五行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());
            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                //获得某一行
                row = sheet.getRow(7+i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 获取指定开始日期和结束日期之间的所有日期列表，包括开始和结束日期。
     *
     * @param begin 开始日期，包含在结果列表中。
     * @param end 结束日期，包含在结果列表中。
     * @return 一个ArrayList，包含从开始日期到结束日期的所有日期。
     */
    private ArrayList<LocalDate> getDateList(LocalDate begin, LocalDate end){
        // 初始化一个空的日期列表
        ArrayList<LocalDate> dateList = new ArrayList<>();
        // 将开始日期添加到列表中
        dateList.add(begin);
        // 当开始日期不等于结束日期时，循环继续
        while(!begin.equals(end)){
            // 将开始日期加一天，然后添加到列表中
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 返回包含所有日期的列表
        return dateList;
    }

}
