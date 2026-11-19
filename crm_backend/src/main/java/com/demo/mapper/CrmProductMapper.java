package com.demo.mapper;

import com.demo.entity.CrmProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CrmProductMapper {

    List<CrmProduct> selectPage(@Param("keyword") String keyword,
                                @Param("categoryId") Long categoryId,
                                @Param("brand") String brand,
                                @Param("status") Integer status,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword,
                   @Param("categoryId") Long categoryId,
                   @Param("brand") String brand,
                   @Param("status") Integer status);

    CrmProduct selectById(Long id);

    CrmProduct selectByCode(String productCode);

    /** 编码前缀（AP）下最大的数字序号，用于生成下一个编码 */
    Integer selectMaxSeq(@Param("prefix") String prefix);

    /** 某分类下的产品数量，删除分类前校验 */
    int countByCategory(Long categoryId);

    int insert(CrmProduct product);

    int update(CrmProduct product);

    int deleteById(Long id);
}
