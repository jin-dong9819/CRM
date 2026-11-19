package com.demo.controller;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.CrmLead;
import com.demo.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @GetMapping
    public Result<PageResult<CrmLead>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(leadService.page(keyword, status, source, level, page, pageSize));
    }

    @PostMapping
    public Result<Void> create(@RequestBody CrmLead lead,
                               @RequestAttribute(value = "empId", required = false) Long operatorId) {
        leadService.save(null, lead, operatorId);
        return Result.ok(null, "新增成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody CrmLead lead,
                               @RequestAttribute(value = "empId", required = false) Long operatorId) {
        leadService.save(id, lead, operatorId);
        return Result.ok(null, "修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestAttribute(value = "empId", required = false) Long operatorId) {
        leadService.delete(id, operatorId);
        return Result.ok(null, "删除成功");
    }
}
