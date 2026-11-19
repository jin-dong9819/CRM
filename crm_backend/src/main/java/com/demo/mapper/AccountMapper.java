package com.demo.mapper;

import com.demo.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AccountMapper {

    @Select("select id, username, balance from account where id = #{id}")
    Account selectById(int id);

    @Update("update account set balance = #{balance} where id = #{id}")
    int update(Account account);
}
