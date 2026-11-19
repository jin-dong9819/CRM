package com.demo.mapper;

import com.demo.entity.CrmOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CrmOrderMapper {

    List<CrmOrder> selectPage(@Param("keyword") String keyword,
                              @Param("status") String status,
                              @Param("payStatus") String payStatus,
                              @Param("customerId") Long customerId,
                              @Param("offset") int offset,
                              @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword,
                   @Param("status") String status,
                   @Param("payStatus") String payStatus,
                   @Param("customerId") Long customerId);

    CrmOrder selectById(Long id);

    CrmOrder selectByNo(String orderNo);

    /** 当前年份前缀下最大的流水号，用于生成下一个编号 */
    Integer selectMaxSeq(@Param("prefix") String prefix);

    int insert(CrmOrder order);

    int update(CrmOrder order);

    int logicDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);
}
