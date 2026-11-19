package com.demo.mapper;

import com.demo.entity.SysRole;
import com.demo.vo.RoleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper {

    List<RoleVO> selectPage(@Param("keyword") String keyword,
                            @Param("offset") int offset,
                            @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword);

    List<SysRole> selectAllEnabled();

    SysRole selectById(Long id);

    List<SysRole> selectByEmployeeId(Long employeeId);

    int insert(SysRole role);

    int update(SysRole role);

    int logicDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    int countByCode(@Param("roleCode") String roleCode, @Param("excludeId") Long excludeId);

    int countByRoleName(@Param("roleName") String roleName, @Param("excludeId") Long excludeId);

    /** 角色被多少员工引用 */
    int countEmployees(Long roleId);

    List<Long> selectPermissionIds(Long roleId);

    int deleteRolePermissions(Long roleId);

    int insertRolePermissions(@Param("roleId") Long roleId,
                              @Param("permissionIds") List<Long> permissionIds);
}
