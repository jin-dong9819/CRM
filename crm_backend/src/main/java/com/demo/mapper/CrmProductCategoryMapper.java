package com.demo.mapper;

import com.demo.entity.CrmProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CrmProductCategoryMapper {

    List<CrmProductCategory> selectAll(@Param("status") Integer status);

    CrmProductCategory selectById(Long id);

    /** 子分类数量，删除前校验 */
    int countChildren(Long parentId);

    int insert(CrmProductCategory category);

    int update(CrmProductCategory category);

    int deleteById(Long id);
}
