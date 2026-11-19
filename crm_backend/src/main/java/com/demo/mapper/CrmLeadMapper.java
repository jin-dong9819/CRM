package com.demo.mapper;

import com.demo.entity.CrmLead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CrmLeadMapper {

    List<CrmLead> selectPage(@Param("keyword") String keyword,
                             @Param("status") String status,
                             @Param("source") String source,
                             @Param("level") String level,
                             @Param("offset") int offset,
                             @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword,
                   @Param("status") String status,
                   @Param("source") String source,
                   @Param("level") String level);

    CrmLead selectById(Long id);

    CrmLead selectByNo(String leadNo);

    /** 当前年份前缀下最大的流水号，用于生成下一个编号 */
    Integer selectMaxSeq(@Param("prefix") String prefix);

    int insert(CrmLead lead);

    int update(CrmLead lead);

    int logicDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);
}
