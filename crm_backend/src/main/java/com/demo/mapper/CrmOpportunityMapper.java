package com.demo.mapper;

import com.demo.entity.CrmOpportunity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CrmOpportunityMapper {

    List<CrmOpportunity> selectPage(@Param("keyword") String keyword,
                                    @Param("stage") String stage,
                                    @Param("customerId") Long customerId,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword,
                   @Param("stage") String stage,
                   @Param("customerId") Long customerId);

    CrmOpportunity selectById(Long id);

    CrmOpportunity selectByNo(String oppNo);

    /** 当前年份前缀下最大的流水号，用于生成下一个编号 */
    Integer selectMaxSeq(@Param("prefix") String prefix);

    int insert(CrmOpportunity opportunity);

    int update(CrmOpportunity opportunity);

    int logicDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);
}
