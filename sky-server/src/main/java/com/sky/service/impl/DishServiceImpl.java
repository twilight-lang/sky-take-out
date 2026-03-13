package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 新增菜品一次一条
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        //这里理论上dishId应该是自增的，但是要用到dishId来新增口味，所以这里要先获取dishId
        //获取dishId的方法是在新增菜品时使用useGeneratedKeys="true" keyProperty="id"来设置的！
        long dishId = dish.getId();

        // 新增口味一次n条
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //得把dishId设置到每个flavor中
        flavors.forEach(flavor -> flavor.setDishId(dishId));

        if (flavors!=null && flavors.size()>0) {
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
