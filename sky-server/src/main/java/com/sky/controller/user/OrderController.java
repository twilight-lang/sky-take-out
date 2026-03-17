package com.sky.controller.user;


import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.UserService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sky.dto.OrdersSubmitDTO;

@RestController("userOrderController")
@RequestMapping("/user/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO orderSubmitDTO) {
        OrderSubmitVO orderSubmitVO = orderService.submit(orderSubmitDTO);
        return Result.success(orderSubmitVO);
    }
}
