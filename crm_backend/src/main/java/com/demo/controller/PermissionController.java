package com.demo.controller;

import com.demo.common.Result;
import com.demo.entity.SysPermission;
import com.demo.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /** 启用状态的权限树（角色分配权限用） */
    @GetMapping("/tree")
    public Result<List<SysPermission>> tree() {
        return Result.ok(permissionService.tree(true));
    }

    /** 全量权限树（含停用，权限管理页用） */
    @GetMapping("/tree/all")
    public Result<List<SysPermission>> treeAll() {
        return Result.ok(permissionService.tree(false));
    }

    @PostMapping
    public Result<Void> add(@RequestBody SysPermission permission) {
        permissionService.save(null, permission);
        return Result.ok(null, "新增成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysPermission permission) {
        permissionService.save(id, permission);
        return Result.ok(null, "修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.ok(null, "删除成功");
    }
}
