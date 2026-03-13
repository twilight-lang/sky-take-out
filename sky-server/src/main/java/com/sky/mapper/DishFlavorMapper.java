package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 新增菜品对应的口味
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品id删除菜品对应的口味
     * @param dishid
     */
    @Delete("delete from dish_flavor where dish_id = #{dishid}")
    void deleteByDishId(Long dishid);

    /*
     * 批量删除菜品对应的口味
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);

    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
