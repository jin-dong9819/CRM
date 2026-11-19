package com.demo.controller;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.CrmOrder;
import com.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<PageResult<CrmOrder>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String payStatus,
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(orderService.page(keyword, status, payStatus, customerId, page, pageSize));
    }

    @PostMapping
    public Result<Void> create(@RequestBody CrmOrder order,
                               @RequestAttribute(value = "empId", required = false) Long operatorId) {
        orderService.save(null, order, operatorId);
        return Result.ok(null, "新增成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody CrmOrder order,
                               @RequestAttribute(value = "empId", required = false) Long operatorId) {
        orderService.save(id, order, operatorId);
        return Result.ok(null, "修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestAttribute(value = "empId", required = false) Long operatorId) {
        orderService.delete(id, operatorId);
        return Result.ok(null, "删除成功");
    }
}
