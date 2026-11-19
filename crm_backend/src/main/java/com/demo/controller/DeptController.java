package com.demo.controller;

import com.demo.common.Result;
import com.demo.entity.SysDept;
import com.demo.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/depts")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    /** 部门树 */
    @GetMapping
    public Result<List<SysDept>> tree() {
        return Result.ok(deptService.tree());
    }

    /** 平铺列表，给下拉框用 */
    @GetMapping("/all")
    public Result<List<SysDept>> list() {
        return Result.ok(deptService.list());
    }

    @PostMapping
    public Result<Void> add(@RequestBody SysDept dept) {
        deptService.add(dept);
        return Result.ok(null, "新增成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysDept dept) {
        dept.setId(id);
        deptService.update(dept);
        return Result.ok(null, "修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.ok(null, "删除成功");
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody SysDept dept) {
        deptService.updateStatus(id, dept.getStatus());
        return Result.ok(null, "状态已更新");
    }
}
