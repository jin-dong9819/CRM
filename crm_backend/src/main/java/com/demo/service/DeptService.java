package com.demo.service;

import com.demo.entity.SysDept;
import com.demo.mapper.SysDeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final SysDeptMapper deptMapper;

    /** 部门树：一次查出全部，按 parentId 分组，再把子节点挂到父节点上 */
    public List<SysDept> tree() {
        List<SysDept> all = deptMapper.selectAll();
        Map<Long, List<SysDept>> childrenMap = all.stream()
                .collect(Collectors.groupingBy(SysDept::getParentId));

        for (SysDept dept : all) {
            List<SysDept> children = childrenMap.get(dept.getId());
            // 没有下级就置空，前端表格不会出现空的展开箭头
            dept.setChildren(children == null || children.isEmpty() ? null : children);
        }
        return childrenMap.getOrDefault(0L, new ArrayList<>());
    }

    /** 平铺列表，给下拉框用 */
    public List<SysDept> list() {
        return deptMapper.selectAll();
    }

    /** 新增：祖先链 = 父级的祖先链 + 父级ID，顶级部门就是 "0" */
    public void add(SysDept dept) {
        dept.setAncestors(ancestorsOf(dept.getParentId()));
        deptMapper.insert(dept);
    }

    /** 修改：换了上级部门，就把自己和所有子孙的祖先链一起更新 */
    public void update(SysDept dept) {
        SysDept old = deptMapper.selectById(dept.getId());
        String ancestors = ancestorsOf(dept.getParentId());
        dept.setAncestors(ancestors);
        deptMapper.update(dept);

        if (!dept.getParentId().equals(old.getParentId())) {
            String oldPrefix = old.getAncestors() + "," + old.getId();
            deptMapper.updateChildrenAncestors(oldPrefix, ancestors + "," + old.getId());
        }
    }

    public void delete(Long id) {
        deptMapper.deleteById(id);
    }

    public void updateStatus(Long id, Integer status) {
        deptMapper.updateStatus(id, status);
    }

    /** 查父级的祖先链，拼上父级ID；父级不存在（或传 0）说明是顶级部门 */
    private String ancestorsOf(Long parentId) {
        SysDept parent = deptMapper.selectById(parentId);
        return parent == null ? "0" : parent.getAncestors() + "," + parent.getId();
    }
}
