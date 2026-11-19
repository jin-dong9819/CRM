package com.demo.mapper;

import com.demo.entity.CrmCustomer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CrmCustomerMapper {

    List<CrmCustomer> selectPage(@Param("keyword") String keyword,
                                 @Param("level") String level,
                                 @Param("status") String status,
                                 @Param("industry") String industry,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword,
                   @Param("level") String level,
                   @Param("status") String status,
                   @Param("industry") String industry);

    CrmCustomer selectById(Long id);

    CrmCustomer selectByNo(String custNo);

    /** 当前年份前缀下最大的流水号，用于生成下一个编号 */
    Integer selectMaxSeq(@Param("prefix") String prefix);

    int insert(CrmCustomer customer);

    int update(CrmCustomer customer);

    int logicDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);
}
