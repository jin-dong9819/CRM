package com.demo.service;

import com.demo.common.BizException;
import com.demo.entity.SysPermission;
import com.demo.mapper.SysPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 权限维护：目录 / 菜单 / 按钮三级树 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    private static final String CODE_PATTERN = "^[A-Za-z][A-Za-z0-9:_-]{1,99}$";

    private final SysPermissionMapper permissionMapper;

    /** 权限树：enabledOnly 为 true 时只返回启用项（角色分配用），否则返回全部（权限管理页用） */
    public List<SysPermission> tree(boolean enabledOnly) {
        List<SysPermission> all = enabledOnly ? permissionMapper.selectAllEnabled() : permissionMapper.selectAll();
        return buildTree(all, 0L);
    }

    @Transactional
    public void save(Long id, SysPermission permission) {
        String permName = trim(permission.getPermName());
        if (!StringUtils.hasText(permName)) throw new BizException("请输入权限名称");

        String permCode = trim(permission.getPermCode());
        if (!StringUtils.hasText(permCode)) throw new BizException("请输入权限标识");
        if (!permCode.matches(CODE_PATTERN)) {
            throw new BizException("权限标识只能由字母开头，包含字母、数字、冒号、下划线或短横线");
        }
        if (permissionMapper.countByCode(permCode, id) > 0) throw new BizException("权限标识已存在");

        Long parentId = permission.getParentId() == null ? 0L : permission.getParentId();
        if (parentId != 0L) {
            if (permissionMapper.selectById(parentId) == null) throw new BizException("上级权限不存在");
            if (id != null && isDescendant(id, parentId)) {
                throw new BizException("上级权限不能是自己或自己的下级");
            }
        }

        Integer permType = permission.getPermType();
        if (permType == null) permType = 2;
        if (permType < 1 || permType > 3) throw new BizException("权限类型不正确");

        SysPermission entity = new SysPermission();
        entity.setParentId(parentId);
        entity.setPermName(permName);
        entity.setPermCode(permCode);
        entity.setPermType(permType);
        entity.setPath(blankToNull(permission.getPath()));
        entity.setComponent(blankToNull(permission.getComponent()));
        entity.setIcon(blankToNull(permission.getIcon()));
        entity.setSort(permission.getSort() == null ? 0 : permission.getSort());
        entity.setVisible(permission.getVisible() == null ? 1 : permission.getVisible());
        entity.setStatus(permission.getStatus() == null ? 1 : permission.getStatus());

        if (id == null) {
            permissionMapper.insert(entity);
        } else {
            if (permissionMapper.selectById(id) == null) throw new BizException("权限不存在");
            entity.setId(id);
            permissionMapper.update(entity);
        }
    }

    @Transactional
    public void delete(Long id) {
        if (permissionMapper.selectById(id) == null) throw new BizException("权限不存在");
        if (permissionMapper.countChildren(id) > 0) throw new BizException("该权限下还有子权限，请先删除子权限");

        permissionMapper.deleteRolePermissions(id);
        permissionMapper.deleteById(id);
    }

    /** targetId 是否在 ancestorId 的子树里，用于阻止把权限挂到自己的下级 */
    private boolean isDescendant(Long id, Long targetId) {
        Set<Long> visited = new HashSet<>();
        Long cursor = targetId;
        while (cursor != null && cursor != 0L && visited.add(cursor)) {
            if (cursor.equals(id)) return true;
            SysPermission parent = permissionMapper.selectById(cursor);
            cursor = parent == null ? null : parent.getParentId();
        }
        return false;
    }

    private List<SysPermission> buildTree(List<SysPermission> all, Long parentId) {
        List<SysPermission> result = new ArrayList<>();
        for (SysPermission permission : all) {
            if (parentId.equals(permission.getParentId())) {
                List<SysPermission> children = buildTree(all, permission.getId());
                // 没有下级置空，前端表格不会出现空展开箭头
                permission.setChildren(children.isEmpty() ? null : children);
                result.add(permission);
            }
        }
        return result;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
