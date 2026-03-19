package com.sky.controller.user;


import com.sky.dto.OrdersPaymentDTO;
import com.sky.entity.OrderDetail;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.UserService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {

        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);

        return Result.success(orderPaymentVO);
    }

    /**
     * 用户查看历史订单
     * @param page
     * @param pageSize
     * @param status   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     */
     @GetMapping("/historyOrders")
     @ApiOperation("用户查看历史订单")
     public Result<PageResult> page(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) Integer status) {
         PageResult pageResult = orderService.pageQuery4User(page, pageSize, status);
         return Result.success(pageResult);
     }
    /**
     * 查询订单详情
     * @param id
     * @return
     */
     @GetMapping("/orderDetail/{id}")
     @ApiOperation("查询订单详情")
     public Result<OrderVO> getDetail(@PathVariable Long id) {
         OrderVO orderVO = orderService.detail(id);
         return Result.success(orderVO);
     }

    /**
     * 用户取消订单
     * @param id
     * @return
     */
     @PutMapping("/cancel/{id}")
     @ApiOperation("用户取消订单")
     public Result cancel(@PathVariable Long id) throws Exception {
         orderService.userCancelById(id);
         return Result.success();
     }

    /**
     * 再来一单
     */
     @PostMapping("/repetition/{id}")
     @ApiOperation("再来一单")
     public Result reorder(@PathVariable Long id) {
         orderService.repetition(id);
         return Result.success();
     }

    /**
     * 催单
     * @param id
     * @return
     */
    @GetMapping("/prompt/{id}")
    @ApiOperation("催单")
    public Result reminder(@PathVariable Long id) {
        orderService.reminder(id);
        return Result.success();
    }
}
