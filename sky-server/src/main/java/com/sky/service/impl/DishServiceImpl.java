package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Employee;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应的口味
     *
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

        if (flavors != null && flavors.size() > 0) {
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断是否有正在销售中的菜品
        ids.forEach(id -> {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });
        //判断是否被套餐关联
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            //如果有被套餐关联的菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//            //删除菜品对应的口味
//            dishFlavorMapper.deleteByDishId(id);
//        }

        //优化后的删除菜品和口味
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
        //这里将多条sql优化为一条，那么性能会得到很大提升！

    }

    /*
     * 根据id查询菜品和对应的口味
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //先查询菜品信息
        Dish dish = dishMapper.getById(id);
        if (dish == null) {
            // throw new NotFoundException(MessageConstant.DISH_NOT_FOUND);
        }
        //再查询菜品对应的口味
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        //将查询到的口味设置到菜品VO中
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        //更新菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        //删除原有的口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //新增新的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && flavors.size() > 0) {
            //设置dishId
            flavors.forEach(flavor -> flavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}