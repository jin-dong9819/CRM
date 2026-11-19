package com.demo.controller;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.CrmCustomer;
import com.demo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public Result<PageResult<CrmCustomer>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String industry,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(customerService.page(keyword, level, status, industry, page, pageSize));
    }

    @PostMapping
    public Result<Void> create(@RequestBody CrmCustomer customer,
                               @RequestAttribute(value = "empId", required = false) Long operatorId) {
        customerService.save(null, customer, operatorId);
        return Result.ok(null, "新增成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody CrmCustomer customer,
                               @RequestAttribute(value = "empId", required = false) Long operatorId) {
        customerService.save(id, customer, operatorId);
        return Result.ok(null, "修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestAttribute(value = "empId", required = false) Long operatorId) {
        customerService.delete(id, operatorId);
        return Result.ok(null, "删除成功");
    }
}
