package com.demo.test;

import java.time.LocalDate;
import java.util.List;

import com.demo.entity.Employee;
import com.demo.mapper.EmployeeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class EmployeeTest {

    /**
     * 1. spring框架 帮助我们创建EmployeeMapper类型的对象
     * 2. 将对象赋值给这个属性
     */

    @Autowired  //TMD神奇    springboot 底层 spring 框架
    private EmployeeMapper employeeMapper;

    @Test   // 单元测试
    public void test1() {
        Employee employee = employeeMapper.selectById(1);
        System.out.println(employee.getName());
    }

    @Test
    public void test2() {
        Employee employee = new Employee();
        employee.setName("嘉欣");
        employee.setGender(1);
        employee.setHireDate(LocalDate.now());
        employeeMapper.insertEmp(employee);
    }

    @Test
    public void test3() {
        Employee employee = new Employee();
        employee.setId(1);
        employee.setName("苏琪");
        employee.setGender(1);
        employee.setHireDate(LocalDate.parse("2023-10-02"));
        employeeMapper.updateEmp(employee);
    }

    @Test
    public void test4() {
        List<Employee> list = employeeMapper.selectAll();
        for (Employee employee : list) {
            System.out.println(employee);
        }
    }

    @Test
    public void test5() {
        List<Employee> list = employeeMapper.selectByName("傻娜");
        for (Employee employee : list) {
            System.out.println(employee);
        }
    }

    @Test
    public void test6() {
        Employee employee = new Employee();
        employee.setName("亚鑫");
        //employee.setGender(1);
        List<Employee> list = employeeMapper.selectByCondition(employee);
        for (Employee employee2 : list) {
            System.out.println(employee2);
        }
    }
}
