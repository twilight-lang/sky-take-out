package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类,用于实现功能字段自动填充
 */
@Aspect
@Component
public class AutoFillAspect {
    //切入点,包下所有类的所有方法+同时满足@AutoFill注解
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void addFillPointcut() {

    }
    //通知，前置通知，在目标方法之前执行
    @Before("addFillPointcut()")
    public void autoFill(JoinPoint joinPoint) throws Throwable {
        //获取当前被拦截的方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operation = autoFill.value();//最终获得了操作类型
        //获取到当前被拦截的方法的参数——实体对象
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];
        //准备赋值的数据
        Long currentId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        //根据当前不同的操作类型，为对应的属性赋值
        if(operation == OperationType.INSERT) {
            //为新增操作赋值
            Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
            setCreateTime.invoke(entity, now);
            Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
            setUpdateTime.invoke(entity, now);
            Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
            setCreateUser.invoke(entity, currentId);
            Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
            setUpdateUser.invoke(entity, currentId);
        } else if(operation == OperationType.UPDATE) {
            //为更新操作赋值
            Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
            setUpdateTime.invoke(entity, now);
            Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
            setUpdateUser.invoke(entity, currentId);
        }

    }
}
