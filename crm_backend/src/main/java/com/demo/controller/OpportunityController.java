package com.demo.controller;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.CrmOpportunity;
import com.demo.service.OpportunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;

    @GetMapping
    public Result<PageResult<CrmOpportunity>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(opportunityService.page(keyword, stage, customerId, page, pageSize));
    }

    @PostMapping
    public Result<Void> create(@RequestBody CrmOpportunity opportunity,
                               @RequestAttribute(value = "empId", required = false) Long operatorId) {
        opportunityService.save(null, opportunity, operatorId);
        return Result.ok(null, "新增成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody CrmOpportunity opportunity,
                               @RequestAttribute(value = "empId", required = false) Long operatorId) {
        opportunityService.save(id, opportunity, operatorId);
        return Result.ok(null, "修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestAttribute(value = "empId", required = false) Long operatorId) {
        opportunityService.delete(id, operatorId);
        return Result.ok(null, "删除成功");
    }
}
