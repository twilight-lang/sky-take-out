package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /*
     * 新增员工
     * @param employeeDTO
     * @return
     */
     void save(EmployeeDTO employeeDTO);

    PageResult page(Integer page, Integer pageSize, String name);

    void startOrStop(Integer status, Long id);
}
