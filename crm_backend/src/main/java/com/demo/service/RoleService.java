package com.demo.service;

import com.demo.common.BizException;
import com.demo.common.PageResult;
import com.demo.dto.RoleDTO;
import com.demo.entity.SysPermission;
import com.demo.entity.SysRole;
import com.demo.mapper.SysPermissionMapper;
import com.demo.mapper.SysRoleMapper;
import com.demo.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoleService {

    /** 数据范围：1全部 2本部门及以下 3本部门 4仅本人 5自定义 */
    private static final Map<Integer, String> SCOPE_LABELS = Map.of(
            1, "全部数据",
            2, "本部门及以下",
            3, "本部门",
            4, "仅本人",
            5, "自定义"
    );

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    public PageResult<RoleVO> page(String keyword, Integer page, Integer pageSize) {
        int current = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 500);
        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim();

        long total = roleMapper.countPage(kw);
        List<RoleVO> list = roleMapper.selectPage(kw, (current - 1) * size, size);
        list.forEach(vo -> vo.setDataScopeLabel(SCOPE_LABELS.getOrDefault(vo.getDataScope(), "未知")));
        return new PageResult<>(list, total);
    }

    public List<SysRole> listAll() {
        return roleMapper.selectAllEnabled();
    }

    @Transactional
    public void save(Long id, RoleDTO dto, Long operatorId) {
        String roleCode = dto.getRoleCode() == null ? "" : dto.getRoleCode().trim().toUpperCase();
        if (!roleCode.matches("^[A-Z][A-Z0-9_]{1,29}$")) {
            throw new BizException("角色编码只能是大写字母、数字和下划线");
        }
        if (roleMapper.countByCode(roleCode, id) > 0) throw new BizException("角色编码已存在");
        if (roleMapper.countByRoleName(dto.getRoleName().trim(), id) > 0) throw new BizException("角色名称已存在");

        SysRole role = new SysRole();
        role.setRoleCode(roleCode);
        role.setRoleName(dto.getRoleName().trim());
        role.setDataScope(dto.getDataScope());
        role.setSort(dto.getSort() == null ? 0 : dto.getSort());
        role.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        role.setRemark(dto.getRemark());

        if (id == null) {
            role.setCreateBy(operatorId);
            role.setUpdateBy(operatorId);
            roleMapper.insert(role);
        } else {
            if (roleMapper.selectById(id) == null) throw new BizException("角色不存在");
            role.setId(id);
            role.setUpdateBy(operatorId);
            roleMapper.update(role);
        }
    }

    @Transactional
    public void delete(Long id, Long operatorId) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) throw new BizException("角色不存在");
        if ("ADMIN".equals(role.getRoleCode())) throw new BizException("超级管理员角色不可删除");
        if (roleMapper.countEmployees(id) > 0) throw new BizException("该角色下还有员工，无法删除");

        roleMapper.deleteRolePermissions(id);
        roleMapper.logicDelete(id, operatorId);
    }

    public List<Long> permissionIds(Long roleId) {
        return roleMapper.selectPermissionIds(roleId);
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds, Long operatorId) {
        if (roleMapper.selectById(roleId) == null) throw new BizException("角色不存在");
        if ("ADMIN".equals(roleMapper.selectById(roleId).getRoleCode())) {
            throw new BizException("超级管理员默认拥有全部权限，无需分配");
        }

        roleMapper.deleteRolePermissions(roleId);
        List<Long> ids = permissionIds == null ? List.of() : permissionIds.stream().distinct().toList();
        if (!ids.isEmpty()) {
            roleMapper.insertRolePermissions(roleId, ids);
        }
    }

    /** 权限树（分配权限弹窗用） */
    public List<SysPermission> permissionTree() {
        List<SysPermission> all = permissionMapper.selectAllEnabled();
        return buildTree(all, 0L);
    }

    private List<SysPermission> buildTree(List<SysPermission> all, Long parentId) {
        List<SysPermission> result = new ArrayList<>();
        for (SysPermission permission : all) {
            if (parentId.equals(permission.getParentId())) {
                List<SysPermission> children = buildTree(all, permission.getId());
                permission.setChildren(children.isEmpty() ? null : children);
                result.add(permission);
            }
        }
        return result;
    }
}
