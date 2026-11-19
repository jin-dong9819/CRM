package com.demo.service;

import com.demo.common.BizException;
import com.demo.common.PageResult;
import com.demo.entity.CrmProduct;
import com.demo.entity.CrmProductCategory;
import com.demo.mapper.CrmProductCategoryMapper;
import com.demo.mapper.CrmProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** 产品与产品分类：易损件 / 养护用品的目录维护 */
@Service
@RequiredArgsConstructor
public class ProductService {

    /** 产品编码前缀，形如 AP0101 */
    private static final String CODE_PREFIX = "AP";

    private final CrmProductMapper productMapper;
    private final CrmProductCategoryMapper categoryMapper;

    /* ==================== 产品 ==================== */

    public PageResult<CrmProduct> page(String keyword, Long categoryId, String brand, Integer status,
                                       Integer page, Integer pageSize) {
        int current = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 500);

        String kw = blankToNull(keyword);
        String br = blankToNull(brand);

        long total = productMapper.countPage(kw, categoryId, br, status);
        List<CrmProduct> list = productMapper.selectPage(kw, categoryId, br, status, (current - 1) * size, size);
        return new PageResult<>(list, total);
    }

    public CrmProduct detail(Long id) {
        CrmProduct product = productMapper.selectById(id);
        if (product == null) throw new BizException("产品不存在");
        return product;
    }

    @Transactional
    public void save(Long id, CrmProduct req) {
        validate(req);

        if (id == null) {
            String code = blankToNull(req.getProductCode());
            if (code == null) {
                code = nextCode();
            } else if (productMapper.selectByCode(code) != null) {
                throw new BizException("产品编码已存在");
            }
            req.setProductCode(code);
            req.setUnit(defaultIfBlank(req.getUnit(), "件"));
            req.setStatus(req.getStatus() == null ? 1 : req.getStatus());
            productMapper.insert(req);
            return;
        }

        CrmProduct old = productMapper.selectById(id);
        if (old == null) throw new BizException("产品不存在");
        req.setId(id);
        req.setUnit(defaultIfBlank(req.getUnit(), "件"));
        req.setStatus(req.getStatus() == null ? old.getStatus() : req.getStatus());
        productMapper.update(req);
    }

    @Transactional
    public void delete(Long id) {
        CrmProduct product = productMapper.selectById(id);
        if (product == null) throw new BizException("产品不存在");
        productMapper.deleteById(id);
    }

    private void validate(CrmProduct req) {
        if (req.getProductName() == null || req.getProductName().isBlank()) {
            throw new BizException("请输入产品名称");
        }
        if (req.getCategoryId() == null) throw new BizException("请选择产品分类");
        if (categoryMapper.selectById(req.getCategoryId()) == null) {
            throw new BizException("产品分类不存在");
        }
        if (req.getPrice() != null && req.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("标准价不能为负数");
        }
        if (req.getCostPrice() != null && req.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("成本价不能为负数");
        }
    }

    /** 编码 = 前缀 + 4 位流水，取现有最大序号 +1（AP0101 -> 102 -> AP0102 之后顺延） */
    private String nextCode() {
        Integer max = productMapper.selectMaxSeq(CODE_PREFIX);
        int seq = max == null ? 1 : max + 1;
        return CODE_PREFIX + String.format("%04d", seq);
    }

    /* ==================== 分类 ==================== */

    public List<CrmProductCategory> listCategories(Integer status) {
        return categoryMapper.selectAll(status);
    }

    @Transactional
    public void saveCategory(Long id, CrmProductCategory req) {
        if (req.getCategoryName() == null || req.getCategoryName().isBlank()) {
            throw new BizException("请输入分类名称");
        }
        req.setParentId(req.getParentId() == null ? 0L : req.getParentId());
        req.setSort(req.getSort() == null ? 1 : req.getSort());
        req.setStatus(req.getStatus() == null ? 1 : req.getStatus());

        if (id == null) {
            categoryMapper.insert(req);
            return;
        }

        CrmProductCategory old = categoryMapper.selectById(id);
        if (old == null) throw new BizException("分类不存在");
        if (req.getParentId().equals(id)) throw new BizException("上级分类不能是自己");
        req.setId(id);
        categoryMapper.update(req);
    }

    @Transactional
    public void deleteCategory(Long id) {
        CrmProductCategory category = categoryMapper.selectById(id);
        if (category == null) throw new BizException("分类不存在");
        if (categoryMapper.countChildren(id) > 0) {
            throw new BizException("该分类下还有子分类，请先删除子分类");
        }
        if (productMapper.countByCategory(id) > 0) {
            throw new BizException("该分类下还有产品，请先移走产品或删除产品");
        }
        categoryMapper.deleteById(id);
    }

    /* ==================== 工具 ==================== */

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
