package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
public class SetmealController {

    @Autowired
    SetmealService setmealService;

    /**
     * 新增套餐
     */
    @PostMapping()
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result create(@RequestBody SetmealDTO setmealDTO) {
        setmealService.saveWithDish(setmealDTO);

        return Result.success();
    }

    /**
     * 分页查询套餐
     */
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        //setmealPageQueryDTO这里参数中不加任何注解，是因为前端发送的请求参数中，参数名和这里的参数名是一致的
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping()
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam("ids") List<Long> ids) {
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据id查询套餐，用于修改页面回显数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
     @PutMapping()
     @CacheEvict(cacheNames = "setmealCache",allEntries = true)
     public Result update(@RequestBody SetmealDTO setmealDTO) {
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐起售/停售
     */
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result status(@PathVariable Integer status, Long id) {
        setmealService.startOrStop(status,id);
        return Result.success();
    }
}
