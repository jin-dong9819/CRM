package com.demo.service;

import com.demo.common.BizException;
import com.demo.common.PageResult;
import com.demo.entity.CrmCustomer;
import com.demo.mapper.CrmCustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    /** 客户编号前缀，形如 KH20260001 */
    private static final String NO_PREFIX = "KH";

    private final CrmCustomerMapper customerMapper;

    public PageResult<CrmCustomer> page(String keyword, String level, String status, String industry,
                                        Integer page, Integer pageSize) {
        int current = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 500);

        String kw = blankToNull(keyword);
        String lv = blankToNull(level);
        String st = blankToNull(status);
        String ind = blankToNull(industry);

        long total = customerMapper.countPage(kw, lv, st, ind);
        List<CrmCustomer> list = customerMapper.selectPage(kw, lv, st, ind, (current - 1) * size, size);
        return new PageResult<>(list, total);
    }

    @Transactional
    public void save(Long id, CrmCustomer req, Long operatorId) {
        validate(req);

        if (id == null) {
            String no = blankToNull(req.getCustNo());
            if (no == null) {
                no = nextNo();
            } else if (customerMapper.selectByNo(no) != null) {
                throw new BizException("客户编号已存在");
            }
            req.setCustNo(no);
            req.setLevel(defaultIfBlank(req.getLevel(), "普通客户"));
            req.setStatus(defaultIfBlank(req.getStatus(), "跟进中"));
            req.setCreateBy(operatorId);
            req.setUpdateBy(operatorId);
            customerMapper.insert(req);
            return;
        }

        CrmCustomer old = customerMapper.selectById(id);
        if (old == null) throw new BizException("客户不存在");
        req.setId(id);
        req.setUpdateBy(operatorId);
        customerMapper.update(req);
    }

    @Transactional
    public void delete(Long id, Long operatorId) {
        CrmCustomer customer = customerMapper.selectById(id);
        if (customer == null) throw new BizException("客户不存在");
        customerMapper.logicDelete(id, operatorId);
    }

    private void validate(CrmCustomer req) {
        if (req.getName() == null || req.getName().isBlank()) throw new BizException("请输入客户名称");
        String phone = blankToNull(req.getPhone());
        if (phone != null && !phone.matches("^(1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})$")) {
            throw new BizException("联系电话格式不正确");
        }
    }

    /** 编号 = 前缀 + 年份 + 4位流水，取当年最大流水号 +1 */
    private String nextNo() {
        String prefix = NO_PREFIX + LocalDate.now().getYear();
        Integer max = customerMapper.selectMaxSeq(prefix);
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
