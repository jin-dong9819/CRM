package com.demo.service;

import com.demo.common.BizException;
import com.demo.common.PageResult;
import com.demo.entity.CrmCustomer;
import com.demo.entity.CrmOrder;
import com.demo.mapper.CrmCustomerMapper;
import com.demo.mapper.CrmOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    /** 订单编号前缀，形如 DD20260001 */
    private static final String NO_PREFIX = "DD";

    private final CrmOrderMapper orderMapper;
    private final CrmCustomerMapper customerMapper;

    public PageResult<CrmOrder> page(String keyword, String status, String payStatus, Long customerId,
                                     Integer page, Integer pageSize) {
        int current = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 500);

        String kw = blankToNull(keyword);
        String st = blankToNull(status);
        String pay = blankToNull(payStatus);

        long total = orderMapper.countPage(kw, st, pay, customerId);
        List<CrmOrder> list = orderMapper.selectPage(kw, st, pay, customerId, (current - 1) * size, size);
        return new PageResult<>(list, total);
    }

    @Transactional
    public void save(Long id, CrmOrder req, Long operatorId) {
        validate(req);
        // 客户名称以库中为准，避免前端传错或只传 ID
        CrmCustomer customer = customerMapper.selectById(req.getCustomerId());
        if (customer == null) throw new BizException("关联客户不存在");
        req.setCustomerName(customer.getName());

        if (id == null) {
            String no = blankToNull(req.getOrderNo());
            if (no == null) {
                no = nextNo();
            } else if (orderMapper.selectByNo(no) != null) {
                throw new BizException("订单编号已存在");
            }
            req.setOrderNo(no);
            req.setPayStatus(defaultIfBlank(req.getPayStatus(), "未付款"));
            req.setStatus(defaultIfBlank(req.getStatus(), "待审核"));
            if (req.getOrderDate() == null) req.setOrderDate(LocalDate.now());
            req.setCreateBy(operatorId);
            req.setUpdateBy(operatorId);
            orderMapper.insert(req);
            return;
        }

        CrmOrder old = orderMapper.selectById(id);
        if (old == null) throw new BizException("订单不存在");
        req.setId(id);
        req.setUpdateBy(operatorId);
        orderMapper.update(req);
    }

    @Transactional
    public void delete(Long id, Long operatorId) {
        CrmOrder order = orderMapper.selectById(id);
        if (order == null) throw new BizException("订单不存在");
        orderMapper.logicDelete(id, operatorId);
    }

    private void validate(CrmOrder req) {
        if (req.getCustomerId() == null) throw new BizException("请选择客户");
        if (req.getProductName() == null || req.getProductName().isBlank()) {
            throw new BizException("请选择订购产品");
        }
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("订单金额需大于 0");
        }
    }

    /** 编号 = 前缀 + 年份 + 4位流水，取当年最大流水号 +1 */
    private String nextNo() {
        String prefix = NO_PREFIX + LocalDate.now().getYear();
        Integer max = orderMapper.selectMaxSeq(prefix);
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
