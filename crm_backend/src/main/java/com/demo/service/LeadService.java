package com.demo.service;

import com.demo.common.BizException;
import com.demo.common.PageResult;
import com.demo.entity.CrmLead;
import com.demo.mapper.CrmLeadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadService {

    /** 线索编号前缀，形如 XS20260001 */
    private static final String NO_PREFIX = "XS";

    private final CrmLeadMapper leadMapper;

    public PageResult<CrmLead> page(String keyword, String status, String source, String level,
                                    Integer page, Integer pageSize) {
        int current = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 500);

        String kw = blankToNull(keyword);
        String st = blankToNull(status);
        String sc = blankToNull(source);
        String lv = blankToNull(level);

        long total = leadMapper.countPage(kw, st, sc, lv);
        List<CrmLead> list = leadMapper.selectPage(kw, st, sc, lv, (current - 1) * size, size);
        return new PageResult<>(list, total);
    }

    @Transactional
    public void save(Long id, CrmLead req, Long operatorId) {
        validate(req);

        if (id == null) {
            String no = blankToNull(req.getLeadNo());
            if (no == null) {
                no = nextNo();
            } else if (leadMapper.selectByNo(no) != null) {
                throw new BizException("线索编号已存在");
            }
            req.setLeadNo(no);
            req.setStatus(defaultIfBlank(req.getStatus(), "未跟进"));
            req.setSource(defaultIfBlank(req.getSource(), "官网表单"));
            req.setLevel(defaultIfBlank(req.getLevel(), "B"));
            req.setCreateBy(operatorId);
            req.setUpdateBy(operatorId);
            leadMapper.insert(req);
            return;
        }

        CrmLead old = leadMapper.selectById(id);
        if (old == null) throw new BizException("线索不存在");
        req.setId(id);
        req.setUpdateBy(operatorId);
        leadMapper.update(req);
    }

    @Transactional
    public void delete(Long id, Long operatorId) {
        CrmLead lead = leadMapper.selectById(id);
        if (lead == null) throw new BizException("线索不存在");
        leadMapper.logicDelete(id, operatorId);
    }

    private void validate(CrmLead req) {
        if (req.getName() == null || req.getName().isBlank()) throw new BizException("请输入线索名称");
        if (req.getCompany() == null || req.getCompany().isBlank()) throw new BizException("请输入公司名称");
        String phone = blankToNull(req.getPhone());
        if (phone != null && !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BizException("手机号格式不正确");
        }
    }

    /** 编号 = 前缀 + 年份 + 4位流水，取当年最大流水号 +1 */
    private String nextNo() {
        String prefix = NO_PREFIX + LocalDate.now().getYear();
        Integer max = leadMapper.selectMaxSeq(prefix);
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
