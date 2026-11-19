package com.demo.mapper;

import com.demo.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionMapper {

    List<SysPermission> selectAllEnabled();

    /** 全量权限（含停用），权限管理页用 */
    List<SysPermission> selectAll();

    SysPermission selectById(Long id);

    /** permCode 是否重复，编辑时排除自己 */
    int countByCode(@Param("permCode") String permCode, @Param("excludeId") Long excludeId);

    /** 直接下级数量，删除前校验 */
    int countChildren(Long parentId);

    int insert(SysPermission permission);

    int update(SysPermission permission);

    int deleteById(Long id);

    /** 删除权限时同步清理角色授权关系，避免脏数据 */
    int deleteRolePermissions(Long permissionId);
}
