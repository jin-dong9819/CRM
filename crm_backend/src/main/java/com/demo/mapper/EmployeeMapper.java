package com.demo.mapper;

import com.demo.entity.Employee;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    // 增
    @Insert("insert into employee(name, gender, hire_date) values (#{name}, #{gender}, #{hireDate})")
    public void insertEmp(Employee employee);

    // 删
    @Delete("delete from employee where id = #{id} ")
    void deleteEmp(int id);

    // 改
    @Update("update employee set name = #{name} , gender=#{gender} , hire_date=#{hireDate} where id = #{id}")
    void updateEmp(Employee employee);

    // 查    根据传入的ID值，从数据库查询员工对象，将结果储存到Employee对象中
    @Select("select * from employee where id = #{id}")
    Employee selectById(int id);

    // 查询全部
    @Select("select * from employee")
    List<Employee> selectAll();

    //根据员工姓名模糊查询
    @Select("select * from employee where name like CONCAT('%',#{name},'%')")
    //  where name like '%'#{name}'%'
    List<Employee> selectByName(@Param("name") String name);

    List<Employee> selectByCondition(Employee employee);
}
