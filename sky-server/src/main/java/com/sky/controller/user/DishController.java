package com.sky.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.json.JacksonObjectMapper;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private static final ObjectMapper objectMapper = new JacksonObjectMapper();

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) throws JsonProcessingException {

        //这里引入reids
        //查询redis中是否存在
        String redisKey = "dish_" + categoryId;
        String json1 = stringRedisTemplate.opsForValue().get(redisKey);
        //存在就返回，无序查数据库
        if(json1 != null){
            List<DishVO> list = objectMapper.readValue(json1, new TypeReference<List<DishVO>>() {});
            return Result.success(list);
        }
        //不存在就查数据库
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
        List<DishVO> list = dishService.listWithFlavor(dish);

        //将查到的数据放入redis
        //将list转换为json字符串
        String json = objectMapper.writeValueAsString(list);
        //将json字符串放入redis
        stringRedisTemplate.opsForValue().set(redisKey, json, 10, TimeUnit.MINUTES);

        return Result.success(list);
    }

}
