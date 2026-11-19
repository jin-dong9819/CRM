package com.demo.controller;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.dto.PermissionAssignDTO;
import com.demo.dto.RoleDTO;
import com.demo.entity.SysPermission;
import com.demo.entity.SysRole;
import com.demo.service.RoleService;
import com.demo.vo.RoleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public Result<PageResult<RoleVO>> page(@RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(roleService.page(keyword, page, pageSize));
    }

    @GetMapping("/all")
    public Result<List<SysRole>> all() {
        return Result.ok(roleService.listAll());
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid RoleDTO dto, @RequestAttribute("empId") Long operatorId) {
        roleService.save(null, dto, operatorId);
        return Result.ok(null, "新增成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid RoleDTO dto,
                               @RequestAttribute("empId") Long operatorId) {
        roleService.save(id, dto, operatorId);
        return Result.ok(null, "修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestAttribute("empId") Long operatorId) {
        roleService.delete(id, operatorId);
        return Result.ok(null, "删除成功");
    }

    /** 角色已勾选的权限ID */
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> permissions(@PathVariable Long id) {
        return Result.ok(roleService.permissionIds(id));
    }

    /** 保存角色权限 */
    @PutMapping("/{id}/permissions")
    public Result<Void> assign(@PathVariable Long id,
                               @RequestBody PermissionAssignDTO dto,
                               @RequestAttribute("empId") Long operatorId) {
        roleService.assignPermissions(id, dto.getPermissionIds(), operatorId);
        return Result.ok(null, "权限已保存");
    }
}
