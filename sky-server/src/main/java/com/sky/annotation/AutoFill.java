package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解,用于标识某个方法需要进行功能字段自动填充处理
 * @Retention(RetentionPolicy.RUNTIME)
 *  注解不仅被保留到 class 文件中，JVM 加载 class 文件后，注解仍然存在。这使得我们可以在运行时通过反射机制读取注解信息。
 *  @Target(ElementType.METHOD) ,表示该注解只能用于方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //数据库操作类型
    OperationType value();
}
