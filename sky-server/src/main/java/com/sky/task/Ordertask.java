package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class Ordertask {

    @Autowired
    private OrderMapper orderMapper;
    /**
     * 定时任务，处理超时订单
     * //每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder(){
        //查询超时订单
        //当前时间减去15分钟
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));

        if(ordersList != null && ordersList.size() > 0){
            for(Orders orders : ordersList){
                //取消订单
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单超时未支付");
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处于派送中状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")//凌晨一点触发
    public void  processDeliveryOrder(){
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusMinutes(60));


        if(ordersList != null && ordersList.size() > 0){
            for(Orders orders : ordersList){
                //取消订单
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单超时未送达");
                orderMapper.update(orders);
            }
        }
    }
}
