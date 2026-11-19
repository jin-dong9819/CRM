package com.demo.mapper;

import com.demo.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("select * from sys_user where phone = #{phone}")
    User selectByPhone(String phone);

    @Select("select * from sys_user where id = #{id}")
    User selectById(Long id);

    @Insert("insert into sys_user(phone,password_hash) values(#{phone},#{passwordHash})")
    int insertUser(User user);
}
