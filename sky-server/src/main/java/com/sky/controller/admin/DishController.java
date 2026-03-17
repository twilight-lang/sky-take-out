package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */

@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        //新增菜品和对应的口味
        dishService.saveWithFlavor(dishDTO);
        //清理缓存数据
        cleanCache("dish_" + dishDTO.getCategoryId());
        return Result.success();
    }

    /**
     * 菜品分页查询
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根批量删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result delete(@RequestParam("ids") List<Long> ids) {
        //删除菜品
        dishService.deleteBatch(ids);
        //清理缓存数据
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据id查询菜品和对应的口味
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 更新菜品和对应的口味
     *
     * @param dishDTO
     */
    @PutMapping
    @Transactional
    public Result updateWithFlavor(@RequestBody DishDTO dishDTO) {
        //更新菜品和对应的口味
        dishService.updateWithFlavor(dishDTO);
        //清理缓存数据
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<Dish>> getAll(Long categoryId) {
        List<Dish> dishList = dishService.list(categoryId);
        return Result.success(dishList);
    }

    /**
     * 清理缓存数据
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys =  stringRedisTemplate.keys(pattern);
        stringRedisTemplate.delete(keys);
    }
}
