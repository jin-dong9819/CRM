package com.demo.controller;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.dto.EmployeeDTO;
import com.demo.dto.PasswordDTO;
import com.demo.dto.StatusDTO;
import com.demo.service.EmployeeService;
import com.demo.vo.EmployeeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public Result<PageResult<EmployeeVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(employeeService.page(keyword, deptId, status, page, pageSize));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid EmployeeDTO dto,
                               @RequestAttribute("empId") Long operatorId) {
        employeeService.save(null, dto, operatorId);
        return Result.ok(null, "新增成功，初始密码 123456");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid EmployeeDTO dto,
                               @RequestAttribute("empId") Long operatorId) {
        employeeService.save(id, dto, operatorId);
        return Result.ok(null, "修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestAttribute("empId") Long operatorId) {
        employeeService.delete(id, operatorId);
        return Result.ok(null, "删除成功");
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestBody StatusDTO dto,
                                     @RequestAttribute("empId") Long operatorId) {
        employeeService.updateStatus(id, dto.getStatus(), operatorId);
        return Result.ok(null, "状态已更新");
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @RequestBody(required = false) PasswordDTO dto,
                                      @RequestAttribute("empId") Long operatorId) {
        employeeService.resetPassword(id, dto == null ? null : dto.getPassword(), operatorId);
        return Result.ok(null, "密码已重置");
    }
}
