package com.demo.mapper;

import com.demo.entity.SysEmployee;
import com.demo.vo.EmployeeRoleVO;
import com.demo.vo.EmployeeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SysEmployeeMapper {

    List<EmployeeVO> selectPage(@Param("keyword") String keyword,
                                @Param("deptId") Long deptId,
                                @Param("status") Integer status,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword,
                   @Param("deptId") Long deptId,
                   @Param("status") Integer status);

    SysEmployee selectById(Long id);

    SysEmployee selectByEmpNo(String empNo);

    int insert(SysEmployee employee);

    int update(SysEmployee employee);

    int logicDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateBy") Long updateBy);

    int updateLoginInfo(@Param("id") Long id,
                        @Param("lastLoginTime") LocalDateTime lastLoginTime,
                        @Param("lastLoginIp") String lastLoginIp);

    int updatePassword(@Param("id") Long id,
                       @Param("password") String password,
                       @Param("pwdUpdateTime") LocalDateTime pwdUpdateTime);

    /** 批量查询员工角色，避免分页时 N+1 */
    List<EmployeeRoleVO> selectRolesByEmpIds(@Param("empIds") List<Long> empIds);

    int deleteEmployeeRoles(Long employeeId);

    int insertEmployeeRoles(@Param("employeeId") Long employeeId, @Param("roleIds") List<Long> roleIds);

    /** 员工拥有的权限标识并集 */
    List<String> selectPermCodesByEmpId(Long employeeId);
}
