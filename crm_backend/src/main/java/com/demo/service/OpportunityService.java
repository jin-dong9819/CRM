package com.demo.service;

import com.demo.common.BizException;
import com.demo.common.PageResult;
import com.demo.entity.CrmCustomer;
import com.demo.entity.CrmOpportunity;
import com.demo.mapper.CrmCustomerMapper;
import com.demo.mapper.CrmOpportunityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpportunityService {

    /** 商机编号前缀，形如 SJ20260001 */
    private static final String NO_PREFIX = "SJ";

    private final CrmOpportunityMapper opportunityMapper;
    private final CrmCustomerMapper customerMapper;

    public PageResult<CrmOpportunity> page(String keyword, String stage, Long customerId,
                                           Integer page, Integer pageSize) {
        int current = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 500);

        String kw = blankToNull(keyword);
        String st = blankToNull(stage);

        long total = opportunityMapper.countPage(kw, st, customerId);
        List<CrmOpportunity> list = opportunityMapper.selectPage(kw, st, customerId, (current - 1) * size, size);
        return new PageResult<>(list, total);
    }

    @Transactional
    public void save(Long id, CrmOpportunity req, Long operatorId) {
        validate(req);
        // 客户名称以库中为准，避免前端传错或只传 ID
        CrmCustomer customer = customerMapper.selectById(req.getCustomerId());
        if (customer == null) throw new BizException("关联客户不存在");
        req.setCustomerName(customer.getName());

        if (id == null) {
            String no = blankToNull(req.getOppNo());
            if (no == null) {
                no = nextNo();
            } else if (opportunityMapper.selectByNo(no) != null) {
                throw new BizException("商机编号已存在");
            }
            req.setOppNo(no);
            req.setStage(defaultIfBlank(req.getStage(), "初步接洽"));
            if (req.getProbability() == null) req.setProbability(10);
            req.setCreateBy(operatorId);
            req.setUpdateBy(operatorId);
            opportunityMapper.insert(req);
            return;
        }

        CrmOpportunity old = opportunityMapper.selectById(id);
        if (old == null) throw new BizException("商机不存在");
        req.setId(id);
        req.setUpdateBy(operatorId);
        opportunityMapper.update(req);
    }

    @Transactional
    public void delete(Long id, Long operatorId) {
        CrmOpportunity opportunity = opportunityMapper.selectById(id);
        if (opportunity == null) throw new BizException("商机不存在");
        opportunityMapper.logicDelete(id, operatorId);
    }

    private void validate(CrmOpportunity req) {
        if (req.getName() == null || req.getName().isBlank()) throw new BizException("请输入商机名称");
        if (req.getCustomerId() == null) throw new BizException("请选择关联客户");
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("商机金额需大于 0");
        }
        if (req.getProbability() != null && (req.getProbability() < 0 || req.getProbability() > 100)) {
            throw new BizException("赢率应在 0 ~ 100 之间");
        }
    }

    /** 编号 = 前缀 + 年份 + 4位流水，取当年最大流水号 +1 */
    private String nextNo() {
        String prefix = NO_PREFIX + LocalDate.now().getYear();
        Integer max = opportunityMapper.selectMaxSeq(prefix);
        int seq = max == null ? 1 : max + 1;
        return prefix + String.format("%04d", seq);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
