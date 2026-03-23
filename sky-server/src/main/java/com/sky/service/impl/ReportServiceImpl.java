package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 统计指定时间范围内的营业额
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //先计算时间范围内的所有日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.isAfter(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String dateListStr = StringUtils.join(dateList, ",");

        List<Double> turnoverList = new ArrayList<>();
        //营业额统计，计算时间范围内已经完成的订单金额
        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);

            turnover = turnover == null ? 0.0 : turnover;

            turnoverList.add(turnover);
        }
        String turnoverListStr = StringUtils.join(turnoverList, ",");

        return TurnoverReportVO.builder().dateList(dateListStr).turnoverList(turnoverListStr).build();
    }

    /**
     * 统计指定时间范围内的用户数量
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //先计算时间范围内的所有日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.isAfter(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String dateListStr = StringUtils.join(dateList, ",");

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        //用户统计，分别统计用户总量和新增用户数量
        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            //查找今天的新增用户数量
            int newUser = userMapper.sumNewByMap(map);
            newUserList.add(newUser);
            //查找今天之前的用户总量
            int totalUser = userMapper.sumTotalByMap(map);
            totalUserList.add(totalUser);
        }
        String totalUserListStr = StringUtils.join(totalUserList, ",");
        String newUserListStr = StringUtils.join(newUserList, ",");

        return UserReportVO.builder().dateList(dateListStr).newUserList(newUserListStr).totalUserList(totalUserListStr).build();

    }

    /**
     * 统计指定时间范围内的订单数量
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //先计算时间范围内的所有日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.isAfter(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String dateListStr = StringUtils.join(dateList, ",");

        List<Integer> totalOrderList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();
        int totalOrderCount = 0;
        int validOrderCount = 0;
        double orderCompletionRate = 0.0;

        //订单数量统计，分别统计订单总量和有效订单数
        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            //当天订单总量
            int totalOrder = orderMapper.sumOrderByMap(map);
            totalOrderList.add(totalOrder);
            //当天有效订单数
            int validOrder = orderMapper.sumOrderByMap(map);
            validOrderList.add(validOrder);
        }
        String totalOrderListStr = StringUtils.join(totalOrderList, ",");
        String validOrderListStr = StringUtils.join(validOrderList, ",");
        //计算数量，累加集合中的所有元素
        //reduce合并
        totalOrderCount = totalOrderList.stream().reduce(Integer::sum).get();
        validOrderCount = validOrderList.stream().reduce(Integer::sum).get();


        //订单完成率，感觉这里得精确一些
        if(totalOrderCount != 0){
            orderCompletionRate = (validOrderCount+0.0 )/ totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(dateListStr)
                .orderCountList(totalOrderListStr)
                .validOrderCountList(validOrderListStr)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 统计销量排行top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //这里的查询条件还挺复杂,还得连表查订单明细，通过菜品id聚合得出number
        //select o.name as name , sum(od.number) as number
        //from orders o left join order_details od on o.id = od.order_id
        //where o.create_time between #{beginTime} and #{endTime} and o.status = #{status}
        //group by o.name
        //order by number desc
        //limit #{number}
        List<GoodsSalesDTO> salesTop10 = orderMapper.top10(beginTime, endTime);
        //商品名称列表
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        //销量列表
        String nameList = StringUtils.join(names, ",");
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO.builder().nameList(nameList).numberList(numberList).build();
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //查询数据库获得营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(
                LocalDateTime.of(dateBegin, LocalTime.MIN),
                LocalDateTime.of(dateEnd, LocalTime.MAX));
        //通过POI写入excel
        //在类路径下读取资源，返回一个输入流对象。
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表.xlsx");

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

            XSSFSheet sheet = workbook.getSheet("Sheet1");

            sheet.getRow(1).getCell(1).setCellValue("时间+" + dateBegin + "至" + dateEnd);
            XSSFRow row = sheet.getRow(3);

            //写入数据
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(3).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());
            row.getCell(5).setCellValue(businessDataVO.getNewUsers());

            //填充明细数据
            for(int i=0;i<30;i++){
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天是营业数据
                BusinessDataVO businessDataVO1 = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                //写入数据
                row = sheet.getRow(i+7);
                row.createCell(1).setCellValue(date.toString());
                row.createCell(2).setCellValue(businessDataVO1.getValidOrderCount());
                row.createCell(3).setCellValue(businessDataVO1.getOrderCompletionRate());
                row.createCell(4).setCellValue(businessDataVO1.getUnitPrice());
                row.createCell(5).setCellValue(businessDataVO1.getNewUsers());
                row.createCell(6).setCellValue(businessDataVO1.getTurnover());
            }


            ServletOutputStream  outputStream = response.getOutputStream();
            workbook.write(outputStream);

            outputStream.close();
            workbook.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //释放资源
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
