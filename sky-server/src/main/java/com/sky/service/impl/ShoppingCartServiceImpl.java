package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setMealMapper;

    /**
     * 新增购物车物品
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //先判断在不在购物车中
        //锁定唯一一条数据，应该是用户id+菜品id（不同口味不同），或者用户id+套餐id
        //根据用户id和菜品id查询购物车中是否存在该物品
        ShoppingCart shoppingCart = new ShoppingCart();
        //注入用户id
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //根据用户id和菜品id，套餐id，口味。查询购物车所有物品
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //如果已经存在该物品，则数量+1
        if(list != null && list.size() > 0) {
            //这里只会查出一条数据，因为用户id+菜品id（不同口味不同），或者用户id+套餐id是唯一的
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.update(cart);
        }else {
            //不存在则新增一条购物车数据
            //判断是菜品还是套餐
            if(shoppingCart.getDishId() != null) {
                //是菜品
                //根据菜品id查询菜品信息
                Dish dish = dishMapper.getById(shoppingCart.getDishId());
                //注入名称，金额，图片
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
            }else {
                //是套餐
                //根据套餐id查询套餐信息
                Setmeal setMeal = setMealMapper.selectById(shoppingCart.getSetmealId());
                //注入名称，金额，图片
                shoppingCart.setName(setMeal.getName());
                shoppingCart.setAmount(setMeal.getPrice());
                shoppingCart.setImage(setMeal.getImage());
            }
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            //新增购物车数据
            shoppingCartMapper.insert(shoppingCart);
        }

    }
}
