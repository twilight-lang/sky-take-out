package com.sky.controller.admin;


import com.sky.entity.User;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 统计营业额
     *
     * @return
     */
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        //
        TurnoverReportVO turnoverReportVO = reportService.getTurnoverStatistics(begin, end);

        return Result.success(turnoverReportVO);
    }

    /**
     * 用户统计
     *
     * @return
     */
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                               @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        //
        UserReportVO userReportVO = reportService.getUserStatistics(begin, end);

        return Result.success(userReportVO);
    }

    /**
     * 订单统计
     *
     * @return
     */
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> orderStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        //
        OrderReportVO orderReportVO = reportService.getOrderStatistics(begin, end);

        return Result.success(orderReportVO);
    }

    /**
     * 销量排行top10
     *
     * @return
     */
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        //统计一定时间内的销量排名top10
        SalesTop10ReportVO salesTop10ReportVO = reportService.getTop10(begin, end);

        return Result.success(salesTop10ReportVO);
    }

    /**
     *导出运营数据报表
     * @param response
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response){
        reportService.exportBusinessData(response);
    }
}