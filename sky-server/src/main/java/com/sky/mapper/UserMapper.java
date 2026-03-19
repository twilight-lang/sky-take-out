package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入用户
     * @param user
     */
    void insert(User user);

    /**
     * 根据用户id查询用户
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 当前时间总用户数
     * @param map
     * @return
     */
    @Select("select count(*) from user where create_time <= #{endTime}")
    int sumTotalByMap(Map<String, Object> map);

    /**
     * 当天新增用户数
     * @param map
     * @return
     */
    @Select("select count(*) from user where create_time >= #{beginTime} and create_time <= #{endTime}")
    int sumNewByMap(Map<String, Object> map);
}
